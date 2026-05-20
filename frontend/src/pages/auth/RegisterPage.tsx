import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'

import { AuthCard } from '../../components/auth/AuthCard'
import { AuthFormField } from '../../components/auth/AuthFormField'
import { PasswordStrengthIndicator } from '../../components/auth/PasswordStrengthIndicator'
import { getAuthErrorMessage, registerUser } from '../../services/authService'
import { registerSchema, type RegisterFormData } from '../../schemas/authSchemas'

export function RegisterPage() {
  const navigate = useNavigate()
  const [apiError, setApiError] = useState<string | null>(null)
  const {
    control,
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      email: '',
      password: '',
      confirmPassword: '',
    },
  })

  const password = useWatch({ control, name: 'password' })

  async function onSubmit(data: RegisterFormData) {
    setApiError(null)

    try {
      await registerUser({
        email: data.email,
        password: data.password,
      })
      navigate('/login', {
        replace: true,
        state: { authMessage: 'Cadastro realizado com sucesso.' },
      })
    } catch (error) {
      setApiError(getAuthErrorMessage(error, 'Nao foi possivel cadastrar.'))
    }
  }

  return (
    <AuthCard
      footer={
        <span>
          Já tem conta?{' '}
          <Link className="font-medium text-[#06B6D4] hover:text-cyan-300" to="/login">
            Entrar
          </Link>
        </span>
      }
      title="Criar conta"
    >
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <AuthFormField
          autoComplete="email"
          error={errors.email}
          label="E-mail"
          placeholder="user@example.com"
          registration={register('email')}
          type="email"
        />
        <AuthFormField
          autoComplete="new-password"
          error={errors.password}
          label="Senha"
          placeholder="StrongPassword123!"
          registration={register('password')}
          type="password"
        />
        <PasswordStrengthIndicator password={password} />
        <AuthFormField
          autoComplete="new-password"
          error={errors.confirmPassword}
          label="Confirmação de senha"
          placeholder="Repita a senha"
          registration={register('confirmPassword')}
          type="password"
        />
        {apiError ? (
          <div className="rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-3 py-2 text-sm text-red-200">
            {apiError}
          </div>
        ) : null}
        <button
          className="w-full rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isSubmitting}
          type="submit"
        >
          {isSubmitting ? 'Cadastrando...' : 'Cadastrar'}
        </button>
      </form>
    </AuthCard>
  )
}
