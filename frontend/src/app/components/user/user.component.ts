import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { ProjectService } from '../../services/project.service';
import { User } from '../../models/user.model';
import { Task } from '../../models/task.model';
import { Project } from '../../models/project.model';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  loading = false;
  sidebarCollapsed = false;
  searchQuery = '';
  hasNotifications = true;
  activeTab = 'dashboard';

  // Dashboard data
  myTasks: Task[] = [];
  myProjects: Project[] = [];
  taskStats: any = {
    total: 0,
    completed: 0,
    inProgress: 0,
    todo: 0
  };

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private taskService: TaskService,
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: User | null) => {
        this.currentUser = user;
        
        // Redirect if user is not a regular user
        if (user && user.role !== 'USER') {
          this.redirectToAppropriateDashboard(user.role);
        } else if (user) {
          this.loadDashboardData();
        }
        
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    if (this.currentUser?.id) {
      // Load assigned tasks
      this.taskService.getAssignedTasks()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (tasks) => {
            this.myTasks = tasks;
            this.calculateTaskStats();
          },
          error: (error) => {
            console.error('Error loading assigned tasks:', error);
          }
        });

      // Load projects (for now, we'll load all projects since we don't have user-project relationship)
      this.projectService.getAllProjects()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (projects) => {
            this.myProjects = projects.slice(0, 3); // Show first 3 projects
          },
          error: (error) => {
            console.error('Error loading projects:', error);
          }
        });
    }
  }

  private calculateTaskStats(): void {
    this.taskStats.total = this.myTasks.length;
    this.taskStats.completed = this.myTasks.filter(task => task.status === 'DONE').length;
    this.taskStats.inProgress = this.myTasks.filter(task => task.status === 'IN_PROGRESS').length;
    this.taskStats.todo = this.myTasks.filter(task => task.status === 'TODO').length;
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

  showMoreOptions(): void {
    // Implement more options menu
    console.log('Showing more options');
  }

  viewProjectDetails(): void {
    // Navigate to project details page
    console.log('Viewing project details');
  }

  // Task management functions
  updateTaskStatus(taskId: number, newStatus: string): void {
    this.taskService.updateTaskStatus(taskId, newStatus)
      .subscribe({
        next: (task) => {
          console.log('Task status updated successfully:', task);
          this.loadDashboardData(); // Reload data to reflect changes
        },
        error: (error) => {
          console.error('Error updating task status:', error);
        }
      });
  }

  viewTaskDetails(taskId: number): void {
    // Navigate to task details page
    console.log('Viewing task details:', taskId);
  }

  markTaskAsComplete(taskId: number): void {
    this.updateTaskStatus(taskId, 'DONE');
  }

  startTask(taskId: number): void {
    this.updateTaskStatus(taskId, 'IN_PROGRESS');
  }

  // Helper methods for template
  getTaskProgress(task: Task): number {
    // Calculate progress based on status
    switch (task.status) {
      case 'DONE':
        return 100;
      case 'IN_PROGRESS':
        return 50;
      case 'TODO':
        return 0;
      default:
        return 0;
    }
  }

  getProjectRole(project: Project): string {
    // For now, return a default role since we don't have user-project relationship
    return 'Member';
  }

  getProjectTaskCount(project: Project): number {
    // For now, return a default count since we don't have user-project relationship
    return 0;
  }

  private redirectToAppropriateDashboard(userRole: string): void {
    switch (userRole) {
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'MANAGER':
        this.router.navigate(['/manager']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }
}
