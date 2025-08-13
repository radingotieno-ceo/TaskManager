import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Task {
  id: number;
  title: string;
  description: string;
  status: string;
  priority: string;
  dueDate: string;
  project: {
    id: number;
    name: string;
  };
  assignedUser?: {
    id: number;
    name: string;
    email: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  taskCount?: number;
}

@Injectable({
  providedIn: 'root'
})
export class TaskAssignmentService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // Get unassigned tasks
  getUnassignedTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/assignment/unassigned`);
  }

  // Get users available for task assignment
  getUsersAvailableForAssignment(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/tasks/assignment/available-users`);
  }

  // Assign task to user
  assignTaskToUser(taskId: number, userId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/tasks/assignment/${taskId}/assign/${userId}`, {});
  }

  // Unassign task
  unassignTask(taskId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/tasks/assignment/${taskId}/unassign`, {});
  }

  // Get tasks assigned to a specific user
  getTasksAssignedToUser(userId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/assignment/user/${userId}/tasks`);
  }

  // Get tasks by project
  getTasksByProject(projectId: number): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}/tasks/assignment/project/${projectId}/tasks`);
  }

  // Bulk assign tasks
  bulkAssignTasks(taskIds: number[], userId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/tasks/assignment/bulk-assign`, {
      taskIds: taskIds,
      userId: userId
    });
  }
}
