package com.taskmanager.service;

import com.taskmanager.dto.RegisterRequest;
import com.taskmanager.dto.UserDto;
import com.taskmanager.entity.User;
import com.taskmanager.entity.Role;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("=== UserService.loadUserByUsername DEBUG ===");
        log.info("Loading user by email: {}", email);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
            
            log.info("Found user: {}", user.getEmail());
            log.info("Stored password hash: {}", user.getPassword());
            log.info("User authorities: {}", user.getAuthorities());
            log.info("Account non-expired: {}", user.isAccountNonExpired());
            log.info("Account non-locked: {}", user.isAccountNonLocked());
            log.info("Credentials non-expired: {}", user.isCredentialsNonExpired());
            log.info("Account enabled: {}", user.isEnabled());
            log.info("User role: {}", user.getRole());
            
            return user;
        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", email);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user by email {}: {}", email, e.getMessage());
            throw e;
        }
    }
    
    public UserDto createUser(RegisterRequest request) {
        log.info("Creating user: {} with role: {}", request.getEmail(), request.getRole());
        
        // Convert string role to Role enum
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role '{}', defaulting to USER", request.getRole());
            role = Role.USER; // Default to USER if invalid role
        }
        
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        log.info("Password encoded successfully for user: {}", request.getEmail());
        
        User user = new User(
            request.getName(),
            request.getEmail(),
            encodedPassword,
            role
        );
        
        User savedUser = userRepository.save(user);
        log.info("User created and saved successfully: {}", savedUser.getEmail());
        return new UserDto(savedUser);
    }
    
    public UserDto findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        log.info("User found: {}", user.getEmail());
        return new UserDto(user);
    }
    
    public boolean existsByEmail(String email) {
        log.info("Checking if user exists by email: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        log.info("User exists: {} for email: {}", exists, email);
        return exists;
    }
}


