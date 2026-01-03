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
    path: 'forgot-password',
    loadComponent: () => import('./auth/forgot-password/forgot-password.component').then((c) => c.ForgotPasswordComponent)
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./auth/reset-password/reset-password.component').then((c) => c.ResetPasswordComponent)
  },
  {
    path: 'confirm-email/:email',
    loadComponent: () => import('./auth/otp-validator/otp-validator.component').then((c) => c.OtpValidatorComponent)
  },
  {
    path: 'register',
    loadComponent: () => import("./auth/register/register.component").then((c) => c.RegisterComponent)
  }
]
