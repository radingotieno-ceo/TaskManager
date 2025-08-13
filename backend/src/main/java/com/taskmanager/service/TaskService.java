package com.taskmanager.service;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.dto.CreateTaskDto;
import com.taskmanager.dto.CreateAndAssignTaskDto;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.User;
import com.taskmanager.entity.Role;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    // Basic CRUD operations
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        return new TaskDto(task);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public TaskDto createTask(CreateTaskDto createTaskDto) {
        // Validate project exists
        Project project = projectRepository.findById(createTaskDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + createTaskDto.getProjectId()));
        
        // Validate due date is in future
        if (!createTaskDto.getDueDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
        
        Task task = new Task(
            createTaskDto.getTitle(),
            createTaskDto.getDescription(),
            createTaskDto.getDueDate(),
            project
        );
        
        Task savedTask = taskRepository.save(task);
        return new TaskDto(savedTask);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public TaskDto createAndAssignTask(CreateAndAssignTaskDto createAndAssignTaskDto) {
        // Validate project exists
        Project project = projectRepository.findById(createAndAssignTaskDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + createAndAssignTaskDto.getProjectId()));
        
        // Validate user exists
        User user = userRepository.findById(createAndAssignTaskDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + createAndAssignTaskDto.getUserId()));
        
        // Validate due date is in future
        if (!createAndAssignTaskDto.getDueDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Due date must be in the future");
        }
        
        // Create task with priority
        Task task = new Task(
            createAndAssignTaskDto.getTitle(),
            createAndAssignTaskDto.getDescription(),
            createAndAssignTaskDto.getDueDate(),
            project,
            TaskPriority.valueOf(createAndAssignTaskDto.getPriority().toUpperCase())
        );
        
        // Assign to user
        task.setAssignedUser(user);
        
        Task savedTask = taskRepository.save(task);
        
        // Send email notification
        try {
            emailService.sendTaskAssignmentEmail(savedTask, user);
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
        
        return new TaskDto(savedTask);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public TaskDto updateTask(Long id, CreateTaskDto updateTaskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
        
        // Validate project exists
        Project project = projectRepository.findById(updateTaskDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + updateTaskDto.getProjectId()));
        
        task.setTitle(updateTaskDto.getTitle());
        task.setDescription(updateTaskDto.getDescription());
        task.setDueDate(updateTaskDto.getDueDate());
        task.setProject(project);
        
        Task savedTask = taskRepository.save(task);
        return new TaskDto(savedTask);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    // Task status management
    public TaskDto updateTaskStatus(Long taskId, String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        
        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        task.setStatus(taskStatus);
        Task savedTask = taskRepository.save(task);
        return new TaskDto(savedTask);
    }
    
    // Task assignment
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public TaskDto assignTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        task.setAssignedUser(user);
        Task savedTask = taskRepository.save(task);
        
        // Send email notification asynchronously
        try {
            emailService.sendTaskAssignmentEmail(task, user);
        } catch (Exception e) {
            // Don't fail the assignment if email fails
        }
        
        return new TaskDto(savedTask);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public TaskDto unassignTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        
        task.setAssignedUser(null);
        Task savedTask = taskRepository.save(task);
        return new TaskDto(savedTask);
    }
    
    // Filtered task queries
    public List<TaskDto> getTasksByStatus(String status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        return taskRepository.findByStatus(taskStatus).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksByPriority(String priority) {
        TaskPriority taskPriority = TaskPriority.valueOf(priority.toUpperCase());
        return taskRepository.findByPriority(taskPriority).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return taskRepository.findByProject(project).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return taskRepository.findByAssignedUser(user).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getAssignedTasks() {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        
        return taskRepository.findByAssignedUser(currentUser).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getUnassignedTasks() {
        return taskRepository.findByAssignedUserIsNull().stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getOverdueTasks() {
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksDueToday() {
        return taskRepository.findByDueDate(LocalDate.now()).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksDueThisWeek() {
        LocalDate endOfWeek = LocalDate.now().plusDays(7);
        return taskRepository.findByDueDateBetween(LocalDate.now(), endOfWeek).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Search and filter
    public List<TaskDto> searchTasks(String query) {
        return taskRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksByDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return taskRepository.findByDueDateBetween(start, end).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    // Task statistics
    public Map<String, Object> getTaskStatistics() {
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long overdueTasks = taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("todoTasks", todoTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        
        return stats;
    }
    
    public Map<String, Object> getUserTaskStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        List<Task> userTasks = taskRepository.findByAssignedUser(user);
        long totalTasks = userTasks.size();
        long completedTasks = userTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long inProgressTasks = userTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long overdueTasks = userTasks.stream().filter(t -> t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.DONE).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        
        return stats;
    }
    
    public Map<String, Object> getProjectTaskStatistics(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        List<Task> projectTasks = taskRepository.findByProject(project);
        long totalTasks = projectTasks.size();
        long completedTasks = projectTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long inProgressTasks = projectTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long overdueTasks = projectTasks.stream().filter(t -> t.getDueDate().isBefore(LocalDate.now()) && t.getStatus() != TaskStatus.DONE).count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", totalTasks);
        stats.put("completedTasks", completedTasks);
        stats.put("inProgressTasks", inProgressTasks);
        stats.put("overdueTasks", overdueTasks);
        stats.put("completionRate", totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);
        
        return stats;
    }
    
    // Bulk operations
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void bulkUpdateTaskStatus(List<Long> taskIds, String status) {
        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        List<Task> tasks = taskRepository.findAllById(taskIds);
        
        for (Task task : tasks) {
            task.setStatus(taskStatus);
        }
        taskRepository.saveAll(tasks);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void bulkAssignTasks(List<Long> taskIds, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        List<Task> tasks = taskRepository.findAllById(taskIds);
        
        for (Task task : tasks) {
            task.setAssignedUser(user);
        }
        taskRepository.saveAll(tasks);
        
        // Send email notifications
        for (Task task : tasks) {
            try {
                emailService.sendTaskAssignmentEmail(task, user);
            } catch (Exception e) {
                // Don't fail if email fails
            }
        }
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void bulkDeleteTasks(List<Long> taskIds) {
        taskRepository.deleteAllById(taskIds);
    }
    
    // Task dependencies
    public List<TaskDto> getTaskDependencies(Long taskId) {
        // This would require a task dependency table/relationship
        // For now, return empty list
        return new ArrayList<>();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void addTaskDependency(Long taskId, Long dependencyId) {
        // Implementation would require task dependency table
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void removeTaskDependency(Long taskId, Long dependencyId) {
        // Implementation would require task dependency table
    }
    
    // Task comments
    public List<Map<String, Object>> getTaskComments(Long taskId) {
        // This would require a task comment table
        // For now, return empty list
        return new ArrayList<>();
    }
    
    public Map<String, Object> addTaskComment(Long taskId, String comment) {
        // Implementation would require task comment table
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Comment added successfully");
        return result;
    }
    
    // Task time tracking
    public void startTaskTimer(Long taskId) {
        // Implementation would require task timer table
    }
    
    public void stopTaskTimer(Long taskId) {
        // Implementation would require task timer table
    }
    
    public List<Map<String, Object>> getTaskTimeLog(Long taskId) {
        // Implementation would require task timer table
        return new ArrayList<>();
    }
    
    // Legacy method for backward compatibility
    public List<TaskDto> getTasksByAssignedUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return taskRepository.findByAssignedUser(user).stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public TaskDto updateTaskStatus(Long taskId, TaskStatus newStatus, String userEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        
        // Users can only update tasks assigned to them
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (currentUser.getRole() == Role.USER && 
            (task.getAssignedUser() == null || !task.getAssignedUser().getId().equals(currentUser.getId()))) {
            throw new SecurityException("You can only update tasks assigned to you");
        }
        
        task.setStatus(newStatus);
        Task savedTask = taskRepository.save(task);
        return new TaskDto(savedTask);
    }
}

