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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.List;
import com.taskmanager.dto.UpdateProfileRequest;
import com.taskmanager.dto.ChangePasswordRequest;
import com.taskmanager.dto.MessageResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import java.io.File;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @PostMapping("/test-cors")
    public ResponseEntity<String> testCors() {
        try {
            log.info("CORS test endpoint called");
            return ResponseEntity.ok("CORS is working! Current time: " + System.currentTimeMillis());
        } catch (Exception e) {
            log.error("CORS test endpoint error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/create-test-user")
    public ResponseEntity<String> createTestUser() {
        try {
            log.info("Creating test user...");
            
            RegisterRequest testRequest = new RegisterRequest(
                "Test User",
                "test@karooth.com",
                "password123",
                "USER"
            );
            
            UserDto user = userService.createUser(testRequest);
            log.info("Test user created successfully: {}", user.getEmail());
            
            return ResponseEntity.ok("Test user created successfully: " + user.getEmail());
        } catch (Exception e) {
            log.error("Error creating test user: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error creating test user: " + e.getMessage());
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

    @GetMapping("/test-jwt")
    public ResponseEntity<String> testJwt() {
        try {
            log.info("Testing JWT authentication...");
            return ResponseEntity.ok("JWT authentication is working! Current time: " + System.currentTimeMillis());
        } catch (Exception e) {
            log.error("JWT test endpoint error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test-jwt-details")
    public ResponseEntity<String> testJwtDetails() {
        try {
            log.info("Testing JWT authentication with details...");
            
            // Get current authentication
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null) {
                return ResponseEntity.status(401).body("No authentication found");
            }
            
            String result = String.format("""
                === JWT AUTHENTICATION TEST ===
                Authenticated: %s
                Principal: %s
                Authorities: %s
                Details: %s
                Current time: %s
                """, 
                auth.isAuthenticated(),
                auth.getPrincipal(),
                auth.getAuthorities(),
                auth.getDetails(),
                System.currentTimeMillis()
            );
            
            log.info("JWT authentication test successful");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("JWT details test endpoint error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/debug-jwt")
    public ResponseEntity<String> debugJwt(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("=== JWT DEBUG ENDPOINT ===");
            log.info("Authorization header: {}", authHeader);
            
            StringBuilder result = new StringBuilder();
            result.append("=== JWT DEBUG RESULTS ===\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Check Authorization header
            if (authHeader == null) {
                result.append("❌ Authorization header is missing\n");
                return ResponseEntity.status(401).body(result.toString());
            }
            
            if (!authHeader.startsWith("Bearer ")) {
                result.append("❌ Authorization header does not start with 'Bearer '\n");
                result.append("Header value: ").append(authHeader).append("\n");
                return ResponseEntity.status(401).body(result.toString());
            }
            
            String token = authHeader.substring(7);
            result.append("✅ Authorization header format is correct\n");
            result.append("Token length: ").append(token.length()).append(" characters\n");
            
            // Try to parse the token
            try {
                String username = jwtUtil.getUsernameFromToken(token);
                result.append("✅ Username extracted from token: ").append(username).append("\n");
                
                // Check if user exists
                if (userService.existsByEmail(username)) {
                    result.append("✅ User exists in database\n");
                    
                    // Load user details
                    UserDetails userDetails = userService.loadUserByUsername(username);
                    result.append("✅ User details loaded successfully\n");
                    result.append("User authorities: ").append(userDetails.getAuthorities()).append("\n");
                    
                    // Validate token
                    boolean isValid = jwtUtil.validateToken(token, userDetails);
                    result.append("Token validation result: ").append(isValid ? "✅ VALID" : "❌ INVALID").append("\n");
                    
                    if (isValid) {
                        result.append("🎉 JWT authentication is working correctly!\n");
                    } else {
                        result.append("❌ JWT token validation failed\n");
                    }
                } else {
                    result.append("❌ User not found in database: ").append(username).append("\n");
                }
                
            } catch (ExpiredJwtException e) {
                result.append("❌ JWT token has expired: ").append(e.getMessage()).append("\n");
            } catch (MalformedJwtException e) {
                result.append("❌ JWT token is malformed: ").append(e.getMessage()).append("\n");
            } catch (UnsupportedJwtException e) {
                result.append("❌ JWT token is unsupported: ").append(e.getMessage()).append("\n");
            } catch (IllegalArgumentException e) {
                result.append("❌ JWT token parsing error: ").append(e.getMessage()).append("\n");
            } catch (Exception e) {
                result.append("❌ Unexpected error: ").append(e.getMessage()).append("\n");
            }
            
            log.info("JWT debug completed");
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("JWT debug endpoint error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/list-users")
    public ResponseEntity<String> listAllUsers() {
        try {
            log.info("Listing all users in database...");
            
            StringBuilder result = new StringBuilder();
            result.append("=== DATABASE USERS LIST ===\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Get all users from repository
            var users = userService.getAllUsers();
            
            if (users.isEmpty()) {
                result.append("❌ No users found in database\n");
                result.append("This might indicate:\n");
                result.append("- Database is empty\n");
                result.append("- Data initialization failed\n");
                result.append("- Wrong database connection\n");
            } else {
                result.append("✅ Found ").append(users.size()).append(" user(s) in database:\n\n");
                
                for (int i = 0; i < users.size(); i++) {
                    var user = users.get(i);
                    result.append(i + 1).append(". ").append(user.getName()).append("\n");
                    result.append("   Email: ").append(user.getEmail()).append("\n");
                    result.append("   Role: ").append(user.getRole()).append("\n");
                    result.append("   ID: ").append(user.getId()).append("\n");
                    result.append("\n");
                }
                
                // Test password verification for known users
                result.append("=== PASSWORD VERIFICATION TESTS ===\n");
                String testPassword = "password123";
                
                for (var user : users) {
                    try {
                        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
                        boolean passwordMatches = passwordEncoder.matches(testPassword, userDetails.getPassword());
                        result.append(user.getEmail()).append(" - Password 'password123' matches: ")
                              .append(passwordMatches ? "✅ YES" : "❌ NO").append("\n");
                    } catch (Exception e) {
                        result.append(user.getEmail()).append(" - Error testing password: ").append(e.getMessage()).append("\n");
                    }
                }
            }
            
            log.info("User list completed. Found {} users", users.size());
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("Error listing users: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error listing users: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getUsersAsJson() {
        try {
            log.info("Getting users as JSON for frontend...");
            List<UserDto> users = userService.getAllUsers();
            log.info("Returning {} users as JSON", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error getting users as JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile/update")
    public ResponseEntity<UserDto> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            log.info("Updating profile for user: {}", request.getEmail());
            UserDto updatedUser = userService.updateProfile(request);
            log.info("Profile updated successfully for user: {}", request.getEmail());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile/change-password")
    public ResponseEntity<MessageResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            log.info("Changing password for user: {}", request.getEmail());
            userService.changePassword(request);
            log.info("Password changed successfully for user: {}", request.getEmail());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error changing password: " + e.getMessage()));
        }
    }

    @PostMapping("/profile/upload-photo")
    public ResponseEntity<MessageResponse> uploadProfilePhoto(@RequestParam("file") MultipartFile file) {
        try {
            log.info("=== PROFILE PHOTO UPLOAD REQUEST ===");
            log.info("File name: {}", file.getOriginalFilename());
            log.info("File size: {} bytes", file.getSize());
            log.info("Content type: {}", file.getContentType());
            log.info("File empty: {}", file.isEmpty());
            
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                log.info("Current user: {}", authentication.getName());
                log.info("User authorities: {}", authentication.getAuthorities());
            } else {
                log.warn("No authentication found in security context");
            }
            
            String photoUrl = userService.uploadProfilePhoto(file);
            log.info("Profile photo uploaded successfully: {}", photoUrl);
            
            // Update the user's profile in the database with the new photo URL
            String userEmail = authentication.getName();
            UserDto updatedUser = userService.updateProfilePhoto(userEmail, photoUrl);
            log.info("User profile updated with new photo URL: {}", updatedUser.getProfilePhotoUrl());
            
            return ResponseEntity.ok(new MessageResponse("Profile photo uploaded successfully", photoUrl));
        } catch (Exception e) {
            log.error("Error uploading profile photo: {}", e.getMessage());
            log.error("Exception type: {}", e.getClass().getSimpleName());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new MessageResponse("Error uploading photo: " + e.getMessage()));
        }
    }

    @GetMapping("/uploads/profile-photos/{filename}")
    public ResponseEntity<Resource> getProfilePhoto(@PathVariable String filename) {
        try {
            // Use the same upload directory path as the upload method
            String userHome = System.getProperty("user.home");
            String filePath = userHome + File.separator + "taskmanager-uploads" + File.separator + "profile-photos" + File.separator + filename;
            File file = new File(filePath);
            
            if (!file.exists()) {
                log.warn("Profile photo not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error serving profile photo: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/check-user/{email}")
    public ResponseEntity<String> checkSpecificUser(@PathVariable String email) {
        try {
            log.info("Checking specific user: {}", email);
            
            StringBuilder result = new StringBuilder();
            result.append("=== USER DETAILS CHECK ===\n");
            result.append("Email: ").append(email).append("\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Check if user exists
            boolean exists = userService.existsByEmail(email);
            result.append("User exists: ").append(exists ? "✅ YES" : "❌ NO").append("\n\n");
            
            if (exists) {
                try {
                    // Get user details
                    UserDto user = userService.findByEmail(email);
                    result.append("=== USER DETAILS ===\n");
                    result.append("ID: ").append(user.getId()).append("\n");
                    result.append("Name: ").append(user.getName()).append("\n");
                    result.append("Email: ").append(user.getEmail()).append("\n");
                    result.append("Role: ").append(user.getRole()).append("\n\n");
                    
                    // Load UserDetails for authentication testing
                    UserDetails userDetails = userService.loadUserByUsername(email);
                    result.append("=== AUTHENTICATION DETAILS ===\n");
                    result.append("Username: ").append(userDetails.getUsername()).append("\n");
                    result.append("Authorities: ").append(userDetails.getAuthorities()).append("\n");
                    result.append("Account non-expired: ").append(userDetails.isAccountNonExpired()).append("\n");
                    result.append("Account non-locked: ").append(userDetails.isAccountNonLocked()).append("\n");
                    result.append("Credentials non-expired: ").append(userDetails.isCredentialsNonExpired()).append("\n");
                    result.append("Account enabled: ").append(userDetails.isEnabled()).append("\n\n");
                    
                    // Test password verification
                    String testPassword = "password123";
                    boolean passwordMatches = passwordEncoder.matches(testPassword, userDetails.getPassword());
                    result.append("=== PASSWORD TEST ===\n");
                    result.append("Test password: ").append(testPassword).append("\n");
                    result.append("Password matches: ").append(passwordMatches ? "✅ YES" : "❌ NO").append("\n");
                    result.append("Stored hash: ").append(userDetails.getPassword()).append("\n");
                    
                } catch (Exception e) {
                    result.append("❌ Error loading user details: ").append(e.getMessage()).append("\n");
                }
            }
            
            log.info("User check completed for: {}", email);
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("Error checking user {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body("Error checking user: " + e.getMessage());
        }
    }

    @PostMapping("/create-default-users")
    public ResponseEntity<String> createDefaultUsers() {
        try {
            log.info("Manually creating default users...");
            
            StringBuilder result = new StringBuilder();
            result.append("=== CREATING DEFAULT USERS ===\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Create default users
            String[] userData = {
                "Test Manager,test@karooth.com,MANAGER",
                "Test User,user@karooth.com,USER", 
                "Admin User,admin@karooth.com,ADMIN"
            };
            
            int createdCount = 0;
            int existingCount = 0;
            
            for (String userInfo : userData) {
                String[] parts = userInfo.split(",");
                String name = parts[0];
                String email = parts[1];
                String role = parts[2];
                
                if (!userService.existsByEmail(email)) {
                    try {
                        RegisterRequest request = new RegisterRequest(name, email, "password123", role);
                        UserDto user = userService.createUser(request);
                        result.append("✅ Created user: ").append(user.getEmail()).append(" (ID: ").append(user.getId()).append(")\n");
                        createdCount++;
                    } catch (Exception e) {
                        result.append("❌ Error creating user ").append(email).append(": ").append(e.getMessage()).append("\n");
                    }
                } else {
                    result.append("ℹ️  User already exists: ").append(email).append("\n");
                    existingCount++;
                }
            }
            
            result.append("\n=== SUMMARY ===\n");
            result.append("Users created: ").append(createdCount).append("\n");
            result.append("Users already existed: ").append(existingCount).append("\n");
            result.append("Total users in database: ").append(userService.getAllUsers().size()).append("\n");
            
            log.info("Default user creation completed. Created: {}, Existing: {}", createdCount, existingCount);
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("Error creating default users: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error creating default users: " + e.getMessage());
        }
    }

    @GetMapping("/database-status")
    public ResponseEntity<String> getDatabaseStatus() {
        try {
            log.info("Checking database status...");
            
            StringBuilder result = new StringBuilder();
            result.append("=== DATABASE STATUS ===\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Get all users
            var users = userService.getAllUsers();
            result.append("Total users in database: ").append(users.size()).append("\n\n");
            
            if (users.isEmpty()) {
                result.append("❌ No users found in database\n");
                result.append("This indicates:\n");
                result.append("- Database is empty\n");
                result.append("- Data initialization failed\n");
                result.append("- Need to create default users\n\n");
                result.append("To create default users, call: POST /api/auth/create-default-users\n");
            } else {
                result.append("✅ Users found in database:\n");
                for (int i = 0; i < users.size(); i++) {
                    var user = users.get(i);
                    result.append(i + 1).append(". ").append(user.getName()).append(" (").append(user.getEmail()).append(") - ").append(user.getRole()).append("\n");
                }
                
                // Check for expected users
                result.append("\n=== EXPECTED USERS CHECK ===\n");
                String[] expectedEmails = {"test@karooth.com", "user@karooth.com", "admin@karooth.com"};
                for (String email : expectedEmails) {
                    boolean exists = userService.existsByEmail(email);
                    result.append(email).append(": ").append(exists ? "✅ FOUND" : "❌ MISSING").append("\n");
                }
            }
            
            log.info("Database status check completed");
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("Error checking database status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error checking database status: " + e.getMessage());
        }
    }

    @GetMapping("/database-full-status")
    public ResponseEntity<String> getFullDatabaseStatus() {
        try {
            log.info("Checking full database status...");
            
            StringBuilder result = new StringBuilder();
            result.append("=== FULL DATABASE STATUS ===\n");
            result.append("Current time: ").append(System.currentTimeMillis()).append("\n\n");
            
            // Users
            var users = userService.getAllUsers();
            result.append("=== USERS ===\n");
            result.append("Total users: ").append(users.size()).append("\n");
            for (int i = 0; i < users.size(); i++) {
                var user = users.get(i);
                result.append(i + 1).append(". ").append(user.getName()).append(" (").append(user.getEmail()).append(") - ").append(user.getRole()).append("\n");
            }
            result.append("\n");
            
            // Summary
            result.append("=== SUMMARY ===\n");
            result.append("Database is using: ").append("File-based H2 (persistent)").append("\n");
            result.append("Data will persist between server restarts: ✅ YES\n");
            result.append("Database file location: ./data/taskmanager.mv.db\n");
            result.append("\n");
            result.append("Projects and tasks will be created automatically on first startup.\n");
            result.append("Check the server logs for database initialization details.\n");
            
            log.info("Full database status check completed");
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("Error checking full database status: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error checking full database status: " + e.getMessage());
        }
    }
}
