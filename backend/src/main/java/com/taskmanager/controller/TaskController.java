package com.taskmanager.controller;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.dto.CreateTaskDto;
import com.taskmanager.dto.CreateAndAssignTaskDto;
import com.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // Basic CRUD operations
    @GetMapping
    public ResponseEntity<List<TaskDto>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody CreateTaskDto createTaskDto) {
        return ResponseEntity.ok(taskService.createTask(createTaskDto));
    }

    @PostMapping("/create-and-assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDto> createAndAssignTask(@Valid @RequestBody CreateAndAssignTaskDto createAndAssignTaskDto) {
        return ResponseEntity.ok(taskService.createAndAssignTask(createAndAssignTaskDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody CreateTaskDto updateTaskDto) {
        return ResponseEntity.ok(taskService.updateTask(id, updateTaskDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    // Task status management
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }

    // Task assignment
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDto> assignTask(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        return ResponseEntity.ok(taskService.assignTask(id, userId));
    }

    @PatchMapping("/{id}/unassign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDto> unassignTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.unassignTask(id));
    }

    // Filtered task queries
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(@PathVariable String status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskDto>> getTasksByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(taskService.getTasksByPriority(priority));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDto>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskDto>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/assigned")
    public ResponseEntity<List<TaskDto>> getAssignedTasks() {
        return ResponseEntity.ok(taskService.getAssignedTasks());
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TaskDto>> getUnassignedTasks() {
        return ResponseEntity.ok(taskService.getUnassignedTasks());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDto>> getOverdueTasks() {
        return ResponseEntity.ok(taskService.getOverdueTasks());
    }

    @GetMapping("/due-today")
    public ResponseEntity<List<TaskDto>> getTasksDueToday() {
        return ResponseEntity.ok(taskService.getTasksDueToday());
    }

    @GetMapping("/due-this-week")
    public ResponseEntity<List<TaskDto>> getTasksDueThisWeek() {
        return ResponseEntity.ok(taskService.getTasksDueThisWeek());
    }

    // Search and filter
    @GetMapping("/search")
    public ResponseEntity<List<TaskDto>> searchTasks(@RequestParam String q) {
        return ResponseEntity.ok(taskService.searchTasks(q));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TaskDto>> getTasksByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        return ResponseEntity.ok(taskService.getTasksByDateRange(start, end));
    }

    // Task statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getTaskStatistics() {
        return ResponseEntity.ok(taskService.getTaskStatistics());
    }

    @GetMapping("/statistics/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserTaskStatistics(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getUserTaskStatistics(userId));
    }

    @GetMapping("/statistics/project/{projectId}")
    public ResponseEntity<Map<String, Object>> getProjectTaskStatistics(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getProjectTaskStatistics(projectId));
    }

    // Bulk operations
    @PatchMapping("/bulk-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> bulkUpdateTaskStatus(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        String status = (String) request.get("status");
        taskService.bulkUpdateTaskStatus(taskIds, status);
        return ResponseEntity.ok(Map.of("message", "Tasks status updated successfully"));
    }

    @PatchMapping("/bulk-assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> bulkAssignTasks(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        Long userId = Long.valueOf(request.get("userId").toString());
        taskService.bulkAssignTasks(taskIds, userId);
        return ResponseEntity.ok(Map.of("message", "Tasks assigned successfully"));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> bulkDeleteTasks(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        taskService.bulkDeleteTasks(taskIds);
        return ResponseEntity.ok(Map.of("message", "Tasks deleted successfully"));
    }

    // Task dependencies
    @GetMapping("/{taskId}/dependencies")
    public ResponseEntity<List<TaskDto>> getTaskDependencies(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskDependencies(taskId));
    }

    @PostMapping("/{taskId}/dependencies/{dependencyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> addTaskDependency(
            @PathVariable Long taskId,
            @PathVariable Long dependencyId) {
        taskService.addTaskDependency(taskId, dependencyId);
        return ResponseEntity.ok(Map.of("message", "Task dependency added successfully"));
    }

    @DeleteMapping("/{taskId}/dependencies/{dependencyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> removeTaskDependency(
            @PathVariable Long taskId,
            @PathVariable Long dependencyId) {
        taskService.removeTaskDependency(taskId, dependencyId);
        return ResponseEntity.ok(Map.of("message", "Task dependency removed successfully"));
    }

    // Task comments
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getTaskComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskComments(taskId));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<Map<String, Object>> addTaskComment(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> request) {
        String comment = request.get("comment");
        return ResponseEntity.ok(taskService.addTaskComment(taskId, comment));
    }

    // Task time tracking
    @PostMapping("/{taskId}/timer/start")
    public ResponseEntity<Map<String, String>> startTaskTimer(@PathVariable Long taskId) {
        taskService.startTaskTimer(taskId);
        return ResponseEntity.ok(Map.of("message", "Task timer started"));
    }

    @PostMapping("/{taskId}/timer/stop")
    public ResponseEntity<Map<String, String>> stopTaskTimer(@PathVariable Long taskId) {
        taskService.stopTaskTimer(taskId);
        return ResponseEntity.ok(Map.of("message", "Task timer stopped"));
    }

    @GetMapping("/{taskId}/timer/log")
    public ResponseEntity<List<Map<String, Object>>> getTaskTimeLog(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskTimeLog(taskId));
    }
}


