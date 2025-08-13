package com.taskmanager.service;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.ProjectPriority;
import com.taskmanager.entity.ProjectStatus;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseInitializationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== DATABASE INITIALIZATION STARTED ===");
        
        // Check if users already exist
        long userCount = userRepository.count();
        log.info("Current user count in database: {}", userCount);
        
        if (userCount == 0) {
            log.info("No users found. Creating default users...");
            createDefaultUsers();
        } else {
            log.info("Users already exist. Skipping default user creation.");
            logExistingUsers();
        }
        
        // Check if projects already exist
        long projectCount = projectRepository.count();
        log.info("Current project count in database: {}", projectCount);
        
        if (projectCount == 0) {
            log.info("No projects found. Creating sample projects...");
            createSampleProjects();
        } else {
            log.info("Projects already exist. Skipping sample project creation.");
        }
        
        // Check if tasks already exist
        long taskCount = taskRepository.count();
        log.info("Current task count in database: {}", taskCount);
        
        if (taskCount == 0) {
            log.info("No tasks found. Creating sample tasks...");
            createSampleTasks();
        } else {
            log.info("Tasks already exist. Skipping sample task creation.");
        }
        
        log.info("=== DATABASE INITIALIZATION COMPLETED ===");
        logDatabaseSummary();
    }

    private void createDefaultUsers() {
        String defaultPassword = "password123";
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        
        List<User> defaultUsers = Arrays.asList(
            new User("Test Manager", "test@karooth.com", encodedPassword, Role.MANAGER),
            new User("Test User", "user@karooth.com", encodedPassword, Role.USER),
            new User("Admin User", "admin@karooth.com", encodedPassword, Role.ADMIN)
        );

        for (User user : defaultUsers) {
            try {
                // Check if user already exists
                if (!userRepository.existsByEmail(user.getEmail())) {
                    User savedUser = userRepository.save(user);
                    log.info("Created default user: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
                } else {
                    log.info("User already exists: {}", user.getEmail());
                }
            } catch (Exception e) {
                log.error("Error creating user {}: {}", user.getEmail(), e.getMessage());
            }
        }
        
        log.info("Default user creation completed. Total users: {}", userRepository.count());
    }

    private void createSampleProjects() {
        try {
            List<Project> sampleProjects = Arrays.asList(
                createProject("Website Redesign", "Complete redesign of the company website with modern UI/UX", 
                           ProjectPriority.HIGH, LocalDate.now().plusMonths(2), ProjectStatus.IN_PROGRESS),
                createProject("Mobile App Development", "Develop a new mobile application for iOS and Android", 
                           ProjectPriority.CRITICAL, LocalDate.now().plusMonths(3), ProjectStatus.IN_PROGRESS),
                createProject("Database Migration", "Migrate legacy database to new cloud infrastructure", 
                           ProjectPriority.MEDIUM, LocalDate.now().plusMonths(1), ProjectStatus.PLANNING),
                createProject("Security Audit", "Conduct comprehensive security audit of all systems", 
                           ProjectPriority.HIGH, LocalDate.now().plusMonths(1), ProjectStatus.PLANNING),
                createProject("Marketing Campaign", "Launch new marketing campaign for Q4 product release", 
                           ProjectPriority.MEDIUM, LocalDate.now().plusMonths(2), ProjectStatus.IN_PROGRESS)
            );

            for (Project project : sampleProjects) {
                Project savedProject = projectRepository.save(project);
                log.info("Created sample project: {} (ID: {})", savedProject.getName(), savedProject.getId());
            }
            
            log.info("Sample project creation completed. Total projects: {}", projectRepository.count());
        } catch (Exception e) {
            log.error("Error creating sample projects: {}", e.getMessage());
        }
    }

    private Project createProject(String name, String description, ProjectPriority priority, LocalDate dueDate, ProjectStatus status) {
        Project project = new Project(name, description, dueDate, priority);
        project.setStatus(status);
        return project;
    }

    private void createSampleTasks() {
        try {
            // Get existing projects and users
            List<Project> projects = projectRepository.findAll();
            Optional<User> manager = userRepository.findByEmail("test@karooth.com");
            Optional<User> user = userRepository.findByEmail("user@karooth.com");
            
            if (projects.isEmpty()) {
                log.warn("No projects found. Cannot create tasks without projects.");
                return;
            }
            
            if (manager.isEmpty() || user.isEmpty()) {
                log.warn("Required users not found. Cannot create tasks without users.");
                return;
            }

            List<Task> sampleTasks = Arrays.asList(
                // Tasks for Website Redesign project
                createTask("Design Homepage", "Create modern homepage design with responsive layout", 
                        TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDate.now().plusWeeks(2), 
                        projects.get(0), user.get()),
                createTask("Implement Navigation", "Build responsive navigation menu", 
                        TaskStatus.TODO, TaskPriority.MEDIUM, LocalDate.now().plusWeeks(3), 
                        projects.get(0), null),
                
                // Tasks for Mobile App Development project
                createTask("Setup Development Environment", "Configure iOS and Android development tools", 
                        TaskStatus.DONE, TaskPriority.HIGH, LocalDate.now().plusDays(5), 
                        projects.get(1), manager.get()),
                createTask("Design App UI", "Create user interface designs for mobile app", 
                        TaskStatus.IN_PROGRESS, TaskPriority.CRITICAL, LocalDate.now().plusWeeks(4), 
                        projects.get(1), user.get()),
                
                // Tasks for Database Migration project
                createTask("Backup Current Database", "Create full backup of existing database", 
                        TaskStatus.TODO, TaskPriority.HIGH, LocalDate.now().plusDays(3), 
                        projects.get(2), manager.get()),
                createTask("Test Migration Scripts", "Validate migration scripts in test environment", 
                        TaskStatus.TODO, TaskPriority.MEDIUM, LocalDate.now().plusWeeks(2), 
                        projects.get(2), user.get()),
                
                // Tasks for Security Audit project
                createTask("Vulnerability Assessment", "Run automated security scans", 
                        TaskStatus.TODO, TaskPriority.HIGH, LocalDate.now().plusWeeks(1), 
                        projects.get(3), manager.get()),
                createTask("Review Access Controls", "Audit user permissions and access rights", 
                        TaskStatus.TODO, TaskPriority.MEDIUM, LocalDate.now().plusWeeks(2), 
                        projects.get(3), user.get()),
                
                // Tasks for Marketing Campaign project
                createTask("Create Campaign Strategy", "Develop comprehensive marketing strategy", 
                        TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDate.now().plusWeeks(1), 
                        projects.get(4), manager.get()),
                createTask("Design Marketing Materials", "Create brochures, banners, and social media content", 
                        TaskStatus.TODO, TaskPriority.MEDIUM, LocalDate.now().plusWeeks(3), 
                        projects.get(4), user.get())
            );

            for (Task task : sampleTasks) {
                Task savedTask = taskRepository.save(task);
                String assignedTo = savedTask.getAssignedUser() != null ? 
                    savedTask.getAssignedUser().getName() : "Unassigned";
                log.info("Created sample task: {} (ID: {}) - Assigned to: {}", 
                        savedTask.getTitle(), savedTask.getId(), assignedTo);
            }
            
            log.info("Sample task creation completed. Total tasks: {}", taskRepository.count());
        } catch (Exception e) {
            log.error("Error creating sample tasks: {}", e.getMessage());
        }
    }

    private Task createTask(String title, String description, TaskStatus status, TaskPriority priority, 
                           LocalDate dueDate, Project project, User assignedUser) {
        Task task = new Task(title, description, dueDate, project, priority);
        task.setStatus(status);
        task.setAssignedUser(assignedUser);
        return task;
    }

    private void logExistingUsers() {
        List<User> users = userRepository.findAll();
        log.info("Existing users in database:");
        for (User user : users) {
            log.info("- {} ({}): {}", user.getName(), user.getEmail(), user.getRole());
        }
    }

    @Transactional
    private void logDatabaseSummary() {
        long userCount = userRepository.count();
        long projectCount = projectRepository.count();
        long taskCount = taskRepository.count();
        
        log.info("=== DATABASE SUMMARY ===");
        log.info("Users: {}", userCount);
        log.info("Projects: {}", projectCount);
        log.info("Tasks: {}", taskCount);
        log.info("Total records: {}", userCount + projectCount + taskCount);
        
        // Log some sample data - using separate transactions to avoid lazy loading issues
        if (userCount > 0) {
            try {
                List<User> users = userRepository.findAll();
                log.info("Sample users:");
                users.forEach(user -> log.info("  - {} ({}) - {}", user.getName(), user.getEmail(), user.getRole()));
            } catch (Exception e) {
                log.warn("Could not log users: {}", e.getMessage());
            }
        }
        
        if (projectCount > 0) {
            try {
                List<Project> projects = projectRepository.findAll();
                log.info("Sample projects:");
                projects.forEach(project -> log.info("  - {} - {} - {}", project.getName(), project.getStatus(), project.getPriority()));
            } catch (Exception e) {
                log.warn("Could not log projects: {}", e.getMessage());
            }
        }
        
        if (taskCount > 0) {
            try {
                // Use basic task information to avoid lazy loading issues
                List<Task> tasks = taskRepository.findAll();
                log.info("Sample tasks:");
                tasks.forEach(task -> {
                    String assignedTo = "Unassigned";
                    if (task.getAssignedUser() != null) {
                        try {
                            assignedTo = task.getAssignedUser().getName();
                        } catch (Exception e) {
                            assignedTo = "User ID: " + task.getAssignedUser().getId();
                        }
                    }
                    log.info("  - {} - {} - Assigned to: {}", task.getTitle(), task.getStatus(), assignedTo);
                });
            } catch (Exception e) {
                log.warn("Could not log tasks: {}", e.getMessage());
                // Fallback to basic task logging without user info
                try {
                    List<Task> tasks = taskRepository.findAll();
                    log.info("Sample tasks (basic):");
                    tasks.forEach(task -> log.info("  - {} - {}", task.getTitle(), task.getStatus()));
                } catch (Exception e2) {
                    log.warn("Could not log basic tasks: {}", e2.getMessage());
                }
            }
        }
    }
}
