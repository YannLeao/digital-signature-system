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

type PublicKeyCredentialWithAttachment = PublicKeyCredential & {
  authenticatorAttachment?: AuthenticatorAttachment | null
}

type RegistrationResponseWithTransports = AuthenticatorAttestationResponse & {
  getTransports?: () => AuthenticatorTransport[]
}

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

export function toCredentialCreationOptions(
  options: PublicKeyCredentialCreationOptions,
): CredentialCreationOptions {
  return {
    publicKey: {
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
    },
  }
}

export function toCredentialRequestOptions(
  options: PublicKeyCredentialRequestOptions,
  mediation?: CredentialMediationRequirement,
  signal?: AbortSignal,
): CredentialRequestOptions {
  return {
    mediation,
    publicKey: {
      ...options,
      allowCredentials: options.allowCredentials?.map((credential) => ({
        ...credential,
        id: base64UrlToArrayBuffer(String(credential.id)),
      })),
      challenge: base64UrlToArrayBuffer(String(options.challenge)),
    },
    signal,
  }
}

export function serializeRegistrationCredential(
  credential: PublicKeyCredential,
): string {
  const response = credential.response as RegistrationResponseWithTransports
  const credentialWithAttachment = credential as PublicKeyCredentialWithAttachment
  const serialized: SerializedRegistrationCredential = {
    authenticatorAttachment: credentialWithAttachment.authenticatorAttachment,
    clientExtensionResults: credential.getClientExtensionResults(),
    id: credential.id,
    rawId: arrayBufferToBase64Url(credential.rawId),
    response: {
      attestationObject: arrayBufferToBase64Url(response.attestationObject),
      clientDataJSON: arrayBufferToBase64Url(response.clientDataJSON),
      transports: response.getTransports?.() as
        | AuthenticatorTransport[]
        | undefined,
    },
    type: credential.type as PublicKeyCredentialType,
  }

  return JSON.stringify(serialized)
}

export function serializeAuthenticationCredential(
  credential: PublicKeyCredential,
): string {
  const response = credential.response as AuthenticatorAssertionResponse
  const credentialWithAttachment = credential as PublicKeyCredentialWithAttachment
  const serialized: SerializedAuthenticationCredential = {
    authenticatorAttachment: credentialWithAttachment.authenticatorAttachment,
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
