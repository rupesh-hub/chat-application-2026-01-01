import {Routes} from '@angular/router';

export const authRoutes: Routes = [
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import("./auth/login/login.component").then((c) => c.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import("./auth/register/register.component").then((c) => c.RegisterComponent)
  }
]
