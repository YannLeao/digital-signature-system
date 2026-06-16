import { z } from 'zod'

const maxPdfSizeBytes = 20 * 1024 * 1024

export const verifyPdfFileSchema = z
  .instanceof(File, { message: 'Selecione um PDF.' })
  .refine((file) => file.size > 0, 'O arquivo esta vazio.')
  .refine((file) => file.size <= maxPdfSizeBytes, 'O PDF deve ter ate 20MB.')
  .refine(
    (file) => file.type === '' || file.type === 'application/pdf',
    'O arquivo deve ser um PDF.',
  )
  .refine(
    (file) => file.name.toLowerCase().endsWith('.pdf'),
    'O arquivo deve ter extensao .pdf.',
  )

const verifySignatureDataSchema = z.object({
  signatureId: z.uuid(),
  signedAt: z.string().min(1),
  signerName: z.string().min(1),
})

export const verifyDocumentResponseSchema = z
  .object({
    status: z.enum(['VALID', 'TAMPERED', 'NOT_FOUND']),
    message: z.string().min(1),
    signature: verifySignatureDataSchema.nullish(),
  })
  .superRefine((response, context) => {
    if (response.status === 'VALID' && !response.signature) {
      context.addIssue({
        code: 'custom',
        message: 'Resposta valida sem dados da assinatura.',
        path: ['signature'],
      })
    }
  })
