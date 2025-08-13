import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  loading = false;
  sidebarCollapsed = false;
  searchQuery = '';
  hasNotifications = true;
  activeTab = 'dashboard';

  // Sample data matching TaskFlow design
  recentProjects = [
    {
      name: 'Mobile App Redesign',
      description: 'Complete UI/UX overhaul for mobile application.',
      progress: 75,
      dueDate: '2024-01-15',
      teamSize: 5
    },
    {
      name: 'API Integration',
      description: 'Integrate third-party payment gateway.',
      progress: 45,
      dueDate: '2024-01-20',
      teamSize: 3
    }
  ];

  recentTasks = [
    {
      name: 'Design user authentication flow',
      project: 'Mobile App Redesign',
      priority: 'HIGH',
      assignee: 'Alex Johnson',
      status: 'TODO',
      dueDate: '2024-01-12'
    },
    {
      name: 'Implement payment validation',
      project: 'API Integration',
      priority: 'MEDIUM',
      assignee: 'Maria Garcia',
      status: 'IN_PROGRESS',
      dueDate: '2024-01-14'
    }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: User | null) => {
        this.currentUser = user;
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
  }

  onSearch(): void {
    // Implement search functionality
    console.log('Searching for:', this.searchQuery);
  }

  showNotifications(): void {
    // Implement notifications functionality
    console.log('Showing notifications');
  }

  canManageProjects(): boolean {
    return this.currentUser?.role === 'ADMIN' || this.currentUser?.role === 'MANAGER';
  }

  canViewReports(): boolean {
    return this.currentUser?.role === 'ADMIN' || this.currentUser?.role === 'MANAGER';
  }

  canManageSettings(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }
}





