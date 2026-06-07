import { api } from './api'
import { authResponseSchema } from '../schemas/apiSchemas'
import type {
  PasskeyAuthFinishRequest,
  PasskeyAuthResponse,
  PasskeyAuthStartResponse,
  PasskeyDevice,
  PasskeyRegisterFinishRequest,
  PasskeyRegisterStartResponse,
  PasskeyStartRequest,
} from '../types/passkey'
import { setAuthSession } from '../utils/authTokenStore'
import { InvalidApiResponseError } from '../utils/parseApiError'

export async function startPasskeyRegistration(
  payload: PasskeyStartRequest,
): Promise<PasskeyRegisterStartResponse> {
  const response = await api.post<PasskeyRegisterStartResponse>(
    '/auth/passkey/register/start',
    payload,
  )
  return response.data
}

export async function finishPasskeyRegistration(
  payload: PasskeyRegisterFinishRequest,
): Promise<void> {
  await api.post('/auth/passkey/register/finish', payload)
}

export async function startPasskeyAuthentication(
  payload: PasskeyStartRequest,
): Promise<PasskeyAuthStartResponse> {
  const response = await api.post<PasskeyAuthStartResponse>(
    '/auth/passkey/auth/start',
    payload,
  )
  return response.data
}

export async function finishPasskeyAuthentication(
  payload: PasskeyAuthFinishRequest,
): Promise<PasskeyAuthResponse> {
  const response = await api.post<PasskeyAuthResponse>(
    '/auth/passkey/auth/finish',
    payload,
  )
  const parsedResponse = authResponseSchema.safeParse(response.data)

  if (!parsedResponse.success) {
    throw new InvalidApiResponseError()
  }

  setAuthSession(parsedResponse.data.accessToken, payload.email)
  return parsedResponse.data
}

export async function listPasskeys(email: string): Promise<PasskeyDevice[]> {
  const response = await api.get<PasskeyDevice[]>('/auth/passkey/devices', {
    params: { email },
  })
  return response.data
}

export async function revokePasskey(id: number, email: string): Promise<void> {
  await api.delete(`/auth/passkey/device/${id}`, {
    params: { email },
  })
}
