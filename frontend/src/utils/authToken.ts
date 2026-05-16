const accessTokenKey = 'access_token'

export function getAccessToken(): string | null {
  return window.localStorage.getItem(accessTokenKey)
}
