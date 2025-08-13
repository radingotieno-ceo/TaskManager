package com.taskmanager.repository;

import com.taskmanager.entity.User;
import com.taskmanager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if user exists by email
    boolean existsByEmail(String email);
    
    // Find users by role
    List<User> findByRole(Role role);
    
    // Find users by name (for search functionality)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Count users by role
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    // Count total users
    @Query("SELECT COUNT(u) FROM User u")
    long countTotalUsers();
    
    // Get users with their task counts
    @Query("SELECT u, COUNT(t) as taskCount FROM User u LEFT JOIN u.assignedTasks t GROUP BY u")
    List<Object[]> findUsersWithTaskCounts();
    
    // Get users with their completed task counts
    @Query("SELECT u, COUNT(t) as completedCount FROM User u LEFT JOIN u.assignedTasks t WHERE t.status = 'DONE' GROUP BY u")
    List<Object[]> findUsersWithCompletedTaskCounts();
    
    // Get active users (users with tasks assigned in last 30 days)
    @Query("SELECT DISTINCT u FROM User u JOIN u.assignedTasks t WHERE t.createdAt >= :thirtyDaysAgo")
    List<User> findActiveUsers(@Param("thirtyDaysAgo") java.time.LocalDateTime thirtyDaysAgo);
    
    // Get users with overdue tasks
    @Query("SELECT DISTINCT u FROM User u JOIN u.assignedTasks t WHERE t.dueDate < :today AND t.status != 'DONE'")
    List<User> findUsersWithOverdueTasks(@Param("today") java.time.LocalDate today);
    
    // Get top performers (users with most completed tasks)
    @Query("SELECT u, COUNT(t) as completedCount FROM User u LEFT JOIN u.assignedTasks t WHERE t.status = 'DONE' GROUP BY u ORDER BY completedCount DESC")
    List<Object[]> findTopPerformers();
    
    // Update user role
    @Modifying
    @Query("UPDATE User u SET u.role = :newRole WHERE u.id = :userId")
    void updateUserRole(@Param("userId") Long userId, @Param("newRole") Role newRole);
    
    // Find users available for task assignment (excluding admins)
    @Query("SELECT u FROM User u WHERE u.role != 'ADMIN' ORDER BY u.name")
    List<User> findUsersAvailableForTaskAssignment();
    
    // Find managers available for managerial duties
    @Query("SELECT u FROM User u WHERE u.role = 'MANAGER' ORDER BY u.name")
    List<User> findManagers();
    
    // Find users by role excluding current user
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.id != :excludeUserId ORDER BY u.name")
    List<User> findByRoleExcludingUser(@Param("role") Role role, @Param("excludeUserId") Long excludeUserId);
    
    // Get user statistics
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserStatisticsByRole();
}
