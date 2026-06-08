import { z } from 'zod'

export const totpCodeSchema = z
  .string()
  .trim()
  .regex(/^\d{6}$/, 'Informe o codigo de 6 digitos.')

export const backupCodeSchema = z
  .string()
  .trim()
  .regex(/^[A-Fa-f0-9]{20}$/, 'Informe um codigo de recuperacao valido.')

export const totpSetupResponseSchema = z.object({
  backupCodes: z.array(z.string().regex(/^[A-Fa-f0-9]{20}$/)),
  otpauthUrl: z.string().startsWith('otpauth://'),
})

