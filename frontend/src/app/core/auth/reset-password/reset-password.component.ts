import {Component, inject, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NotificationService} from '../../../shared/services/notification.service';
import {AuthService} from '../auth.service';
import {AbstractControl, FormsModule} from '@angular/forms';
import {MessageType} from '../../../shared/component/notification.model';
import {HttpErrorResponse} from '@angular/common/http';
import {NotificationComponent} from '../../../shared/component/notification.component';
import {RouterLink} from '@angular/router';
import {FormErrorComponent} from '../../../shared/form-validation/form-error.component';

@Component({
  selector: 'chat-reset-password',
  imports: [CommonModule, FormsModule, NotificationComponent, RouterLink, FormErrorComponent],
  standalone: true,
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent {

  @ViewChild("resetPasswordForm") resetPasswordForm;
  protected resetForm: {
    email: string,
    password: string,
    confirmPassword: string
  } = {
    email: '',
    password: '',
    confirmPassword: ''
  }
  protected isLoading: boolean = false;

  protected _notificationService: NotificationService = inject(NotificationService);
  private _authService: AuthService = inject(AuthService);
  protected isPasswordShown: boolean = false;
  protected isConfirmPasswordShown: boolean = false;

  onSubmit = () => {
    this.isLoading = true;
    if (this.resetPasswordForm.invalid) {
      Object.values(this.resetPasswordForm.controls).forEach((control: AbstractControl) => {
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
    this._authService.forgotPasswordRequest(this.resetForm)
      .subscribe({
        next: (response: any) => {
          console.log(response);
        },
        error: (error: HttpErrorResponse) => {
          console.log(error)
        }
      });
  }

  protected showPassword = () => {
    this.isPasswordShown = !this.isPasswordShown;
  }

  protected showConfirmPassword = () => {
    this.isConfirmPasswordShown = !this.isConfirmPasswordShown;
  }

}
