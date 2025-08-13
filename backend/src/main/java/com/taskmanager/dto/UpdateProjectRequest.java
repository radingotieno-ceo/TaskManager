package com.taskmanager.dto;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;

public class UpdateProjectRequest {
    
    private String name;
    private String description;
    private String priority;
    private String status;
    
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
    
    // Constructors
    public UpdateProjectRequest() {}
    
    public UpdateProjectRequest(String name, String description, String priority, String status, LocalDate dueDate) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.dueDate = dueDate;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
