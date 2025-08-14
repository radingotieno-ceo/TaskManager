import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
  profilePhotoUrl?: string;
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
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  // Get all users
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/auth/users`);
  }

  // Get all managers
  getAllManagers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/auth/users`).pipe(
      map(users => users.filter(user => user.role === 'MANAGER'))
    );
  }

  // Get users available for task assignment
  getUsersAvailableForTaskAssignment(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/auth/users`).pipe(
      map(users => users.filter(user => user.role === 'USER'))
    );
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
    return this.http.get<User[]>(`${this.apiUrl}/auth/users`).pipe(
      map(users => {
        const totalUsers = users.length;
        const adminUsers = users.filter(u => u.role === 'ADMIN').length;
        const managerUsers = users.filter(u => u.role === 'MANAGER').length;
        const regularUsers = users.filter(u => u.role === 'USER').length;
        
        const roleBreakdown = {
          ADMIN: adminUsers,
          MANAGER: managerUsers,
          USER: regularUsers
        };
        
        return {
          totalUsers,
          adminUsers,
          managerUsers,
          regularUsers,
          roleBreakdown
        };
      })
    );
  }

  // Delete user
  deleteUser(userId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/admin/users/${userId}`);
  }
}
