package com.taskmanager.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain chain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        log.debug("JWT Filter processing: {} {}", requestMethod, requestURI);
        
        // Skip JWT processing for specific auth endpoints and OPTIONS requests
        if ("OPTIONS".equals(requestMethod) || 
            requestURI.equals("/api/auth/login") ||
            requestURI.equals("/api/auth/register") ||
            requestURI.startsWith("/api/auth/list-users") ||
            requestURI.startsWith("/api/auth/check-user/") ||
            requestURI.startsWith("/api/auth/test-jwt") ||
            requestURI.startsWith("/api/auth/test-jwt-details") ||
            requestURI.startsWith("/api/auth/debug-jwt") ||
            requestURI.startsWith("/api/auth/create-default-users") ||
            requestURI.startsWith("/api/auth/database-status") ||
            requestURI.startsWith("/api/auth/database-full-status") ||
            requestURI.startsWith("/api/auth/uploads/profile-photos/")) {
            log.debug("Skipping JWT processing for: {} {}", requestMethod, requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        // Debug logging
        log.debug("=== JWT FILTER DEBUG ===");
        log.debug("Request URI: {}", requestURI);
        log.debug("Request Method: {}", requestMethod);
        log.debug("Authorization header present: {}", requestTokenHeader != null);
        log.debug("Existing authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        
        String username = null;
        String jwtToken = null;
        
        // Check if Authorization header exists and has Bearer prefix
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found for request: {} {}", requestMethod, requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // Extract JWT token
        jwtToken = requestTokenHeader.substring(7);
        log.debug("JWT token extracted (length: {})", jwtToken.length());
        
        try {
            username = jwtUtil.getUsernameFromToken(jwtToken);
            log.debug("Username extracted from token: {}", username);
        } catch (IllegalArgumentException e) {
            log.error("Unable to get JWT Token: {}", e.getMessage());
            chain.doFilter(request, response);
            return;
        } catch (ExpiredJwtException e) {
            log.error("JWT Token has expired: {}", e.getMessage());
            chain.doFilter(request, response);
            return;
        } catch (MalformedJwtException e) {
            log.error("JWT Token is malformed: {}", e.getMessage());
            chain.doFilter(request, response);
            return;
        } catch (UnsupportedJwtException e) {
            log.error("JWT Token is unsupported: {}", e.getMessage());
            chain.doFilter(request, response);
            return;
        } catch (Exception e) {
            log.error("Unexpected error processing JWT token: {}", e.getMessage());
            chain.doFilter(request, response);
            return;
        }
        
        // Set authentication if username is valid and no existing authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                log.debug("Loading user details for: {}", username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                log.debug("User details loaded successfully for: {}", username);
                log.debug("User authorities: {}", userDetails.getAuthorities());
                
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    log.info("JWT token validation successful for user: {}", username);
                    
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication set in SecurityContext for user: {}", username);
                } else {
                    log.warn("JWT token validation failed for user: {}", username);
                }
            } catch (Exception e) {
                log.error("Error loading user details or setting authentication for {}: {}", username, e.getMessage());
            }
        } else {
            if (username == null) {
                log.warn("Username is null from JWT token");
            } else {
                log.debug("Authentication already exists for user: {}", username);
            }
        }
        
        log.debug("=== END JWT FILTER DEBUG ===");
        chain.doFilter(request, response);
    }
}


