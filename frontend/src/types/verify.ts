export type VerifyStatus = 'VALID' | 'TAMPERED' | 'NOT_FOUND'

export type VerifySignatureData = {
  signatureId: string
  signedAt: string
  signerName: string
}

export type VerifyDocumentResponse = {
  status: VerifyStatus
  message: string
  signature?: VerifySignatureData | null
}

export type VerifyDocumentError = {
  message: string
  status?: number
  code?: string
}
