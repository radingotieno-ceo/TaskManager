package com.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    
    @Value("${jwt.secret:your-256-bit-secret-your-256-bit-secret-your-256-bit-secret-your-256-bit-secret}")
    private String secret;
    
    @Value("${jwt.expiration:86400}")
    private Long expiration;
    
    private Key secretKey;
    
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT Secret key initialized. Secret length: {} characters", secret.length());
    }
    
    public String generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        String token = createToken(claims, userDetails.getUsername());
        log.info("JWT token generated successfully for user: {}", userDetails.getUsername());
        return token;
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);
        
        log.debug("Creating JWT token - Subject: {}, Issued: {}, Expires: {}", 
                 subject, now, expiryDate);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            log.debug("Validating JWT token for user: {}", userDetails.getUsername());
            final String username = getUsernameFromToken(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            log.info("JWT token validation result for user {}: {}", userDetails.getUsername(), isValid);
            return isValid;
        } catch (Exception e) {
            log.error("JWT token validation failed for user {}: {}", userDetails.getUsername(), e.getMessage());
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        try {
            return getClaimFromToken(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Error extracting username from JWT token: {}", e.getMessage());
            throw e;
        }
    }
    
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage());
            throw e;
        }
    }
    
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            boolean expired = expiration.before(new Date());
            if (expired) {
                log.warn("JWT token has expired. Expiration: {}, Current: {}", expiration, new Date());
            }
            return expired;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't check
        }
    }
}


