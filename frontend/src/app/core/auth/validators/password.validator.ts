import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive} from '@angular/core';

@Directive({
  selector: '[chatPasswordValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: PasswordValidator,
      multi: true
    }
  ],
  standalone: true
})
export class PasswordValidator implements Validator {

  validate(control: AbstractControl): ValidationErrors | null {
    const password = control?.value;
    if (!password) return null;

    // Example: require at least 8 chars, 1 number, 1 uppercase
    const hasNumber = /\d/.test(password);
    const hasUpper = /[A-Z]/.test(password);
    const validLength = password.length >= 8;

    const isValid = hasNumber && hasUpper && validLength;
    return isValid ? null : {
      strength: {
        requiredLength: 8,
        hasNumber: hasNumber,
        hasUpper: hasUpper,
        message: 'Password must be at least 8 characters long, containing uppercase and lowercase letters, a special character, and a number.'
      }
    };
  }

}
