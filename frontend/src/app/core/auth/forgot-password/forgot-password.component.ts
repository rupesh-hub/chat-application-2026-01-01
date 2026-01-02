import {Component, inject, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {AbstractControl, FormsModule} from '@angular/forms';
import {NotificationService} from '../../../shared/services/notification.service';
import {NotificationComponent} from '../../../shared/component/notification.component';
import {MessageType} from '../../../shared/component/notification.model';
import {AuthService} from '../auth.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'chat-forgot-password',
  imports: [CommonModule, RouterLink, FormsModule, NotificationComponent],
  standalone: true,
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {

  email: string;
  @ViewChild("forgotPasswordForm") forgotPasswordForm;
  protected _notificationService: NotificationService = inject(NotificationService);
  private _authService: AuthService = inject(AuthService);
  isLoading: boolean = false;

  protected onSubmit = (): void => {
    this.isLoading = true;
    if (this.forgotPasswordForm.invalid) {
      Object.values(this.forgotPasswordForm.controls).forEach((control: AbstractControl) => {
        control.markAsTouched();
      });
      this._notificationService.publish({
        message: 'Please fill out required fields!',
        timestamp: new Date().toString(),
        type: MessageType.Error
      });
      this.isLoading = false;
      return;
    }

    this._authService.forgotPasswordRequest(this.email)
      .subscribe({
        next: (response: any) => {
          console.log(response);
        },
        error: (error: HttpErrorResponse) => {
          console.log(error)
        }
      });
  }


}
