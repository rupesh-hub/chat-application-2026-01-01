import {Component, inject, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, NgForm} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../auth.service';
import {HttpErrorResponse} from '@angular/common/http';

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

  togglePassword() {
    this.showPassword = !this.showPassword;
  }

  protected request: any = {
    email: 'dulal.rupesh@gmail.com',
    password: 'Rupesh@2053'
  }

  @ViewChild("loginForm") authenticationForm: NgForm;

  ngOnInit() {
    this._authService.isAuthenticated$.subscribe((isAuth) => {
      if (isAuth) {
        this._router.navigate(["/chats"])
      }
    })
  }

  protected onLogin(): void {
    this._authService.login(this.authenticationForm.value).subscribe(
      {
        error: (error: HttpErrorResponse) => {
          console.log(error)
        }
      }
    );
  }


}
