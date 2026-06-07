import axios from 'axios'

import { api } from './api'
import type {
  ApiErrorResponse,
  AuthResponse,
  AuthMessageResponse,
  LoginRequest,
  RegisterUserRequest,
} from '../types/auth'
import { clearAccessToken, setAccessToken } from '../utils/authToken'

export async function registerUser(
  payload: RegisterUserRequest,
): Promise<AuthMessageResponse> {
  const response = await api.post<AuthMessageResponse>('/auth/register', payload)
  return response.data
}

export async function loginUser(
  payload: LoginRequest,
): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/login', payload)
  setAccessToken(response.data.accessToken)
  return response.data
}

export async function refreshSession(): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/refresh')
  setAccessToken(response.data.accessToken)
  return response.data
}

export async function logoutUser(): Promise<AuthMessageResponse> {
  try {
    const response = await api.post<AuthMessageResponse>('/auth/logout')
    return response.data
  } finally {
    clearAccessToken()
  }
}

export function getAuthErrorMessage(error: unknown, fallbackMessage: string) {
  if (!axios.isAxiosError<ApiErrorResponse>(error)) {
    return 'Nao foi possivel conectar ao servidor. Tente novamente.'
  }

  const status = error.response?.status
  const apiError = error.response?.data

  if (status === 401) {
    return 'Credenciais invalidas.'
  }

  if (status === 429) {
    return 'Muitas tentativas. Tente novamente em instantes.'
  }

  if (status === 400) {
    return apiError?.fields?.[0]?.message ?? apiError?.message ?? fallbackMessage
  }

  return apiError?.message ?? fallbackMessage
}
