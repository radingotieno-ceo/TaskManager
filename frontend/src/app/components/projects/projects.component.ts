import { Component, OnInit } from '@angular/core';
import { ProjectService } from '../../services/project.service';
import { Project } from '../../models/project.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['./projects.component.scss']
})
export class ProjectsComponent implements OnInit {
  projects: Project[] = [];
  loading = true;

  constructor(
    private projectService: ProjectService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.projectService.getAllProjects().subscribe({
      next: (projects: Project[]) => {
        this.projects = projects;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Error loading projects:', error);
        this.snackBar.open('Failed to load projects', 'Close', {
          duration: 3000
        });
        this.loading = false;
      }
    });
  }

  createProject(): void {
    this.router.navigate(['/projects/create']);
  }

  viewProject(project: Project): void {
    this.router.navigate(['/projects', project.id]);
  }

  editProject(project: Project): void {
    this.router.navigate(['/projects', project.id, 'edit']);
  }

  deleteProject(project: Project): void {
    if (confirm(`Are you sure you want to delete project "${project.name}"?`)) {
      this.projectService.deleteProject(project.id).subscribe({
        next: () => {
          this.snackBar.open('Project deleted successfully', 'Close', {
            duration: 2000
          });
          this.loadProjects();
        },
        error: (error: any) => {
          console.error('Error deleting project:', error);
          this.snackBar.open('Failed to delete project', 'Close', {
            duration: 3000
          });
        }
      });
    }
  }
}
