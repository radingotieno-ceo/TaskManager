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

  // Admin dashboard data - Project Portfolio
  adminProjects = [
    {
      name: 'Enterprise CRM Redesign',
      priority: 'HIGH',
      members: 12,
      dueDate: 'Dec 15, 2024',
      status: 'In Progress',
      progress: 78
    },
    {
      name: 'Data Migration Project',
      priority: 'CRITICAL',
      members: 6,
      dueDate: 'Nov 28, 2024',
      status: 'In Progress',
      progress: 95
    },
    {
      name: 'Security Audit Implementation',
      priority: 'HIGH',
      members: 4,
      dueDate: 'Dec 20, 2024',
      status: 'In Progress',
      progress: 45
    }
  ];

  // Admin dashboard data - Recent Activities
  recentActivities = [
    {
      avatar: 'SJ',
      user: 'Sarah Johnson',
      action: 'Completed 5 tasks',
      role: 'ADMIN',
      time: '4 hours ago'
    },
    {
      avatar: 'DR',
      user: 'David Rodriguez',
      action: 'Updated user permissions',
      role: 'ADMIN',
      time: '6 hours ago'
    },
    {
      avatar: 'LT',
      user: 'Lisa Thompson',
      action: 'Generated monthly report',
      role: 'MANAGER',
      time: '1 day ago'
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
        
        // Redirect to role-specific dashboard
        if (user) {
          this.redirectToRoleSpecificDashboard(user.role);
        }
      });
  }

  private redirectToRoleSpecificDashboard(userRole: string): void {
    switch (userRole) {
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'MANAGER':
        this.router.navigate(['/manager']);
        break;
      case 'USER':
        this.router.navigate(['/user']);
        break;
      default:
        // Fallback to user dashboard
        this.router.navigate(['/user']);
    }
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

  addUser(): void {
    // Implement add user functionality
    console.log('Adding new user');
  }

  newProject(): void {
    // Implement new project functionality
    console.log('Creating new project');
  }

  viewAnalytics(): void {
    // Navigate to analytics page
    console.log('Viewing analytics');
  }

  showProjectOptions(project: any): void {
    // Show project options menu
    console.log('Showing options for project:', project.name);
  }

  manageTeamSettings(): void {
    // Navigate to team settings
    console.log('Managing team settings');
  }

  // Role-based access methods
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





