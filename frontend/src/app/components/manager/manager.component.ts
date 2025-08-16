import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { UserService } from '../../services/user.service';
import { ProjectService } from '../../services/project.service';
import { User } from '../../models/user.model';
import { Task } from '../../models/task.model';
import { Project } from '../../models/project.model';

@Component({
  selector: 'app-manager',
  templateUrl: './manager.component.html',
  styleUrls: ['./manager.component.scss']
})
export class ManagerComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  loading = false;
  sidebarCollapsed = false;
  searchQuery = '';
  hasNotifications = true;
  activeTab = 'dashboard';

  // Dashboard data
  summaryCards: any = {
    totalProjects: 0,
    activeTasks: 0,
    completedTasks: 0,
    overdueTasks: 0
  };
  recentProjects: Project[] = [];
  recentTasks: Task[] = [];
  availableUsers: User[] = [];

  // Task assignment data
  unassignedTasks: Task[] = [];
  selectedTask: Task | null = null;
  selectedUser: User | null = null;
  showTaskAssignmentModal = false;
  showCreateTaskModal = false;

  // Create task form data
  newTask = {
    title: '',
    description: '',
    priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL',
    dueDate: '',
    projectId: null as number | null
  };

  // Form data
  newProjectForm = {
    name: '',
    description: '',
    priority: 'MEDIUM',
    dueDate: ''
  };

  newTaskForm = {
    title: '',
    description: '',
    dueDate: '',
    projectId: null as number | null,
    userId: null as number | null,
    priority: 'MEDIUM'
  };

  // Available projects for task creation
  availableProjects: Project[] = [];
  
  // Modal states
  showCreateProjectModal = false;
  showCreateAndAssignTaskModal = false;

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

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private taskService: TaskService,
    private userService: UserService,
    private projectService: ProjectService,
    private router: Router,
    private fb: FormBuilder
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
    console.log('🔍 ManagerComponent: Testing authentication...');
    const token = this.authService.getToken();
    console.log('🔍 ManagerComponent: Token available:', !!token);
    
    // Check if user is logged in and has appropriate role
    this.authService.currentUser
      .pipe(takeUntil(this.destroy$))
      .subscribe((user: User | null) => {
        console.log('🔍 ManagerComponent: Current user received:', user);
        if (user) {
          this.currentUser = user;
          
          // Redirect if user is not a manager or admin
          if (user.role !== 'MANAGER' && user.role !== 'ADMIN') {
            console.log('🔄 ManagerComponent: Redirecting non-manager to appropriate dashboard');
            this.redirectToAppropriateDashboard(user.role);
            return;
          }
          
          console.log('✅ ManagerComponent: Loading dashboard for manager:', user.email);
          // Load dashboard data
          this.loadDashboardData();
          
          // Load projects and users for task assignment
          this.loadProjects();
          this.loadTaskAssignmentData();
        } else {
          console.warn('⚠️ ManagerComponent: No user data available');
        }
        
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    console.log('🔍 ManagerComponent: Loading dashboard data for manager:', this.currentUser);
    
    // Load summary statistics
    this.loadSummaryStatistics();
    
    // Load recent projects
    console.log('🔍 ManagerComponent: Loading recent projects...');
    this.projectService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          console.log('✅ ManagerComponent: Received projects from server:', projects);
          this.recentProjects = projects.slice(0, 5); // Get first 5 projects
          console.log('✅ ManagerComponent: Set recent projects:', this.recentProjects);
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error loading projects:', error);
        }
      });

    // Load recent tasks
    console.log('🔍 ManagerComponent: Loading recent tasks...');
    this.taskService.getAllTasks()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          console.log('✅ ManagerComponent: Received tasks from server:', tasks);
          this.recentTasks = tasks.slice(0, 5); // Get first 5 tasks
          console.log('✅ ManagerComponent: Set recent tasks:', this.recentTasks);
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error loading tasks:', error);
        }
      });
  }

  private loadSummaryStatistics(): void {
    console.log('🔍 ManagerComponent: Loading summary statistics...');
    
    // Load all tasks to calculate statistics
    this.taskService.getAllTasks()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          console.log('✅ ManagerComponent: Calculating statistics from tasks:', tasks.length);
          
          // Calculate statistics
          this.summaryCards.activeTasks = tasks.filter(t => t.status === 'IN_PROGRESS').length;
          this.summaryCards.completedTasks = tasks.filter(t => t.status === 'DONE').length;
          this.summaryCards.overdueTasks = tasks.filter(t => 
            new Date(t.dueDate) < new Date() && t.status !== 'DONE'
          ).length;
          
          // Get total projects from the projects array (will be updated when projects load)
          this.projectService.getAllProjects()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (projects) => {
                this.summaryCards.totalProjects = projects.length;
                console.log('✅ ManagerComponent: Summary statistics calculated:', this.summaryCards);
              },
              error: (error) => {
                console.error('❌ ManagerComponent: Error loading projects for statistics:', error);
              }
            });
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error loading task statistics:', error);
        }
      });
  }

  private loadTaskAssignmentData(): void {
    console.log('🔍 ManagerComponent: Loading task assignment data...');
    
    // Load unassigned tasks
    this.taskService.getUnassignedTasks()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          console.log('✅ ManagerComponent: Received unassigned tasks:', tasks);
          this.unassignedTasks = tasks;
          console.log('✅ ManagerComponent: Set unassigned tasks:', this.unassignedTasks.length);
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error loading unassigned tasks:', error);
        }
      });

    // Load available users - using the correct service method
    console.log('🔍 ManagerComponent: Starting to load users...');
    this.userService.getAvailableUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users) => {
          console.log('🔍 ManagerComponent: Received users from service:', users);
          console.log('🔍 ManagerComponent: Users before filtering:', users);
          
          // Filter for USER role only
          this.availableUsers = users.filter(user => user.role === 'USER');
          
          console.log('🔍 ManagerComponent: Users after filtering (USER role):', this.availableUsers);
          console.log('🔍 ManagerComponent: availableUsers array length:', this.availableUsers.length);
          
          // Check each user's role
          users.forEach((user, index) => {
            console.log(`🔍 User ${index + 1}:`, user.name, '- Role:', user.role, '- Type:', typeof user.role);
          });
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error loading available users:', error);
        }
      });
  }

  private loadProjects(): void {
    console.log('🔍 ManagerComponent: Loading available projects...');
    this.projectService.getAllProjects()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (projects) => {
          console.log('✅ ManagerComponent: Received projects for task creation:', projects);
          this.availableProjects = projects;
          console.log('✅ ManagerComponent: Set available projects:', this.availableProjects.length);
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error loading projects:', error);
        }
      });
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  setActiveTab(tab: string): void {
    this.activeTab = tab;
    
    // Refresh data when switching to dashboard tab to ensure latest data
    if (tab === 'dashboard' && this.currentUser?.id) {
      console.log('🔄 ManagerComponent: Refreshing data for dashboard tab');
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

  newProject(): void {
    // Show create project modal
    this.showCreateProjectModal = true;
    this.resetNewProjectForm();
  }

  assignTask(): void {
    // Show task assignment modal
    this.showTaskAssignmentModal = true;
  }

  // New method for creating and assigning tasks
  createAndAssignTask(): void {
    // Show create and assign task modal
    this.showCreateAndAssignTaskModal = true;
    this.resetNewTaskForm();
    
    // Load projects and users for the form
    this.loadProjects();
    this.loadTaskAssignmentData(); // This loads available users
    
    // Debug: Check if data is loaded
    setTimeout(() => {
      console.log('=== DEBUG: Data Check ===');
      console.log('Available projects:', this.availableProjects);
      console.log('Available users:', this.availableUsers);
      console.log('Projects length:', this.availableProjects?.length);
      console.log('Users length:', this.availableUsers?.length);
    }, 1000);
  }

  // Task assignment functions
  openTaskAssignmentModal(task: Task): void {
    this.selectedTask = task;
    this.selectedUser = null;
    this.showTaskAssignmentModal = true;
  }

  closeTaskAssignmentModal(): void {
    this.showTaskAssignmentModal = false;
    this.selectedTask = null;
    this.selectedUser = null;
  }

  selectUserForAssignment(user: User): void {
    this.selectedUser = user;
  }

  assignTaskToUser(): void {
    if (this.selectedTask && this.selectedUser) {
      this.taskService.assignTaskToUser(this.selectedTask.id, this.selectedUser.id)
        .subscribe({
          next: (response) => {
            console.log('Task assigned successfully');
            this.closeTaskAssignmentModal();
            this.loadTaskAssignmentData(); // Reload data
            this.loadDashboardData(); // Reload dashboard data
          },
          error: (error) => {
            console.error('Error assigning task:', error);
          }
        });
    }
  }

  // Create project functions
  showCreateProjectForm(): void {
    this.showCreateProjectModal = true;
    this.resetNewProjectForm();
  }

  closeCreateProjectModal(): void {
    this.showCreateProjectModal = false;
    this.resetNewProjectForm();
  }

  resetNewProjectForm(): void {
    this.newProjectForm = {
      name: '',
      description: '',
      priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH',
      dueDate: ''
    };
  }

  createProject(): void {
    if (this.newProjectForm.name && this.newProjectForm.dueDate) {
      const createProjectRequest = {
        name: this.newProjectForm.name,
        description: this.newProjectForm.description,
        priority: this.newProjectForm.priority as 'LOW' | 'MEDIUM' | 'HIGH',
        dueDate: this.newProjectForm.dueDate
      };

      this.projectService.createProject(createProjectRequest)
        .subscribe({
          next: (project) => {
            console.log('Project created successfully:', project);
            alert('Project created successfully!');
            this.closeCreateProjectModal();
            this.loadDashboardData(); // Reload data
            this.loadProjects(); // Reload projects list
          },
          error: (error) => {
            console.error('Error creating project:', error);
            alert('Error creating project: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    }
  }

  // Create task functions
  showCreateTaskForm(): void {
    this.showCreateTaskModal = true;
    this.resetNewTaskForm();
  }

  closeCreateTaskModal(): void {
    this.showCreateTaskModal = false;
    this.resetNewTaskForm();
  }



  createTask(): void {
    if (this.newTaskForm.title && this.newTaskForm.dueDate && this.newTaskForm.projectId && this.newTaskForm.projectId > 0) {
      const createTaskRequest = {
        title: this.newTaskForm.title,
        description: this.newTaskForm.description,
        dueDate: this.newTaskForm.dueDate,
        projectId: this.newTaskForm.projectId
      };

      this.taskService.createTask(createTaskRequest)
        .subscribe({
          next: (task) => {
            console.log('Task created successfully:', task);
            alert('Task created successfully!');
            this.closeCreateTaskModal();
            this.loadTaskAssignmentData(); // Reload data
            this.loadDashboardData(); // Reload dashboard data
          },
          error: (error) => {
            console.error('Error creating task:', error);
            alert('Error creating task: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    }
  }

  unassignTask(taskId: number): void {
    if (confirm('Are you sure you want to unassign this task?')) {
      this.taskService.unassignTask(taskId)
        .subscribe({
          next: (response) => {
            console.log('Task unassigned successfully');
            this.loadTaskAssignmentData(); // Reload data
            this.loadDashboardData(); // Reload dashboard data
          },
          error: (error) => {
            console.error('Error unassigning task:', error);
          }
        });
    }
  }

  bulkAssignTasks(taskIds: number[], userId: number): void {
    this.taskService.bulkAssignTasks(taskIds, userId)
      .subscribe({
        next: (response) => {
          console.log('Tasks assigned successfully');
          this.loadTaskAssignmentData(); // Reload data
          this.loadDashboardData(); // Reload dashboard data
        },
        error: (error) => {
          console.error('Error bulk assigning tasks:', error);
        }
      });
  }

  viewUserTasks(userId: number): void {
    this.taskService.getTasksAssignedToUser(userId)
      .subscribe({
        next: (tasks) => {
          console.log('Tasks assigned to user:', tasks);
          // You can implement a modal or navigate to a detailed view
        },
        error: (error) => {
          console.error('Error loading user tasks:', error);
        }
      });
  }

  viewProjectTasks(projectId: number): void {
    this.taskService.getTasksByProject(projectId)
      .subscribe({
        next: (tasks) => {
          console.log('Tasks in project:', tasks);
          // You can implement a modal or navigate to a detailed view
        },
        error: (error) => {
          console.error('Error loading project tasks:', error);
        }
      });
  }

  viewProjectDetails(project: Project): void {
    // Navigate to project details page
    console.log('Viewing project details:', project);
  }

  viewTaskDetails(task: Task): void {
    // Navigate to task details page
    console.log('Viewing task details:', task);
  }

  // Settings Methods
  getProfilePhotoUrl(photoUrl: string | undefined): string {
    if (!photoUrl) {
      return '';
    }
    return `http://localhost:8080${photoUrl}`;
  }

  onFileSelected(event: any): void {
    console.log('🔍 ManagerComponent: File selected event:', event);
    const file = event.target.files[0];
    console.log('🔍 ManagerComponent: Selected file:', file);
    if (file) {
      console.log('🔍 ManagerComponent: File details - name:', file.name, 'size:', file.size, 'type:', file.type);
      this.uploadProfilePhoto(file);
    } else {
      console.warn('⚠️ ManagerComponent: No file selected');
    }
  }

  uploadProfilePhoto(file: File): void {
    console.log('🔍 ManagerComponent: Starting photo upload for file:', file.name);
    this.uploadingPhoto = true;
    this.authService.uploadProfilePhoto(file)
      .subscribe({
        next: (response) => {
          console.log('✅ ManagerComponent: Profile photo uploaded successfully:', response);
          if (this.currentUser) {
            this.currentUser.profilePhotoUrl = response.data;
            this.authService.updateCurrentUser(this.currentUser);
            console.log('✅ ManagerComponent: Updated current user with new photo URL:', response.data);
          }
          this.uploadingPhoto = false;
        },
        error: (error) => {
          console.error('❌ ManagerComponent: Error uploading profile photo:', error);
          console.error('❌ ManagerComponent: Error details:', error.error, error.status, error.statusText);
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

  // Helper method for template
  getProjectTeamSize(project: Project): number {
    // For now, return a default team size since we don't have team size in the model
    return 3; // Default team size
  }

  private redirectToAppropriateDashboard(userRole: string): void {
    switch (userRole) {
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'USER':
        this.router.navigate(['/user']);
        break;
      default:
        this.router.navigate(['/dashboard']);
    }
  }

  resetNewTaskForm(): void {
    this.newTaskForm = {
      title: '',
      description: '',
      dueDate: '',
      projectId: null,
      userId: null,
      priority: 'MEDIUM'
    };
  }

  closeCreateAndAssignTaskModal(): void {
    this.showCreateAndAssignTaskModal = false;
    this.resetNewTaskForm();
  }

  submitCreateAndAssignTask(): void {
    if (this.newTaskForm.title && this.newTaskForm.dueDate && this.newTaskForm.projectId && this.newTaskForm.userId) {
      const taskData = {
        title: this.newTaskForm.title,
        description: this.newTaskForm.description,
        dueDate: this.newTaskForm.dueDate,
        projectId: this.newTaskForm.projectId,
        userId: this.newTaskForm.userId,
        priority: this.newTaskForm.priority
      };

      this.taskService.createAndAssignTask(taskData)
        .subscribe({
          next: (task) => {
            console.log('Task created and assigned successfully:', task);
            alert('Task created and assigned successfully!');
            this.closeCreateAndAssignTaskModal();
            this.loadDashboardData(); // Reload data
            this.loadTaskAssignmentData(); // Reload task data
          },
          error: (error) => {
            console.error('Error creating and assigning task:', error);
            alert('Error creating and assigning task: ' + (error.error?.message || error.message || 'Unknown error'));
          }
        });
    } else {
      alert('Please fill in all required fields: Title, Due Date, Project, and User');
    }
  }

  // Test method to manually check user loading
  testUserLoading(): void {
    console.log('=== TESTING USER LOADING ===');
    console.log('🔍 Current token:', this.authService.getToken());
    
    this.userService.getAvailableUsers()
      .subscribe({
        next: (users) => {
          console.log('✅ Users loaded successfully:', users);
          console.log('Users with USER role:', users.filter(u => u.role === 'USER'));
          
          // Test role comparison
          users.forEach(user => {
            console.log(`Testing user: ${user.name}`);
            console.log(`  Role: "${user.role}" (type: ${typeof user.role})`);
            console.log(`  user.role === 'USER': ${user.role === 'USER'}`);
            console.log(`  user.role === "USER": ${user.role === "USER"}`);
            console.log(`  user.role.toLowerCase() === 'user': ${user.role.toLowerCase() === 'user'}`);
          });
          
          // Test filtering
          const userRoleUsers = users.filter(u => u.role === 'USER');
          const userRoleUsers2 = users.filter(u => u.role === "USER");
          const userRoleUsers3 = users.filter(u => u.role.toLowerCase() === 'user');
          
          console.log('Filter results:');
          console.log('  u.role === \'USER\':', userRoleUsers);
          console.log('  u.role === "USER":', userRoleUsers2);
          console.log('  u.role.toLowerCase() === \'user\':', userRoleUsers3);
        },
        error: (error) => {
          console.error('❌ Error loading users:', error);
        }
      });
  }
}
