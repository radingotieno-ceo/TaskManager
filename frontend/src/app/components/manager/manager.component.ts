import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { UserService } from '../../services/user.service';
import { ProjectService } from '../../services/project.service';
import { User } from '../../models/user.model';
import { Task } from '../../models/task.model';
import { Project } from '../../models/project.model';

@Component({
  selector: 'app-manager',
  templateUrl: './manager.component.html',
  styleUrls: ['./manager.component.scss']
})
export class ManagerComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  loading = false;
  sidebarCollapsed = false;
  searchQuery = '';
  hasNotifications = true;
  activeTab = 'dashboard';

  // Dashboard data
  summaryCards: any = {
    totalProjects: 0,
    activeTasks: 0,
    completedTasks: 0,
    overdueTasks: 0
  };
  recentProjects: Project[] = [];
  recentTasks: Task[] = [];
  availableUsers: User[] = [];

  // Task assignment data
  unassignedTasks: Task[] = [];
  selectedTask: Task | null = null;
  selectedUser: User | null = null;
  showTaskAssignmentModal = false;
  showCreateTaskModal = false;

  // Create task form data
  newTask = {
    title: '',
    description: '',
    priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL',
    dueDate: '',
    projectId: null as number | null
  };

  // Create project form data
  newProjectForm = {
    name: '',
    description: '',
    priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH',
    dueDate: ''
  };

  // Available projects for task creation
  availableProjects: Project[] = [];
  
  // Modal states
  showCreateProjectModal = false;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private taskService: TaskService,
    private userService: UserService,
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: User | null) => {
        this.currentUser = user;
        
        // Redirect if user is not a manager
        if (user && user.role !== 'MANAGER') {
          this.redirectToAppropriateDashboard(user.role);
        } else if (user) {
          this.loadDashboardData();
          this.loadTaskAssignmentData();
          this.loadProjects();
        }
        
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    // Load summary statistics
    this.loadSummaryStatistics();
    
    // Load recent projects
    this.projectService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          this.recentProjects = projects.slice(0, 5); // Get first 5 projects
        },
        error: (error) => {
          console.error('Error loading projects:', error);
        }
      });

    // Load recent tasks
    this.taskService.getAllTasks()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          this.recentTasks = tasks.slice(0, 5); // Get first 5 tasks
        },
        error: (error) => {
          console.error('Error loading tasks:', error);
        }
      });
  }

  private loadSummaryStatistics(): void {
    // Load all tasks to calculate statistics
    this.taskService.getAllTasks()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          this.summaryCards.totalProjects = this.recentProjects.length;
          this.summaryCards.activeTasks = tasks.filter(t => t.status === 'IN_PROGRESS').length;
          this.summaryCards.completedTasks = tasks.filter(t => t.status === 'DONE').length;
          this.summaryCards.overdueTasks = tasks.filter(t => 
            new Date(t.dueDate) < new Date() && t.status !== 'DONE'
          ).length;
        },
        error: (error) => {
          console.error('Error loading task statistics:', error);
        }
      });
  }

  private loadTaskAssignmentData(): void {
    // Load unassigned tasks
    this.taskService.getUnassignedTasks()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          this.unassignedTasks = tasks;
        },
        error: (error) => {
          console.error('Error loading unassigned tasks:', error);
        }
      });

    // Load available users
    this.userService.getAvailableUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          this.availableUsers = users;
        },
        error: (error) => {
          console.error('Error loading available users:', error);
        }
      });
  }

  private loadProjects(): void {
    this.projectService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          this.availableProjects = projects;
        },
        error: (error) => {
          console.error('Error loading projects:', error);
        }
      });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  onSearch(): void {
    // Implement search functionality
    console.log('Searching for:', this.searchQuery);
  }

  showNotifications(): void {
    // Implement notifications functionality
    console.log('Showing notifications');
  }

  newProject(): void {
    // Show create project modal
    this.showCreateProjectModal = true;
    this.resetNewProjectForm();
  }

  assignTask(): void {
    // Show task assignment modal
    this.showTaskAssignmentModal = true;
  }

  // Task assignment functions
  openTaskAssignmentModal(task: Task): void {
    this.selectedTask = task;
    this.selectedUser = null;
    this.showTaskAssignmentModal = true;
  }

  closeTaskAssignmentModal(): void {
    this.showTaskAssignmentModal = false;
    this.selectedTask = null;
    this.selectedUser = null;
  }

  selectUserForAssignment(user: User): void {
    this.selectedUser = user;
  }

  assignTaskToUser(): void {
    if (this.selectedTask && this.selectedUser) {
      this.taskService.assignTaskToUser(this.selectedTask.id, this.selectedUser.id)
        .subscribe({
          next: (response) => {
            console.log('Task assigned successfully');
            this.closeTaskAssignmentModal();
            this.loadTaskAssignmentData(); // Reload data
            this.loadDashboardData(); // Reload dashboard data
          },
          error: (error) => {
            console.error('Error assigning task:', error);
          }
        });
    }
  }

  // Create project functions
  showCreateProjectForm(): void {
    this.showCreateProjectModal = true;
    this.resetNewProjectForm();
  }

  closeCreateProjectModal(): void {
    this.showCreateProjectModal = false;
    this.resetNewProjectForm();
  }

  resetNewProjectForm(): void {
    this.newProjectForm = {
      name: '',
      description: '',
      priority: 'MEDIUM',
      dueDate: ''
    };
  }

  createProject(): void {
    if (this.newProjectForm.name && this.newProjectForm.dueDate) {
      const createProjectRequest = {
        name: this.newProjectForm.name,
        description: this.newProjectForm.description,
        priority: this.newProjectForm.priority,
        dueDate: this.newProjectForm.dueDate
      };

      this.projectService.createProject(createProjectRequest)
        .subscribe({
          next: (project) => {
            console.log('Project created successfully:', project);
            alert('Project created successfully!');
            this.closeCreateProjectModal();
            this.loadDashboardData(); // Reload data
            this.loadProjects(); // Reload projects list
          },
          error: (error) => {
            console.error('Error creating project:', error);
            alert('Error creating project: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    }
  }

  // Create task functions
  showCreateTaskForm(): void {
    this.showCreateTaskModal = true;
    this.resetNewTaskForm();
  }

  closeCreateTaskModal(): void {
    this.showCreateTaskModal = false;
    this.resetNewTaskForm();
  }

  resetNewTaskForm(): void {
    this.newTask = {
      title: '',
      description: '',
      priority: 'MEDIUM',
      dueDate: '',
      projectId: null
    };
  }

  createTask(): void {
    if (this.newTask.title && this.newTask.dueDate && this.newTask.projectId && this.newTask.projectId > 0) {
      const createTaskRequest = {
        title: this.newTask.title,
        description: this.newTask.description,
        dueDate: this.newTask.dueDate,
        projectId: this.newTask.projectId
      };

      this.taskService.createTask(createTaskRequest)
        .subscribe({
          next: (task) => {
            console.log('Task created successfully:', task);
            alert('Task created successfully!');
            this.closeCreateTaskModal();
            this.loadTaskAssignmentData(); // Reload data
            this.loadDashboardData(); // Reload dashboard data
          },
          error: (error) => {
            console.error('Error creating task:', error);
            alert('Error creating task: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    }
  }

  unassignTask(taskId: number): void {
    if (confirm('Are you sure you want to unassign this task?')) {
      this.taskService.unassignTask(taskId)
        .subscribe({
          next: (response) => {
            console.log('Task unassigned successfully');
            this.loadTaskAssignmentData(); // Reload data
            this.loadDashboardData(); // Reload dashboard data
          },
          error: (error) => {
            console.error('Error unassigning task:', error);
          }
        });
    }
  }

  bulkAssignTasks(taskIds: number[], userId: number): void {
    this.taskService.bulkAssignTasks(taskIds, userId)
      .subscribe({
        next: (response) => {
          console.log('Tasks assigned successfully');
          this.loadTaskAssignmentData(); // Reload data
          this.loadDashboardData(); // Reload dashboard data
        },
        error: (error) => {
          console.error('Error bulk assigning tasks:', error);
        }
      });
  }

  viewUserTasks(userId: number): void {
    this.taskService.getTasksAssignedToUser(userId)
      .subscribe({
        next: (tasks) => {
          console.log('Tasks assigned to user:', tasks);
          // You can implement a modal or navigate to a detailed view
        },
        error: (error) => {
          console.error('Error loading user tasks:', error);
        }
      });
  }

  viewProjectTasks(projectId: number): void {
    this.taskService.getTasksByProject(projectId)
      .subscribe({
        next: (tasks) => {
          console.log('Tasks in project:', tasks);
          // You can implement a modal or navigate to a detailed view
        },
        error: (error) => {
          console.error('Error loading project tasks:', error);
        }
      });
  }

  // Helper method for template
  getProjectTeamSize(project: Project): number {
    // For now, return a default team size since we don't have team size in the model
    return 3; // Default team size
  }

  private redirectToAppropriateDashboard(userRole: string): void {
    switch (userRole) {
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'USER':
        this.router.navigate(['/user']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }
}
