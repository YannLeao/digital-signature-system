import {zodResolver} from '@hookform/resolvers/zod'
import {useCallback, useEffect, useRef, useState} from 'react'
import {useForm, useWatch} from 'react-hook-form'
import {Link, useLocation, useNavigate} from 'react-router-dom'

import {AuthCard} from '../../components/auth/AuthCard'
import {AuthFormField} from '../../components/auth/AuthFormField'
import {useAuth} from '../../hooks/useAuth'
import {type LoginFormData, loginSchema} from '../../schemas/authSchemas'
import {getAuthErrorMessage} from '../../services/authService'
import {finishPasskeyAuthentication, startPasskeyAuthentication,} from '../../services/passkeyService'
import {
  isConditionalUiSupported,
  isUserCancellation,
  serializeAuthenticationCredential,
  toCredentialRequestOptions,
} from '../../utils/webauthn'
import {AlertCircle, CheckCircle} from "lucide-react";

type LoginState = {
  authMessage?: string
}

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as LoginState | null
  const { login } = useAuth()
  const [apiError, setApiError] = useState<string | null>(null)
  const {
    control,
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
  const watchedEmail = useWatch({ control, name: 'email' })
  const [supportsConditionalUi, setSupportsConditionalUi] = useState(false)
  const passkeyAbortController = useRef<AbortController | null>(null)

  useEffect(() => {
    let isMounted = true

    void isConditionalUiSupported().then((isSupported) => {
      if (isMounted) {
        setSupportsConditionalUi(isSupported)
      }
    })

    return () => {
      isMounted = false
    }
  }, [])

  async function onSubmit(data: LoginFormData) {
    setApiError(null)

    try {
      const response = await login(data)

      if (response.requiresTwoFactor) {
        navigate('/two-factor', { replace: true })
        return
      }

      navigate('/app', {
        replace: true,
        state: { authMessage: 'Login realizado com sucesso.' },
      })
    } catch (error) {
      setApiError(getAuthErrorMessage(error, 'Nao foi possivel entrar.'))
    }
  }

  const authenticateWithConditionalUi = useCallback(
    async (email: string, signal: AbortSignal) => {
      try {
        const options = await startPasskeyAuthentication({ email })

        if (signal.aborted) {
          return
        }

        const credential = await navigator.credentials.get(
          toCredentialRequestOptions(options, 'conditional', signal),
        )

        if (!(credential instanceof PublicKeyCredential)) {
          return
        }

        await finishPasskeyAuthentication({
          credential: serializeAuthenticationCredential(credential),
          email,
        })
        navigate('/app', {
          replace: true,
          state: { authMessage: 'Login com passkey realizado com sucesso.' },
        })
      } catch (error) {
        if (signal.aborted || isUserCancellation(error)) {
          return
        }

        setApiError(getAuthErrorMessage(error, 'Nao foi possivel usar a passkey.'))
      }
    },
    [navigate],
  )

  useEffect(() => {
    const email = watchedEmail?.trim()

    passkeyAbortController.current?.abort()

    if (!supportsConditionalUi || !email || !isEmailLike(email)) {
      return
    }

    const controller = new AbortController()
    passkeyAbortController.current = controller

    const timeoutId = window.setTimeout(() => {
      void authenticateWithConditionalUi(email, controller.signal)
    }, 250)

    return () => {
      window.clearTimeout(timeoutId)
      controller.abort()
    }
  }, [authenticateWithConditionalUi, supportsConditionalUi, watchedEmail])

  return (
      <AuthCard
          footer={
            <span>
        Ainda não tem conta?{' '}
              <Link
                  className="font-semibold text-[#06B6D4] hover:text-cyan-300 underline-offset-4 hover:underline transition-all"
                  to="/register"
              >
          Criar conta
        </Link>
      </span>
          }
          title="Entrar no Painel"
      >
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          {state?.authMessage ? (
              <div className="flex items-center gap-2 rounded-lg border border-[#10B981]/40 bg-[#10B981]/10 px-3 py-2.5 text-sm text-emerald-200">
                <CheckCircle className="h-4 w-4 shrink-0 text-[#10B981]" />
                <span>{state.authMessage}</span>
              </div>
          ) : null}

          <AuthFormField
              autoComplete="username webauthn"
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
              placeholder="Sua senha secreta"
              registration={register('password')}
              type="password"
          />

          {apiError ? (
              <div className="flex items-center gap-2 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-3 py-2.5 text-sm text-red-200">
                <AlertCircle className="h-4 w-4 shrink-0 text-[#EF4444]" />
                <span>{apiError}</span>
              </div>
          ) : null}

          <button
              className="w-full flex items-center justify-center gap-2 rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-[#0B1120] transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
              disabled={isSubmitting}
              type="submit"
          >
            {isSubmitting ? (
                <>
                  <span className="h-4 w-4 animate-spin rounded-full border-2 border-[#0B1120] border-t-transparent" />
                  Entrando...
                </>
            ) : (
                'Entrar na plataforma'
            )}
          </button>
        </form>
      </AuthCard>
  )
}

function isEmailLike(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
}
