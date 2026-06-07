import { createContext } from 'react'

import type {
  AuthMessageResponse,
  AuthResponse,
  LoginRequest,
  RegisterUserRequest,
} from '../types/auth'

export type AuthContextValue = {
  accessToken: string | null
  email: string | null
  isAuthenticated: boolean
  login: (payload: LoginRequest) => Promise<AuthResponse>
  logout: () => Promise<void>
  refresh: () => Promise<AuthResponse>
  register: (payload: RegisterUserRequest) => Promise<AuthMessageResponse>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

