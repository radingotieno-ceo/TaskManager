import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { LoginComponent } from './components/auth/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  
  // Role-based dashboard routes - redirect to role-specific dashboards
  { 
    path: 'dashboard', 
    canActivate: [AuthGuard],
    resolve: {
      redirect: () => {
        // This will be handled by the component
        return null;
      }
    },
    component: DashboardComponent
  },
  
  // Admin-specific routes
  { 
    path: 'admin', 
    loadChildren: () => import('./components/admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ADMIN'] }
  },
  
  // Manager-specific routes
  { 
    path: 'manager', 
    loadChildren: () => import('./components/manager/manager.module').then(m => m.ManagerModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['MANAGER'] }
  },
  
  // User-specific routes
  { 
    path: 'user', 
    loadChildren: () => import('./components/user/user.module').then(m => m.UserModule),
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['USER'] }
  },
  
  // Projects routes
  { 
    path: 'projects', 
    loadChildren: () => import('./components/projects/projects.module').then(m => m.ProjectsModule),
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN', 'MANAGER'] }
  },
  
  // Tasks routes
  { 
    path: 'tasks', 
    loadChildren: () => import('./components/tasks/tasks.module').then(m => m.TasksModule),
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN', 'MANAGER', 'USER'] }
  },
  
  // Reports routes (Admin and Manager only) - TODO: Implement
  // { 
  //   path: 'reports', 
  //   loadChildren: () => import('./components/reports/reports.module').then(m => m.ReportsModule),
  //   canActivate: [AuthGuard, RoleGuard],
  //   data: { roles: ['ADMIN', 'MANAGER'] }
  // },
  
  // Team management routes (Admin only) - TODO: Implement
  // { 
  //   path: 'team', 
  //   loadChildren: () => import('./components/team/team.module').then(m => m.TeamModule),
  //   canActivate: [AuthGuard, RoleGuard],
  //   data: { roles: ['ADMIN'] }
  // },
  
  // Settings routes - TODO: Implement
  // { 
  //   path: 'settings', 
  //   loadChildren: () => import('./components/settings/settings.module').then(m => m.SettingsModule),
  //   canActivate: [AuthGuard],
  //   data: { roles: ['ADMIN', 'MANAGER', 'USER'] }
  // },
  
  // Catch all route
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }


