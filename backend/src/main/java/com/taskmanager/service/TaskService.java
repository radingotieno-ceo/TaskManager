package com.taskmanager.service;

import com.taskmanager.dto.CreateTaskDto;
import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.User;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.Role;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
    }
    
    public List<TaskDto> getTasksByAssignedUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return taskRepository.findByAssignedUser(user).stream()
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
    
    @PreAuthorize("hasRole('MANAGER')")
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

