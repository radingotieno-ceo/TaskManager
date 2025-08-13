package com.taskmanager.service;

import com.taskmanager.dto.ProjectDto;
import com.taskmanager.entity.Project;
import com.taskmanager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList());
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = new Project(projectDto.getName(), projectDto.getDescription());
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        project.setName(projectDto.getName());
        project.setDescription(projectDto.getDescription());
        
        Project savedProject = projectRepository.save(project);
        return new ProjectDto(savedProject);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}


