package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import com.taskmanager.entity.ProjectStatus;
import com.taskmanager.entity.ProjectPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Find projects by name (for search functionality)
    List<Project> findByNameContainingIgnoreCase(String name);
    
    // Find projects by description (for search functionality)
    List<Project> findByDescriptionContainingIgnoreCase(String description);
    
    // Find projects by name or description (for search functionality)
    List<Project> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    // Find projects by status
    List<Project> findByStatus(ProjectStatus status);
    
    // Find projects by priority
    List<Project> findByPriority(ProjectPriority priority);
    
    // Find projects by due date
    List<Project> findByDueDate(LocalDate dueDate);
    
    // Find projects by date range
    List<Project> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find overdue projects
    List<Project> findByDueDateBeforeAndStatusNot(LocalDate dueDate, ProjectStatus status);
    
    // Count total projects
    @Query("SELECT COUNT(p) FROM Project p")
    long countTotalProjects();
    
    // Count projects by status
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    long countByStatus(@Param("status") ProjectStatus status);
    
    // Count projects by priority
    @Query("SELECT COUNT(p) FROM Project p WHERE p.priority = :priority")
    long countByPriority(@Param("priority") ProjectPriority priority);
    
    // Count overdue projects
    long countByDueDateBeforeAndStatusNot(LocalDate dueDate, ProjectStatus status);
    
    // Get projects with their task counts
    @Query("SELECT p, COUNT(t) as taskCount FROM Project p LEFT JOIN p.tasks t GROUP BY p")
    List<Object[]> findProjectsWithTaskCounts();
    
    // Get projects with their completed task counts
    @Query("SELECT p, COUNT(t) as completedCount FROM Project p LEFT JOIN p.tasks t WHERE t.status = 'DONE' GROUP BY p")
    List<Object[]> findProjectsWithCompletedTaskCounts();
    
    // Get recent projects (created in last 30 days)
    @Query("SELECT p FROM Project p WHERE p.createdAt >= :thirtyDaysAgo ORDER BY p.createdAt DESC")
    List<Project> findRecentProjects(@Param("thirtyDaysAgo") java.time.LocalDateTime thirtyDaysAgo);
    
    // Get projects with overdue tasks
    @Query("SELECT DISTINCT p FROM Project p JOIN p.tasks t WHERE t.dueDate < :today AND t.status != 'DONE'")
    List<Project> findProjectsWithOverdueTasks(@Param("today") LocalDate today);
    
    // Get projects due soon (within 7 days)
    @Query("SELECT p FROM Project p WHERE p.dueDate BETWEEN :today AND :sevenDaysLater ORDER BY p.dueDate")
    List<Project> findProjectsDueSoon(@Param("today") LocalDate today, @Param("sevenDaysLater") LocalDate sevenDaysLater);
    
    // Get overdue projects
    @Query("SELECT p FROM Project p WHERE p.dueDate < :today AND p.status != 'COMPLETED' ORDER BY p.dueDate")
    List<Project> findOverdueProjects(@Param("today") LocalDate today);
    
    // Update project status
    @Modifying
    @Query("UPDATE Project p SET p.status = :status WHERE p.id = :projectId")
    void updateProjectStatus(@Param("projectId") Long projectId, @Param("status") ProjectStatus status);
    
    // Update project priority
    @Modifying
    @Query("UPDATE Project p SET p.priority = :priority WHERE p.id = :projectId")
    void updateProjectPriority(@Param("projectId") Long projectId, @Param("priority") ProjectPriority priority);
    
    // Get project statistics
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> getProjectStatisticsByStatus();
    
    @Query("SELECT p.priority, COUNT(p) FROM Project p GROUP BY p.priority")
    List<Object[]> getProjectStatisticsByPriority();
}


