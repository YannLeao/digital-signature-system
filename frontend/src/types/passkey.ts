import type { AuthResponse } from './auth'

export type PasskeyDevice = {
  id: number
  deviceName: string
  createdAt: string
  lastUsed: string | null
  active: boolean
}

export type PasskeyStartRequest = {
  email: string
}

export type PasskeyRegisterStartResponse = PublicKeyCredentialCreationOptions

export type PasskeyRegisterFinishRequest = {
  email: string
  credential: string
  deviceName: string
}

export type PasskeyAuthStartResponse = PublicKeyCredentialRequestOptions

export type PasskeyAuthFinishRequest = {
  email: string
  credential: string
}

export type PasskeyAuthResponse = AuthResponse

export type SerializedRegistrationCredential = {
  id: string
  rawId: string
  type: PublicKeyCredentialType
  response: {
    attestationObject: string
    clientDataJSON: string
    transports?: AuthenticatorTransport[]
  }
  authenticatorAttachment?: AuthenticatorAttachment | null
  clientExtensionResults: AuthenticationExtensionsClientOutputs
}

export type SerializedAuthenticationCredential = {
  id: string
  rawId: string
  type: PublicKeyCredentialType
  response: {
    authenticatorData: string
    clientDataJSON: string
    signature: string
    userHandle: string | null
  }
  authenticatorAttachment?: AuthenticatorAttachment | null
  clientExtensionResults: AuthenticationExtensionsClientOutputs
}

