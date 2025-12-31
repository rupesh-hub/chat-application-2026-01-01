import {Routes} from '@angular/router';

export const chatRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./chat.component').then((c) => c.ChatComponent)
  }
]
