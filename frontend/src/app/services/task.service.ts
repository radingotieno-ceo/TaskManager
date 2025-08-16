import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Task, CreateTaskRequest, UpdateTaskRequest, AssignTaskRequest } from '../models/task.model';
import { User } from '../models/user.model';
import { AuthService } from './auth.service';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // Get all tasks
  getAllTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks`, { headers: this.getHeaders() });
  }

  // Get task by ID
  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/tasks/${id}`, { headers: this.getHeaders() });
  }

  // Create new task
  createTask(task: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/tasks`, task, { headers: this.getHeaders() });
  }

  // Update task
  updateTask(id: number, task: UpdateTaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/tasks/${id}`, task, { headers: this.getHeaders() });
  }

  // Delete task
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tasks/${id}`, { headers: this.getHeaders() });
  }

  // Update task status
  updateTaskStatus(id: number, status: string): Observable<Task> {
    return this.http.patch<Task>(`${this.apiUrl}/tasks/${id}/status`, { status }, { headers: this.getHeaders() });
  }

  // Assign task to user
  assignTask(taskId: number, userId: number): Observable<Task> {
    return this.http.patch<Task>(`${this.apiUrl}/tasks/${taskId}/assign`, { userId }, { headers: this.getHeaders() });
  }

  // Unassign task
  unassignTask(taskId: number): Observable<Task> {
    return this.http.patch<Task>(`${this.apiUrl}/tasks/${taskId}/unassign`, {}, { headers: this.getHeaders() });
  }

  // Get tasks by status
  getTasksByStatus(status: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/status/${status}`, { headers: this.getHeaders() });
  }

  // Get tasks by priority
  getTasksByPriority(priority: string): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/priority/${priority}`, { headers: this.getHeaders() });
  }

  // Get tasks by project
  getTasksByProject(projectId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/project/${projectId}`, { headers: this.getHeaders() });
  }

  // Get assigned tasks for current user
  getAssignedTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/assigned`, { headers: this.getHeaders() });
  }

  // Task Assignment specific endpoints
  getUnassignedTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks`, { headers: this.getHeaders() })
      .pipe(
        map(tasks => tasks.filter(task => !task.assignedUser))
      );
  }

  getAvailableUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/auth/users`, { headers: this.getHeaders() })
      .pipe(
        map(users => users.filter(user => user.role === 'USER'))
      );
  }

  assignTaskToUser(taskId: number, userId: number): Observable<any> {
    return this.http.patch<Task>(`${this.apiUrl}/tasks/${taskId}/assign`, { userId }, { headers: this.getHeaders() });
  }

  // New method for creating and assigning task in one operation
  createAndAssignTask(taskData: any): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/tasks/create-and-assign`, taskData, { headers: this.getHeaders() });
  }

  getTasksAssignedToUser(userId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/assignment/user/${userId}/tasks`, { headers: this.getHeaders() });
  }

  bulkAssignTasks(taskIds: number[], userId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/tasks/assignment/bulk-assign`, { taskIds, userId }, { headers: this.getHeaders() });
  }
}
