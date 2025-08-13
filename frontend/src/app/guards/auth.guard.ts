import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private router: Router, private authService: AuthService) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const currentUser = this.authService.currentUserValue;
    
    if (currentUser && this.authService.isAuthenticated()) {
      // Check if route has role requirements
      if (route.data['roles'] && route.data['roles'].length > 0) {
        const userHasRole = this.authService.hasAnyRole(route.data['roles']);
        if (!userHasRole) {
          this.router.navigate(['/dashboard']);
          return false;
        }
      }
      return true;
    }

    // Not logged in, redirect to login page
    this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
}


