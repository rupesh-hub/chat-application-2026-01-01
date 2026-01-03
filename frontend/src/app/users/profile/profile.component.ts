import {Component, ElementRef, inject, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, NgForm} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';

import {AuthService} from '../../core/auth/auth.service';
import {UsersService} from '../users.service';
import {NotificationService} from '../../shared/services/notification.service';
import {MessageType} from '../../shared/component/notification.model';

import {FormErrorComponent} from '../../shared/form-validation/form-error.component';
import {PasswordValidator} from '../../core/auth/validators/password.validator';
import {CurrentPasswordMatchValidator} from '../../core/auth/validators/current-password-match.validator';
import {PasswordMatchValidator} from '../../core/auth/validators/password-match.validator';
import {UserResponse, UserUpdateRequest} from '../user.model';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'chat-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    FormErrorComponent,
    PasswordValidator,
    CurrentPasswordMatchValidator,
    PasswordMatchValidator,
    RouterLink
  ],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  private _authService = inject(AuthService);
  private _usersService = inject(UsersService);
  private _notificationService = inject(NotificationService);

  @ViewChild('fileInput') fileInput!: ElementRef;
  @ViewChild('pwdForm') pwdForm!: NgForm;

  // Tabs and State
  activeTab: 'profile' | 'security' | 'preferences' = 'profile';
  isEditing = false;
  isUploading = false;
  isLoading = false;
  isChangingPassword = false;

  // Form Data
  protected user!: UserResponse;
  protected passwordForm = {
    currentPassword: '',
    password: '',
    confirmPassword: ''
  };

  settings = {
    notifications: true,
    darkMode: false,
    readReceipts: true
  };

  ngOnInit() {
    this.fetchUserProfile();
  }

  private fetchUserProfile() {
    this._usersService.profile().subscribe({
      next: (response: UserResponse) => {
        this.user = response;
      },
      error: (err) => this.notify('Could not load profile', MessageType.Error)
    });
  }

  // --- Profile Picture Logic ---
  protected triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.[0]) {
      this.isUploading = true;
      const formData = new FormData();
      formData.append('profile', input.files[0]);

      this._usersService.uploadProfilePicture(formData).subscribe({
        next: (res: any) => {
          // Sync the new URL to the user object
          this.user.profile = res.url || res;
          this.isUploading = false;
          this.notify('Avatar updated successfully', MessageType.Success);
        },
        error: (error: HttpErrorResponse) => {
          this.isUploading = false;
          this.notify(error?.error?.message || 'Upload failed', MessageType.Error);
        }
      });
    }
  }

  // --- Profile Edit Logic ---
  protected saveProfile(): void {
    const request: UserUpdateRequest = {
      firstname: this.user.firstname,
      lastname: this.user.lastname
    }
    this.isLoading = true;
    this._usersService.updateProfile(request).subscribe({
      next: (response: UserResponse) => {
        this.isEditing = false;
        this.isLoading = false;
        this.notify('Profile updated', MessageType.Success);
        this.user.firstname = response.firstname;
        this.user.lastname = response.lastname;
      },
      error: (err) => {
        this.isLoading = false;
        this.notify('Update failed', MessageType.Error);
      }
    });
  }

  // --- Password Logic ---
  protected togglePasswordChange(): void {
    this.isChangingPassword = !this.isChangingPassword;
    if (!this.isChangingPassword) {
      this.passwordForm = {currentPassword: '', password: '', confirmPassword: ''};
    }
  }

  protected onUpdatePassword(): void {
    // Check if form is valid using the ViewChild reference
    if (this.pwdForm.invalid) {
      this.notify('Please fix the errors in the form', MessageType.Error);
      return;
    }

    this.isLoading = true;
    this._usersService.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.notify('Password updated successfully!', MessageType.Success);
        this.togglePasswordChange();
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.notify(error?.error?.message || 'Password update failed', MessageType.Error);
      }
    });
  }

  protected logout(): void {
    this._authService.logout();
  }

  private notify(message: string, type: MessageType) {
    this._notificationService.publish({message, type, timestamp: new Date().toString()});
  }
}
