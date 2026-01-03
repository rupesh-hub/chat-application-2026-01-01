export interface UserResponse {
  id: number;
  name: string;
  firstname: string;
  lastname: string;
  email: string;
  profile?: string;
  status?: 'online' | 'offline' | 'typing';
  lastSeen?: string;
  createdAt?: string
}

export interface ChangePasswordRequest {
  currentPassword: string;
  password: string;
  confirmPassword: string;
}

export interface UserUpdateRequest{
  firstname: string;
  lastname: string;
}
