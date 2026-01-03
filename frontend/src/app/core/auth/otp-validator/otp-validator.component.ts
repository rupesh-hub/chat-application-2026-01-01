import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AuthService} from '../auth.service';
import {ActivatedRoute} from '@angular/router';
import {NotificationService} from '../../../shared/services/notification.service';
import {HttpErrorResponse} from '@angular/common/http';
import {MessageType} from '../../../shared/component/notification.model';
import {NotificationComponent} from '../../../shared/component/notification.component';

@Component({
  selector: 'ums-otp-validator',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NotificationComponent],
  standalone: true,
  templateUrl: './otp-validator.component.html',
  styles: ``
})
export class OtpValidatorComponent implements OnInit {

  private _authService: AuthService = inject(AuthService);
  private route: ActivatedRoute = inject(ActivatedRoute);
  private email: string = '';
  protected _notificationService: NotificationService = inject(NotificationService);

  loading = false;

  otpForm = new FormGroup({
    d0: new FormControl(''),
    d1: new FormControl(''),
    d2: new FormControl(''),
    d3: new FormControl(''),
    d4: new FormControl(''),
    d5: new FormControl(''),
  });

  ngOnInit() {
    this.email = this.route.snapshot.paramMap.get('email');
  }

  get otpControls() {
    return Object.values(this.otpForm.controls);
  }

  onInput(event: Event, index: number) {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/\D/g, ''); // numbers only

    if (input.value && index < 5) {
      this.focusInput(index + 1);
    }

    this.checkAutoSubmit();
  }

  onKeyDown(event: KeyboardEvent, index: number) {
    if (event.key === 'Backspace' && !this.otpControls[index].value && index > 0) {
      this.focusInput(index - 1);
    }
  }

  onPaste(event: ClipboardEvent) {
    event.preventDefault();
    const pasted = event.clipboardData?.getData('text')?.replace(/\D/g, '') ?? '';

    pasted.split('').slice(0, 6).forEach((digit, i) => {
      this.otpControls[i].setValue(digit);
    });

    this.focusInput(Math.min(pasted.length, 5));
    this.checkAutoSubmit();
  }

  focusInput(index: number) {
    const inputs = document.querySelectorAll<HTMLInputElement>('input');
    inputs[index]?.focus();
  }

  checkAutoSubmit() {
    const otp = this.getOtpValue();
    if (otp.length === 6) {
      this.submitOtp();
    }
  }

  getOtpValue(): string {
    return this.otpControls.map(c => c.value).join('');
  }

  submitOtp() {
    if (this.loading) return;

    const token = this.getOtpValue();
    if (token.length !== 6) return;

    this.loading = true;
    this._authService.confirmEmail(token, this.email)
      .subscribe({
        next: () => {
          this.notify('Account has been successfully activated !', MessageType.Success)
        },
        error: (error: HttpErrorResponse) => {
          this.notify(error.error?.message || error?.message || 'Something went wrong. Please try again.',
            MessageType.Error)
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
