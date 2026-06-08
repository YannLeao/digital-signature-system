type AuthSession = {
  accessToken: string | null
  email: string | null
}

type AccessTokenListener = (session: AuthSession) => void

const authSession: AuthSession = {
  accessToken: null,
  email: null,
}
const listeners = new Set<AccessTokenListener>()

export function getAccessToken(): string | null {
  return authSession.accessToken
}

export function getAuthenticatedEmail(): string | null {
  return authSession.email
}

export function setAccessToken(token: string): void {
  authSession.accessToken = token
  notifyListeners()
}

export function setAuthenticatedEmail(email: string): void {
  authSession.email = email.trim().toLowerCase()
  notifyListeners()
}

export function setAuthSession(token: string, email: string): void {
  authSession.accessToken = token
  authSession.email = email.trim().toLowerCase()
  notifyListeners()
}

export function clearAccessToken(): void {
  authSession.accessToken = null
  authSession.email = null
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

