import type { PasskeyDevice } from '../../types/passkey'

type RevokePasskeyDialogProps = {
  device: PasskeyDevice | null
  onCancel: () => void
  onConfirm: (device: PasskeyDevice) => Promise<void>
}

export function RevokePasskeyDialog({
  device,
  onCancel,
  onConfirm,
}: RevokePasskeyDialogProps) {
  if (!device) {
    return null
  }

  return (
    <div
      aria-labelledby="revoke-passkey-title"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-[#020617]/80 px-4"
      role="dialog"
    >
      <div className="w-full max-w-md rounded-xl border border-[#374151] bg-[#111827] p-6 shadow-2xl">
        <h2
          className="text-lg font-semibold text-[#F9FAFB]"
          id="revoke-passkey-title"
        >
          Revogar passkey
        </h2>
        <p className="mt-3 text-sm leading-6 text-[#9CA3AF]">
          Esta acao remove o acesso por passkey para {device.deviceName}. O
          login por senha continua disponivel.
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
            onClick={onCancel}
            type="button"
          >
            Cancelar
          </button>
          <button
            className="rounded-lg bg-[#EF4444] px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-400"
            onClick={() => void onConfirm(device)}
            type="button"
          >
            Revogar
          </button>
        </div>
      </div>
    </div>
  )
}

