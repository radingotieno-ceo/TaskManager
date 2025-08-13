import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {
    return this.authService.currentUser.pipe(
      take(1),
      map(user => {
        if (!user) {
          this.router.navigate(['/login']);
          return false;
        }

        const requiredRoles = route.data['roles'] as string[];
        
        if (!requiredRoles || requiredRoles.length === 0) {
          return true; // No role requirements
        }

        const hasRequiredRole = requiredRoles.includes(user.role);
        
        if (!hasRequiredRole) {
          // Redirect to appropriate dashboard based on user role
          this.redirectToAppropriateDashboard(user.role);
          return false;
        }

        return true;
      })
    );
  }

  private redirectToAppropriateDashboard(userRole: string): void {
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
        this.router.navigate(['/dashboard']);
    }
  }
}
