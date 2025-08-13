import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { DashboardService, DashboardData } from '../../services/dashboard.service';
import { UserManagementService } from '../../services/user-management.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  loading = false;
  sidebarCollapsed = false;
  searchQuery = '';
  hasNotifications = true;
  activeTab = 'dashboard';

  // Real dashboard data
  dashboardData: DashboardData = {};
  systemOverview: any = {};
  projectPortfolio: any[] = [];
  teamOverview: any = {};
  recentActivities: any[] = [];
  systemAlerts: any[] = [];

  // User management data
  allUsers: any[] = [];
  allManagers: any[] = [];
  userStatistics: any = {};

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private userManagementService: UserManagementService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loading = true;
    
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: User | null) => {
        this.currentUser = user;
        
        // Redirect if user is not admin
        if (user && user.role !== 'ADMIN') {
          this.redirectToAppropriateDashboard(user.role);
        } else if (user) {
          this.loadDashboardData();
          this.loadUserManagementData();
        }
        
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    this.dashboardService.getAdminDashboardData()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: DashboardData) => {
          this.dashboardData = data;
          this.systemOverview = data.systemOverview || {};
          this.projectPortfolio = data.projectPortfolio || [];
          this.teamOverview = data.teamOverview || {};
          this.recentActivities = data.recentActivities || [];
          this.systemAlerts = data.systemAlerts || [];
        },
        error: (error) => {
          console.error('Error loading admin dashboard data:', error);
        }
      });
  }

  private loadUserManagementData(): void {
    // Load all users
    this.userManagementService.getAllUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          this.allUsers = users;
        },
        error: (error) => {
          console.error('Error loading users:', error);
        }
      });

    // Load all managers
    this.userManagementService.getAllManagers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (managers) => {
          this.allManagers = managers;
        },
        error: (error) => {
          console.error('Error loading managers:', error);
        }
      });

    // Load user statistics
    this.userManagementService.getUserStatistics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statistics) => {
          this.userStatistics = statistics;
        },
        error: (error) => {
          console.error('Error loading user statistics:', error);
        }
      });
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

  // User management functions
  promoteUserToManager(userId: number): void {
    this.userManagementService.promoteUserToManager(userId)
      .subscribe({
        next: (response) => {
          console.log('User promoted:', response.message);
          this.loadUserManagementData(); // Reload data
        },
        error: (error) => {
          console.error('Error promoting user:', error);
        }
      });
  }

  demoteManagerToUser(userId: number): void {
    this.userManagementService.demoteManagerToUser(userId)
      .subscribe({
        next: (response) => {
          console.log('Manager demoted:', response.message);
          this.loadUserManagementData(); // Reload data
        },
        error: (error) => {
          console.error('Error demoting manager:', error);
        }
      });
  }

  updateUserRole(userId: number, newRole: string): void {
    this.userManagementService.updateUserRole(userId, newRole)
      .subscribe({
        next: (response) => {
          console.log('User role updated:', response.message);
          this.loadUserManagementData(); // Reload data
        },
        error: (error) => {
          console.error('Error updating user role:', error);
        }
      });
  }

  deleteUser(userId: number): void {
    if (confirm('Are you sure you want to delete this user?')) {
      this.userManagementService.deleteUser(userId)
        .subscribe({
          next: (response) => {
            console.log('User deleted:', response.message);
            this.loadUserManagementData(); // Reload data
          },
          error: (error) => {
            console.error('Error deleting user:', error);
          }
        });
    }
  }

  private redirectToAppropriateDashboard(userRole: string): void {
    switch (userRole) {
      case 'MANAGER':
        this.router.navigate(['/manager']);
        break;
      case 'USER':
        this.router.navigate(['/user']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }
}
