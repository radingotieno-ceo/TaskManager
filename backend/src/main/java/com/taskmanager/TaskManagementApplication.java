package com.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taskmanager.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableRetry
@RestController
public class TaskManagementApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }

    @GetMapping("/test-password-encoding")
    public String testPasswordEncoding() {
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String password = "password123";
            String hash = encoder.encode(password);
            boolean matches = encoder.matches(password, hash);
            
            return "Password: " + password + 
                   "\nGenerated Hash: " + hash + 
                   "\nMatches: " + matches +
                   "\n\nTest with existing hash: " +
                   "\nExisting hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi" +
                   "\nMatches existing: " + encoder.matches(password, "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi");
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\nStack trace: " + e.getStackTrace()[0];
        }
    }

    @GetMapping("/simple-test")
    public String simpleTest() {
        return "Simple test endpoint is working!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is running!";
    }

    @GetMapping("/test-db")
    public String testDatabase() {
        try {
            boolean testExists = userService.existsByEmail("test@karooth.com");
            return "Database test: test@karooth.com exists = " + testExists;
        } catch (Exception e) {
            return "Database error: " + e.getMessage();
        }
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting TaskManagementApplication...");
        System.out.println("📊 Database initialization will be handled by DatabaseInitializationService");
        System.out.println("🎯 TaskManagementApplication startup complete!");
    }
}

