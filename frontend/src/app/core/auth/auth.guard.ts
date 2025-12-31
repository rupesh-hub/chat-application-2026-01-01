import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';
import {map, take} from 'rxjs/operators';
import {AuthService} from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isAuthenticated$.pipe(
    take(1), // Take the latest value and complete
    map(isAuth => {
      if (isAuth) return true;
      else return router.createUrlTree(['/auth/login']);
    })
  );
};
