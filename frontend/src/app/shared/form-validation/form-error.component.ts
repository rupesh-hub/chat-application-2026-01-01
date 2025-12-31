import {Component, inject, Input, OnInit} from '@angular/core';
import {AbstractControl, FormGroup, NgControl} from '@angular/forms';
import {FormErrorService} from './form-error.service';
import {CommonModule} from '@angular/common';

type ValidationTarget = AbstractControl | NgControl | null | undefined;

@Component({
  selector: 'chat-form-error',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div *ngIf="shouldShowError()" class="error-message mt-0.5">
      <small class="flex items-start gap-1">
        <svg class="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
        </svg>
        <span>{{ message }}</span>
      </small>
    </div>
  `,
  styles: ``
})
export class FormErrorComponent implements OnInit {

  @Input() control?: AbstractControl | NgControl | null;
  @Input() parentForm?: FormGroup | null;
  @Input() formErrorKey?: string;
  @Input() displayTriggerControl?: AbstractControl | null;
  protected message: string | null = null;
  private targetControl: ValidationTarget;
  private _validationService: FormErrorService = inject(FormErrorService);

  ngOnInit() {
    this.targetControl = this.control || this.parentForm;
    if (!this.targetControl) return;
    const monitorControls = new Set<AbstractControl>([this.targetControl as AbstractControl]);
    if (this.displayTriggerControl && this.displayTriggerControl !== this.targetControl)
      monitorControls.add(this.displayTriggerControl);

    monitorControls.forEach(ctrl => {
      ctrl.valueChanges.subscribe(() => this.updateErrorMessage());
      ctrl.statusChanges.subscribe(() => this.updateErrorMessage());
    });

    this.updateErrorMessage();

  }

  shouldShowError(): boolean {
    const control = this.targetControl;
    if (!control || !this.message) return false;
    const absControl = control as AbstractControl;
    if (this.parentForm && this.formErrorKey) {
      if (!absControl.hasError(this.formErrorKey)) return false;
      const triggerControl = this.displayTriggerControl || absControl;
      return triggerControl.dirty || triggerControl.touched;
    }

    return absControl.invalid && (absControl.dirty || absControl.touched);
  }

  private updateErrorMessage(): void {
    const control = this.targetControl;
    if (!control) {
      this.message = null;
      return;
    }

    const absControl = control as AbstractControl;
    if (!absControl.errors) {
      this.message = null;
      return;
    }

    if (this.parentForm && this.formErrorKey) {
      if (absControl.hasError(this.formErrorKey)) {
        const specificError = absControl.errors[this.formErrorKey];
        this.message = this._validationService.getErrorMessage({[this.formErrorKey]: specificError});
      } else this.message = null;

      return;
    }

    this.message = this._validationService.getErrorMessage(absControl.errors);
  }

}
