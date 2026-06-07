import { api } from './api'
import { authResponseSchema } from '../schemas/apiSchemas'
import type {
  AuthResponse,
  AuthMessageResponse,
  LoginRequest,
  RegisterUserRequest,
} from '../types/auth'
import { setAccessToken, clearAccessToken } from '../utils/authTokenStore'
import { InvalidApiResponseError, parseApiError } from '../utils/parseApiError'

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
  const parsedResponse = parseAuthResponse(response.data)
  setAccessToken(parsedResponse.accessToken)
  return parsedResponse
}

export async function refreshSession(): Promise<AuthResponse> {
  const response = await api.post<AuthResponse>('/auth/refresh')
  const parsedResponse = parseAuthResponse(response.data)
  setAccessToken(parsedResponse.accessToken)
  return parsedResponse
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
  const apiError = parseApiError(error, fallbackMessage)

  if (apiError.status === 401) {
    return 'Credenciais invalidas.'
  }

  if (apiError.status === 429) {
    return 'Muitas tentativas. Tente novamente em instantes.'
  }

  if (apiError.status === 400) {
    return apiError?.fields?.[0]?.message ?? apiError?.message ?? fallbackMessage
  }

  return apiError?.message ?? fallbackMessage
}

function parseAuthResponse(data: unknown): AuthResponse {
  const parsedResponse = authResponseSchema.safeParse(data)

  if (!parsedResponse.success) {
    throw new InvalidApiResponseError()
  }

  return parsedResponse.data
}
