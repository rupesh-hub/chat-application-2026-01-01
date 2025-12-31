import {AbstractControl, ValidationErrors} from '@angular/forms';
import {AuthService} from '../auth.service';
import {Observable} from 'rxjs';

export class CustomValidators {

  public static passwordValidator(control: AbstractControl<string | null>): ValidationErrors | null {
    const password = control?.value?.trim();

    if (!password) return null;

    const forbiddenPasswords = ['test', 'admin', 'user', 'password'];
    const lowerPassword = password.toLowerCase();
    if (forbiddenPasswords.some(word => lowerPassword.includes(word))) {
      return {
        forbidden: {
          message: 'Password not allowed! Please choose another.'
        }
      };
    }

    // Min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
    const pattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    return pattern.test(password) ? null : {
      pattern: {
        message: 'Password must contains 8 characters and uppercase, lowercase, number, and special character.'
      }
    }

  }

  public static passwordMatchValidator = (control: AbstractControl<any>): ValidationErrors | null => {
    const password: string = control.get('password')?.value;
    const confirmPassword: string = control.get('confirmPassword')?.value;
    return password && confirmPassword && password === confirmPassword ? null :
      {
        mismatch: {
          message: 'Password and confirm password must match.'
        }
      };
  }

  public static spaceValidator = (control: AbstractControl<string | null>) => {
    const value = control.value;
    if (!value) return null;

    return (value.indexOf(' ') !== -1) ? {
      space: {
        message: 'White space not allowed.'
      }
    } : null;
  }

  public static emailAsyncValidator = (authService: AuthService) => {
    let timer: any;
    return (control: AbstractControl) => {
      return new Observable<ValidationErrors | null>((observer) => {
        const value = control.value;

        // clear previous timer
        if (timer) clearTimeout(timer);

        // if empty → no error
        if (!value) {
          observer.next(null);
          observer.complete();
          return;
        }

        // debounce manually
        timer = setTimeout(() => {
          authService.existsByEmail(value).subscribe({
            next: (res: boolean) => {
              observer.next(res ? {
                taken: {
                  message: `Email '${value}' already taken.`
                }
              } : null);
              observer.complete();
            },
            error: () => {
              observer.next(null);
              observer.complete();
            }
          });
        }, 500);
      });
    };
  }
}
