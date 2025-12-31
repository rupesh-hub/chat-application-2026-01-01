export interface UserResponse {
  id: number;
  name: string;
  email: string;
  profile?: string;
  status?: 'online' | 'offline' | 'typing';
  lastSeen?: string;
}
