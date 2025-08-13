package com.taskmanager.controller;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.service.TaskAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks/assignment")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class TaskAssignmentController {

    @Autowired
    private TaskAssignmentService taskAssignmentService;

    @GetMapping("/unassigned")
    public ResponseEntity<List<Task>> getUnassignedTasks() {
        List<Task> unassignedTasks = taskAssignmentService.getUnassignedTasks();
        return ResponseEntity.ok(unassignedTasks);
    }

    @GetMapping("/available-users")
    public ResponseEntity<List<User>> getUsersAvailableForAssignment() {
        List<User> availableUsers = taskAssignmentService.getUsersAvailableForAssignment();
        return ResponseEntity.ok(availableUsers);
    }

    @PostMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<Map<String, String>> assignTaskToUser(
            @PathVariable Long taskId,
            @PathVariable Long userId) {
        
        taskAssignmentService.assignTaskToUser(taskId, userId);
        return ResponseEntity.ok(Map.of("message", "Task assigned successfully"));
    }

    @PostMapping("/{taskId}/unassign")
    public ResponseEntity<Map<String, String>> unassignTask(@PathVariable Long taskId) {
        taskAssignmentService.unassignTask(taskId);
        return ResponseEntity.ok(Map.of("message", "Task unassigned successfully"));
    }

    @GetMapping("/user/{userId}/tasks")
    public ResponseEntity<List<Task>> getTasksAssignedToUser(@PathVariable Long userId) {
        List<Task> tasks = taskAssignmentService.getTasksAssignedToUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}/tasks")
    public ResponseEntity<List<Task>> getTasksByProject(@PathVariable Long projectId) {
        List<Task> tasks = taskAssignmentService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<Map<String, String>> bulkAssignTasks(
            @RequestBody Map<String, Object> request) {
        
        @SuppressWarnings("unchecked")
        List<Long> taskIds = (List<Long>) request.get("taskIds");
        Long userId = Long.valueOf(request.get("userId").toString());
        
        taskAssignmentService.bulkAssignTasks(taskIds, userId);
        return ResponseEntity.ok(Map.of("message", "Tasks assigned successfully"));
    }
}
