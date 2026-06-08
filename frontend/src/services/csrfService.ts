import axios from 'axios'

import { env } from '../utils/env'
import { getCookie } from '../utils/cookies'

export const csrfCookieName = 'XSRF-TOKEN'
export const csrfHeaderName = 'X-CSRF-Token'

let csrfRequest: Promise<void> | null = null

export function getCsrfToken(): string | null {
  return getCookie(csrfCookieName)
}

export async function ensureCsrfToken(): Promise<string | null> {
  const existingToken = getCsrfToken()

  if (existingToken) {
    return existingToken
  }

  await requestCsrfToken()
  return getCsrfToken()
}

export async function refreshCsrfToken(): Promise<string | null> {
  await requestCsrfToken()
  return getCsrfToken()
}

async function requestCsrfToken(): Promise<void> {
  if (!csrfRequest) {
    csrfRequest = axios
      .get('/auth/csrf', {
        baseURL: env.VITE_API_BASE_URL,
        timeout: 10000,
        withCredentials: true,
        headers: {
          Accept: 'application/json',
        },
      })
      .then(() => undefined)
      .finally(() => {
        csrfRequest = null
      })
  }

  return csrfRequest
}

