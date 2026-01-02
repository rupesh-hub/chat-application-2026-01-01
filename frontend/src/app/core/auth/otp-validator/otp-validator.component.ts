import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'ums-otp-validator',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  standalone: true,
  templateUrl: './otp-validator.component.html',
  styles: ``
})
export class OtpValidatorComponent {

  loading = false;

  otpForm = new FormGroup({
    d0: new FormControl(''),
    d1: new FormControl(''),
    d2: new FormControl(''),
    d3: new FormControl(''),
    d4: new FormControl(''),
    d5: new FormControl(''),
  });

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

    const otp = this.getOtpValue();
    if (otp.length !== 6) return;

    this.loading = true;
    console.log('OTP Submitted:', otp);

  }


}
