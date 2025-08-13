import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class UserService {
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

  // Get all users (Admin only)
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users`, { headers: this.getHeaders() });
  }

  // Get all managers (Admin only)
  getAllManagers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users/managers`, { headers: this.getHeaders() });
  }

  // Get users available for task assignment
  getUsersAvailableForTaskAssignment(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/users/available-for-tasks`, { headers: this.getHeaders() });
  }

  // Get available users for assignment (Manager/Admin)
  getAvailableUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/tasks/assignment/available-users`, { headers: this.getHeaders() });
  }

  // Update user role (Admin only)
  updateUserRole(userId: number, role: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/admin/users/${userId}/role`, { role }, { headers: this.getHeaders() });
  }

  // Promote user to manager (Admin only)
  promoteUserToManager(userId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/admin/users/${userId}/promote-to-manager`, {}, { headers: this.getHeaders() });
  }

  // Demote manager to user (Admin only)
  demoteManagerToUser(userId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/admin/users/${userId}/demote-to-user`, {}, { headers: this.getHeaders() });
  }

  // Get user statistics (Admin only)
  getUserStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/users/statistics`, { headers: this.getHeaders() });
  }

  // Delete user (Admin only)
  deleteUser(userId: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/admin/users/${userId}`, { headers: this.getHeaders() });
  }

  // Get current user profile
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/auth/profile`, { headers: this.getHeaders() });
  }

  // Update current user profile
  updateProfile(userData: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/auth/profile`, userData, { headers: this.getHeaders() });
  }
}

