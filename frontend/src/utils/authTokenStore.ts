type AccessTokenListener = (token: string | null) => void

let accessToken: string | null = null
const listeners = new Set<AccessTokenListener>()

export function getAccessToken(): string | null {
  return accessToken
}

export function setAccessToken(token: string): void {
  accessToken = token
  notifyListeners()
}

export function clearAccessToken(): void {
  accessToken = null
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
  listeners.forEach((listener) => {
    listener(accessToken)
  })
}

