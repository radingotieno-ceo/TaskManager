package com.taskmanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateAndAssignTaskDto {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    @Future(message = "Due date must be in the future")
    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
    
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String priority = "MEDIUM"; // Default priority
    
    // Constructors
    public CreateAndAssignTaskDto() {}
    
    public CreateAndAssignTaskDto(String title, String description, LocalDate dueDate, Long projectId, Long userId, String priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.userId = userId;
        this.priority = priority;
    }
    
    // Getters and setters
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
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
}
