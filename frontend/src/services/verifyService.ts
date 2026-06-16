import { api } from './api'
import { verifyDocumentResponseSchema } from '../schemas/verifyDocumentSchemas'
import type { VerifyDocumentResponse } from '../types/verify'
import { InvalidApiResponseError } from '../utils/parseApiError'

export async function verifyDocument(
  file: File,
): Promise<VerifyDocumentResponse> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await api.post<unknown>('/verify', formData, {
    headers: {
      Accept: 'application/json',
    },
  })

  const parsedResponse = verifyDocumentResponseSchema.safeParse(response.data)

  if (!parsedResponse.success) {
    throw new InvalidApiResponseError()
  }

  return parsedResponse.data
}
