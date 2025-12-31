export interface User {
  id: string
  username: string
  email?: string
}

export interface UserSession {
  id: string
  username: string
  email?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  message: string
  statusCode: number
  data: UserSession
}
