package com.taskmanager.controller;

import com.taskmanager.dto.ProjectDto;
import com.taskmanager.dto.CreateProjectRequest;
import com.taskmanager.dto.UpdateProjectRequest;
import com.taskmanager.dto.ProjectStatisticsDto;
import com.taskmanager.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // Basic CRUD operations
    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    // Project statistics
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectStatisticsDto> getProjectStatistics() {
        return ResponseEntity.ok(projectService.getProjectStatistics());
    }

    // Filtered project queries
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectDto>> getProjectsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(projectService.getProjectsByStatus(status));
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<ProjectDto>> getProjectsByPriority(@PathVariable String priority) {
        return ResponseEntity.ok(projectService.getProjectsByPriority(priority));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ProjectDto>> getOverdueProjects() {
        return ResponseEntity.ok(projectService.getOverdueProjects());
    }

    // User assignment to projects
    @PostMapping("/{projectId}/assign-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> assignUserToProject(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectService.assignUserToProject(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "User assigned to project successfully"));
    }

    @DeleteMapping("/{projectId}/remove-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.ok(Map.of("message", "User removed from project successfully"));
    }

    @GetMapping("/{projectId}/users")
    public ResponseEntity<List<Map<String, Object>>> getProjectUsers(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectUsers(projectId));
    }

    // Project progress and status management
    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> updateProjectStatus(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return ResponseEntity.ok(projectService.updateProjectStatus(projectId, status));
    }

    @PatchMapping("/{projectId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ProjectDto> updateProjectProgress(
            @PathVariable Long projectId,
            @RequestBody Map<String, Integer> request) {
        Integer progress = request.get("progress");
        return ResponseEntity.ok(projectService.updateProjectProgress(projectId, progress));
    }

    // Search and filter
    @GetMapping("/search")
    public ResponseEntity<List<ProjectDto>> searchProjects(@RequestParam String q) {
        return ResponseEntity.ok(projectService.searchProjects(q));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ProjectDto>> getProjectsByDateRange(
            @RequestParam String start,
            @RequestParam String end) {
        return ResponseEntity.ok(projectService.getProjectsByDateRange(start, end));
    }

    // Bulk operations
    @PatchMapping("/bulk-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> bulkUpdateProjectStatus(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> projectIds = (List<Long>) request.get("projectIds");
        String status = (String) request.get("status");
        projectService.bulkUpdateProjectStatus(projectIds, status);
        return ResponseEntity.ok(Map.of("message", "Projects status updated successfully"));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> bulkDeleteProjects(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> projectIds = (List<Long>) request.get("projectIds");
        projectService.bulkDeleteProjects(projectIds);
        return ResponseEntity.ok(Map.of("message", "Projects deleted successfully"));
    }

    // Project tasks
    @GetMapping("/{projectId}/tasks")
    public ResponseEntity<List<Map<String, Object>>> getProjectTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectTasks(projectId));
    }

    // Project analytics
    @GetMapping("/{projectId}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getProjectAnalytics(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectAnalytics(projectId));
    }

    // Project timeline
    @GetMapping("/{projectId}/timeline")
    public ResponseEntity<List<Map<String, Object>>> getProjectTimeline(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectTimeline(projectId));
    }
}


