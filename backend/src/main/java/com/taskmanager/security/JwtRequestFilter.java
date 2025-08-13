package com.taskmanager.security;

import com.taskmanager.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
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

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain chain) throws ServletException, IOException {
        
        // Skip JWT processing for auth endpoints (login, register, etc.) and OPTIONS requests
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        if (requestURI.startsWith("/api/auth/") || "OPTIONS".equals(requestMethod)) {
            chain.doFilter(request, response);
            return;
        }
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        // Debug logging
        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Auth header: " + (requestTokenHeader != null ? "Present" : "Missing"));
        System.out.println("Existing auth: " + SecurityContextHolder.getContext().getAuthentication());
        
        String username = null;
        String jwtToken = null;
        
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            System.out.println("No valid Authorization header found");
            chain.doFilter(request, response);
            return;
        }
        
        jwtToken = requestTokenHeader.substring(7);
        try {
            username = jwtUtil.getUsernameFromToken(jwtToken);
            System.out.println("Parsed username: " + username);
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to get JWT Token: " + e.getMessage());
            chain.doFilter(request, response);
            return;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Token has expired: " + e.getMessage());
            chain.doFilter(request, response);
            return;
        }
        
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            System.out.println("User details loaded: " + userDetails.getUsername());
            System.out.println("Authorities: " + userDetails.getAuthorities());
            
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                System.out.println("Token validation: SUCCESS");
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Authentication set in SecurityContext");
            } else {
                System.out.println("Token validation: FAILED");
            }
        } else {
            System.out.println("Username is null or authentication already exists");
        }
        System.out.println("=== END JWT FILTER DEBUG ===");
        chain.doFilter(request, response);
    }
}


