package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Find projects by name (for search functionality)
    List<Project> findByNameContainingIgnoreCase(String name);
    
    // Find projects by description (for search functionality)
    List<Project> findByDescriptionContainingIgnoreCase(String description);
    
    // Count total projects
    @Query("SELECT COUNT(p) FROM Project p")
    long countTotalProjects();
    
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
    List<Project> findProjectsWithOverdueTasks(@Param("today") java.time.LocalDate today);
}


