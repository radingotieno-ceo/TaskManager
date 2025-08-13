package com.taskmanager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    @Lazy
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configuring SecurityFilterChain...");
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/health", "/test-db", "/simple-test", "/test-password-encoding").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/list-users", "/api/auth/check-user/**", "/api/auth/test-jwt", "/api/auth/test-jwt-details", "/api/auth/debug-jwt", "/api/auth/create-default-users", "/api/auth/database-status", "/api/auth/database-full-status").permitAll()
                
                // Protected auth endpoints - require authentication
                .requestMatchers("/api/auth/users").authenticated()
                
                // Swagger UI and OpenAPI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                
                // H2 Console (for development)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                
                // CORS preflight requests - allow OPTIONS without authentication
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Dashboard endpoints - require authentication
                .requestMatchers("/api/dashboard/**").authenticated()
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Task assignment endpoints - require ADMIN or MANAGER role
                .requestMatchers("/api/tasks/assignment/**").hasAnyRole("ADMIN", "MANAGER")
                
                // Project and task endpoints - require authentication
                .requestMatchers("/api/projects/**").authenticated()
                .requestMatchers("/api/tasks/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // For H2 console

        log.info("SecurityFilterChain configured successfully");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS...");
        
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow specific origins
        config.setAllowedOriginPatterns(List.of("http://localhost:4200", "http://127.0.0.1:4200"));
        
        // Allow all common HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        
        // Allow all headers including Authorization
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials
        config.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        config.setMaxAge(3600L);
        
        // Expose Authorization header to client
        config.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        log.info("CORS configuration completed");
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}


