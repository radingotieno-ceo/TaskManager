import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface UserStatistics {
  totalUsers: number;
  adminUsers: number;
  managerUsers: number;
  regularUsers: number;
  roleBreakdown: {
    [key: string]: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // Get all users
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users`);
  }

  // Get all managers
  getAllManagers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users/managers`);
  }

  // Get users available for task assignment
  getUsersAvailableForTaskAssignment(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users/available-for-tasks`);
  }

  // Update user role
  updateUserRole(userId: number, role: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.apiUrl}/admin/users/${userId}/role`, { role });
  }

  // Promote user to manager
  promoteUserToManager(userId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/admin/users/${userId}/promote-to-manager`, {});
  }

  // Demote manager to user
  demoteManagerToUser(userId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/admin/users/${userId}/demote-to-user`, {});
  }

  // Get user statistics
  getUserStatistics(): Observable<UserStatistics> {
    return this.http.get<UserStatistics>(`${this.apiUrl}/admin/users/statistics`);
  }

  // Delete user
  deleteUser(userId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/admin/users/${userId}`);
  }
}
