import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface DashboardData {
  systemOverview?: {
    totalUsers: number;
    activeProjects: number;
    tasksCompleted: number;
    systemHealth: number;
  };
  projectPortfolio?: Array<{
    id: number;
    name: string;
    priority: string;
    members: number;
    dueDate: string;
    status: string;
    progress: number;
  }>;
  teamOverview?: {
    totalUsers: number;
    activeUsers: number;
    managers: number;
    regularUsers: number;
  };
  recentActivities?: Array<{
    avatar: string;
    user: string;
    action: string;
    role: string;
    time: string;
  }>;
  systemAlerts?: Array<{
    type: string;
    title: string;
    description: string;
  }>;
  summaryCards?: {
    totalProjects: number;
    activeTasks: number;
    completedTasks: number;
    overdueTasks: number;
  };
  recentProjects?: Array<{
    id: number;
    name: string;
    description: string;
    progress: number;
    dueDate: string;
    teamSize: number;
  }>;
  recentTasks?: Array<{
    id: number;
    name: string;
    project: string;
    priority: string;
    assignee: string;
    status: string;
    dueDate: string;
  }>;
  availableUsers?: Array<{
    id: number;
    name: string;
    email: string;
    role: string;
    taskCount: number;
  }>;
  myTasks?: Array<{
    id: number;
    name: string;
    project: string;
    priority: string;
    progress: number;
    status: string;
    dueDate: string;
  }>;
  myProjects?: Array<{
    id: number;
    name: string;
    role: string;
    progress: number;
    tasksAssigned: number;
  }>;
  taskStats?: {
    totalTasks: number;
    activeTasks: number;
    completedTasks: number;
    overdueTasks: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // Get admin dashboard data
  getAdminDashboardData(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/admin`)
      .pipe(
        catchError(error => {
          console.error('Error fetching admin dashboard data:', error);
          return throwError(() => error);
        })
      );
  }

  // Get manager dashboard data
  getManagerDashboardData(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/manager`)
      .pipe(
        catchError(error => {
          console.error('Error fetching manager dashboard data:', error);
          return throwError(() => error);
        })
      );
  }

  // Get user dashboard data
  getUserDashboardData(userId: number): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/user/${userId}`)
      .pipe(
        catchError(error => {
          console.error('Error fetching user dashboard data:', error);
          return throwError(() => error);
        })
      );
  }

  // Get role-based dashboard data
  getRoleBasedDashboardData(role: string, userId?: number): Observable<DashboardData> {
    let url = `${this.apiUrl}/dashboard/role-based/${role}`;
    if (userId) {
      url += `?userId=${userId}`;
    }
    return this.http.get<DashboardData>(url)
      .pipe(
        catchError(error => {
          console.error('Error fetching role-based dashboard data:', error);
          return throwError(() => error);
        })
      );
  }

  // Get project progress
  getProjectProgress(projectId: number): Observable<{ projectId: number; progress: number }> {
    return this.http.get<{ projectId: number; progress: number }>(`${this.apiUrl}/dashboard/project/${projectId}/progress`)
      .pipe(
        catchError(error => {
          console.error('Error fetching project progress:', error);
          return throwError(() => error);
        })
      );
  }
}
