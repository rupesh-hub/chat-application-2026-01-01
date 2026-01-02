import {Component, inject, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AbstractControl, FormsModule} from '@angular/forms';
import {FormErrorComponent} from '../../shared/form-validation/form-error.component';
import {PasswordValidator} from '../../core/auth/validators/password.validator';
import {PasswordMatchValidator} from '../../core/auth/validators/password-match.validator';
import {CurrentPasswordMatchValidator} from '../../core/auth/validators/current-password-match.validator';
import {NotificationService} from '../../shared/services/notification.service';
import {AuthService} from '../../core/auth/auth.service';
import {MessageType} from '../../shared/component/notification.model';
import {HttpErrorResponse} from '@angular/common/http';
import {NotificationComponent} from '../../shared/component/notification.component';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule, FormErrorComponent, PasswordValidator, PasswordMatchValidator, CurrentPasswordMatchValidator, NotificationComponent],
  templateUrl: './change-password.component.html',
})
export class ChangePasswordComponent {
  isLoading = false;
  showOldPwd = false;
  showNewPwd = false;

  @ViewChild("changePasswordForm") changePasswordForm;
  protected _notificationService: NotificationService = inject(NotificationService);
  private _authService: AuthService = inject(AuthService);

  // Model
  pwdData = {
    email: 'alexandra.s@chatstream.io', // Dummy data
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  onSubmit() {
    this.isLoading = true;
    if (this.changePasswordForm.invalid) {
      Object.values(this.changePasswordForm.controls).forEach((control: AbstractControl) => {
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

    this._authService.changePassword(this.changePasswordForm.value)
      .subscribe({
        next: (response: any) => {
          console.log(response);
        },
        error: (error: HttpErrorResponse) => {
          console.log(error)
        }
      });
  }

  // Logic for Password Strength Meter
  getStrength() {
    const pwd: any = this.pwdData.newPassword;
    if (!pwd) return 0;
    let strength = 0;
    if (pwd.length > 8) strength += 1;
    if (/[A-Z]/.test(pwd)) strength += 1;
    if (/[0-9]/.test(pwd)) strength += 1;
    if (/[^A-Za-z0-9]/.test(pwd)) strength += 1;
    return strength;
  }
}
