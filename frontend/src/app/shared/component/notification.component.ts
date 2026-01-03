import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NotificationMessage} from './notification.model';
import {NotificationService} from '../services/notification.service';

@Component({
  selector: 'chat-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="message"
         class="mb-6 animate-fade-in border-l-4 rounded-xl p-4 shadow-lg shadow-slate-100 transition-all"
         [ngClass]="{
           'bg-rose-50 border-rose-500 text-rose-900': message.type === 'Error',
           'bg-amber-50 border-amber-500 text-amber-900': message.type === 'Warn',
           'bg-emerald-50 border-emerald-500 text-emerald-900': message.type === 'Success',
           'bg-slate-50 border-slate-500 text-slate-900': message.type === 'Other'
         }">
      <div class="flex items-start gap-3">
        <div class="mt-0.5">
          <i class="fas" [ngClass]="{
            'fa-circle-xmark text-rose-500': message.type === 'Error',
            'fa-triangle-exclamation text-amber-500': message.type === 'Warn',
            'fa-circle-check text-emerald-500': message.type === 'Success',
            'fa-circle-info text-slate-500': message.type === 'Other'
          }"></i>
        </div>

        <div class="flex-1">
          <p class="text-sm font-bold leading-none mb-1 capitalize">{{ message.type }}</p>
          <p class="text-sm opacity-90 font-medium">{{ message.message }}</p>
          <p class="text-[10px] opacity-50 mt-2 tracking-wider uppercase font-bold">
            {{ message.timestamp | date:'shortTime' }}
          </p>
        </div>

        <button (click)="onClose()" class="text-slate-400 hover:text-slate-600 transition-colors">
          <i class="fas fa-xmark"></i>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .animate-fade-in {
      animation: fadeIn 0.3s ease-out;
    }

    @keyframes fadeIn {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  `]
})
export class NotificationComponent {
  @Input() message: NotificationMessage | null = null;

  constructor(private notificationService: NotificationService) {
  }

  onClose() {
    // We access the service directly to clear the message
    (this.notificationService as any).clearMessage();
  }
}
