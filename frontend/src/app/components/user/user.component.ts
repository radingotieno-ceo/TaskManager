import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { ProjectService } from '../../services/project.service';
import { User } from '../../models/user.model';
import { Task } from '../../models/task.model';
import { Project } from '../../models/project.model';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  loading = false;
  sidebarCollapsed = false;
  searchQuery = '';
  hasNotifications = true;
  activeTab = 'dashboard';

  // Dashboard data
  myTasks: Task[] = [];
  myProjects: Project[] = [];
  taskStats: any = {
    total: 0,
    completed: 0,
    inProgress: 0,
    todo: 0
  };

  // Settings properties
  showProfileEditModal = false;
  showPasswordModal = false;
  updatingProfile = false;
  changingPassword = false;
  uploadingPhoto = false;
  
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  
  preferences = {
    emailNotifications: true,
    taskReminders: true,
    theme: 'light'
  };

  selectedPhotoPreview: string | null = null;
  showPhotoPreviewModal = false;
  selectedPhotoFile: File | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private taskService: TaskService,
    private projectService: ProjectService,
    private router: Router,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.initializeForms();
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
  }

  private passwordMatchValidator(form: FormGroup): { [key: string]: any } | null {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  ngOnInit(): void {
    this.loading = true;
    
    // Test authentication
    console.log('🔍 UserComponent: Testing authentication...');
    const token = this.authService.getToken();
    console.log('🔍 UserComponent: Token available:', !!token);
    
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: User | null) => {
        console.log('🔍 UserComponent: Current user received:', user);
        this.currentUser = user;
        
        // Redirect if user is not a regular user
        if (user && user.role !== 'USER') {
          console.log('🔄 UserComponent: Redirecting non-user to appropriate dashboard');
          this.redirectToAppropriateDashboard(user.role);
        } else if (user) {
          console.log('✅ UserComponent: Loading dashboard for user:', user.email);
          this.loadDashboardData();
        } else {
          console.warn('⚠️ UserComponent: No user data available');
        }
        
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    console.log('🔍 UserComponent: Loading dashboard data for user:', this.currentUser);
    
    if (this.currentUser?.id) {
      // Load assigned tasks
      console.log('🔍 UserComponent: Loading assigned tasks...');
      this.taskService.getAssignedTasks()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (tasks) => {
            console.log('✅ UserComponent: Received tasks from server:', tasks);
            this.myTasks = tasks;
            this.calculateTaskStats();
            console.log('✅ UserComponent: Task stats calculated:', this.taskStats);
          },
          error: (error) => {
            console.error('❌ UserComponent: Error loading assigned tasks:', error);
          }
        });

      // Load projects that the user is involved in (projects where user has tasks)
      console.log('🔍 UserComponent: Loading user projects...');
      this.taskService.getAssignedTasks()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (tasks) => {
            // Get unique projects from user's tasks
            const projectIds = [...new Set(tasks.map(task => task.project?.id).filter(id => id))];
            console.log('🔍 UserComponent: User is involved in projects:', projectIds);
            
            if (projectIds.length > 0) {
              // Load project details for each project the user is involved in
              this.projectService.getAllProjects()
                .pipe(takeUntil(this.destroy$))
                .subscribe({
                  next: (allProjects) => {
                    this.myProjects = allProjects.filter(project => projectIds.includes(project.id));
                    console.log('✅ UserComponent: Loaded user projects:', this.myProjects);
                  },
                  error: (error) => {
                    console.error('❌ UserComponent: Error loading projects:', error);
                  }
                });
            } else {
              this.myProjects = [];
              console.log('✅ UserComponent: No projects found for user');
            }
          },
          error: (error) => {
            console.error('❌ UserComponent: Error getting user tasks for projects:', error);
          }
        });
    } else {
      console.warn('⚠️ UserComponent: No current user ID available');
    }
  }

  private calculateTaskStats(): void {
    this.taskStats.total = this.myTasks.length;
    this.taskStats.completed = this.myTasks.filter(task => task.status === 'DONE').length;
    this.taskStats.inProgress = this.myTasks.filter(task => task.status === 'IN_PROGRESS').length;
    this.taskStats.todo = this.myTasks.filter(task => task.status === 'TODO').length;
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    
    // Refresh data when switching to tasks tab to ensure latest data
    if (tab === 'tasks' && this.currentUser?.id) {
      console.log('🔄 UserComponent: Refreshing data for tasks tab');
      this.loadDashboardData();
    }
  }

  onSearch(): void {
    // Implement search functionality
    console.log('Searching for:', this.searchQuery);
  }

  showNotifications(): void {
    // Implement notifications functionality
    console.log('Showing notifications');
  }

  showMoreOptions(): void {
    // Implement more options menu
    console.log('Showing more options');
  }

  viewProjectDetails(): void {
    // Navigate to project details page
    console.log('Viewing project details');
  }

  // Task management functions
  updateTaskStatus(taskId: number, newStatus: string): void {
    this.taskService.updateTaskStatus(taskId, newStatus)
      .subscribe({
        next: (task) => {
          console.log('Task status updated successfully:', task);
          this.loadDashboardData(); // Reload data to reflect changes
        },
        error: (error) => {
          console.error('Error updating task status:', error);
        }
      });
  }

  viewTaskDetails(taskId: number): void {
    // Navigate to task details page
    console.log('Viewing task details:', taskId);
  }

  markTaskAsComplete(taskId: number): void {
    this.updateTaskStatus(taskId, 'DONE');
  }

  startTask(taskId: number): void {
    this.updateTaskStatus(taskId, 'IN_PROGRESS');
  }

  // Helper methods for template
  getTaskProgress(task: Task): number {
    // Calculate progress based on status
    switch (task.status) {
      case 'DONE':
        return 100;
      case 'IN_PROGRESS':
        return 50;
      case 'TODO':
        return 0;
      default:
        return 0;
    }
  }

  getProjectRole(project: Project): string {
    // Check if user has tasks in this project
    const userTasksInProject = this.myTasks.filter(task => task.project?.id === project.id);
    if (userTasksInProject.length > 0) {
      return 'Member';
    }
    return 'Not Assigned';
  }

  getProjectTaskCount(project: Project): number {
    // Count tasks assigned to user in this project
    return this.myTasks.filter(task => task.project?.id === project.id).length;
  }

  private redirectToAppropriateDashboard(userRole: string): void {
    switch (userRole) {
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'MANAGER':
        this.router.navigate(['/manager']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }

  // Settings Methods
  getProfilePhotoUrl(photoUrl: string | undefined): string {
    if (!photoUrl) {
      return '';
    }
    // Add timestamp to prevent caching issues
    const timestamp = new Date().getTime();
    return `http://localhost:8080${photoUrl}?t=${timestamp}`;
  }

  onImageError(event: any): void {
    console.warn('⚠️ UserComponent: Profile image failed to load, using placeholder');
    // Hide the image and show placeholder
    event.target.style.display = 'none';
    const placeholder = event.target.parentElement?.querySelector('.profile-placeholder');
    if (placeholder) {
      placeholder.style.display = 'flex';
    }
  }

  onImageLoad(event: any): void {
    console.log('✅ UserComponent: Profile image loaded successfully');
    // Hide placeholder when image loads
    const placeholder = event.target.parentElement?.querySelector('.profile-placeholder');
    if (placeholder) {
      placeholder.style.display = 'none';
    }
  }

  onFileSelected(event: any): void {
    console.log('🔍 UserComponent: File selected event:', event);
    const file = event.target.files[0];
    console.log('🔍 UserComponent: Selected file:', file);
    if (file) {
      console.log('🔍 UserComponent: File details - name:', file.name, 'size:', file.size, 'type:', file.type);
      // Show preview first
      this.showPhotoPreview(file);
    } else {
      console.warn('⚠️ UserComponent: No file selected');
    }
  }

  showPhotoPreview(file: File): void {
    // Store the selected file
    this.selectedPhotoFile = file;
    // Create a preview URL for the selected file
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.selectedPhotoPreview = e.target.result;
      this.showPhotoPreviewModal = true;
    };
    reader.readAsDataURL(file);
  }

  confirmPhotoUpload(): void {
    if (this.selectedPhotoFile) {
      this.uploadProfilePhoto(this.selectedPhotoFile);
      this.closePhotoPreviewModal();
    }
  }

  closePhotoPreviewModal(): void {
    this.showPhotoPreviewModal = false;
    this.selectedPhotoPreview = null;
    this.selectedPhotoFile = null;
    // Reset file input
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  uploadProfilePhoto(file: File): void {
    console.log('🔍 UserComponent: Starting photo upload for file:', file.name);
    this.uploadingPhoto = true;
    this.authService.uploadProfilePhoto(file)
      .subscribe({
        next: (response) => {
          console.log('✅ UserComponent: Profile photo uploaded successfully:', response);
          if (this.currentUser) {
            // Update the current user with the new photo URL
            this.currentUser.profilePhotoUrl = response.data;
            this.authService.updateCurrentUser(this.currentUser);
            console.log('✅ UserComponent: Updated current user with new photo URL:', response.data);
            
            // Force a refresh of the profile photo display
            setTimeout(() => {
              console.log('🔄 UserComponent: Refreshing profile photo display');
              // Trigger change detection
              this.cdr.detectChanges();
            }, 100);
          }
          this.uploadingPhoto = false;
        },
        error: (error) => {
          console.error('❌ UserComponent: Error uploading profile photo:', error);
          console.error('❌ UserComponent: Error details:', error.error, error.status, error.statusText);
          this.uploadingPhoto = false;
        }
      });
  }

  removeProfilePhoto(): void {
    if (this.currentUser) {
      this.currentUser.profilePhotoUrl = undefined;
      this.authService.updateCurrentUser(this.currentUser);
    }
  }

  editProfile(): void {
    if (this.currentUser) {
      this.profileForm.patchValue({
        name: this.currentUser.name,
        email: this.currentUser.email
      });
      this.showProfileEditModal = true;
    }
  }

  closeProfileEditModal(): void {
    this.showProfileEditModal = false;
    this.profileForm.reset();
  }

  updateProfile(): void {
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
            console.log('Profile updated successfully:', updatedUser);
            this.currentUser = updatedUser;
            this.authService.updateCurrentUser(updatedUser);
            this.closeProfileEditModal();
            this.updatingProfile = false;
          },
          error: (error) => {
            console.error('Error updating profile:', error);
            this.updatingProfile = false;
          }
        });
    }
  }

  showChangePasswordModal(): void {
    this.passwordForm.reset();
    this.showPasswordModal = true;
  }

  closePasswordModal(): void {
    this.showPasswordModal = false;
    this.passwordForm.reset();
  }

  changePassword(): void {
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
            console.log('Password changed successfully:', response);
            this.closePasswordModal();
            this.changingPassword = false;
          },
          error: (error) => {
            console.error('Error changing password:', error);
            this.changingPassword = false;
          }
        });
    }
  }

  showTwoFactorAuth(): void {
    console.log('Two-factor authentication coming soon...');
  }
}
