import { getAccessToken } from './authTokenStore'

export function currentSessionIdFromAccessToken(): string | null {
  const token = getAccessToken()

  if (!token) {
    return null
  }

  const [, payload] = token.split('.')

  if (!payload) {
    return null
  }

  try {
    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedPayload = normalizedPayload.padEnd(
      normalizedPayload.length + ((4 - (normalizedPayload.length % 4)) % 4),
      '=',
    )
    const decoded = JSON.parse(atob(paddedPayload)) as { session_id?: unknown }

    return typeof decoded.session_id === 'string' ? decoded.session_id : null
  } catch {
    return null
  }
}
