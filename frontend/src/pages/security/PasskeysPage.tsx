import { useState } from 'react'

import { AddPasskeyButton } from '../../components/passkeys/AddPasskeyButton'
import { PasskeyList } from '../../components/passkeys/PasskeyList'
import { RevokePasskeyDialog } from '../../components/passkeys/RevokePasskeyDialog'
import { useAuth } from '../../hooks/useAuth'
import { usePasskeys } from '../../hooks/usePasskeys'
import type { PasskeyDevice } from '../../types/passkey'
import { isWebAuthnSupported } from '../../utils/webauthn'

export function PasskeysPage() {
  const { email } = useAuth()
  const {
    devices,
    error,
    isAdding,
    isLoading,
    registerPasskey,
    revokeDevice,
  } = usePasskeys(email)
  const [deviceToRevoke, setDeviceToRevoke] = useState<PasskeyDevice | null>(
    null,
  )
  const browserSupported = isWebAuthnSupported()

  async function handleConfirmRevoke(device: PasskeyDevice) {
    await revokeDevice(device.id)
    setDeviceToRevoke(null)
  }

  return (
    <section className="max-w-4xl">
      <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
        Seguranca da conta
      </p>
      <h1 className="text-3xl font-semibold text-[#F9FAFB]">
        Gerenciar passkeys
      </h1>
      <p className="mt-4 max-w-2xl text-base leading-7 text-[#9CA3AF]">
        Adicione autenticadores do dispositivo para entrar com passkey e revogue
        acessos que nao devem mais ser aceitos.
      </p>

      <div className="mt-8 rounded-xl border border-[#374151] bg-[#1F2937] p-5">
        {!browserSupported ? (
          <div className="mb-5 rounded-lg border border-[#F59E0B]/40 bg-[#F59E0B]/10 px-4 py-3 text-sm text-amber-100">
            Este navegador nao oferece suporte a passkeys.
          </div>
        ) : null}

        {!email ? (
          <div className="rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
            Nao foi possivel identificar o e-mail da sessao. Entre novamente
            para gerenciar passkeys.
          </div>
        ) : null}

        {error ? (
          <div className="mb-5 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
            {error}
          </div>
        ) : null}

        <PasskeyList
          devices={devices}
          isLoading={isLoading}
          onRevoke={setDeviceToRevoke}
        />

        <AddPasskeyButton
          disabled={!email || !browserSupported || isAdding}
          onAdd={registerPasskey}
        />
      </div>

      <RevokePasskeyDialog
        device={deviceToRevoke}
        onCancel={() => setDeviceToRevoke(null)}
        onConfirm={handleConfirmRevoke}
      />
    </section>
  )
}
