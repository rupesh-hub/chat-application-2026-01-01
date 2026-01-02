import {Routes} from '@angular/router';
import {authGuard} from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/auth',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import("./core/auth.routes").then((m) => m.authRoutes)
  },
  {
    path: 'chats',
    loadChildren: () => import("./chats/chat.routes").then((m) => m.chatRoutes),
    canActivate: [authGuard]
  },
  {
    path: 'users',
    loadChildren: () => import('./users/users.routes').then((m) => m.usersRoutes),
  }
];
