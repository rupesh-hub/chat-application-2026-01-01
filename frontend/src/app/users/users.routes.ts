import {Routes} from '@angular/router';

export const usersRoutes: Routes = [
  {
    path: '',
    redirectTo: '/users/profile',
    pathMatch: 'full'
  },
  {
    path: 'profile',
    loadComponent: () => import('./profile/profile.component').then(m => m.ProfileComponent)
  }
];
