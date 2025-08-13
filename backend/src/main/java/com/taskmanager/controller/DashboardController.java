package com.taskmanager.controller;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        // Find the user by email and get their role-based dashboard data
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        Role userRole = user.getRole();
        Long userId = user.getId();
        
        Map<String, Object> dashboardData = dashboardService.getUserRoleStats(userId, userRole);
        
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/manager")
    public ResponseEntity<Map<String, Object>> getManagerDashboard() {
        Map<String, Object> dashboardData = dashboardService.getManagerDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable Long userId) {
        Map<String, Object> dashboardData = dashboardService.getUserDashboardData(userId);
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        Map<String, Object> dashboardData = dashboardService.getAdminDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/role-based/{role}")
    public ResponseEntity<Map<String, Object>> getRoleBasedDashboard(
            @PathVariable String role,
            @RequestParam(required = false) Long userId) {
        
        Role userRole = Role.valueOf(role.toUpperCase());
        Map<String, Object> dashboardData;
        
        switch (userRole) {
            case ADMIN:
                dashboardData = dashboardService.getAdminDashboardData();
                break;
            case MANAGER:
                dashboardData = dashboardService.getManagerDashboardData();
                break;
            case USER:
                if (userId == null) {
                    return ResponseEntity.badRequest().build();
                }
                dashboardData = dashboardService.getUserDashboardData(userId);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/project/{projectId}/progress")
    public ResponseEntity<Map<String, Object>> getProjectProgress(@PathVariable Long projectId) {
        double progress = dashboardService.calculateProjectProgress(projectId);
        Map<String, Object> response = Map.of(
            "projectId", projectId,
            "progress", Math.round(progress)
        );
        return ResponseEntity.ok(response);
    }
}
