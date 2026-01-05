import {Component, inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';
import {AbstractControl, FormsModule, NgForm} from '@angular/forms';
import {NotificationService} from '../../../shared/services/notification.service';
import {MessageType} from '../../../shared/component/notification.model';
import {AuthService} from '../auth.service';
import {HttpErrorResponse} from '@angular/common/http';
import {Subscription} from 'rxjs';

@Component({
  selector: 'chat-forgot-password',
  imports: [CommonModule, RouterLink, FormsModule],
  standalone: true,
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent implements OnInit, OnDestroy {

  @ViewChild("forgotPasswordForm") forgotPasswordForm!: NgForm;
  private subs = new Subscription();

  email: string = '';
  isLoading: boolean = false;
  protected cooldownSeconds = 0;

  private _notificationService = inject(NotificationService);
  private _authService = inject(AuthService);
  private timerInterval: any;
  private readonly COOLDOWN_STORAGE_KEY = 'fp_email_cooldowns';

  ngOnInit() {
    this.cleanupStorage();
  }

  ngOnDestroy() {
    this.stopTimer();
    this.subs.unsubscribe();
  }

  protected onEmailChange(): void {
    this.checkCooldownForEmail(this.email);
  }

  private checkCooldownForEmail(email: string): void {
    this.stopTimer();
    const cooldowns = this.getCooldownsFromStorage();
    const expiry = cooldowns[email.toLowerCase()];

    if (expiry) {
      const remaining = Math.floor((expiry - Date.now()) / 1000);
      if (remaining > 0) {
        this.startTimer(remaining);
      } else {
        this.removeEmailFromStorage(email);
      }
    } else {
      this.cooldownSeconds = 0;
    }
  }

  private startTimer(seconds: number): void {
    this.cooldownSeconds = seconds;
    this.timerInterval = setInterval(() => {
      this.cooldownSeconds--;
      if (this.cooldownSeconds <= 0) {
        this.stopTimer();
        this.removeEmailFromStorage(this.email);
      }
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  protected onSubmit = (): void => {
    if (this.cooldownSeconds > 0) {
      this.notify(`Please wait. You can request another reset for this email in ${this.formatTime(this.cooldownSeconds)}.`, MessageType.Error);
      return;
    }

    if (this.forgotPasswordForm.invalid) {
      Object.values(this.forgotPasswordForm.controls).forEach((control: AbstractControl) => {
        control.markAsTouched();
      });
      this.notify('Please fill out required fields!', MessageType.Error);
      return;
    }

    this.isLoading = true;
    const requestEmail = this.email.toLowerCase();

    this.subs.add(this._authService.forgotPasswordRequest(requestEmail).subscribe({
        next: () => {
          this.saveCooldownToStorage(requestEmail);
          this.startTimer(15 * 60); // 15 Minutes

          this._notificationService.publish({
            message: 'Success! Please check your email for the reset link.',
            timestamp: new Date().toString(),
            type: MessageType.Success
          });
          this.notify('Success! Please check your email for the reset link.', MessageType.Success);
          this.isLoading = false;
        },
        error: (error: HttpErrorResponse) => {
          this.isLoading = false;
          this.notify(error.error?.message || 'An error occurred. Please try again.', MessageType.Error);
        }
      })
    );
  }

  // --- Storage Helpers ---

  private getCooldownsFromStorage(): Record<string, number> {
    const data = localStorage.getItem(this.COOLDOWN_STORAGE_KEY);
    return data ? JSON.parse(data) : {};
  }

  private saveCooldownToStorage(email: string): void {
    const cooldowns = this.getCooldownsFromStorage();
    cooldowns[email] = Date.now() + 15 * 60 * 1000;
    localStorage.setItem(this.COOLDOWN_STORAGE_KEY, JSON.stringify(cooldowns));
  }

  private removeEmailFromStorage(email: string): void {
    const cooldowns = this.getCooldownsFromStorage();
    delete cooldowns[email.toLowerCase()];
    localStorage.setItem(this.COOLDOWN_STORAGE_KEY, JSON.stringify(cooldowns));
  }

  private cleanupStorage(): void {
    const cooldowns = this.getCooldownsFromStorage();
    const now = Date.now();
    let changed = false;

    for (const email in cooldowns) {
      if (cooldowns[email] < now) {
        delete cooldowns[email];
        changed = true;
      }
    }
    if (changed) localStorage.setItem(this.COOLDOWN_STORAGE_KEY, JSON.stringify(cooldowns));
  }

  protected formatTime(totalSeconds: number): string {
    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    return mins > 0 ? `${mins}m ${secs}s` : `${secs}s`;
  }

  private notify = (message: string, type: MessageType) => {
    this._notificationService.publish({
      message: message,
      timestamp: new Date().toString(),
      type: type
    });
  }
}
