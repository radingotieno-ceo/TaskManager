import { Component, OnInit } from '@angular/core';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks.component.html',
  styleUrls: ['./tasks.component.scss']
})
export class TasksComponent implements OnInit {
  tasks: Task[] = [];
  loading = true;
  filterStatus: string = 'all';

  constructor(
    private taskService: TaskService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading = true;
    this.taskService.getAllTasks().subscribe({
      next: (tasks: Task[]) => {
        this.tasks = tasks;
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

  get filteredTasks(): Task[] {
    if (this.filterStatus === 'all') {
      return this.tasks;
    }
    return this.tasks.filter(task => task.status === this.filterStatus);
  }

  createTask(): void {
    this.router.navigate(['/tasks/create']);
  }

  updateTaskStatus(task: Task): void {
    let newStatus: 'TODO' | 'IN_PROGRESS' | 'DONE';
    switch (task.status) {
      case 'TODO':
        newStatus = 'IN_PROGRESS';
        break;
      case 'IN_PROGRESS':
        newStatus = 'DONE';
        break;
      case 'DONE':
        newStatus = 'TODO';
        break;
      default:
        newStatus = 'TODO';
    }

    this.taskService.updateTaskStatus(task.id, newStatus).subscribe({
      next: (updatedTask: Task) => {
        this.snackBar.open(`Task status updated to ${newStatus}`, 'Close', {
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
      this.taskService.deleteTask(task.id).subscribe({
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
}
