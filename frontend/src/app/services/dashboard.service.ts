import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface DashboardData {
  // Manager/Admin Dashboard
  totalProjects?: number;
  recentProjects?: any[];
  projectsWithOverdueTasks?: any[];
  totalTasks?: number;
  activeTasks?: number;
  completedTasks?: number;
  overdueTasks?: any[];
  tasksDueThisWeek?: any[];
  totalUsers?: number;
  activeUsers?: any[];
  usersWithOverdueTasks?: any[];
  topPerformers?: any[];

  // User Dashboard
  myTasks?: any[];
  dueToday?: number;
  dueThisWeek?: number;
  completedCount?: number;
  projectProgress?: any[];
  highPriorityTasks?: any[];

  // Admin Dashboard
  adminUsers?: number;
  managerUsers?: number;
  regularUsers?: number;
  todoTasks?: number;
  inProgressTasks?: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // Get dashboard data based on user role
  getDashboardData(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/data`);
  }

  // Get manager dashboard data
  getManagerDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/manager`);
  }

  // Get user dashboard data
  getUserDashboard(userId: number): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/user/${userId}`);
  }

  // Get admin dashboard data
  getAdminDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.apiUrl}/dashboard/admin`);
  }

  // Get role-based dashboard data
  getRoleBasedDashboard(role: string, userId?: number): Observable<DashboardData> {
    let url = `${this.apiUrl}/dashboard/role-based/${role}`;
    if (userId) {
      url += `?userId=${userId}`;
    }
    return this.http.get<DashboardData>(url);
  }

  // Get project progress
  getProjectProgress(projectId: number): Observable<{projectId: number, progress: number}> {
    return this.http.get<{projectId: number, progress: number}>(`${this.apiUrl}/dashboard/project/${projectId}/progress`);
  }
}
