import type { ApiErrorResponse, ApiFieldError } from './api'

export type RegisterUserRequest = {
  email: string
  password: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type AuthMessageResponse = {
  message: string
}

export type AuthResponse = {
  accessToken: string
  tokenType: string
  expiresIn: number
  requiresTwoFactor?: boolean
}

export type RefreshResponse = AuthResponse

export type { ApiErrorResponse, ApiFieldError }
