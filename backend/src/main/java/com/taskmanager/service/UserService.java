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

import java.util.List;
import java.util.stream.Collectors;
import com.taskmanager.dto.UpdateProfileRequest;
import com.taskmanager.dto.ChangePasswordRequest;
import com.taskmanager.exception.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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

    public List<UserDto> getAllUsers() {
        log.info("Getting all users from database");
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
        log.info("Found {} users in database", userDtos.size());
        return userDtos;
    }

    public UserDto updateProfile(UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + request.getEmail()));
        
        user.setName(request.getName());
        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        
        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", request.getEmail());
        return new UserDto(savedUser);
    }

    public void changePassword(ChangePasswordRequest request) {
        log.info("Changing password for user: {}", request.getEmail());
        
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + request.getEmail()));
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Encode and set new password
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedNewPassword);
        
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", request.getEmail());
    }

    public String uploadProfilePhoto(MultipartFile file) {
        log.info("Uploading profile photo, file size: {} bytes", file.getSize());
        
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }
        
        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "profile_" + System.currentTimeMillis() + extension;
            
            // Create uploads directory if it doesn't exist
            String uploadDir = "uploads/profile-photos";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    log.error("Failed to create upload directory: {}", uploadDir);
                    throw new RuntimeException("Failed to create upload directory");
                }
                log.info("Created upload directory: {}", uploadDir);
            }
            
            // Save file
            String filePath = uploadDir + "/" + filename;
            File dest = new File(filePath);
            file.transferTo(dest);
            
            // Return the URL (for now, just return the filename)
            String photoUrl = "/api/auth/uploads/profile-photos/" + filename;
            log.info("Profile photo uploaded successfully: {}", photoUrl);
            return photoUrl;
            
        } catch (IOException e) {
            log.error("Error saving profile photo: {}", e.getMessage());
            throw new RuntimeException("Error saving profile photo", e);
        }
    }
}


