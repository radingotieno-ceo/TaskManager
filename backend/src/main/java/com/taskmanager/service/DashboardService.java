package com.taskmanager.service;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectStatus;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class DashboardService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // Admin Dashboard Data
    public Map<String, Object> getAdminDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // System Overview - Summary Cards
        Map<String, Object> systemOverview = new HashMap<>();
        systemOverview.put("totalUsers", userRepository.count());
        systemOverview.put("activeProjects", projectRepository.countByStatus(ProjectStatus.IN_PROGRESS));
        systemOverview.put("tasksCompleted", taskRepository.findByStatus(TaskStatus.DONE).size());
        systemOverview.put("systemHealth", calculateSystemHealth());

        dashboardData.put("systemOverview", systemOverview);

        // Project Portfolio
        List<Map<String, Object>> projectPortfolio = new ArrayList<>();
        List<Project> projects = projectRepository.findAll();
        
        for (Project project : projects) {
            Map<String, Object> projectData = new HashMap<>();
            projectData.put("id", project.getId());
            projectData.put("name", project.getName());
            projectData.put("priority", project.getPriority().name());
            projectData.put("members", countProjectMembers(project.getId()));
            projectData.put("dueDate", formatDate(project.getDueDate()));
            projectData.put("status", project.getStatus().name());
            projectData.put("progress", calculateProjectProgress(project.getId()));
            
            projectPortfolio.add(projectData);
        }
        dashboardData.put("projectPortfolio", projectPortfolio);

        // Team Overview
        Map<String, Object> teamOverview = new HashMap<>();
        teamOverview.put("totalUsers", userRepository.count());
        teamOverview.put("activeUsers", userRepository.findActiveUsers(LocalDateTime.now().minusDays(30)).size());
        teamOverview.put("managers", userRepository.countByRole(Role.MANAGER));
        teamOverview.put("regularUsers", userRepository.countByRole(Role.USER));

        dashboardData.put("teamOverview", teamOverview);

        // Recent Activities
        List<Map<String, Object>> recentActivities = generateRecentActivities();
        dashboardData.put("recentActivities", recentActivities);

        // System Alerts
        List<Map<String, Object>> systemAlerts = generateSystemAlerts();
        dashboardData.put("systemAlerts", systemAlerts);

        return dashboardData;
    }

    // Manager Dashboard Data
    public Map<String, Object> getManagerDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Summary Cards
        Map<String, Object> summaryCards = new HashMap<>();
        summaryCards.put("totalProjects", projectRepository.count());
        summaryCards.put("activeTasks", taskRepository.findByStatus(TaskStatus.IN_PROGRESS).size());
        summaryCards.put("completedTasks", taskRepository.findByStatus(TaskStatus.DONE).size());
        summaryCards.put("overdueTasks", taskRepository.findByDueDateBeforeAndStatusNot(today, TaskStatus.DONE).size());

        dashboardData.put("summaryCards", summaryCards);

        // Recent Projects
        List<Map<String, Object>> recentProjects = new ArrayList<>();
        List<Project> projects = projectRepository.findRecentProjects(LocalDateTime.now().minusDays(30));
        
        for (Project project : projects) {
            Map<String, Object> projectData = new HashMap<>();
            projectData.put("id", project.getId());
            projectData.put("name", project.getName());
            projectData.put("description", project.getDescription());
            projectData.put("progress", calculateProjectProgress(project.getId()));
            projectData.put("dueDate", formatDate(project.getDueDate()));
            projectData.put("teamSize", countProjectMembers(project.getId()));
            
            recentProjects.add(projectData);
        }
        dashboardData.put("recentProjects", recentProjects);

        // Recent Tasks
        List<Map<String, Object>> recentTasks = new ArrayList<>();
        List<Task> tasks = taskRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());
        
        for (Task task : tasks) {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("id", task.getId());
            taskData.put("name", task.getTitle());
            taskData.put("project", task.getProject().getName());
            taskData.put("priority", task.getPriority().name());
            taskData.put("assignee", task.getAssignedUser() != null ? task.getAssignedUser().getName() : "Unassigned");
            taskData.put("status", task.getStatus().name());
            taskData.put("dueDate", formatDate(task.getDueDate()));
            
            recentTasks.add(taskData);
        }
        dashboardData.put("recentTasks", recentTasks);

        // Available Users for Task Assignment
        List<Map<String, Object>> availableUsers = new ArrayList<>();
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", user.getRole().name());
            userData.put("taskCount", taskRepository.findByAssignedUser(user).size());
            
            availableUsers.add(userData);
        }
        dashboardData.put("availableUsers", availableUsers);

        return dashboardData;
    }

    // User Dashboard Data
    public Map<String, Object> getUserDashboardData(Long userId) {
        Map<String, Object> dashboardData = new HashMap<>();
        LocalDate today = LocalDate.now();

        // My Tasks
        List<Map<String, Object>> myTasks = new ArrayList<>();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Task> userTasks = taskRepository.findByAssignedUser(user);
        
        for (Task task : userTasks) {
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("id", task.getId());
            taskData.put("name", task.getTitle());
            taskData.put("project", task.getProject().getName());
            taskData.put("priority", task.getPriority().name());
            taskData.put("progress", calculateTaskProgress(task));
            taskData.put("status", task.getStatus().name());
            taskData.put("dueDate", formatDate(task.getDueDate()));
            
            myTasks.add(taskData);
        }
        dashboardData.put("myTasks", myTasks);

        // My Projects
        List<Map<String, Object>> myProjects = new ArrayList<>();
        Set<Project> userProjects = userTasks.stream()
                .map(Task::getProject)
                .collect(Collectors.toSet());
        
        for (Project project : userProjects) {
            Map<String, Object> projectData = new HashMap<>();
            projectData.put("id", project.getId());
            projectData.put("name", project.getName());
            projectData.put("role", "Developer"); // Default role
            projectData.put("progress", calculateProjectProgress(project.getId()));
            projectData.put("tasksAssigned", userTasks.stream()
                    .filter(t -> t.getProject().getId().equals(project.getId()))
                    .count());
            
            myProjects.add(projectData);
        }
        dashboardData.put("myProjects", myProjects);

        // Task Statistics
        Map<String, Object> taskStats = new HashMap<>();
        taskStats.put("totalTasks", userTasks.size());
        taskStats.put("activeTasks", userTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count());
        taskStats.put("completedTasks", userTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count());
        taskStats.put("overdueTasks", userTasks.stream()
                .filter(task -> task.getDueDate().isBefore(today) && task.getStatus() != TaskStatus.DONE)
                .count());

        dashboardData.put("taskStats", taskStats);

        return dashboardData;
    }

    // Helper methods
    public double calculateProjectProgress(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        List<Task> projectTasks = taskRepository.findByProject(project);
        
        if (projectTasks.isEmpty()) return 0.0;
        
        long completedTasks = projectTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();
        return Math.round((double) completedTasks / projectTasks.size() * 100);
    }

    private double calculateTaskProgress(Task task) {
        // Simple progress calculation based on status
        switch (task.getStatus()) {
            case TODO: return 0.0;
            case IN_PROGRESS: return 50.0;
            case DONE: return 100.0;
            default: return 0.0;
        }
    }

    private int countProjectMembers(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return (int) taskRepository.findByProject(project).stream()
                .map(Task::getAssignedUser)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    private double calculateSystemHealth() {
        // Calculate system health based on various metrics
        long totalTasks = taskRepository.count();
        long overdueTasks = taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).size();
        
        if (totalTasks == 0) return 100.0;
        
        double healthPercentage = ((double) (totalTasks - overdueTasks) / totalTasks) * 100;
        return Math.round(healthPercentage);
    }

    private List<Map<String, Object>> generateRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // Generate sample activities based on recent data
        List<Task> recentTasks = taskRepository.findAll().stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(3)
                .collect(Collectors.toList());
        
        for (Task task : recentTasks) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("avatar", generateAvatar(task.getAssignedUser()));
            activity.put("user", task.getAssignedUser() != null ? task.getAssignedUser().getName() : "System");
            activity.put("action", "Updated task: " + task.getTitle());
            activity.put("role", task.getAssignedUser() != null ? task.getAssignedUser().getRole().name() : "SYSTEM");
            activity.put("time", "2 hours ago"); // Simplified for now
            
            activities.add(activity);
        }
        
        return activities;
    }

    private List<Map<String, Object>> generateSystemAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        // Check for overdue tasks
        long overdueTasks = taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).size();
        if (overdueTasks > 0) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "warning");
            alert.put("title", "Overdue Tasks");
            alert.put("description", overdueTasks + " tasks are overdue");
            alerts.add(alert);
        }
        
        // Check for projects due soon
        List<Project> projectsDueSoon = projectRepository.findByDueDateBetween(
                LocalDate.now(), LocalDate.now().plusDays(7));
        if (!projectsDueSoon.isEmpty()) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("type", "info");
            alert.put("title", "Projects Due Soon");
            alert.put("description", projectsDueSoon.size() + " projects are due within 7 days");
            alerts.add(alert);
        }
        
        return alerts;
    }

    private String generateAvatar(User user) {
        if (user == null) return "S";
        return user.getName().substring(0, Math.min(2, user.getName().length())).toUpperCase();
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
