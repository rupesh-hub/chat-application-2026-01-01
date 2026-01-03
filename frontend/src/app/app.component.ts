import {Component, inject} from "@angular/core"
import {RouterOutlet} from "@angular/router"
import {NotificationService} from './shared/services/notification.service';
import {NotificationComponent} from './shared/component/notification.component';
import {CommonModule} from '@angular/common';

@Component({
  selector: "chat-root",
  imports: [RouterOutlet, NotificationComponent, CommonModule],
  standalone: true,
  template: `
    <div class="fixed top-6 right-6 z-[9999] max-w-sm w-full">
      <div *ngIf="_notificationService.message$ | async as message">
        <chat-notification [message]="message"></chat-notification>
      </div>
    </div>

    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    chat-notification {
      display: block;
      pointer-events: auto;
    }
  `]
})
export class AppComponent {

  protected _notificationService: NotificationService = inject(NotificationService);
}
