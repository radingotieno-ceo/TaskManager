import { Component, OnInit, OnDestroy } from '@angular/core';
import { TaskService } from '../../services/task.service';
import { ProjectService } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';
import { Task, TaskStatus } from '../../models/task.model';
import { Project } from '../../models/project.model';
import { User } from '../../models/user.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.scss']
})
export class TasksComponent implements OnInit, OnDestroy {
  tasks: Task[] = [];
  projects: Project[] = [];
  currentUser: User | null = null;
  loading = true;
  
  // Filtering and sorting
  filterStatus: string = 'all';
  filterPriority: string = 'all';
  filterProject: string = 'all';
  filterAssignee: string = 'all';
  searchQuery: string = '';
  sortBy: string = 'dueDate';
  sortOrder: 'asc' | 'desc' = 'asc';
  
  // Bulk operations
  selectedTasks: Set<number> = new Set();
  selectAll: boolean = false;
  
  // Statistics
  taskStats: any = {};
  
  // Pagination
  currentPage: number = 1;
  pageSize: number = 10;
  totalTasks: number = 0;
  
  // Available filters
  statusOptions = [
    { value: 'all', label: 'All Statuses' },
    { value: 'TODO', label: 'To Do' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'DONE', label: 'Done' }
  ];
  
  priorityOptions = [
    { value: 'all', label: 'All Priorities' },
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private taskService: TaskService,
    private projectService: ProjectService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadProjects();
    this.loadTasks();
    this.loadTaskStatistics();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadCurrentUser(): void {
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUser = user;
      });
  }

  private loadProjects(): void {
    this.projectService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          this.projects = projects;
        },
        error: (error) => {
          console.error('Error loading projects:', error);
        }
      });
  }

  loadTasks(): void {
    this.loading = true;
    
    // Determine which tasks to load based on user role
    let tasksObservable;
    if (this.currentUser?.role === 'USER') {
      tasksObservable = this.taskService.getAssignedTasks();
    } else {
      tasksObservable = this.taskService.getAllTasks();
    }

    tasksObservable
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks: Task[]) => {
          this.tasks = tasks;
          this.totalTasks = tasks.length;
          this.loading = false;
        },
        error: (error: any) => {
          console.error('Error loading tasks:', error);
          this.snackBar.open('Failed to load tasks', 'Close', {
            duration: 3000
          });
          this.loading = false;
        }
      });
  }

  private loadTaskStatistics(): void {
    // Calculate statistics from loaded tasks
    this.taskStats = {
      total: this.tasks.length,
      completed: this.tasks.filter(t => t.status === 'DONE').length,
      inProgress: this.tasks.filter(t => t.status === 'IN_PROGRESS').length,
      todo: this.tasks.filter(t => t.status === 'TODO').length,
      overdue: this.tasks.filter(t => new Date(t.dueDate) < new Date() && t.status !== 'DONE').length
    };
  }

  get filteredTasks(): Task[] {
    let filtered = this.tasks;

    // Apply status filter
    if (this.filterStatus !== 'all') {
      filtered = filtered.filter(task => task.status === this.filterStatus);
    }

    // Apply priority filter
    if (this.filterPriority !== 'all') {
      filtered = filtered.filter(task => task.priority === this.filterPriority);
    }

    // Apply project filter
    if (this.filterProject !== 'all') {
      filtered = filtered.filter(task => task.projectId === parseInt(this.filterProject));
    }

    // Apply assignee filter
    if (this.filterAssignee !== 'all') {
      filtered = filtered.filter(task => task.assignedUser?.id === parseInt(this.filterAssignee));
    }

    // Apply search filter
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(task => 
        task.title.toLowerCase().includes(query) ||
        task.description?.toLowerCase().includes(query) ||
        task.project?.name.toLowerCase().includes(query)
      );
    }

    // Apply sorting
    filtered.sort((a, b) => {
      let aValue: any, bValue: any;
      
      switch (this.sortBy) {
        case 'dueDate':
          aValue = new Date(a.dueDate);
          bValue = new Date(b.dueDate);
          break;
        case 'priority':
          aValue = this.getPriorityWeight(a.priority);
          bValue = this.getPriorityWeight(b.priority);
          break;
        case 'status':
          aValue = a.status;
          bValue = b.status;
          break;
        case 'title':
          aValue = a.title.toLowerCase();
          bValue = b.title.toLowerCase();
          break;
        default:
          aValue = a[this.sortBy as keyof Task];
          bValue = b[this.sortBy as keyof Task];
      }

      if (this.sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    return filtered;
  }

  private getPriorityWeight(priority: string): number {
    switch (priority) {
      case 'HIGH': return 3;
      case 'MEDIUM': return 2;
      case 'LOW': return 1;
      default: return 0;
    }
  }

  // Task CRUD operations
  createTask(): void {
    this.router.navigate(['/tasks/create']);
  }

  viewTask(task: Task): void {
    this.router.navigate(['/tasks', task.id]);
  }

  editTask(task: Task): void {
    this.router.navigate(['/tasks', task.id, 'edit']);
  }

  updateTaskStatus(task: Task, newStatus: TaskStatus): void {
    this.taskService.updateTaskStatus(task.id, newStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedTask: Task) => {
          this.snackBar.open(`Task status updated to ${this.getStatusLabel(newStatus)}`, 'Close', {
            duration: 2000
          });
          this.loadTasks();
        },
        error: (error: any) => {
          console.error('Error updating task status:', error);
          this.snackBar.open('Failed to update task status', 'Close', {
            duration: 3000
          });
        }
      });
  }

  deleteTask(task: Task): void {
    if (confirm(`Are you sure you want to delete task "${task.title}"?`)) {
      this.taskService.deleteTask(task.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.snackBar.open('Task deleted successfully', 'Close', {
              duration: 2000
            });
            this.loadTasks();
          },
          error: (error: any) => {
            console.error('Error deleting task:', error);
            this.snackBar.open('Failed to delete task', 'Close', {
              duration: 3000
            });
          }
        });
    }
  }

  // Bulk operations
  toggleSelectAll(): void {
    if (this.selectAll) {
      this.selectedTasks.clear();
      this.selectAll = false;
    } else {
      this.filteredTasks.forEach(task => this.selectedTasks.add(task.id));
      this.selectAll = true;
    }
  }

  toggleTaskSelection(taskId: number): void {
    if (this.selectedTasks.has(taskId)) {
      this.selectedTasks.delete(taskId);
      this.selectAll = false;
    } else {
      this.selectedTasks.add(taskId);
      if (this.selectedTasks.size === this.filteredTasks.length) {
        this.selectAll = true;
      }
    }
  }

  bulkUpdateStatus(newStatus: TaskStatus): void {
    if (this.selectedTasks.size === 0) {
      this.snackBar.open('Please select tasks to update', 'Close', { duration: 3000 });
      return;
    }

    const taskIds = Array.from(this.selectedTasks);
    // Update tasks one by one since bulkUpdateTaskStatus doesn't exist
    const updatePromises = taskIds.map(taskId => 
      this.taskService.updateTaskStatus(taskId, newStatus).toPromise()
    );
    
    Promise.all(updatePromises)
      .then(() => {
        this.snackBar.open(`Updated ${taskIds.length} tasks to ${this.getStatusLabel(newStatus)}`, 'Close', {
          duration: 3000
        });
        this.selectedTasks.clear();
        this.selectAll = false;
        this.loadTasks();
      })
      .catch((error) => {
        console.error('Error bulk updating tasks:', error);
        this.snackBar.open('Failed to update tasks', 'Close', { duration: 3000 });
      });
  }

  bulkDeleteTasks(): void {
    if (this.selectedTasks.size === 0) {
      this.snackBar.open('Please select tasks to delete', 'Close', { duration: 3000 });
      return;
    }

    if (confirm(`Are you sure you want to delete ${this.selectedTasks.size} tasks?`)) {
      const taskIds = Array.from(this.selectedTasks);
      // Delete tasks one by one since bulkDeleteTasks doesn't exist
      const deletePromises = taskIds.map(taskId => 
        this.taskService.deleteTask(taskId).toPromise()
      );
      
      Promise.all(deletePromises)
        .then(() => {
          this.snackBar.open(`Deleted ${taskIds.length} tasks successfully`, 'Close', {
            duration: 3000
          });
          this.selectedTasks.clear();
          this.selectAll = false;
          this.loadTasks();
        })
        .catch((error) => {
          console.error('Error bulk deleting tasks:', error);
          this.snackBar.open('Failed to delete tasks', 'Close', { duration: 3000 });
        });
    }
  }

  // Filter and sort methods
  applyFilters(): void {
    this.currentPage = 1;
    // Filters are applied automatically through the getter
  }

  clearFilters(): void {
    this.filterStatus = 'all';
    this.filterPriority = 'all';
    this.filterProject = 'all';
    this.filterAssignee = 'all';
    this.searchQuery = '';
    this.currentPage = 1;
  }

  sortTasks(column: string): void {
    if (this.sortBy === column) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortOrder = 'asc';
    }
  }

  // Utility methods
  getStatusClass(status: string): string {
    switch (status) {
      case 'TODO':
        return 'status-todo';
      case 'IN_PROGRESS':
        return 'status-in-progress';
      case 'DONE':
        return 'status-done';
      default:
        return '';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'TODO':
        return 'To Do';
      case 'IN_PROGRESS':
        return 'In Progress';
      case 'DONE':
        return 'Done';
      default:
        return status;
    }
  }

  getPriorityClass(priority: string): string {
    switch (priority) {
      case 'HIGH':
        return 'priority-high';
      case 'MEDIUM':
        return 'priority-medium';
      case 'LOW':
        return 'priority-low';
      default:
        return '';
    }
  }

  isTaskOverdue(task: Task): boolean {
    return new Date(task.dueDate) < new Date() && task.status !== 'DONE';
  }

  canEditTask(task: Task): boolean {
    if (!this.currentUser) return false;
    
    // Admin can edit all tasks
    if (this.currentUser.role === 'ADMIN') return true;
    
    // Manager can edit tasks in their projects
    if (this.currentUser.role === 'MANAGER') return true;
    
    // User can only edit their assigned tasks
    return task.assignedUser?.id === this.currentUser.id;
  }

  canDeleteTask(task: Task): boolean {
    if (!this.currentUser) return false;
    
    // Only Admin and Manager can delete tasks
    return ['ADMIN', 'MANAGER'].includes(this.currentUser.role);
  }
}
