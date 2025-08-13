import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User, AuthResponse, LoginRequest, RegisterRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient) {
    // Initialize with user from localStorage if exists
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public getToken(): string | null {
    return localStorage.getItem('token');
  }

  public isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    // Check if token is expired
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp > currentTime;
    } catch (error) {
      return false;
    }
  }

  // Role-based permission methods
  hasRole(role: string): boolean {
    return this.currentUserValue?.role === role;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.includes(this.currentUserValue?.role || '');
  }

  canManageProjects(): boolean {
    return this.hasAnyRole(['ADMIN', 'MANAGER']);
  }

  canCreateProject(): boolean {
    return this.hasAnyRole(['ADMIN', 'MANAGER']);
  }

  canCreateTask(): boolean {
    return this.hasAnyRole(['ADMIN', 'MANAGER']);
  }

  canViewReports(): boolean {
    return this.hasAnyRole(['ADMIN', 'MANAGER']);
  }

  canManageSettings(): boolean {
    return this.hasRole('ADMIN');
  }

  // Test backend connection
  testBackendConnection(): Observable<any> {
    console.log('AuthService: Testing backend connection...');
    return this.http.get('http://localhost:8080/health');
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    console.log('AuthService: Attempting login to:', `${this.apiUrl}/login`);
    console.log('AuthService: Request data:', request);
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(map(response => {
        console.log('AuthService: Login successful, response:', response);
        // Store user details and token in localStorage
        localStorage.setItem('token', response.token);
        localStorage.setItem('currentUser', JSON.stringify(response.user));
        this.currentUserSubject.next(response.user);
        return response;
      }));
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, request);
  }

  logout(): void {
    // Remove user from local storage and set current user to null
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }
}
