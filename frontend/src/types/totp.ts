import type { AuthResponse } from './auth'

export type TotpSetupResponse = {
  otpauthUrl: string
  backupCodes: string[]
}

export type TotpVerifyRequest = {
  code: string
}

export type TotpVerifyResponse = AuthResponse

export type BackupCodesResponse = {
  backupCodes: string[]
}

export type BackupCodeVerifyRequest = {
  code: string
}

export type TotpStatus = {
  enabled: boolean
}

