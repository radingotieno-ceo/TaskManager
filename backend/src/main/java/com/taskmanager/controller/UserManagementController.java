package com.taskmanager.controller;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/managers")
    public ResponseEntity<List<User>> getAllManagers() {
        List<User> managers = userManagementService.getManagers();
        return ResponseEntity.ok(managers);
    }

    @GetMapping("/available-for-tasks")
    public ResponseEntity<List<User>> getUsersAvailableForTaskAssignment() {
        List<User> users = userManagementService.getUsersAvailableForTaskAssignment();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Map<String, String>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        String newRole = request.get("role");
        if (newRole == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
        }

        try {
            Role role = Role.valueOf(newRole.toUpperCase());
            userManagementService.updateUserRole(userId, role);
            return ResponseEntity.ok(Map.of("message", "User role updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }
    }

    @PostMapping("/{userId}/promote-to-manager")
    public ResponseEntity<Map<String, String>> promoteUserToManager(@PathVariable Long userId) {
        userManagementService.promoteUserToManager(userId);
        return ResponseEntity.ok(Map.of("message", "User promoted to manager successfully"));
    }

    @PostMapping("/{userId}/demote-to-user")
    public ResponseEntity<Map<String, String>> demoteManagerToUser(@PathVariable Long userId) {
        userManagementService.demoteManagerToUser(userId);
        return ResponseEntity.ok(Map.of("message", "Manager demoted to user successfully"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> statistics = userManagementService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
