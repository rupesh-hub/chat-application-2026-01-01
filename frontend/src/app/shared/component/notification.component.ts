import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NotificationMessage} from './notification.model';

@Component({
  selector: 'chat-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <ng-container *ngIf="message">
      <div
        [ngClass]="{
          'bg-red-50 border-red-300': message.type === 'Error',
          'bg-yellow-50 border-yellow-300': message.type === 'Warn',
          'bg-green-50 border-green-300': message.type === 'Success',
          'bg-gray-50 border-gray-300': message.type === 'Other'
        }"
        class="p-4 rounded border border-dashed"
      >
        <div class="flex items-center justify-start">
          <ng-container *ngIf="message.type === 'Error'">
            <i class="fas fa-exclamation-circle text-red-500 mr-2"></i>
          </ng-container>
          <ng-container *ngIf="message.type === 'Warn'">
            <i class="fas fa-exclamation-triangle text-yellow-500 mr-2"></i>
          </ng-container>
          <ng-container *ngIf="message.type === 'Success'">
            <i class="fas fa-check-circle text-green-500 mr-2"></i>
          </ng-container>
          <ng-container *ngIf="message.type === 'Other'">
            <i class="fas fa-info-circle text-gray-500 mr-2"></i>
          </ng-container>
          <h5 class="text-xs italic font-serif font-semibold mt-0.5 flex justify-between items-center w-full"
              [ngClass]="{
                'text-red-700': message.type === 'Error',
                'text-yellow-700': message.type === 'Warn',
                'text-green-700': message.type === 'Success',
                'text-gray-700': message.type === 'Other'
              }"
          >
            <span>{{ message?.message }}</span>
            <span>{{ message?.timestamp | date:'short' }}</span>
          </h5>

        </div>
      </div>
    </ng-container>
  `,
  styles: ``
})
export class NotificationComponent {
  @Input() message: NotificationMessage = null;
}
