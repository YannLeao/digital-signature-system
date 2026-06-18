const actionLabels: Record<string, string> = {
  LOGIN: 'Login realizado',
  LOGOUT: 'Logout',
  AUTH_FAIL: 'Falha de autenticacao',
  TOKEN_ISSUED: 'Token emitido',
  DOC_SIGNED: 'Documento assinado',
  DOC_VERIFIED: 'Documento verificado',
  PASSWORD_CHANGED: 'Senha alterada',
}

export function formatActivityAction(action: string): string {
  return actionLabels[action] ?? action.replaceAll('_', ' ').toLowerCase()
}

export function safeActivityMetadata(
  metadata: Record<string, unknown> | null,
): string {
  if (!metadata) {
    return ''
  }

  const safeKeys = ['status', 'signatureId', 'flow', 'reason']
  const parts = safeKeys
    .map((key) => {
      const value = metadata[key]
      return typeof value === 'string' && value ? `${key}: ${value}` : null
    })
    .filter(Boolean)

  return parts.join(' | ')
}

export function formatActivityMetadata(
  metadata: Record<string, unknown> | null,
): string {
  return safeActivityMetadata(metadata) || 'Sem detalhes'
}
