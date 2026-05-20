import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'

import { AuthCard } from '../../components/auth/AuthCard'
import { AuthFormField } from '../../components/auth/AuthFormField'
import { getAuthErrorMessage, loginUser } from '../../services/authService'
import { loginSchema, type LoginFormData } from '../../schemas/authSchemas'

type LoginState = {
  authMessage?: string
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as LoginState | null
  const [apiError, setApiError] = useState<string | null>(null)
  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  })

  async function onSubmit(data: LoginFormData) {
    setApiError(null)

    try {
      await loginUser(data)
      navigate('/', {
        replace: true,
        state: { authMessage: 'Login realizado com sucesso.' },
      })
    } catch (error) {
      setApiError(getAuthErrorMessage(error, 'Nao foi possivel entrar.'))
    }
  }

  return (
    <AuthCard
      footer={
        <span>
          Ainda não tem conta?{' '}
          <Link
            className="font-medium text-[#06B6D4] hover:text-cyan-300"
            to="/register"
          >
            Criar conta
          </Link>
        </span>
      }
      title="Entrar"
    >
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        {state?.authMessage ? (
          <div className="rounded-lg border border-[#10B981]/40 bg-[#10B981]/10 px-3 py-2 text-sm text-emerald-200">
            {state.authMessage}
          </div>
        ) : null}
        <AuthFormField
          autoComplete="email"
          error={errors.email}
          label="E-mail"
          placeholder="user@example.com"
          registration={register('email')}
          type="email"
        />
        <AuthFormField
          autoComplete="current-password"
          error={errors.password}
          label="Senha"
          placeholder="Sua senha"
          registration={register('password')}
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
          {isSubmitting ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
    </AuthCard>
  )
}
