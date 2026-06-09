import { z } from 'zod'

const maxPdfSizeBytes = 20 * 1024 * 1024

export const pdfFileSchema = z
  .instanceof(File, { message: 'Selecione um PDF.' })
  .refine((file) => file.size > 0, 'Documento PDF invalido.')
  .refine((file) => file.size <= maxPdfSizeBytes, 'O PDF deve ter ate 20MB.')
  .refine(
    (file) => file.type === 'application/pdf',
    'O arquivo deve ser um PDF.',
  )
  .refine(
    (file) => file.name.toLowerCase().endsWith('.pdf'),
    'O arquivo deve ter extensao .pdf.',
  )

export const signDocumentRequestSchema = z.object({
  sealPage: z.number().int().min(1, 'Pagina selecionada e invalida.'),
  sealX: z.number().min(0, 'Posicao do selo invalida.'),
  sealY: z.number().min(0, 'Posicao do selo invalida.'),
})
