package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.TaskPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks assigned to a specific user
    List<Task> findByAssignedUserId(Long userId);
    
    // Find tasks by assigned user (entity)
    List<Task> findByAssignedUser(com.taskmanager.entity.User user);
    
    // Find tasks by project
    List<Task> findByProjectId(Long projectId);
    
    // Find tasks by project (entity)
    List<Task> findByProject(com.taskmanager.entity.Project project);
    
    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);
    
    // Find tasks by priority
    List<Task> findByPriority(TaskPriority priority);
    
    // Find tasks assigned to a user with specific status
    List<Task> findByAssignedUserIdAndStatus(Long userId, TaskStatus status);
    
    // Find tasks assigned to a user with specific priority
    List<Task> findByAssignedUserIdAndPriority(Long userId, TaskPriority priority);
    
    // Find unassigned tasks
    @Query("SELECT t FROM Task t WHERE t.assignedUser IS NULL")
    List<Task> findUnassignedTasks();
    
    // Find assigned tasks (not null assigned user)
    List<Task> findByAssignedUserIsNotNull();
    
    // Find unassigned tasks (null assigned user)
    List<Task> findByAssignedUserIsNull();
    
    // Find tasks due today
    @Query("SELECT t FROM Task t WHERE DATE(t.dueDate) = :today")
    List<Task> findTasksDueToday(@Param("today") LocalDate today);
    
    // Find tasks by due date
    List<Task> findByDueDate(LocalDate dueDate);
    
    // Find tasks due this week
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startOfWeek AND :endOfWeek")
    List<Task> findTasksDueThisWeek(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);
    
    // Find tasks by date range
    List<Task> findByDueDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find overdue tasks
    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE'")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);
    
    // Find overdue tasks by date and status
    List<Task> findByDueDateBeforeAndStatusNot(LocalDate dueDate, TaskStatus status);
    
    // Search tasks by title or description
    List<Task> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    
    // Count tasks by status
    long countByStatus(TaskStatus status);
    
    // Count overdue tasks
    long countByDueDateBeforeAndStatusNot(LocalDate dueDate, TaskStatus status);
    
    // Count tasks by status for a user
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.status = :status")
    long countByAssignedUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);
    
    // Count tasks due today for a user
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND DATE(t.dueDate) = :today")
    long countTasksDueTodayForUser(@Param("userId") Long userId, @Param("today") LocalDate today);
    
    // Count tasks due this week for a user
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.dueDate BETWEEN :startOfWeek AND :endOfWeek")
    long countTasksDueThisWeekForUser(@Param("userId") Long userId, @Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);
    
    // Get tasks with high priority (due within 3 days)
    @Query("SELECT t FROM Task t WHERE t.assignedUser.id = :userId AND t.dueDate BETWEEN :today AND :threeDaysLater")
    List<Task> findHighPriorityTasks(@Param("userId") Long userId, @Param("today") LocalDate today, @Param("threeDaysLater") LocalDate threeDaysLater);
    
    // Get project progress (percentage of completed tasks)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = 'DONE'")
    long countCompletedTasksByProject(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    long countTotalTasksByProject(@Param("projectId") Long projectId);
    
    // Assign task to user
    @Modifying
    @Query("UPDATE Task t SET t.assignedUser.id = :userId WHERE t.id = :taskId")
    void assignTaskToUser(@Param("taskId") Long taskId, @Param("userId") Long userId);
    
    // Find tasks by project and status
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
    
    // Find tasks by project and priority
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.priority = :priority")
    List<Task> findByProjectIdAndPriority(@Param("projectId") Long projectId, @Param("priority") TaskPriority priority);
    
    // Count tasks by priority for a user
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :userId AND t.priority = :priority")
    long countByAssignedUserIdAndPriority(@Param("userId") Long userId, @Param("priority") TaskPriority priority);
}


