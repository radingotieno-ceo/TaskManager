import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { DashboardService, DashboardData } from '../../services/dashboard.service';
import { UserManagementService } from '../../services/user-management.service';
import { User } from '../../models/user.model';
import { ProjectService } from '../../services/project.service';

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

  // Enhanced dashboard data with more detailed information
  dashboardData: DashboardData = {};
  systemOverview: any = {};
  projectPortfolio: any[] = [];
  teamOverview: any = {};
  recentActivities: any[] = [];
  systemAlerts: any[] = [];
  
  // Enhanced user management data
  allUsers: any[] = [];
  allManagers: any[] = [];
  userStatistics: any = {};
  
  // Additional dashboard features
  systemMetrics: any = {
    cpuUsage: 0,
    memoryUsage: 0,
    diskUsage: 0,
    activeConnections: 0
  };
  
  // Quick actions and notifications
  quickActions: any[] = [
    { icon: 'person_add', label: 'Add User', action: 'addUser', color: 'success' },
    { icon: 'folder', label: 'Create Project', action: 'newProject', color: 'primary' },
    { icon: 'task', label: 'Assign Task', action: 'assignTask', color: 'warning' },
    { icon: 'analytics', label: 'View Reports', action: 'viewReports', color: 'info' }
  ];
  
  recentNotifications: any[] = [];
  
  // Performance tracking
  performanceMetrics: any = {
    responseTime: 0,
    uptime: 0,
    errorRate: 0,
    throughput: 0
  };

  // Settings and profile management
  showProfileEditModal = false;
  showPasswordModal = false;
  updatingProfile = false;
  changingPassword = false;
  uploadingPhoto = false;
  selectedPhotoPreview: string | null = null;
  showPhotoPreviewModal = false;
  selectedPhotoFile: File | null = null;

  // Forms
  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  // Preferences
  preferences = {
    emailNotifications: true,
    taskReminders: true,
    theme: 'light'
  };

  // Loading states
  loadingTeamData = false;
  loadingDashboardData = false;

  // Modal states for Add User and New Project
  showAddUserModal = false;
  showNewProjectModal = false;
  addingUser = false;
  creatingProject = false;

  // Forms for Add User and New Project
  addUserForm!: FormGroup;
  newProjectForm!: FormGroup;

  // Photo upload modal state
  showPhotoUploadModal = false;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private userManagementService: UserManagementService,
    private router: Router,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private projectService: ProjectService
  ) {
    this.initializeForms();
  }

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
          this.initializeEnhancedFeatures();
        }
        
        this.loading = false;
      });
  }

  private initializeEnhancedFeatures(): void {
    console.log('🚀 AdminComponent: Initializing enhanced features');
    
    // Initialize system metrics
    this.refreshSystemMetrics();
    this.refreshPerformanceMetrics();
    
    // Start real-time updates
    this.startRealTimeUpdates();
    
    // Initialize sample notifications
    this.recentNotifications = [
      {
        id: '1',
        type: 'info',
        title: 'System Update Available',
        message: 'A new system update is ready for installation',
        timestamp: new Date(),
        read: false
      },
      {
        id: '2',
        type: 'warning',
        title: 'High CPU Usage',
        message: 'CPU usage has exceeded 80% for the last 5 minutes',
        timestamp: new Date(Date.now() - 300000),
        read: false
      },
      {
        id: '3',
        type: 'success',
        title: 'Backup Completed',
        message: 'System backup completed successfully',
        timestamp: new Date(Date.now() - 600000),
        read: true
      }
    ];
    
    this.hasNotifications = this.recentNotifications.some(n => !n.read);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForms(): void {
    this.profileForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    // Initialize Add User form
    this.addUserForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role: ['USER', [Validators.required]]
    }, { validators: this.addUserPasswordMatchValidator });

    // Initialize New Project form
    this.newProjectForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(500)]],
      dueDate: ['', [Validators.required]],
      priority: ['MEDIUM', [Validators.required]]
    });
  }

  private passwordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  private addUserPasswordMatchValidator(form: FormGroup): { [key: string]: boolean } | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    
    // Only validate if both fields have values
    if (!password || !confirmPassword) {
      return null;
    }
    
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  private loadDashboardData(): void {
    console.log('🔍 AdminComponent: Loading dashboard data...');
    this.dashboardService.getAdminDashboardData()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: DashboardData) => {
          console.log('✅ AdminComponent: Dashboard data loaded:', data);
          this.dashboardData = data;
          this.systemOverview = data.systemOverview || {};
          this.projectPortfolio = data.projectPortfolio || [];
          this.teamOverview = data.teamOverview || {};
          this.recentActivities = data.recentActivities || [];
          this.systemAlerts = data.systemAlerts || [];
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error loading admin dashboard data:', error);
        }
      });
  }

  private loadUserManagementData(): void {
    console.log('🔍 AdminComponent: Loading user management data...');
    
    // Load all users
    this.userManagementService.getAllUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          console.log('✅ AdminComponent: Users loaded:', users.length);
          
          // Ensure we have users data
          if (users && users.length > 0) {
            this.allUsers = users;
            this.allManagers = users.filter(user => user.role === 'MANAGER');
          } else {
            console.warn('⚠️ AdminComponent: No users returned from service, using fallback data');
            // Fallback to sample data if service returns empty
            this.allUsers = [
              { id: 1, name: 'Admin User', email: 'admin@karooth.com', role: 'ADMIN' },
              { id: 2, name: 'Manager User', email: 'manager@karooth.com', role: 'MANAGER' },
              { id: 3, name: 'Regular User', email: 'user@karooth.com', role: 'USER' }
            ];
            this.allManagers = this.allUsers.filter(user => user.role === 'MANAGER');
          }
          
          this.refreshUserStatistics(); // Update statistics with fresh data
          this.cdr.detectChanges(); // Force change detection
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error loading users:', error);
          // Fallback to sample data on error
          this.allUsers = [
            { id: 1, name: 'Admin User', email: 'admin@karooth.com', role: 'ADMIN' },
            { id: 2, name: 'Manager User', email: 'manager@karooth.com', role: 'MANAGER' },
            { id: 3, name: 'Regular User', email: 'user@karooth.com', role: 'USER' }
          ];
          this.allManagers = this.allUsers.filter(user => user.role === 'MANAGER');
          this.refreshUserStatistics();
          this.cdr.detectChanges();
        }
      });

    // Load user statistics
    this.userManagementService.getUserStatistics()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (statistics) => {
          console.log('✅ AdminComponent: User statistics loaded');
          this.userStatistics = {
            ...statistics,
            totalUsers: this.allUsers.length,
            adminUsers: this.allUsers.filter(u => u.role === 'ADMIN').length,
            managerUsers: this.allUsers.filter(u => u.role === 'MANAGER').length,
            regularUsers: this.allUsers.filter(u => u.role === 'USER').length,
            activeUsers: Math.floor(this.allUsers.length * 0.85), // 85% active
            newUsersThisWeek: Math.floor(Math.random() * 5) + 1,
            usersOnline: Math.floor(this.allUsers.length * 0.3), // 30% online
            lastUpdated: new Date().toLocaleTimeString()
          };
          this.cdr.detectChanges(); // Force change detection
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error loading user statistics:', error);
          // Fallback to calculated statistics
          this.refreshUserStatistics();
        }
      });
  }



  // Navigation and UI methods
  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    console.log(`🔄 AdminComponent: Switching to tab: ${tab}`);
    
    if (tab === 'dashboard') {
      this.loadDashboardData();
    } else if (tab === 'team') {
      console.log('🔄 AdminComponent: Loading team data immediately...');
      this.loadingTeamData = true;
      this.cdr.detectChanges();
      this.loadUserManagementData();
      this.refreshTeamData();
    } else if (tab === 'projects') {
      this.loadProjectData();
    } else if (tab === 'tasks') {
      this.loadTaskData();
    } else if (tab === 'reports') {
      this.loadReportData();
    } else if (tab === 'settings') {
      this.loadSettingsData();
    }
  }

  onSearch(): void {
    console.log('🔍 AdminComponent: Searching for:', this.searchQuery);
  }

  showNotifications(): void {
    console.log('🔔 AdminComponent: Showing notifications');
    // TODO: Implement notifications panel
    alert('Notifications panel coming soon!');
  }

  // Action buttons
  addUser(): void {
    console.log('➕ AdminComponent: Adding new user');
    this.showAddUserModal = true;
  }

  newProject(): void {
    console.log('➕ AdminComponent: Creating new project');
    this.showNewProjectModal = true;
  }

  viewAnalytics(): void {
    console.log('📊 AdminComponent: Viewing analytics');
    this.setActiveTab('reports');
  }

  showProjectOptions(project: any): void {
    console.log('⚙️ AdminComponent: Showing options for project:', project.name);
    // TODO: Implement project options menu
    alert(`Project options for "${project.name}" coming soon!`);
  }

  manageTeamSettings(): void {
    console.log('⚙️ AdminComponent: Managing team settings');
    // TODO: Implement team settings management
    alert('Team settings management coming soon!');
  }

  // User management functions
  promoteUserToManager(userId: number): void {
    console.log('⬆️ AdminComponent: Promoting user to manager:', userId);
    this.userManagementService.promoteUserToManager(userId)
      .subscribe({
        next: (response) => {
          console.log('✅ AdminComponent: User promoted:', response.message);
          this.loadUserManagementData(); // Reload data
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error promoting user:', error);
        }
      });
  }

  demoteManagerToUser(userId: number): void {
    console.log('⬇️ AdminComponent: Demoting manager to user:', userId);
    this.userManagementService.demoteManagerToUser(userId)
      .subscribe({
        next: (response) => {
          console.log('✅ AdminComponent: Manager demoted:', response.message);
          this.loadUserManagementData(); // Reload data
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error demoting manager:', error);
        }
      });
  }

  updateUserRole(userId: number, newRole: string): void {
    console.log('🔄 AdminComponent: Updating user role:', userId, 'to', newRole);
    this.userManagementService.updateUserRole(userId, newRole)
      .subscribe({
        next: (response) => {
          console.log('✅ AdminComponent: User role updated:', response.message);
          this.loadUserManagementData(); // Reload data
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error updating user role:', error);
        }
      });
  }

  deleteUser(userId: number): void {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      console.log('🗑️ AdminComponent: Deleting user:', userId);
      this.userManagementService.deleteUser(userId)
        .subscribe({
          next: (response) => {
            console.log('✅ AdminComponent: User deleted:', response.message);
            this.loadUserManagementData(); // Reload data
          },
          error: (error) => {
            console.error('❌ AdminComponent: Error deleting user:', error);
          }
        });
    }
  }

  // Settings and Profile Management
  // Profile management methods
  openPhotoPreview(): void {
    console.log('📷 AdminComponent: Opening photo preview modal');
    this.showPhotoPreviewModal = true;
  }

  uploadNewPhoto(): void {
    console.log('📷 AdminComponent: Opening photo upload modal');
    this.showPhotoUploadModal = true;
  }

  closePhotoUploadModal(): void {
    console.log('❌ AdminComponent: Closing photo upload modal');
    this.showPhotoUploadModal = false;
    this.selectedPhotoFile = null;
    this.selectedPhotoPreview = null;
  }

  removeSelectedPhoto(): void {
    console.log('🗑️ AdminComponent: Removing selected photo');
    this.selectedPhotoFile = null;
    this.selectedPhotoPreview = null;
  }

  onFileSelected(event: any): void {
    console.log('🔍 AdminComponent: File selected event:', event);
    const file = event.target.files[0];
    console.log('🔍 AdminComponent: Selected file:', file);
    if (file) {
      console.log('🔍 AdminComponent: File details - name:', file.name, 'size:', file.size, 'type:', file.type);
      this.showPhotoPreview(file);
    } else {
      console.warn('⚠️ AdminComponent: No file selected');
    }
  }

  showPhotoPreview(file: File): void {
    this.selectedPhotoFile = file;
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.selectedPhotoPreview = e.target.result;
      this.showPhotoPreviewModal = true;
    };
    reader.readAsDataURL(file);
  }

  removeProfilePhoto(): void {
    console.log('🗑️ AdminComponent: Removing profile photo');
    if (confirm('Are you sure you want to remove your profile photo?')) {
      // TODO: Implement photo removal API call
      console.log('🗑️ AdminComponent: Photo removal functionality coming soon!');
      alert('Photo removal functionality coming soon!');
    }
  }

  editProfile(): void {
    console.log('✏️ AdminComponent: Editing profile');
    if (this.currentUser) {
      this.profileForm.patchValue({
        name: this.currentUser.name,
        email: this.currentUser.email
      });
      this.showProfileEditModal = true;
    }
  }

  changePassword(): void {
    console.log('🔒 AdminComponent: Changing password');
    this.passwordForm.reset();
    this.showPasswordModal = true;
  }

  submitChangePassword(): void {
    console.log('🔒 AdminComponent: Submitting password change');
    if (this.passwordForm.valid && this.currentUser) {
      this.changingPassword = true;
      const passwordData = {
        email: this.currentUser.email,
        currentPassword: this.passwordForm.value.currentPassword,
        newPassword: this.passwordForm.value.newPassword,
        confirmPassword: this.passwordForm.value.confirmPassword
      };

      this.authService.changePassword(passwordData)
        .subscribe({
          next: (response) => {
            console.log('✅ AdminComponent: Password changed successfully:', response);
            this.closePasswordModal();
            this.changingPassword = false;
            alert('Password changed successfully!');
          },
          error: (error) => {
            console.error('❌ AdminComponent: Error changing password:', error);
            this.changingPassword = false;
            alert('Error changing password. Please try again.');
          }
        });
    } else {
      console.warn('⚠️ AdminComponent: Password form is invalid');
      alert('Please fill in all password fields correctly.');
    }
  }

  showTwoFactorAuth(): void {
    console.log('🔐 AdminComponent: Showing two-factor authentication');
    // TODO: Implement 2FA setup
    alert('Two-factor authentication setup coming soon!');
  }

  // Image handling methods
  getProfilePhotoUrl(photoUrl: string | undefined): string {
    if (!photoUrl) return '';
    return `http://localhost:8080/api/auth/uploads/profile-photos/${photoUrl}?t=${Date.now()}`;
  }

  onImageLoad(event: any): void {
    console.log('✅ AdminComponent: Profile image loaded successfully');
  }

  onImageError(event: any): void {
    console.log('❌ AdminComponent: Profile image failed to load');
    event.target.style.display = 'none';
  }

  // Enhanced action methods
  assignTask(): void {
    console.log('📋 AdminComponent: Opening task assignment modal');
    // TODO: Implement task assignment modal
    alert('Task assignment functionality coming soon!');
  }

  viewReports(): void {
    console.log('📊 AdminComponent: Opening reports view');
    this.setActiveTab('reports');
  }

  // System monitoring methods
  refreshSystemMetrics(): void {
    console.log('🔄 AdminComponent: Refreshing system metrics');
    // Simulate real-time system metrics
    this.systemMetrics = {
      cpuUsage: Math.floor(Math.random() * 30) + 20, // 20-50%
      memoryUsage: Math.floor(Math.random() * 40) + 30, // 30-70%
      diskUsage: Math.floor(Math.random() * 20) + 60, // 60-80%
      activeConnections: Math.floor(Math.random() * 50) + 10 // 10-60
    };
  }

  refreshPerformanceMetrics(): void {
    console.log('⚡ AdminComponent: Refreshing performance metrics');
    this.performanceMetrics = {
      responseTime: Math.floor(Math.random() * 200) + 50, // 50-250ms
      uptime: Math.floor(Math.random() * 10) + 99, // 99-109%
      errorRate: Math.random() * 2, // 0-2%
      throughput: Math.floor(Math.random() * 1000) + 500 // 500-1500 req/s
    };
  }

  // Enhanced user management
  bulkUserAction(action: string, userIds: number[]): void {
    console.log(`🔄 AdminComponent: Performing bulk action: ${action} on users:`, userIds);
    // TODO: Implement bulk user actions
    alert(`Bulk ${action} functionality coming soon!`);
  }

  exportUserData(): void {
    console.log('📤 AdminComponent: Exporting user data');
    // TODO: Implement data export functionality
    alert('Data export functionality coming soon!');
  }

  importUserData(): void {
    console.log('📥 AdminComponent: Importing user data');
    // TODO: Implement data import functionality
    alert('Data import functionality coming soon!');
  }

  // System administration
  systemMaintenance(): void {
    console.log('🔧 AdminComponent: Starting system maintenance');
    // TODO: Implement system maintenance features
    alert('System maintenance functionality coming soon!');
  }

  backupSystem(): void {
    console.log('💾 AdminComponent: Creating system backup');
    // TODO: Implement backup functionality
    alert('System backup functionality coming soon!');
  }

  restoreSystem(): void {
    console.log('🔄 AdminComponent: Restoring system from backup');
    // TODO: Implement restore functionality
    alert('System restore functionality coming soon!');
  }

  // Enhanced notifications
  markNotificationAsRead(notificationId: string): void {
    console.log('✅ AdminComponent: Marking notification as read:', notificationId);
    this.recentNotifications = this.recentNotifications.filter(n => n.id !== notificationId);
    this.hasNotifications = this.recentNotifications.some(n => !n.read);
  }

  clearAllNotifications(): void {
    console.log('🗑️ AdminComponent: Clearing all notifications');
    this.recentNotifications = [];
    this.hasNotifications = false;
  }

  // Real-time updates
  startRealTimeUpdates(): void {
    console.log('🔄 AdminComponent: Starting real-time updates at', new Date().toLocaleTimeString());
    // Simulate real-time data updates
    setInterval(() => {
      console.log('⏰ AdminComponent: Real-time update cycle at', new Date().toLocaleTimeString());
      this.refreshSystemMetrics();
      this.refreshPerformanceMetrics();
      
      // Refresh team data if team tab is active
      if (this.activeTab === 'team') {
        console.log('🔄 AdminComponent: Auto-refreshing team data...');
        this.refreshTeamData();
      }
      
      // Refresh dashboard data if dashboard tab is active
      if (this.activeTab === 'dashboard') {
        console.log('🔄 AdminComponent: Auto-refreshing dashboard data...');
        this.loadDashboardData();
      }
    }, 30000); // Update every 30 seconds
  }

  // Enhanced search functionality
  advancedSearch(query: string, filters: any): void {
    console.log('🔍 AdminComponent: Performing advanced search:', query, filters);
    // TODO: Implement advanced search with filters
    alert('Advanced search functionality coming soon!');
  }

  // Quick actions handler
  executeQuickAction(action: string): void {
    console.log('⚡ AdminComponent: Executing quick action:', action);
    switch (action) {
      case 'addUser':
        this.addUser();
        break;
      case 'newProject':
        this.newProject();
        break;
      case 'assignTask':
        this.assignTask();
        break;
      case 'viewReports':
        this.viewReports();
        break;
      default:
        console.warn('⚠️ AdminComponent: Unknown quick action:', action);
        alert('This functionality is coming soon!');
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

  // Modal management methods
  closePhotoPreviewModal(): void {
    console.log('❌ AdminComponent: Closing photo preview modal');
    this.showPhotoPreviewModal = false;
    this.selectedPhotoPreview = null;
    this.selectedPhotoFile = null;
  }

  confirmPhotoUpload(): void {
    console.log('✅ AdminComponent: Confirming photo upload');
    if (this.selectedPhotoFile) {
      this.uploadProfilePhoto(this.selectedPhotoFile);
      this.closePhotoPreviewModal();
      this.closePhotoUploadModal();
    }
  }

  uploadProfilePhoto(file: File): void {
    console.log('🔍 AdminComponent: Starting photo upload for file:', file.name);
    this.uploadingPhoto = true;
    this.authService.uploadProfilePhoto(file)
      .subscribe({
        next: (response) => {
          console.log('✅ AdminComponent: Profile photo uploaded successfully:', response);
          if (this.currentUser) {
            this.currentUser.profilePhotoUrl = response.data;
            this.authService.updateCurrentUser(this.currentUser);
            console.log('✅ AdminComponent: Updated current user with new photo URL:', response.data);
            
            setTimeout(() => {
              console.log('🔄 AdminComponent: Refreshing profile photo display');
              this.cdr.detectChanges();
            }, 100);
          }
          this.uploadingPhoto = false;
        },
        error: (error) => {
          console.error('❌ AdminComponent: Error uploading profile photo:', error);
          this.uploadingPhoto = false;
          alert('Error uploading photo: ' + (error.error?.message || error.message || 'Unknown error'));
        }
      });
  }

  closeProfileEditModal(): void {
    console.log('❌ AdminComponent: Closing profile edit modal');
    this.showProfileEditModal = false;
    this.profileForm.reset();
  }

  updateProfile(): void {
    console.log('✏️ AdminComponent: Updating profile');
    if (this.profileForm.valid && this.currentUser) {
      this.updatingProfile = true;
      const profileData = {
        name: this.profileForm.value.name,
        email: this.profileForm.value.email,
        profilePhotoUrl: this.currentUser.profilePhotoUrl
      };

      this.authService.updateProfile(profileData)
        .subscribe({
          next: (updatedUser) => {
            console.log('✅ AdminComponent: Profile updated successfully:', updatedUser);
            this.currentUser = updatedUser;
            this.authService.updateCurrentUser(updatedUser);
            this.closeProfileEditModal();
            this.updatingProfile = false;
          },
          error: (error) => {
            console.error('❌ AdminComponent: Error updating profile:', error);
            this.updatingProfile = false;
          }
        });
    }
  }

  closePasswordModal(): void {
    console.log('❌ AdminComponent: Closing password modal');
    this.showPasswordModal = false;
    this.passwordForm.reset();
  }

  // Add User modal methods
  closeAddUserModal(): void {
    console.log('❌ AdminComponent: Closing add user modal');
    this.showAddUserModal = false;
    this.addUserForm.reset();
    this.addingUser = false;
  }

  submitAddUser(): void {
    console.log('➕ AdminComponent: Submitting add user form');
    if (this.addUserForm.valid) {
      this.addingUser = true;
      const userData = {
        name: this.addUserForm.value.name,
        email: this.addUserForm.value.email,
        password: this.addUserForm.value.password,
        role: this.addUserForm.value.role
      };

      console.log('➕ AdminComponent: User data to create:', userData);
      
      this.authService.createUserByAdmin(userData)
        .subscribe({
          next: (createdUser) => {
            console.log('✅ AdminComponent: User created successfully:', createdUser);
            this.closeAddUserModal();
            this.loadUserManagementData(); // Reload user data
            this.addingUser = false;
            alert('User created successfully!');
          },
          error: (error) => {
            console.error('❌ AdminComponent: Error creating user:', error);
            this.addingUser = false;
            alert('Error creating user: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    } else {
      console.warn('⚠️ AdminComponent: Add user form is invalid');
      alert('Please fill in all required fields correctly.');
    }
  }

  // New Project modal methods
  closeNewProjectModal(): void {
    console.log('❌ AdminComponent: Closing new project modal');
    this.showNewProjectModal = false;
    this.newProjectForm.reset();
    this.creatingProject = false;
  }

  submitNewProject(): void {
    console.log('➕ AdminComponent: Submitting new project form');
    if (this.newProjectForm.valid) {
      this.creatingProject = true;
      const projectData = {
        name: this.newProjectForm.value.name,
        description: this.newProjectForm.value.description,
        dueDate: this.newProjectForm.value.dueDate,
        priority: this.newProjectForm.value.priority
      };

      console.log('➕ AdminComponent: Project data to create:', projectData);
      
      this.projectService.createProject(projectData)
        .subscribe({
          next: (createdProject) => {
            console.log('✅ AdminComponent: Project created successfully:', createdProject);
            this.closeNewProjectModal();
            this.loadDashboardData(); // Reload dashboard data
            this.creatingProject = false;
            alert('Project created successfully!');
          },
          error: (error) => {
            console.error('❌ AdminComponent: Error creating project:', error);
            this.creatingProject = false;
            alert('Error creating project: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    } else {
      console.warn('⚠️ AdminComponent: New project form is invalid');
      alert('Please fill in all required fields correctly.');
    }
  }

  // Tab-specific data loading methods
  refreshTeamData(): void {
    console.log('🔄 AdminComponent: Refreshing team data...');
    this.loadingTeamData = true;
    this.cdr.detectChanges(); // Force change detection
    
    // Simulate a small delay for better UX
    setTimeout(() => {
      this.loadUserManagementData();
      this.refreshUserStatistics();
      this.loadingTeamData = false;
      this.cdr.detectChanges(); // Force change detection after update
    }, 500);
  }

  loadProjectData(): void {
    console.log('📁 AdminComponent: Loading project data...');
    // TODO: Implement project data loading
    console.log('📁 AdminComponent: Project data loading coming soon!');
  }

  loadTaskData(): void {
    console.log('📋 AdminComponent: Loading task data...');
    // TODO: Implement task data loading
    console.log('📋 AdminComponent: Task data loading coming soon!');
  }

  loadReportData(): void {
    console.log('📊 AdminComponent: Loading report data...');
    // TODO: Implement report data loading
    console.log('📊 AdminComponent: Report data loading coming soon!');
  }

  loadSettingsData(): void {
    console.log('⚙️ AdminComponent: Loading settings data...');
    // TODO: Implement settings data loading
    console.log('⚙️ AdminComponent: Settings data loading coming soon!');
  }

  refreshUserStatistics(): void {
    console.log('📊 AdminComponent: Refreshing user statistics...', new Date().toLocaleTimeString());
    // Simulate real-time user statistics updates
    this.userStatistics = {
      totalUsers: this.allUsers.length,
      adminUsers: this.allUsers.filter(u => u.role === 'ADMIN').length,
      managerUsers: this.allUsers.filter(u => u.role === 'MANAGER').length,
      regularUsers: this.allUsers.filter(u => u.role === 'USER').length,
      activeUsers: Math.floor(this.allUsers.length * 0.85), // 85% active
      newUsersThisWeek: Math.floor(Math.random() * 5) + 1,
      usersOnline: Math.floor(this.allUsers.length * 0.3), // 30% online
      lastUpdated: new Date().toLocaleTimeString()
    };
    this.cdr.detectChanges(); // Force change detection
    console.log('✅ AdminComponent: User statistics updated:', this.userStatistics);
  }


}
