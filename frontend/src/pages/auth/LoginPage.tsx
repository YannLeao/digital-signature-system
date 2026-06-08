import { zodResolver } from '@hookform/resolvers/zod'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'

import { AuthCard } from '../../components/auth/AuthCard'
import { AuthFormField } from '../../components/auth/AuthFormField'
import { useAuth } from '../../hooks/useAuth'
import { loginSchema, type LoginFormData } from '../../schemas/authSchemas'
import { getAuthErrorMessage } from '../../services/authService'
import {
  finishPasskeyAuthentication,
  startPasskeyAuthentication,
} from '../../services/passkeyService'
import {
  isConditionalUiSupported,
  isUserCancellation,
  serializeAuthenticationCredential,
  toCredentialRequestOptions,
} from '../../utils/webauthn'

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
  const [passkeyStatus, setPasskeyStatus] = useState<string | null>(null)
  const [supportsConditionalUi, setSupportsConditionalUi] = useState(false)
  const passkeyAbortController = useRef<AbortController | null>(null)

  useEffect(() => {
    let isMounted = true

    void isConditionalUiSupported().then((isSupported) => {
      if (isMounted) {
        setSupportsConditionalUi(isSupported)
        setPasskeyStatus(
          isSupported
            ? 'Passkey disponivel pelo preenchimento automatico do e-mail.'
            : null,
        )
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

      navigate('/', {
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
        navigate('/', {
          replace: true,
          state: { authMessage: 'Login com passkey realizado com sucesso.' },
        })
      } catch (error) {
        if (signal.aborted || isUserCancellation(error)) {
          return
        }

        setPasskeyStatus(null)
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
          Ainda nao tem conta?{' '}
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
          autoComplete="username webauthn"
          error={errors.email}
          label="E-mail"
          placeholder="user@example.com"
          registration={register('email')}
          type="email"
        />
        {passkeyStatus ? (
          <div className="rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 px-3 py-2 text-sm text-cyan-100">
            {passkeyStatus}
          </div>
        ) : null}
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

function isEmailLike(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
}
