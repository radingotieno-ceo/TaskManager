package com.taskmanager.service;

import com.taskmanager.dto.ProjectDto;
import com.taskmanager.dto.CreateProjectRequest;
import com.taskmanager.dto.UpdateProjectRequest;
import com.taskmanager.dto.ProjectStatisticsDto;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectStatus;
import com.taskmanager.entity.ProjectPriority;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@Transactional
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    // Basic CRUD operations
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        return new ProjectDto(project);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto createProject(CreateProjectRequest request) {
        Project project = new Project(
            request.getName(), 
            request.getDescription(),
            request.getDueDate(),
            ProjectPriority.valueOf(request.getPriority().toUpperCase())
        );
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            project.setPriority(ProjectPriority.valueOf(request.getPriority().toUpperCase()));
        }
        if (request.getStatus() != null) {
            project.setStatus(ProjectStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getDueDate() != null) {
            project.setDueDate(request.getDueDate());
        }
        
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }
    
    // Project statistics
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectStatisticsDto getProjectStatistics() {
        long totalProjects = projectRepository.count();
        long activeProjects = projectRepository.countByStatus(ProjectStatus.IN_PROGRESS);
        long completedProjects = projectRepository.countByStatus(ProjectStatus.COMPLETED);
        long overdueProjects = projectRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), ProjectStatus.COMPLETED);
        
        // Calculate average progress
        List<Project> allProjects = projectRepository.findAll();
        double averageProgress = allProjects.stream()
                .mapToDouble(this::calculateProjectProgress)
                .average()
                .orElse(0.0);
        
        // Task statistics
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(com.taskmanager.entity.TaskStatus.DONE);
        long overdueTasks = taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), com.taskmanager.entity.TaskStatus.DONE);
        
        return new ProjectStatisticsDto(
            totalProjects, activeProjects, completedProjects, overdueProjects,
            averageProgress, totalTasks, completedTasks, overdueTasks
        );
    }
    
    // Filtered project queries
    public List<ProjectDto> getProjectsByStatus(String status) {
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        return projectRepository.findByStatus(projectStatus).stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    public List<ProjectDto> getProjectsByPriority(String priority) {
        ProjectPriority projectPriority = ProjectPriority.valueOf(priority.toUpperCase());
        return projectRepository.findByPriority(projectPriority).stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    public List<ProjectDto> getOverdueProjects() {
        return projectRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), ProjectStatus.COMPLETED).stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    // User assignment to projects
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void assignUserToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Create a default task to establish the project-user relationship
        // This is a workaround until a proper project_user table is implemented
        com.taskmanager.entity.Task defaultTask = new com.taskmanager.entity.Task(
            "Project Assignment - " + user.getName(),
            "User assigned to project: " + project.getName(),
            LocalDate.now().plusDays(30), // Default due date
            project
        );
        defaultTask.setAssignedUser(user);
        defaultTask.setStatus(com.taskmanager.entity.TaskStatus.TODO);
        
        taskRepository.save(defaultTask);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void removeUserFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        
        // Remove all tasks assigned to this user in this project
        // This effectively removes the user from the project
        List<com.taskmanager.entity.Task> userTasksInProject = taskRepository.findByProject(project).stream()
                .filter(task -> task.getAssignedUser() != null && task.getAssignedUser().getId().equals(userId))
                .collect(Collectors.toList());
        
        for (com.taskmanager.entity.Task task : userTasksInProject) {
            task.setAssignedUser(null);
            taskRepository.save(task);
        }
    }
    
    public List<Map<String, Object>> getProjectUsers(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        // Return users who have tasks assigned in this project
        List<com.taskmanager.entity.Task> projectTasks = taskRepository.findByProject(project);
        Set<User> users = projectTasks.stream()
                .map(com.taskmanager.entity.Task::getAssignedUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        return users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("name", user.getName());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    return userMap;
                })
                .collect(Collectors.toList());
    }
    
    // Project progress and status management
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto updateProjectStatus(Long projectId, String status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        project.setStatus(projectStatus);
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto updateProjectProgress(Long projectId, Integer progress) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        // Validate progress is between 0 and 100
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        
        // For now, we'll calculate progress based on task completion
        // In a real implementation, you might store this as a field
        // double calculatedProgress = calculateProjectProgress(project);
        
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    // Search and filter
    public List<ProjectDto> searchProjects(String query) {
        return projectRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query).stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    public List<ProjectDto> getProjectsByDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return projectRepository.findByDueDateBetween(start, end).stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    // Bulk operations
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void bulkUpdateProjectStatus(List<Long> projectIds, String status) {
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        List<Project> projects = projectRepository.findAllById(projectIds);
        
        for (Project project : projects) {
            project.setStatus(projectStatus);
        }
        projectRepository.saveAll(projects);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public void bulkDeleteProjects(List<Long> projectIds) {
        projectRepository.deleteAllById(projectIds);
    }
    
    // Project tasks
    public List<Map<String, Object>> getProjectTasks(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        List<com.taskmanager.entity.Task> tasks = taskRepository.findByProject(project);
        return tasks.stream()
                .map(task -> {
                    Map<String, Object> taskMap = new HashMap<>();
                    taskMap.put("id", task.getId());
                    taskMap.put("title", task.getTitle());
                    taskMap.put("description", task.getDescription());
                    taskMap.put("status", task.getStatus());
                    taskMap.put("priority", task.getPriority());
                    taskMap.put("dueDate", task.getDueDate());
                    if (task.getAssignedUser() != null) {
                        taskMap.put("assignedUser", task.getAssignedUser().getName());
                    }
                    return taskMap;
                })
                .collect(Collectors.toList());
    }
    
    // Project analytics
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Map<String, Object> getProjectAnalytics(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        List<com.taskmanager.entity.Task> tasks = taskRepository.findByProject(project);
        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == com.taskmanager.entity.TaskStatus.DONE)
                .count();
        long inProgressTasks = tasks.stream()
                .filter(task -> task.getStatus() == com.taskmanager.entity.TaskStatus.IN_PROGRESS)
                .count();
        long overdueTasks = tasks.stream()
                .filter(task -> task.getDueDate().isBefore(LocalDate.now()) && 
                               task.getStatus() != com.taskmanager.entity.TaskStatus.DONE)
                .count();
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("projectId", projectId);
        analytics.put("projectName", project.getName());
        analytics.put("totalTasks", totalTasks);
        analytics.put("completedTasks", completedTasks);
        analytics.put("inProgressTasks", inProgressTasks);
        analytics.put("overdueTasks", overdueTasks);
        analytics.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        analytics.put("progress", calculateProjectProgress(project));
        
        return analytics;
    }
    
    // Project timeline
    public List<Map<String, Object>> getProjectTimeline(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        List<com.taskmanager.entity.Task> tasks = taskRepository.findByProject(project);
        return tasks.stream()
                .map(task -> {
                    Map<String, Object> timelineItem = new HashMap<>();
                    timelineItem.put("id", task.getId());
                    timelineItem.put("title", task.getTitle());
                    timelineItem.put("status", task.getStatus());
                    timelineItem.put("dueDate", task.getDueDate());
                    timelineItem.put("createdAt", task.getCreatedAt());
                    timelineItem.put("updatedAt", task.getUpdatedAt());
                    return timelineItem;
                })
                .sorted((a, b) -> {
                    LocalDate dateA = (LocalDate) a.get("dueDate");
                    LocalDate dateB = (LocalDate) b.get("dueDate");
                    return dateA.compareTo(dateB);
                })
                .collect(Collectors.toList());
    }
    
    // Helper methods
    private double calculateProjectProgress(Project project) {
        List<com.taskmanager.entity.Task> tasks = taskRepository.findByProject(project);
        if (tasks.isEmpty()) {
            return 0.0;
        }
        
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus() == com.taskmanager.entity.TaskStatus.DONE)
                .count();
        
        return (double) completedTasks / tasks.size() * 100;
    }
    
    // Legacy method for backward compatibility
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = new Project(projectDto.getName(), projectDto.getDescription());
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
}


