import type { FormControl } from "@angular/forms"

export interface AuthenticationResponse {
  access_token: string
  email: string
  name: string
  roles: string
  profile: string
}

export interface AuthenticationRequest {
  email: string
  password: string
}

export interface GlobalResponse<T> {
  message: string
  status: string
  code: string
  data: T
}

export type RegistrationFormModel = {
  firstname: FormControl<string | null>
  lastname: FormControl<string | null>
  username: FormControl<string | null>
  email: FormControl<string | null>
  password: FormControl<string | null>
  confirmPassword: FormControl<string | null>
}

export interface ResetPasswordRequest {
  username: string
  password: string
  confirmPassword: string
}

export interface ChangePasswordRequest {
  username: string
  currentPassword: string
  password: string
  confirmPassword: string
}
