export function extractTotpSecret(otpauthUrl: string): string | null {
  try {
    const url = new URL(otpauthUrl)
    return url.searchParams.get('secret')
  } catch {
    return null
  }
}

export function formatTotpSecret(secret: string): string {
  return secret.match(/.{1,5}/g)?.join('-') ?? secret
}
