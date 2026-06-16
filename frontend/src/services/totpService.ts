import { api } from './api'
import { authResponseSchema } from '../schemas/apiSchemas'
import { totpSetupResponseSchema } from '../schemas/totpSchemas'
import type {
  BackupCodesResponse,
  BackupCodeVerifyRequest,
  TotpSetupResponse,
  TotpStatus,
  TotpVerifyRequest,
  TotpVerifyResponse,
} from '../types/totp'
import {
  getAuthenticatedEmail,
  setAccessToken,
  setAuthSession,
  getTwoFactorToken,
} from '../utils/authTokenStore'
import { InvalidApiResponseError } from '../utils/parseApiError'

export async function startTotpSetup(): Promise<TotpSetupResponse> {
  const response = await api.post<TotpSetupResponse>('/auth/2fa/setup')
  const parsedResponse = totpSetupResponseSchema.safeParse(response.data)

  if (!parsedResponse.success) {
    throw new InvalidApiResponseError()
  }

  return parsedResponse.data
}

export async function verifyTotpSetup(
  payload: TotpVerifyRequest,
): Promise<BackupCodesResponse> {
  const response = await api.post<BackupCodesResponse>(
    '/auth/2fa/setup/confirm',
    payload,
    { timeout: 60000 },
  )
  return response.data
}

export async function verifyTotpLogin(
  payload: TotpVerifyRequest,
): Promise<TotpVerifyResponse> {
  const twoFactorToken = getTwoFactorToken()
  if (!twoFactorToken) {
    throw new InvalidApiResponseError()
  }

  const response = await api.post<TotpVerifyResponse>(
    '/auth/2fa/verify',
    payload,
    {
      headers: {
        Authorization: `Bearer ${twoFactorToken}`,
      },
      timeout: 60000,
    },
  )
  const parsedResponse = authResponseSchema.safeParse(response.data)

  if (!parsedResponse.success) {
    throw new InvalidApiResponseError()
  }

  const email = getAuthenticatedEmail()
  if (email) {
    setAuthSession(parsedResponse.data.accessToken, email)
  } else {
    setAccessToken(parsedResponse.data.accessToken)
  }

  return parsedResponse.data
}

export async function verifyBackupCode(
  payload: BackupCodeVerifyRequest,
): Promise<TotpVerifyResponse> {
  return verifyTotpLogin(payload)
}

export async function getTotpStatus(): Promise<TotpStatus> {
  const response = await api.get<TotpStatus>('/auth/2fa/status')
  return response.data
}
