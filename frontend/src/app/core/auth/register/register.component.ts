import {ChangeDetectionStrategy, Component, DestroyRef, inject, type OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {finalize} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';

import {FormErrorComponent} from '../../../shared/form-validation/form-error.component';
import {CustomValidators} from '../validators/custom.validators';
import {AuthService} from '../auth.service';
import {NotificationService} from '../../../shared/services/notification.service';
import {MessageType} from '../../../shared/component/notification.model';

@Component({
  selector: 'chat-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormErrorComponent, RouterLink],
  templateUrl: './register.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  private fb = inject(FormBuilder);
  private _authService = inject(AuthService);
  private _notificationService = inject(NotificationService);
  private _destroyRef = inject(DestroyRef);

  // --- Signals for State Management ---
  protected loading = signal(false);
  protected showPassword = signal(false);
  protected showConfirmPassword = signal(false);
  protected imagePreview = signal<string | null>(null);

  protected registerForm!: FormGroup;
  private selectedImage: File | null = null;

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
      this.notify('Please fill all required fields correctly!', MessageType.Error);
      return;
    }

    this.loading.set(true);
    const formData = new FormData();
    const {firstname, lastname, email, password} = this.registerForm.value;

    formData.append('firstname', firstname);
    formData.append('lastname', lastname);
    formData.append('email', email);
    formData.append('password', password);

    if (this.selectedImage) formData.append('profile', this.selectedImage);

    this._authService.register(formData)
      .pipe(
        takeUntilDestroyed(this._destroyRef),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: () => {
          this.notify('You have been successfully registered to our system! Please check your email to confirm registration.',
            MessageType.Success);
        },
        error: (error: HttpErrorResponse) => {
          let errorMsg = 'An unexpected error occurred.';
          if (error.status === 403) errorMsg = 'Account disabled. Please verify your email.';
          if (error.error?.message) errorMsg = error.error.message;
          this.notify(errorMsg, MessageType.Error);
        }
      });
  }

  protected onImageSelected = (event: Event) => {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedImage = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview.set(reader.result as string);
      };
      reader.readAsDataURL(this.selectedImage);
    }
  }

  protected togglePasswordVisibility(field: 'pass' | 'confirm') {
    if (field === 'pass') this.showPassword.update(v => !v);
    else this.showConfirmPassword.update(v => !v);
  }

  protected get firstname() {
    return this.registerForm.get('firstname')!;
  }

  protected get lastname() {
    return this.registerForm.get('lastname')!;
  }

  protected control(name: string) {
    return this.registerForm.get(name)!;
  }

  private notify = (message: string, type: MessageType) => {
    this._notificationService.publish({
      message,
      timestamp: new Date().toString(),
      type
    });
  }
}
