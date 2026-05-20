import { z } from 'zod'

export const passwordRules = {
  minLength: {
    label: 'Minimo de 12 caracteres',
    test: (value: string) => value.length >= 12,
  },
  uppercase: {
    label: 'Letra maiuscula',
    test: (value: string) => /[A-Z]/.test(value),
  },
  lowercase: {
    label: 'Letra minuscula',
    test: (value: string) => /[a-z]/.test(value),
  },
  number: {
    label: 'Numero',
    test: (value: string) => /\d/.test(value),
  },
  symbol: {
    label: 'Simbolo especial',
    test: (value: string) => /[^A-Za-z0-9]/.test(value),
  },
} as const

const emailSchema = z
  .string()
  .trim()
  .min(1, 'Informe o e-mail.')
  .email('Informe um e-mail valido.')

const passwordSchema = z
  .string()
  .min(1, 'Informe a senha.')
  .refine(passwordRules.minLength.test, passwordRules.minLength.label)
  .refine(passwordRules.uppercase.test, passwordRules.uppercase.label)
  .refine(passwordRules.lowercase.test, passwordRules.lowercase.label)
  .refine(passwordRules.number.test, passwordRules.number.label)
  .refine(passwordRules.symbol.test, passwordRules.symbol.label)

export const registerSchema = z
  .object({
    email: emailSchema,
    password: passwordSchema,
    confirmPassword: z.string().min(1, 'Confirme a senha.'),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'As senhas devem ser iguais.',
    path: ['confirmPassword'],
  })

export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, 'Informe a senha.'),
})

export type RegisterFormData = z.infer<typeof registerSchema>
export type LoginFormData = z.infer<typeof loginSchema>
