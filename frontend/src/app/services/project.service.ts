import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Project, CreateProjectRequest, UpdateProjectRequest, ProjectStatistics } from '../models/project.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = 'http://localhost:8080/api/projects';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private getHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // Basic CRUD operations
  getAllProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  createProject(project: CreateProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, project, { headers: this.getHeaders() });
  }

  updateProject(id: number, project: UpdateProjectRequest): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, project, { headers: this.getHeaders() });
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  // Advanced project management
  getProjectStatistics(): Observable<ProjectStatistics> {
    return this.http.get<ProjectStatistics>(`${this.apiUrl}/statistics`, { headers: this.getHeaders() });
  }

  getProjectsByStatus(status: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/status/${status}`, { headers: this.getHeaders() });
  }

  getProjectsByPriority(priority: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/priority/${priority}`, { headers: this.getHeaders() });
  }

  getOverdueProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/overdue`, { headers: this.getHeaders() });
  }

  // User assignment to projects
  assignUserToProject(projectId: number, userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${projectId}/assign-user/${userId}`, {}, { headers: this.getHeaders() });
  }

  removeUserFromProject(projectId: number, userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${projectId}/remove-user/${userId}`, { headers: this.getHeaders() });
  }

  getProjectUsers(projectId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${projectId}/users`, { headers: this.getHeaders() });
  }

  // Project progress and status
  updateProjectStatus(projectId: number, status: string): Observable<Project> {
    return this.http.patch<Project>(`${this.apiUrl}/${projectId}/status`, { status }, { headers: this.getHeaders() });
  }

  updateProjectProgress(projectId: number, progress: number): Observable<Project> {
    return this.http.patch<Project>(`${this.apiUrl}/${projectId}/progress`, { progress }, { headers: this.getHeaders() });
  }

  // Search and filter
  searchProjects(query: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/search?q=${query}`, { headers: this.getHeaders() });
  }

  getProjectsByDateRange(startDate: string, endDate: string): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.apiUrl}/date-range?start=${startDate}&end=${endDate}`, { headers: this.getHeaders() });
  }

  // Bulk operations
  bulkUpdateProjectStatus(projectIds: number[], status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/bulk-status`, { projectIds, status }, { headers: this.getHeaders() });
  }

  bulkDeleteProjects(projectIds: number[]): Observable<any> {
    return this.http.post(`${this.apiUrl}/bulk-delete`, { projectIds }, { headers: this.getHeaders() });
  }
}
