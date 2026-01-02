import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive, Input} from '@angular/core';

@Directive({
  selector: '[chatPasswordMatchValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: PasswordMatchValidator,
      multi: true
    },
  ],
  standalone: true
})
export class PasswordMatchValidator implements Validator {

  @Input("chatPasswordMatchValidator") passwordField: string;

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control?.parent) return null;
    const password = control?.parent?.get(this.passwordField);
    if (!password) return null;
    return password.value === control.value ? null : {
      mismatch: true
    };
  }

}
