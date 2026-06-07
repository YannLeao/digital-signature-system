import { z } from 'zod'

export const apiFieldErrorSchema = z.object({
  field: z.string(),
  message: z.string(),
})

export const apiErrorResponseSchema = z.object({
  code: z.string(),
  message: z.string(),
  timestamp: z.string(),
  fields: z.array(apiFieldErrorSchema).optional(),
})

export const authResponseSchema = z.object({
  accessToken: z.string().min(1),
  tokenType: z.string().min(1),
  expiresIn: z.number().nonnegative(),
  requiresTwoFactor: z.boolean().optional(),
})

export const refreshResponseSchema = authResponseSchema

