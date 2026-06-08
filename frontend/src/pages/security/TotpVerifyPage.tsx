import { useActionState, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'

import { TotpCodeInput } from '../../components/totp/TotpCodeInput'
import { useAuth } from '../../hooks/useAuth'
import { backupCodeSchema, totpCodeSchema } from '../../schemas/totpSchemas'
import { getAuthErrorMessage } from '../../services/authService'
import { verifyBackupCode, verifyTotpLogin } from '../../services/totpService'

export function TotpVerifyPage() {
  const navigate = useNavigate()
  const { isAuthenticated, isTwoFactorPending } = useAuth()
  const [code, setCode] = useState('')
  const [useRecoveryCode, setUseRecoveryCode] = useState(false)

  const [totpError, submitTotp, isTotpPending] = useActionState(
      async (_: unknown, nextCode: string) => {
        const parsedCode = totpCodeSchema.safeParse(nextCode)
        if (!parsedCode.success) return null

        try {
          await verifyTotpLogin({ code: parsedCode.data })
          navigate('/app', { replace: true })
          return null
        } catch (requestError) {
          return getAuthErrorMessage(requestError, 'Codigo invalido ou expirado.')
        }
      },
      null,
  )

  const [backupError, backupAction, isBackupPending] = useActionState(
      async (_: unknown, formData: FormData) => {
        const raw = formData.get('backupCode') as string
        const parsedCode = backupCodeSchema.safeParse(raw)

        if (!parsedCode.success) {
          return parsedCode.error.issues[0]?.message ?? 'Codigo invalido.'
        }

        try {
          await verifyBackupCode({ code: parsedCode.data.toUpperCase() })
          navigate('/app', { replace: true })
          return null
        } catch (requestError) {
          return getAuthErrorMessage(requestError, 'Codigo de recuperacao invalido.')
        }
      },
      null,
  )

  if (isAuthenticated) {
    return <Navigate replace to="/app" />
  }

  if (!isTwoFactorPending) {
    return <Navigate replace to="/login" />
  }

  const isPending = isTotpPending || isBackupPending
  const error = totpError ?? backupError

  return (
      <section className="mx-auto max-w-md">
        <div className="rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-2xl">
          <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
            Verificacao em duas etapas
          </p>
          <h1 className="text-2xl font-semibold text-[#F9FAFB]">
            Informe o codigo do autenticador
          </h1>
          <p className="mt-3 text-sm leading-6 text-[#9CA3AF]">
            Abra seu aplicativo autenticador e digite o codigo de 6 digitos para
            concluir o login.
          </p>

          {!useRecoveryCode ? (
              <div className="mt-6 space-y-4">
                <TotpCodeInput
                    disabled={isPending}
                    onChange={setCode}
                    onComplete={(nextCode) => submitTotp(nextCode)}
                    value={code}
                />
                <button
                    className="w-full rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
                    disabled={isPending || code.length !== 6}
                    onClick={() => submitTotp(code)}
                    type="button"
                >
                  {isTotpPending ? 'Verificando...' : 'Verificar'}
                </button>
              </div>
          ) : (
              <form action={backupAction} className="mt-6 space-y-4">
                <label className="block">
              <span className="mb-2 block text-sm font-medium text-[#F9FAFB]">
                Codigo de recuperacao
              </span>
                  <input
                      autoComplete="off"
                      className="w-full rounded-lg border border-[#374151] bg-[#111827] px-3 py-3 font-mono text-[#F9FAFB] outline-none transition placeholder:text-[#6B7280] focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/30"
                      disabled={isPending}
                      maxLength={20}
                      name="backupCode"
                      onChange={(event) => {
                        event.target.value = event.target.value
                            .replace(/[^A-Fa-f0-9]/g, '')
                            .slice(0, 20)
                      }}
                      placeholder="Codigo de 20 caracteres"
                      type="text"
                  />
                </label>
                <button
                    className="w-full rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
                    disabled={isPending}
                    type="submit"
                >
                  {isBackupPending ? 'Verificando...' : 'Usar codigo'}
                </button>
              </form>
          )}

          {error ? (
              <div className="mt-4 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
                {error}
              </div>
          ) : null}

          <button
              className="mt-5 text-sm font-medium text-[#06B6D4] transition hover:text-cyan-300"
              onClick={() => setUseRecoveryCode((current) => !current)}
              type="button"
          >
            {useRecoveryCode
                ? 'Usar codigo do autenticador'
                : 'Usar codigo de recuperacao'}
          </button>
        </div>
      </section>
  )
}