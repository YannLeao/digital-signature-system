import type { FormEvent } from 'react'

import { TotpCodeInput } from './TotpCodeInput'
import { TotpQrCode } from './TotpQrCode'
import { TotpSecretField } from './TotpSecretField'

type TotpSetupCardProps = {
  code: string
  error?: string | null
  isConfirming?: boolean
  isStarting?: boolean
  manualSecret: string
  onCodeChange: (code: string) => void
  onConfirm: (code: string) => Promise<void>
  onStart: () => Promise<void>
  otpauthUrl?: string
}

export function TotpSetupCard({
  code,
  error,
  isConfirming,
  isStarting,
  manualSecret,
  onCodeChange,
  onConfirm,
  onStart,
  otpauthUrl,
}: TotpSetupCardProps) {
  async function submitConfirmation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await onConfirm(code)
  }

  if (!otpauthUrl) {
    return (
      <div className="rounded-xl border border-[#374151] bg-[#1F2937] p-5">
        <h2 className="text-lg font-semibold text-[#F9FAFB]">
          Autenticacao em duas etapas
        </h2>
        <p className="mt-2 text-sm leading-6 text-[#9CA3AF]">
          Configure um aplicativo autenticador para proteger o login com um
          segundo fator.
        </p>
        {error ? (
          <div className="mt-4 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
            {error}
          </div>
        ) : null}
        <button
          className="mt-5 rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isStarting}
          onClick={() => void onStart()}
          type="button"
        >
          {isStarting ? 'Gerando configuracao...' : 'Ativar autenticacao em duas etapas'}
        </button>
      </div>
    )
  }

  return (
    <div className="rounded-xl border border-[#374151] bg-[#1F2937] p-5">
      <h2 className="text-lg font-semibold text-[#F9FAFB]">
        Escaneie o QR Code
      </h2>
      <p className="mt-2 text-sm leading-6 text-[#9CA3AF]">
        Use Google Authenticator, Microsoft Authenticator, Authy ou app
        compativel. Depois, informe o codigo de 6 digitos para confirmar.
      </p>
      <div className="mt-5 flex flex-col gap-5 lg:flex-row">
        <TotpQrCode otpauthUrl={otpauthUrl} />
        <div className="flex-1 space-y-4">
          <TotpSecretField secret={manualSecret} />
          <form className="space-y-4" onSubmit={submitConfirmation}>
            <TotpCodeInput
              disabled={isConfirming}
              onChange={onCodeChange}
              onComplete={(nextCode) => void onConfirm(nextCode)}
              value={code}
            />
            {error ? (
              <div className="rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
                {error}
              </div>
            ) : null}
            <button
              className="rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
              disabled={isConfirming || code.length !== 6}
              type="submit"
            >
              {isConfirming ? 'Confirmando...' : 'Confirmar ativacao'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

