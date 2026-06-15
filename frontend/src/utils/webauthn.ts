import type {
  SerializedAuthenticationCredential,
  SerializedRegistrationCredential,
} from '../types/passkey'

type ConditionalCredentialContainer = CredentialsContainer & {
  preventSilentAccess?: () => Promise<void>
}

type ConditionalPublicKeyCredential = typeof PublicKeyCredential & {
  isConditionalMediationAvailable?: () => Promise<boolean>
}

type JsonObject = Record<string, unknown>

export function isWebAuthnSupported(): boolean {
  return (
    typeof window !== 'undefined' &&
    'PublicKeyCredential' in window &&
    Boolean(navigator.credentials)
  )
}

export async function isConditionalUiSupported(): Promise<boolean> {
  if (!isWebAuthnSupported()) {
    return false
  }

  const credential = PublicKeyCredential as ConditionalPublicKeyCredential

  if (!credential.isConditionalMediationAvailable) {
    return false
  }

  try {
    return credential.isConditionalMediationAvailable()
  } catch {
    return false
  }
}

export function base64UrlToArrayBuffer(value: string): ArrayBuffer {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padded = normalized.padEnd(
    normalized.length + ((4 - (normalized.length % 4)) % 4),
    '=',
  )
  const binary = window.atob(padded)
  const bytes = new Uint8Array(binary.length)

  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }

  return bytes.buffer
}

export function arrayBufferToBase64Url(value: ArrayBuffer): string {
  const bytes = new Uint8Array(value)
  let binary = ''

  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte)
  })

  return window
    .btoa(binary)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/g, '')
}

function isPlainObject(value: unknown): value is JsonObject {
  return (
    typeof value === 'object' &&
    value !== null &&
    !Array.isArray(value) &&
    !(value instanceof ArrayBuffer)
  )
}

function removeNullishValues(value: unknown): unknown {
  if (value === null || value === undefined) {
    return undefined
  }

  if (Array.isArray(value)) {
    return value.map(removeNullishValues).filter((item) => item !== undefined)
  }

  if (!isPlainObject(value)) {
    return value
  }

  return Object.fromEntries(
    Object.entries(value)
      .map(([key, item]) => [key, removeNullishValues(item)] as const)
      .filter(([, item]) => item !== undefined),
  )
}

export function toCredentialCreationOptions(
  options: PublicKeyCredentialCreationOptions,
): CredentialCreationOptions {
  const publicKey = removeNullishValues({
    ...options,
    challenge: base64UrlToArrayBuffer(String(options.challenge)),
    excludeCredentials: options.excludeCredentials?.map((credential) => ({
      ...credential,
      id: base64UrlToArrayBuffer(String(credential.id)),
    })),
    user: {
      ...options.user,
      id: base64UrlToArrayBuffer(String(options.user.id)),
    },
  }) as PublicKeyCredentialCreationOptions

  return {
    publicKey,
  }
}

export function toCredentialRequestOptions(
  options: PublicKeyCredentialRequestOptions,
  mediation?: CredentialMediationRequirement,
  signal?: AbortSignal,
): CredentialRequestOptions {
  const publicKey = removeNullishValues({
    ...options,
    allowCredentials: options.allowCredentials?.map((credential) => ({
      ...credential,
      id: base64UrlToArrayBuffer(String(credential.id)),
    })),
    challenge: base64UrlToArrayBuffer(String(options.challenge)),
  }) as PublicKeyCredentialRequestOptions

  return {
    mediation,
    publicKey,
    signal,
  }
}

export function serializeRegistrationCredential(
  credential: PublicKeyCredential,
): string {
  const response = credential.response as AuthenticatorAttestationResponse
  const serialized: SerializedRegistrationCredential = {
    clientExtensionResults: credential.getClientExtensionResults(),
    id: credential.id,
    rawId: arrayBufferToBase64Url(credential.rawId),
    response: {
      attestationObject: arrayBufferToBase64Url(response.attestationObject),
      clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
    },
    type: credential.type as PublicKeyCredentialType,
  }

  return JSON.stringify(serialized)
}

export function serializeAuthenticationCredential(
  credential: PublicKeyCredential,
): string {
  const response = credential.response as AuthenticatorAssertionResponse
  const serialized: SerializedAuthenticationCredential = {
    clientExtensionResults: credential.getClientExtensionResults(),
    id: credential.id,
    rawId: arrayBufferToBase64Url(credential.rawId),
    response: {
      authenticatorData: arrayBufferToBase64Url(response.authenticatorData),
      clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
      signature: arrayBufferToBase64Url(response.signature),
      userHandle: response.userHandle
        ? arrayBufferToBase64Url(response.userHandle)
        : null,
    },
    type: credential.type as PublicKeyCredentialType,
  }

  return JSON.stringify(serialized)
}

export async function preventSilentCredentialAccess(): Promise<void> {
  const credentials = navigator.credentials as ConditionalCredentialContainer
  await credentials.preventSilentAccess?.()
}

export function isUserCancellation(error: unknown): boolean {
  return error instanceof DOMException && error.name === 'NotAllowedError'
}

export function getWebAuthnErrorMessage(error: unknown): string | null {
  if (error instanceof DOMException) {
    if (error.name === 'NotAllowedError') {
      return 'Criacao da passkey cancelada ou tempo esgotado.'
    }

    if (error.name === 'SecurityError') {
      return 'O navegador recusou a passkey para esta origem. Verifique se esta usando localhost e a origem configurada no backend.'
    }

    if (error.name === 'NotSupportedError') {
      return 'O autenticador selecionado nao oferece suporte a esta configuracao de passkey.'
    }

    return 'O navegador nao concluiu a criacao da passkey.'
  }

  if (error instanceof TypeError) {
    return 'O navegador recusou as opcoes de criacao da passkey.'
  }

  return null
}
