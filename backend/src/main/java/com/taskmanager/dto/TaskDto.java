package com.taskmanager.dto;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private ProjectDto project;
    private UserDto assignedUser;
    private LocalDateTime createdAt;
    
    // Constructor from entity
    public TaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.dueDate = task.getDueDate();
        this.project = new ProjectDto(task.getProject());
        this.assignedUser = task.getAssignedUser() != null ? new UserDto(task.getAssignedUser()) : null;
        this.createdAt = task.getCreatedAt();
    }
    
    // Default constructor
    public TaskDto() {}
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public ProjectDto getProject() {
        return project;
    }
    
    public void setProject(ProjectDto project) {
        this.project = project;
    }
    
    public UserDto getAssignedUser() {
        return assignedUser;
    }
    
    public void setAssignedUser(UserDto assignedUser) {
        this.assignedUser = assignedUser;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


