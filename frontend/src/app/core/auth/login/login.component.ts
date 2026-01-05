import {Component, DestroyRef, inject, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, NgForm} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../auth.service';
import {HttpErrorResponse} from '@angular/common/http';
import {NotificationService} from '../../../shared/services/notification.service';
import {MessageType} from '../../../shared/component/notification.model';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';

@Component({
  selector: 'chat-login',
  imports: [CommonModule, FormsModule, RouterLink],
  standalone: true,
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {

  showPassword = false;
  isLoading = false;
  isSuccess = false;
  private _authService: AuthService = inject(AuthService);
  private _router: Router = inject(Router);
  protected _notificationService: NotificationService = inject(NotificationService);

  /*Subscriptions*/
  private _destroyRef = inject(DestroyRef);

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  protected request: any = {
    email: 'dulal.rupesh@gmail.com',
    password: 'Rupesh@2053'
  }

  @ViewChild("loginForm") authenticationForm: NgForm;

  ngOnInit() {
    this._authService.isAuthenticated$
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe((isAuth) => {
        if (isAuth) {
          this._router.navigate(["/chats"])
        }
      })
  }

  protected onLogin(): void {
    this._authService.login(this.authenticationForm.value)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe(
        {
          next: () => {
            this.notify('Authentication success !', MessageType.Success)
          },
          error: (error: HttpErrorResponse) => {
            const message: string = error.message || error.error?.message || 'Login failed. Please try again.';
            this.notify(message, MessageType.Error)
          }
        }
      );
  }

  private notify = (message: string, type: MessageType) => {
    this._notificationService.publish({
      message: message,
      timestamp: new Date().toString(),
      type: type
    });
  }

}
