package com.taskmanager.controller;

import com.taskmanager.dto.AuthRequest;
import com.taskmanager.dto.AuthResponse;
import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.dto.UserDto;
import com.taskmanager.service.UserService;
import com.taskmanager.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("=== LOGIN ATTEMPT DEBUG ===");
        log.info("Login attempt for email: {}", authRequest.getEmail());
        log.info("Password provided: {}", authRequest.getPassword());
        
        try {
            // Check if user exists first
            if (!userService.existsByEmail(authRequest.getEmail())) {
                log.error("User not found: {}", authRequest.getEmail());
                throw new RuntimeException("Invalid credentials");
            }
            
            log.info("User exists in database, attempting authentication...");
            
            // Enhanced debugging: Load user manually to check details
            try {
                UserDetails userDetails = userService.loadUserByUsername(authRequest.getEmail());
                log.info("=== USER DETAILS DEBUG ===");
                log.info("Loaded user: {}", userDetails.getUsername());
                log.info("Stored password hash: {}", userDetails.getPassword());
                log.info("User authorities: {}", userDetails.getAuthorities());
                log.info("Account non-expired: {}", userDetails.isAccountNonExpired());
                log.info("Account non-locked: {}", userDetails.isAccountNonLocked());
                log.info("Credentials non-expired: {}", userDetails.isCredentialsNonExpired());
                log.info("Account enabled: {}", userDetails.isEnabled());
                
                // Test password manually
                boolean passwordMatches = passwordEncoder.matches(authRequest.getPassword(), userDetails.getPassword());
                log.info("Manual password check result: {}", passwordMatches);
                
                if (!passwordMatches) {
                    log.error("Password does not match stored value!");
                    log.error("Input password: {}", authRequest.getPassword());
                    log.error("Stored hash: {}", userDetails.getPassword());
                    throw new RuntimeException("Invalid credentials");
                }
                
                log.info("Password verification successful!");
                
            } catch (Exception e) {
                log.error("Error during user details loading: {}", e.getMessage());
                throw e;
            }
            
            // Authenticate the user with Spring Security
            try {
                log.info("Attempting Spring Security authentication...");
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
                );
                log.info("Spring Security authentication successful!");
            } catch (Exception authException) {
                log.error("Spring Security authentication failed: {}", authException.getMessage());
                authException.printStackTrace();
                throw new RuntimeException("Invalid credentials");
            }

            // Get user details and generate token
            try {
                UserDto user = userService.findByEmail(authRequest.getEmail());
                log.info("User details retrieved: {}", user.getEmail());
                
                UserDetails userDetails = userService.loadUserByUsername(authRequest.getEmail());
                log.info("UserDetails loaded for token generation: {}", userDetails.getUsername());
                
                String token = jwtUtil.generateToken(userDetails);
                log.info("JWT token generated successfully");

                log.info("=== LOGIN SUCCESSFUL ===");
                return ResponseEntity.ok(new AuthResponse(token, user));
            } catch (Exception userException) {
                log.error("Error getting user details or generating token: {}", userException.getMessage());
                userException.printStackTrace();
                throw userException;
            }
        } catch (Exception e) {
            log.error("Login error for {}: {}", authRequest.getEmail(), e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if user already exists
            if (userService.existsByEmail(request.getEmail())) {
                throw new RuntimeException("User with email " + request.getEmail() + " already exists");
            }
            
            log.info("Creating user: {} with role: {}", request.getEmail(), request.getRole());
            
            UserDto user = userService.createUser(request);
            log.info("User created successfully: {}", user.getEmail());
            
            // Create UserDetails for token generation
            UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token, user));
        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/test-password")
    public ResponseEntity<String> testPassword() {
        log.info("Testing password encoding...");
        String plainPassword = "password123";
        String storedHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG";
        
        boolean matches = passwordEncoder.matches(plainPassword, storedHash);
        
        String result = String.format("""
            === PASSWORD TEST RESULTS ===
            Plain password: %s
            Stored hash: %s
            Matches: %s
            Current time: %s
            """, plainPassword, storedHash, matches, System.currentTimeMillis());
        
        log.info("Password test result: {}", matches);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHash() {
        log.info("Generating fresh hash for 'password123'...");
        String plainPassword = "password123";
        String newHash = passwordEncoder.encode(plainPassword);
        
        String result = String.format("""
            === FRESH HASH GENERATION ===
            Plain password: %s
            Generated hash: %s
            Verification test: %s
            Current time: %s
            """, plainPassword, newHash, passwordEncoder.matches(plainPassword, newHash), System.currentTimeMillis());
        
        log.info("Generated hash: {}", newHash);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        try {
            log.info("Auth controller test endpoint called");
            return ResponseEntity.ok("Auth controller is working! Current time: " + System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Test endpoint error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/debug/users")
    public ResponseEntity<String> debugUsers() {
        try {
            log.info("Debug users endpoint called");
            StringBuilder result = new StringBuilder();
            result.append("=== DATABASE USERS DEBUG ===\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Check if specific users exist
            String[] testEmails = {"test@karooth.com", "admin@karooth.com", "mike@karooth.com", "debug@test.com"};
            for (String email : testEmails) {
                try {
                    boolean exists = userService.existsByEmail(email);
                    result.append(email).append(": ").append(exists ? "EXISTS" : "NOT FOUND").append("\n");
                    
                    if (exists) {
                        try {
                            UserDetails userDetails = userService.loadUserByUsername(email);
                            result.append("  - Password hash: ").append(userDetails.getPassword()).append("\n");
                            result.append("  - Authorities: ").append(userDetails.getAuthorities()).append("\n");
                        } catch (Exception e) {
                            result.append("  - Error loading details: ").append(e.getMessage()).append("\n");
                        }
                    }
                } catch (Exception e) {
                    result.append(email).append(": ERROR - ").append(e.getMessage()).append("\n");
                }
            }
            
            log.info("Debug users completed");
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            log.error("Error checking users: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error checking users: " + e.getMessage() + "\nStack trace: " + e.getStackTrace()[0]);
        }
    }
    
    @GetMapping("/test-stored-hash")
    public ResponseEntity<String> testStoredHash() {
        try {
            log.info("Testing stored hash against 'password123'...");
            String testEmail = "test@karooth.com";
            String plainPassword = "password123";
            
            if (!userService.existsByEmail(testEmail)) {
                return ResponseEntity.badRequest().body("Test user not found: " + testEmail);
            }
            
            UserDetails userDetails = userService.loadUserByUsername(testEmail);
            String storedHash = userDetails.getPassword();
            
            boolean matches = passwordEncoder.matches(plainPassword, storedHash);
            
            String result = String.format("""
                === STORED HASH TEST ===
                Test user: %s
                Plain password: %s
                Stored hash: %s
                Matches: %s
                Current time: %s
                """, testEmail, plainPassword, storedHash, matches, System.currentTimeMillis());
            
            log.info("Stored hash test result: {}", matches);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing stored hash: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
