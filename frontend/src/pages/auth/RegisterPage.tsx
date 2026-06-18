import {zodResolver} from '@hookform/resolvers/zod'
import {useState} from 'react'
import {useForm, useWatch} from 'react-hook-form'
import {Link, useNavigate} from 'react-router-dom'

import {AuthCard} from '../../components/auth/AuthCard'
import {AuthFormField} from '../../components/auth/AuthFormField'
import {PasswordStrengthIndicator} from '../../components/auth/PasswordStrengthIndicator'
import {getAuthErrorMessage, registerUser} from '../../services/authService'
import {type RegisterFormData, registerSchema} from '../../schemas/authSchemas'
import {AlertCircle} from "lucide-react";

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
              <Link
                  className="font-semibold text-[#06B6D4] hover:text-cyan-300 underline-offset-4 hover:underline transition-all"
                  to="/login"
              >
          Entrar
        </Link>
      </span>
          }
          title="Criar nova conta"
      >
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <AuthFormField
              autoComplete="email"
              error={errors.email}
              label="E-mail"
              placeholder="seu-email@universidade.edu"
              registration={register('email')}
              type="email"
          />
          <AuthFormField
              autoComplete="new-password"
              error={errors.password}
              label="Senha"
              placeholder="Senha robusta"
              registration={register('password')}
              type="password"
          />

          <PasswordStrengthIndicator password={password} />

          <AuthFormField
              autoComplete="new-password"
              error={errors.confirmPassword}
              label="Confirmação de senha"
              placeholder="Repita a senha idêntica"
              registration={register('confirmPassword')}
              type="password"
          />

          {apiError ? (
              <div className="flex items-center gap-2 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-3 py-2.5 text-sm text-red-200">
                <AlertCircle className="h-4 w-4 shrink-0 text-[#EF4444]" />
                <span>{apiError}</span>
              </div>
          ) : null}

          <button
              className="w-full flex items-center justify-center gap-2 rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-[#0B1120] transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60 font-bold"
              disabled={isSubmitting}
              type="submit"
          >
            {isSubmitting ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-[#0B1120] border-t-transparent" />
                  Cadastrando...
                </>
            ) : (
                'Confirmar Cadastro'
            )}
          </button>
        </form>
      </AuthCard>
  )
}
