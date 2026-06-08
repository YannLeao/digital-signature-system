import axios, {
  AxiosHeaders,
  type AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'

import {
  csrfHeaderName,
  ensureCsrfToken,
  refreshCsrfToken,
} from './csrfService'
import { refreshResponseSchema } from '../schemas/apiSchemas'
import { isApiAxiosError } from '../utils/parseApiError'
import type { ApiErrorResponse } from '../types/api'
import type { RefreshResponse } from '../types/auth'
import {
  clearAccessToken,
  getAccessToken,
  getTwoFactorToken,
  setAccessToken,
} from '../utils/authTokenStore'
import { env } from '../utils/env'

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _retryAuth?: boolean
  _retryCsrf?: boolean
  _skipAuthRefresh?: boolean
}

const mutatingMethods = new Set(['post', 'put', 'patch', 'delete'])
let refreshRequest: Promise<string | null> | null = null

export const api = axios.create({
  baseURL: env.VITE_API_BASE_URL,
  timeout: 10000,
  withCredentials: true,
  headers: {
    Accept: 'application/json',
  },
})

api.interceptors.request.use(async (config) => {
  const headers = AxiosHeaders.from(config.headers)
  const token = authorizationTokenFor(config.url ?? '')
  const method = config.method?.toLowerCase() ?? 'get'

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  if (mutatingMethods.has(method)) {
    const csrfToken = await ensureCsrfToken()

    if (csrfToken) {
      headers.set(csrfHeaderName, csrfToken)
    }
  }

  config.headers = headers
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiErrorResponse>) => {
    const originalRequest = error.config as RetriableRequestConfig | undefined
    const status = error.response?.status

    if (!originalRequest) {
      return Promise.reject(error)
    }

    if (status === 401 && shouldRefreshAccessToken(originalRequest)) {
      originalRequest._retryAuth = true
      const accessToken = await refreshAccessToken()

      if (!accessToken) {
        clearAccessToken()
        redirectToLogin()
        return Promise.reject(error)
      }

      return api(originalRequest)
    }

    if (isCsrfError(error) && shouldRetryCsrf(originalRequest)) {
      originalRequest._retryCsrf = true
      const csrfToken = await refreshCsrfToken()

      if (csrfToken) {
        return api(originalRequest)
      }
    }

    if (status === 429) {
      return Promise.reject(error)
    }

    return Promise.reject(error)
  },
)

async function refreshAccessToken(): Promise<string | null> {
  if (!refreshRequest) {
    refreshRequest = api
      .post<RefreshResponse, AxiosResponse<RefreshResponse>>(
        '/auth/refresh',
        undefined,
        { _skipAuthRefresh: true } as RetriableRequestConfig,
      )
      .then((response) => {
        const parsedResponse = parseRefreshResponse(response.data)

        if (!parsedResponse) {
          clearAccessToken()
          return null
        }

        setAccessToken(parsedResponse.accessToken)
        return parsedResponse.accessToken
      })
      .catch(() => {
        clearAccessToken()
        return null
      })
      .finally(() => {
        refreshRequest = null
      })
  }

  return refreshRequest
}

function authorizationTokenFor(url: string): string | null {
  if (url.endsWith('/auth/2fa/verify')) {
    return getTwoFactorToken()
  }

  return getAccessToken()
}

function shouldRefreshAccessToken(config: RetriableRequestConfig): boolean {
  if (config._retryAuth || config._skipAuthRefresh) {
    return false
  }

  const url = config.url ?? ''
  return (
    !url.endsWith('/auth/login') &&
    !url.endsWith('/auth/register') &&
    !url.endsWith('/auth/refresh')
  )
}

function shouldRetryCsrf(config: RetriableRequestConfig): boolean {
  const method = config.method?.toLowerCase() ?? 'get'

  return !config._retryCsrf && mutatingMethods.has(method)
}

function parseRefreshResponse(data: unknown): RefreshResponse | null {
  const parsedResponse = refreshResponseSchema.safeParse(data)

  if (!parsedResponse.success) {
    return null
  }

  return parsedResponse.data
}

function isCsrfError(error: AxiosError<ApiErrorResponse>): boolean {
  return (
    isApiAxiosError(error) &&
    error.response?.status === 403 &&
    error.response.data.code === 'SEC_001'
  )
}

function redirectToLogin(): void {
  if (window.location.pathname !== '/login') {
    window.location.assign('/login')
  }
}
