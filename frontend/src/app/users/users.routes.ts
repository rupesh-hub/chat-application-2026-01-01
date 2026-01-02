import {Routes} from '@angular/router';

export const usersRoutes: Routes = [
  {
    path: '',
    redirectTo: '/users/profile',
    pathMatch: 'full'
  },
  {
    path: 'change-password',
    loadComponent: () => import('./change-password/change-password.component').then(m => m.ChangePasswordComponent)
  },
  {
    path: 'profile',
    loadComponent: () => import('./profile/profile.component').then(m => m.ProfileComponent)
  }
];
