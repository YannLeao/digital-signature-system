export function formatUserAgent(userAgent: string | null | undefined): string {
  if (!userAgent) {
    return 'Navegador nao informado'
  }

  const browser = browserName(userAgent)
  const system = systemName(userAgent)

  return [browser, system].filter(Boolean).join(' - ') || 'Dispositivo'
}

export function formatDeviceName(
  deviceInfo: string | null | undefined,
  userAgent: string | null | undefined,
): string {
  const normalizedDevice = deviceInfo?.trim()

  if (normalizedDevice && normalizedDevice.toLowerCase() !== 'unknown') {
    return normalizedDevice
  }

  return deviceKind(userAgent) === 'mobile' ? 'Dispositivo movel' : 'Desktop'
}

export function deviceKind(userAgent: string | null | undefined): 'mobile' | 'desktop' {
  return userAgent && /Android|iPhone|iPad|Mobile/i.test(userAgent)
    ? 'mobile'
    : 'desktop'
}

function browserName(userAgent: string): string {
  if (/Edg\//.test(userAgent)) {
    return 'Microsoft Edge'
  }
  if (/Chrome\//.test(userAgent)) {
    return 'Google Chrome'
  }
  if (/Firefox\//.test(userAgent)) {
    return 'Firefox'
  }
  if (/Safari\//.test(userAgent)) {
    return 'Safari'
  }
  return 'Navegador'
}

function systemName(userAgent: string): string {
  if (/Windows/i.test(userAgent)) {
    return 'Windows'
  }
  if (/Android/i.test(userAgent)) {
    return 'Android'
  }
  if (/iPhone|iPad/i.test(userAgent)) {
    return 'iOS'
  }
  if (/Mac OS|Macintosh/i.test(userAgent)) {
    return 'macOS'
  }
  if (/Linux/i.test(userAgent)) {
    return 'Linux'
  }
  return ''
}
