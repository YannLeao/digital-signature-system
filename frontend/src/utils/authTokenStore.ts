type AuthSession = {
  accessToken: string | null
  email: string | null
  twoFactorToken: string | null
}

type AccessTokenListener = (session: AuthSession) => void

const authSession: AuthSession = {
  accessToken: null,
  email: null,
  twoFactorToken: null,
}
const listeners = new Set<AccessTokenListener>()

export function getAccessToken(): string | null {
  return authSession.accessToken
}

export function getAuthenticatedEmail(): string | null {
  return authSession.email
}

export function getTwoFactorToken(): string | null {
  return authSession.twoFactorToken
}

export function setAccessToken(token: string): void {
  authSession.accessToken = token
  authSession.twoFactorToken = null
  notifyListeners()
}

export function setAuthenticatedEmail(email: string): void {
  authSession.email = email.trim().toLowerCase()
  notifyListeners()
}

export function setAuthSession(token: string, email: string): void {
  authSession.accessToken = token
  authSession.email = email.trim().toLowerCase()
  authSession.twoFactorToken = null
  notifyListeners()
}

export function setTwoFactorSession(token: string, email: string): void {
  authSession.accessToken = null
  authSession.email = email.trim().toLowerCase()
  authSession.twoFactorToken = token
  notifyListeners()
}

export function clearAccessToken(): void {
  authSession.accessToken = null
  authSession.email = null
  authSession.twoFactorToken = null
  notifyListeners()
}

export function subscribeToAccessToken(
  listener: AccessTokenListener,
): () => void {
  listeners.add(listener)

  return () => {
    listeners.delete(listener)
  }
}

function notifyListeners(): void {
  const snapshot = { ...authSession }

  listeners.forEach((listener) => {
    listener(snapshot)
  })
}

