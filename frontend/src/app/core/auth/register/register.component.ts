import { Component, inject, type OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormErrorComponent } from '../../../shared/form-validation/form-error.component';
import { CustomValidators } from '../validators/custom.validators';
import { AuthService } from '../auth.service';
import { NotificationService } from '../../../shared/services/notification.service';
import { MessageType } from '../../../shared/component/notification.model';
import { NotificationComponent } from '../../../shared/component/notification.component';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'chat-register',
  imports: [CommonModule, ReactiveFormsModule, FormErrorComponent, NotificationComponent, RouterLink],
  standalone: true,
  templateUrl: './register.component.html',
  styles: [`

    input {
      @apply w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-md focus:ring-4 focus:ring-indigo-50 focus:border-indigo-600 outline-none transition-all text-sm
    }

    input.ng-invalid.ng-touched {
      @apply bg-red-50 border border-red-500;
    }

    input.ng-valid {
      @apply bg-green-50/50 border border-green-500;
    }

    input.ng-pending {
      @apply bg-orange-50 border border-orange-600;
    }
  `]
})
export class RegisterComponent implements OnInit {

  protected registerForm!: FormGroup;
  private _authService: AuthService = inject(AuthService);
  protected _notificationService: NotificationService = inject(NotificationService);

  protected loading = false;
  protected showPassword = false;
  protected showConfirmPassword = false;
  protected selectedImage: File | null = null;
  protected imagePreview: string | null = null;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      firstname: ['', [Validators.required, CustomValidators.spaceValidator]],
      lastname: ['', [Validators.required, CustomValidators.spaceValidator]],
      email: ['', [Validators.required, Validators.email], [CustomValidators.emailAsyncValidator(this._authService)]],
      password: ['', [Validators.required, CustomValidators.passwordValidator, CustomValidators.spaceValidator]],
      confirmPassword: ['', [Validators.required, CustomValidators.spaceValidator]],
    }, {
      validators: CustomValidators.passwordMatchValidator
    });
  }

  protected onRegister = () => {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this._notificationService.publish({
        message: 'Please fill all required fields correctly!',
        timestamp: new Date().toString(),
        type: MessageType.Error
      });
      return;
    }

    this.loading = true;
    const formData = new FormData();

    // Extract values except confirmPassword
    const { firstname, lastname, email, password } = this.registerForm.value;

    formData.append('firstname', firstname);
    formData.append('lastname', lastname);
    formData.append('email', email);
    formData.append('password', password);

    if (this.selectedImage) {
      formData.append('profile', this.selectedImage);
    }

    this._authService.register(formData)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (response) => {
          this._notificationService.publish({
            message: 'Registration successful!',
            timestamp: new Date().toString(),
            type: MessageType.Success
          });
          // Redirect logic here if needed
        },
        error: (error: HttpErrorResponse) => {
          this._notificationService.publish({
            message: error.error?.message || 'Registration failed. Please try again.',
            timestamp: new Date().toString(),
            type: MessageType.Error
          });
        }
      });
  }

  protected onImageSelected = (event: Event) => {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedImage = input.files[0];

      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(this.selectedImage);
    }
  }

  protected togglePasswordVisibility(field: 'pass' | 'confirm') {
    if (field === 'pass') this.showPassword = !this.showPassword;
    else this.showConfirmPassword = !this.showConfirmPassword;
  }

  protected get firstname() { return this.registerForm.get('firstname')!; }
  protected get lastname() { return this.registerForm.get('lastname')!; }
  protected control(name: string) { return this.registerForm.get(name)!; }
}
