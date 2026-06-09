export type StampPosition = {
  page: number
  x: number
  y: number
}

export type SignDocumentRequest = {
  sealPage: number
  sealX: number
  sealY: number
}

export type SignatureMetadata = {
  signatureId: string
  originalHash: string
  signedHash: string
  signedAt: string
}

export type SignedDocument = {
  blob: Blob
  filename: string
  metadata: SignatureMetadata
}

export type PdfPageSize = {
  width: number
  height: number
}
