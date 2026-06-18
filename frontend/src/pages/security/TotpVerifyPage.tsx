import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { ShieldCheck, AlertTriangle, Key } from 'lucide-react'

import { TotpCodeInput } from '../../components/totp/TotpCodeInput'
import { useAuth } from '../../hooks/useAuth'
import { backupCodeSchema, totpCodeSchema } from '../../schemas/totpSchemas'
import { getAuthErrorMessage } from '../../services/authService'
import { verifyBackupCode, verifyTotpLogin } from '../../services/totpService'

export function TotpVerifyPage() {
  const navigate = useNavigate()
  const { isAuthenticated, isTwoFactorPending } = useAuth()
  const [code, setCode] = useState('')
  const [backupCode, setBackupCode] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isPending, setIsPending] = useState(false)
  const [useRecoveryCode, setUseRecoveryCode] = useState(false)

  async function submitTotp(nextCode: string) {
    const parsedCode = totpCodeSchema.safeParse(nextCode)
    if (!parsedCode.success || isPending) return

    setIsPending(true)
    setError(null)

    try {
      await verifyTotpLogin({ code: parsedCode.data })
      navigate('/app', { replace: true })
    } catch (requestError) {
      setError(getAuthErrorMessage(requestError, 'Código inválido ou expirado.'))
    } finally {
      setIsPending(false)
    }

  }

  async function submitBackupCode(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const parsedCode = backupCodeSchema.safeParse(backupCode)
    if (!parsedCode.success || isPending) {
      setError(parsedCode.error?.issues[0]?.message ?? 'Código inválido.')
      return
    }

    setIsPending(true)
    setError(null)

    try {
      await verifyBackupCode({ code: parsedCode.data.toUpperCase() })
      navigate('/app', { replace: true })
    } catch (requestError) {
      setError(getAuthErrorMessage(requestError, 'Código de recuperação inválido.'))
    } finally {
      setIsPending(false)
    }
  }

  if (isAuthenticated) return <Navigate replace to="/app" />
  if (!isTwoFactorPending) return <Navigate replace to="/login" />

  return (
      <div className="flex min-h-screen items-center justify-center bg-[#0B0F19] p-4">
        <section className="mx-auto w-full max-w-md rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-2xl shadow-black/30 animate-fade-in">
          <div className="mb-6 flex flex-col items-center text-center sm:items-start sm:text-left">
            <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 text-[#67E8F9]">
              {useRecoveryCode ? <Key className="h-5 w-5" /> : <ShieldCheck className="h-5 w-5" />}
            </div>
            <p className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4]">
              Segundo Fator de Autenticação
            </p>
            <h1 className="mt-1 text-2xl font-bold text-[#F9FAFB] tracking-tight">
              {useRecoveryCode ? 'Código de Recuperação' : 'Verificação de Segurança'}
            </h1>
            <p className="mt-2 text-sm text-[#9CA3AF]">
              {useRecoveryCode
                  ? 'Insira um dos seus códigos de segurança de 20 caracteres gerados no momento da configuração.'
                  : 'Abra seu app autenticador para visualizar o token dinâmico atual.'}
            </p>
          </div>

          {!useRecoveryCode ? (
              <div className="space-y-5">
                <TotpCodeInput
                    disabled={isPending}
                    onChange={setCode}
                    onComplete={(nextCode) => void submitTotp(nextCode)}
                    value={code}
                    error={!!error}
                />
                <button
                    className="w-full rounded-lg bg-[#06B6D4] py-3 text-sm font-semibold text-[#111827] transition hover:bg-[#22D3EE] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/20 disabled:cursor-not-allowed disabled:opacity-50"
                    disabled={isPending || code.length !== 6}
                    onClick={() => void submitTotp(code)}
                    type="button"
                >
                  {isPending ? 'Validando token...' : 'Acessar conta'}
                </button>
              </div>
          ) : (
              <form className="space-y-5" onSubmit={submitBackupCode}>
                <label className="block group">
              <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-[#9CA3AF] group-focus-within:text-[#06B6D4] transition-colors">
                Código de Contingência
              </span>
                  <input
                      autoComplete="off"
                      className={`w-full rounded-lg border bg-[#111827] px-4 py-3 font-mono text-sm uppercase tracking-wider text-[#F9FAFB] outline-none transition-all placeholder:text-[#6B7280] focus:ring-4 ${
                          error ? 'border-red-500/50 focus:border-red-500 focus:ring-red-500/10' : 'border-[#374151] focus:border-[#06B6D4] focus:ring-[#06B6D4]/10'
                      }`}
                      disabled={isPending}
                      maxLength={20}
                      name="backupCode"
                      onChange={(event) =>
                          setBackupCode(
                              event.target.value.replace(/[^A-Fa-f0-9]/g, '').slice(0, 20).toUpperCase()
                          )
                      }
                      placeholder="XXXX-XXXX-XXXX-XXXX"
                      type="text"
                      value={backupCode}
                  />
                </label>
                <button
                    className="w-full rounded-lg bg-[#06B6D4] py-3 text-sm font-semibold text-[#111827] transition hover:bg-[#22D3EE] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/20 disabled:cursor-not-allowed disabled:opacity-50"
                    disabled={isPending || backupCode.length !== 20}
                    type="submit"
                >
                  {isPending ? 'Verificando chave...' : 'Autenticar com código'}
                </button>
              </form>
          )}

          {error && (
              <div className="mt-4 flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/5 px-4 py-2.5 text-xs font-medium text-red-400">
                <AlertTriangle className="h-3.5 w-3.5 shrink-0" />
                <span>{error}</span>
              </div>
          )}

          <div className="mt-6 border-t border-[#374151] pt-4 text-center">
            <button
                className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4] hover:text-[#22D3EE] transition-colors focus:outline-none"
                onClick={() => {
                  setError(null)
                  setUseRecoveryCode((current) => !current)
                }}
                type="button"
            >
              {useRecoveryCode ? 'Usar token dinâmico' : 'Problemas com o app? Usar código reserva'}
            </button>
          </div>
        </section>
      </div>
  )
}
