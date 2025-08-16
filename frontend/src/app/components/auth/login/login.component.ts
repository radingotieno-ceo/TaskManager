import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';
import { LoginRequest, RegisterRequest } from '../../../models/user.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  isLoginMode = true;

  // Data objects for forms
  loginData: LoginRequest = {
    email: '',
    password: ''
  };

  registerData: RegisterRequest = {
    name: '',
    email: '',
    password: '',
    role: 'USER' // Default role
  };

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initForm();
    // Ensure login form is visible by default
    this.isLoginMode = true;
    this.showLoginForm();
    console.log('Login component initialized'); // Debug log
  }

  initForm(): void {
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  // Form switching functionality - SIMPLE SHOW/HIDE
  switchToLogin(): void {
    this.isLoginMode = true;
    this.showLoginForm();
    console.log('Switched to login mode');
  }

  switchToRegister(): void {
    this.isLoginMode = false;
    this.showRegisterForm();
    console.log('Switched to register mode');
  }

  // Helper methods to show specific forms
  private showLoginForm(): void {
    const aContainer = document.querySelector("#a-container") as HTMLElement;
    const bContainer = document.querySelector("#b-container") as HTMLElement;
    if (aContainer && bContainer) {
      aContainer.style.display = "none";
      bContainer.style.display = "flex";
    }
  }

  private showRegisterForm(): void {
    const aContainer = document.querySelector("#a-container") as HTMLElement;
    const bContainer = document.querySelector("#b-container") as HTMLElement;
    if (aContainer && bContainer) {
      aContainer.style.display = "flex";
      bContainer.style.display = "none";
    }
  }

  // Ensure correct form is visible and prevent wrong form submission
  ensureLoginFormVisible(): void {
    this.isLoginMode = true;
    // Force the login form to be visible and prevent animation interference
    const aContainer = document.querySelector("#a-container") as HTMLElement;
    const bContainer = document.querySelector("#b-container") as HTMLElement;
    if (aContainer && bContainer) {
      // Remove any animation classes that might interfere
      aContainer.classList.remove("is-txl");
      bContainer.classList.remove("is-txl");
      bContainer.classList.remove("is-z200");
    }
  }

  private animateSwitch(): void {
    // ANIMATION DISABLED - This method is now a no-op
    console.log('Animation disabled - forms remain visible');
    return;
    
    // Original animation code commented out:
    /*
    const switchCtn = document.querySelector("#switch-cnt") as HTMLElement;
    const switchC1 = document.querySelector("#switch-c1") as HTMLElement;
    const switchC2 = document.querySelector("#switch-c2") as HTMLElement;
    const switchCircle = document.querySelectorAll(".switch__circle");
    const aContainer = document.querySelector("#a-container") as HTMLElement;
    const bContainer = document.querySelector("#b-container") as HTMLElement;

    if (switchCtn) {
      switchCtn.classList.add("is-gx");
      setTimeout(() => {
        switchCtn.classList.remove("is-gx");
      }, 1500);

      switchCtn.classList.toggle("is-txr");
      switchCtn.classList.toggle("is-txl");

      switchCircle.forEach(circle => {
        circle.classList.toggle("is-txr");
      });

      switchC1.classList.toggle("is-hidden");
      switchC2.classList.toggle("is-hidden");
      aContainer.classList.toggle("is-txl");
      bContainer.classList.toggle("is-txl");
      bContainer.classList.toggle("is-z200");
    }
    */
  }

  // Login functionality - simplified
  onLogin(): void {
    console.log('Login method called'); // Debug log
    console.log('loginData object:', this.loginData); // Debug log
    console.log('Email:', this.loginData.email); // Debug log
    console.log('Password:', this.loginData.password); // Debug log
    
    if (!this.loginData.email || !this.loginData.password) {
      console.log('Validation failed - missing fields'); // Debug log
      this.showMessage('Please fill in all fields', 'error');
      return;
    }

    this.loading = true;
    console.log('Attempting login with:', this.loginData); // Debug log
    
    // Force login mode to be true
    this.isLoginMode = true;
    
    this.authService.login(this.loginData).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Login successful:', response); // Debug log
        this.showMessage('Login successful!', 'success');
        // Navigation is now handled by AuthService
      },
      error: (error) => {
        this.loading = false;
        console.error('Login error:', error); // Debug log
        this.showMessage(error.error?.message || 'Login failed', 'error');
      }
    });
  }

  // Registration functionality - simplified
  onSubmit(): void {
    console.log('Register method called'); // Debug log
    
    if (!this.registerData.name || !this.registerData.email || !this.registerData.password || !this.registerData.role) {
      this.showMessage('Please fill in all fields', 'error');
      return;
    }

    this.loading = true;
    console.log('Attempting registration with:', this.registerData); // Debug log
    
    // Force register mode to be false
    this.isLoginMode = false;
    
    this.authService.register(this.registerData).subscribe({
      next: (response) => {
        this.loading = false;
        this.showMessage('Registration successful!', 'success');
        // Navigation is now handled by AuthService
      },
      error: (error) => {
        this.loading = false;
        console.error('Registration error:', error); // Debug log
        this.showMessage(error.error?.message || 'Registration failed', 'error');
      }
    });
  }

  // Forgot password functionality
  forgotPassword(): void {
    this.showMessage('Password reset functionality coming soon!', 'info');
  }

  // Helper method to show messages
  private showMessage(message: string, type: 'success' | 'error' | 'info'): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: `${type}-snackbar`
    });
  }

  // Getter for form controls
  get f() {
    return this.loginForm.controls;
  }
}


