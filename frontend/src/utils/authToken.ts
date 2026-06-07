const accessTokenKey = 'access_token'

let accessToken = window.localStorage.getItem(accessTokenKey)

export function getAccessToken(): string | null {
  return accessToken
}

export function setAccessToken(token: string): void {
  accessToken = token
  window.localStorage.setItem(accessTokenKey, token)
}

export function clearAccessToken(): void {
  accessToken = null
  window.localStorage.removeItem(accessTokenKey)
}
