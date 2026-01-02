import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Directive, Input} from '@angular/core';

@Directive({
  selector: '[chatCurrentPasswordMatchValidator]',
  providers: [
    {
      provide: NG_VALIDATORS,
      useExisting: CurrentPasswordMatchValidator,
      multi: true
    }
  ],
  standalone: true
})
export class CurrentPasswordMatchValidator implements Validator {

  @Input("chatCurrentPasswordMatchValidator") currentPasswordField: string;

  validate(control: AbstractControl): ValidationErrors | null {
    if (!control?.parent) return null;
    const password = control?.parent?.get(this.currentPasswordField);
    if (!password) return null;
    return password.value !== control.value ? null : {
      matched: {
        message: 'Current password and new password must not be same.'
      }
    };
  }

}
