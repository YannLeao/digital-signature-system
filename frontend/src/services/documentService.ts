import type { AxiosResponse } from 'axios'
import axios from 'axios'

import { api } from './api'
import { signDocumentRequestSchema } from '../schemas/signDocumentSchemas'
import { apiErrorResponseSchema } from '../schemas/apiSchemas'
import type {
  SignDocumentRequest,
  SignatureMetadata,
  SignedDocument,
} from '../types/document'
import { InvalidApiResponseError } from '../utils/parseApiError'
import { signedFilename } from '../utils/downloadPdf'

export class DocumentSignError extends Error {
  code?: string
  status?: number

  constructor(message: string, status?: number, code?: string) {
    super(message)
    this.name = 'DocumentSignError'
    this.status = status
    this.code = code
  }
}

export async function signDocument(
  file: File,
  request: SignDocumentRequest,
): Promise<SignedDocument> {
  const parsedRequest = signDocumentRequestSchema.safeParse(request)

  if (!parsedRequest.success) {
    throw new InvalidApiResponseError()
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append(
    'request',
    new Blob([JSON.stringify(parsedRequest.data)], {
      type: 'application/json',
    }),
  )

  const response = await postSignedDocument(formData)

  if (!(response.data instanceof Blob)) {
    throw new InvalidApiResponseError()
  }

  return {
    blob: response.data,
    filename: signedFilename(file.name),
    metadata: metadataFromHeaders(response),
  }
}

async function postSignedDocument(
  formData: FormData,
): Promise<AxiosResponse<Blob>> {
  try {
    return await api.post<Blob, AxiosResponse<Blob>>(
      '/documents/sign',
      formData,
      {
        responseType: 'blob',
        headers: {
          Accept: 'application/pdf',
        },
      },
    )
  } catch (error) {
    const parsedBlobError = await parseBlobApiError(error)

    if (parsedBlobError) {
      throw parsedBlobError
    }

    throw error
  }
}

async function parseBlobApiError(
  error: unknown,
): Promise<DocumentSignError | null> {
  if (!axios.isAxiosError(error) || !(error.response?.data instanceof Blob)) {
    return null
  }

  const blob = error.response.data

  if (!blob.type.includes('application/json')) {
    return null
  }

  try {
    const parsedJson = JSON.parse(await blob.text()) as unknown
    const parsedError = apiErrorResponseSchema.safeParse(parsedJson)

    if (!parsedError.success) {
      return null
    }

    return new DocumentSignError(
      parsedError.data.message,
      error.response.status,
      parsedError.data.code,
    )
  } catch {
    return null
  }
}

function metadataFromHeaders(response: AxiosResponse<Blob>): SignatureMetadata {
  const signatureId = response.headers['x-signature-id']
  const originalHash = response.headers['x-original-hash']
  const signedHash = response.headers['x-signed-hash']
  const signedAt = response.headers['x-signed-at']

  if (
    typeof signatureId !== 'string' ||
    typeof originalHash !== 'string' ||
    typeof signedHash !== 'string' ||
    typeof signedAt !== 'string'
  ) {
    throw new InvalidApiResponseError()
  }

  return {
    signatureId,
    originalHash,
    signedHash,
    signedAt,
  }
}
