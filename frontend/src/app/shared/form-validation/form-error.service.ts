import {Injectable} from '@angular/core';
import {ValidationErrors} from '@angular/forms';

interface ValidationMessageMap {
  [key: string]: (error: any) => string;
}

@Injectable({
  providedIn: 'root'
})
export class FormErrorService {

  private validationMessages: ValidationMessageMap = {
    'required': () => 'This field is required.',
    'email': () => 'Please enter a valid email address.',
    'minlength': (error: { requiredLength: number, actualLength: number }) =>
      `The minimum length is ${error.requiredLength} characters. You entered ${error.actualLength}.`,
    'maxlength': (error: { requiredLength: number, actualLength: number }) =>
      `The maximum length is ${error.requiredLength} characters.`,
    'pattern': () => 'The input format is invalid.',
    'forbidden': () => 'This value is not allowed! Please take another one.',
    'mismatch': () => 'Passwords do not match.',
    'taken': () => 'This value is already taken.',
    'strength': () => 'Value provided must be at least 8 characters long, containing uppercase and lowercase letters, a special character, and a number.'
  };

  public getErrorMessage(errors: ValidationErrors | null): string | null {
    if (!errors) return null;
    const key = Object.keys(errors)[0];
    const value = errors[key];
    if (value && typeof value === 'object' && value['message']) return value['message'] as string;
    if (this.validationMessages[key]) return this.validationMessages[key](value);
    return 'An unknown validation error occurred.';
  }

}
