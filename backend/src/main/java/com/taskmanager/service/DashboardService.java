package com.taskmanager.service;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // Manager/Admin Dashboard Data
    public Map<String, Object> getManagerDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        // Project Statistics
        dashboardData.put("totalProjects", projectRepository.countTotalProjects());
        dashboardData.put("recentProjects", projectRepository.findRecentProjects(LocalDateTime.now().minusDays(30)));
        dashboardData.put("projectsWithOverdueTasks", projectRepository.findProjectsWithOverdueTasks(today));

        // Task Statistics
        dashboardData.put("totalTasks", taskRepository.count());
        dashboardData.put("activeTasks", taskRepository.findByStatus(TaskStatus.IN_PROGRESS).size());
        dashboardData.put("completedTasks", taskRepository.findByStatus(TaskStatus.DONE).size());
        dashboardData.put("overdueTasks", taskRepository.findOverdueTasks(today));
        dashboardData.put("tasksDueThisWeek", taskRepository.findTasksDueThisWeek(startOfWeek, endOfWeek));

        // User Statistics
        dashboardData.put("totalUsers", userRepository.countTotalUsers());
        dashboardData.put("activeUsers", userRepository.findActiveUsers(LocalDateTime.now().minusDays(30)));
        dashboardData.put("usersWithOverdueTasks", userRepository.findUsersWithOverdueTasks(today));
        dashboardData.put("topPerformers", userRepository.findTopPerformers());

        return dashboardData;
    }

    // User Dashboard Data
    public Map<String, Object> getUserDashboardData(Long userId) {
        Map<String, Object> dashboardData = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        LocalDate threeDaysLater = today.plusDays(3);

        // User's Task Statistics
        dashboardData.put("myTasks", taskRepository.findByAssignedUserId(userId));
        dashboardData.put("activeTasks", taskRepository.findByAssignedUserIdAndStatus(userId, TaskStatus.IN_PROGRESS));
        dashboardData.put("completedTasks", taskRepository.findByAssignedUserIdAndStatus(userId, TaskStatus.DONE));
        dashboardData.put("overdueTasks", taskRepository.findOverdueTasks(today).stream()
                .filter(task -> task.getAssignedUser().getId().equals(userId))
                .toList());
        dashboardData.put("highPriorityTasks", taskRepository.findHighPriorityTasks(userId, today, threeDaysLater));

        // Task Counts
        dashboardData.put("totalMyTasks", taskRepository.countByAssignedUserIdAndStatus(userId, TaskStatus.TODO) +
                taskRepository.countByAssignedUserIdAndStatus(userId, TaskStatus.IN_PROGRESS) +
                taskRepository.countByAssignedUserIdAndStatus(userId, TaskStatus.DONE));
        dashboardData.put("dueToday", taskRepository.countTasksDueTodayForUser(userId, today));
        dashboardData.put("dueThisWeek", taskRepository.countTasksDueThisWeekForUser(userId, startOfWeek, endOfWeek));
        dashboardData.put("completedCount", taskRepository.countByAssignedUserIdAndStatus(userId, TaskStatus.DONE));

        // Project Progress for User's Tasks
        List<Task> userTasks = taskRepository.findByAssignedUserId(userId);
        Map<Long, Map<String, Object>> projectProgress = new HashMap<>();
        
        for (Task task : userTasks) {
            Long projectId = task.getProject().getId();
            if (!projectProgress.containsKey(projectId)) {
                long totalTasks = taskRepository.countTotalTasksByProject(projectId);
                long completedTasks = taskRepository.countCompletedTasksByProject(projectId);
                double progress = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
                
                Map<String, Object> progressData = new HashMap<>();
                progressData.put("project", task.getProject());
                progressData.put("totalTasks", totalTasks);
                progressData.put("completedTasks", completedTasks);
                progressData.put("progress", Math.round(progress));
                progressData.put("myTasksInProject", userTasks.stream()
                        .filter(t -> t.getProject().getId().equals(projectId))
                        .toList());
                
                projectProgress.put(projectId, progressData);
            }
        }
        dashboardData.put("projectProgress", projectProgress.values());

        return dashboardData;
    }

    // Admin Dashboard Data
    public Map<String, Object> getAdminDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        LocalDate today = LocalDate.now();

        // System Overview
        dashboardData.put("totalUsers", userRepository.countTotalUsers());
        dashboardData.put("totalProjects", projectRepository.countTotalProjects());
        dashboardData.put("totalTasks", taskRepository.count());

        // User Statistics by Role
        dashboardData.put("adminUsers", userRepository.countByRole(Role.ADMIN));
        dashboardData.put("managerUsers", userRepository.countByRole(Role.MANAGER));
        dashboardData.put("regularUsers", userRepository.countByRole(Role.USER));

        // Task Statistics
        dashboardData.put("todoTasks", taskRepository.findByStatus(TaskStatus.TODO).size());
        dashboardData.put("inProgressTasks", taskRepository.findByStatus(TaskStatus.IN_PROGRESS).size());
        dashboardData.put("completedTasks", taskRepository.findByStatus(TaskStatus.DONE).size());
        dashboardData.put("overdueTasks", taskRepository.findOverdueTasks(today));

        // Performance Metrics
        dashboardData.put("topPerformers", userRepository.findTopPerformers());
        dashboardData.put("activeUsers", userRepository.findActiveUsers(LocalDateTime.now().minusDays(30)));
        dashboardData.put("projectsWithOverdueTasks", projectRepository.findProjectsWithOverdueTasks(today));

        // Recent Activity
        dashboardData.put("recentProjects", projectRepository.findRecentProjects(LocalDateTime.now().minusDays(30)));

        return dashboardData;
    }

    // Helper method to calculate project progress percentage
    public double calculateProjectProgress(Long projectId) {
        long totalTasks = taskRepository.countTotalTasksByProject(projectId);
        if (totalTasks == 0) return 0.0;
        
        long completedTasks = taskRepository.countCompletedTasksByProject(projectId);
        return (double) completedTasks / totalTasks * 100;
    }

    // Helper method to get user's role-based statistics
    public Map<String, Object> getUserRoleStats(Long userId, Role userRole) {
        Map<String, Object> stats = new HashMap<>();

        switch (userRole) {
            case ADMIN:
                stats.putAll(getAdminDashboardData());
                break;
            case MANAGER:
                stats.putAll(getManagerDashboardData());
                break;
            case USER:
                stats.putAll(getUserDashboardData(userId));
                break;
        }

        return stats;
    }
}
