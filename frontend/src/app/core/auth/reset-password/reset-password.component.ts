import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NotificationService} from '../../../shared/services/notification.service';
import {AuthService} from '../auth.service';
import {AbstractControl, FormsModule} from '@angular/forms';
import {MessageType} from '../../../shared/component/notification.model';
import {HttpErrorResponse} from '@angular/common/http';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormErrorComponent} from '../../../shared/form-validation/form-error.component';

@Component({
  selector: 'chat-reset-password',
  imports: [CommonModule, FormsModule, RouterLink, FormErrorComponent],
  standalone: true,
  templateUrl: './reset-password.component.html',
})
export class ResetPasswordComponent implements OnInit {

  @ViewChild("resetPasswordForm") resetPasswordForm;
  protected resetForm: {
    email: string,
    password: string,
    confirmPassword: string
  } = {
    email: '',
    password: '',
    confirmPassword: '',
  }
  protected isLoading: boolean = false;
  private token: string;

  protected _notificationService: NotificationService = inject(NotificationService);
  private _authService: AuthService = inject(AuthService);
  private route: ActivatedRoute = inject(ActivatedRoute);
  private router: Router = inject(Router);
  protected isPasswordShown: boolean = false;
  protected isConfirmPasswordShown: boolean = false;

  ngOnInit() {
    this.resetForm.email = this.route.snapshot.queryParamMap.get('email');
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  onSubmit = () => {
    this.isLoading = true;
    if (this.resetPasswordForm.invalid) {
      Object.values(this.resetPasswordForm.controls).forEach((control: AbstractControl) => {
        control.markAsTouched();
      });
      this.notify('Please fill out required fields!', MessageType.Error);
      this.isLoading = false;
      return;
    }
    const request = {
      email: this.resetForm.email,
      token: this.token,
      password: this.resetForm.password,
      confirmPassword: this.resetForm.confirmPassword
    };
    this._authService.resetPassword(request)
      .subscribe({
        next: () => {
          this.notify('Password reset successful !', MessageType.Success);
          this.router.navigate(['/auth/login'])
        },
        error: (error: HttpErrorResponse) => {
          this.notify(error?.error?.message || error?.message, MessageType.Error);
          this.isLoading = false;
          return;
        }
      });
  }

  protected showPassword = () => {
    this.isPasswordShown = !this.isPasswordShown;
  }

  protected showConfirmPassword = () => {
    this.isConfirmPasswordShown = !this.isConfirmPasswordShown;
  }

  protected resendConfirmationToken = () => {
    this._authService.resendConfirmationToken(
      this.resetForm.email,
      this.token
    ).subscribe({
      next: (response: string) => {
        this.notify(response, MessageType.Success);
        this.router.navigate(['/auth/login'])
      }
    })
  }

  private notify = (message: string, type: MessageType) => {
    this._notificationService.publish({
      message: message,
      timestamp: new Date().toString(),
      type: type
    });
  }

}
