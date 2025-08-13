package com.taskmanager.dto;

public class ProjectStatisticsDto {
    
    private Long totalProjects;
    private Long activeProjects;
    private Long completedProjects;
    private Long overdueProjects;
    private Double averageProgress;
    private Long totalTasks;
    private Long completedTasks;
    private Long overdueTasks;
    
    // Constructors
    public ProjectStatisticsDto() {}
    
    public ProjectStatisticsDto(Long totalProjects, Long activeProjects, Long completedProjects, 
                               Long overdueProjects, Double averageProgress, Long totalTasks, 
                               Long completedTasks, Long overdueTasks) {
        this.totalProjects = totalProjects;
        this.activeProjects = activeProjects;
        this.completedProjects = completedProjects;
        this.overdueProjects = overdueProjects;
        this.averageProgress = averageProgress;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.overdueTasks = overdueTasks;
    }
    
    // Getters and setters
    public Long getTotalProjects() {
        return totalProjects;
    }
    
    public void setTotalProjects(Long totalProjects) {
        this.totalProjects = totalProjects;
    }
    
    public Long getActiveProjects() {
        return activeProjects;
    }
    
    public void setActiveProjects(Long activeProjects) {
        this.activeProjects = activeProjects;
    }
    
    public Long getCompletedProjects() {
        return completedProjects;
    }
    
    public void setCompletedProjects(Long completedProjects) {
        this.completedProjects = completedProjects;
    }
    
    public Long getOverdueProjects() {
        return overdueProjects;
    }
    
    public void setOverdueProjects(Long overdueProjects) {
        this.overdueProjects = overdueProjects;
    }
    
    public Double getAverageProgress() {
        return averageProgress;
    }
    
    public void setAverageProgress(Double averageProgress) {
        this.averageProgress = averageProgress;
    }
    
    public Long getTotalTasks() {
        return totalTasks;
    }
    
    public void setTotalTasks(Long totalTasks) {
        this.totalTasks = totalTasks;
    }
    
    public Long getCompletedTasks() {
        return completedTasks;
    }
    
    public void setCompletedTasks(Long completedTasks) {
        this.completedTasks = completedTasks;
    }
    
    public Long getOverdueTasks() {
        return overdueTasks;
    }
    
    public void setOverdueTasks(Long overdueTasks) {
        this.overdueTasks = overdueTasks;
    }
}
