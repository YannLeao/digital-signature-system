import {AlertTriangle, ShieldAlert} from 'lucide-react'
import type {PasskeyDevice} from '../../types/passkey'

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
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#020617]/70 backdrop-blur-sm px-4 animate-fade-in"
          role="dialog"
      >
        <div className="w-full max-w-md rounded-xl border border-rose-500/20 bg-[#111827] p-6 shadow-2xl shadow-black/80 border-t-4 border-t-rose-500">
          <div className="flex items-center gap-2.5 text-rose-400">
            <ShieldAlert className="h-5 w-5" />
            <h2 className="text-lg font-bold text-[#F9FAFB] tracking-tight" id="revoke-passkey-title">
              Revogar passkey
            </h2>
          </div>

          <p className="mt-3 text-sm leading-relaxed text-[#9CA3AF]">
            Você está prestes a revogar o acesso do dispositivo <span className="font-semibold text-white">"{device.deviceName}"</span>[cite: 26]. Esta ação removerá a autenticação biométrica associada a ele[cite: 26].
          </p>

          <div className="mt-4 flex items-start gap-2 rounded-lg bg-rose-500/10 px-3 py-2.5 text-xs text-rose-300 border border-rose-500/20">
            <AlertTriangle className="h-4 w-4 shrink-0 text-rose-500 mt-0.5" />
            <span>O login convencional por e-mail e senha continuará operando normalmente[cite: 26].</span>
          </div>

          <div className="mt-6 flex justify-end gap-3">
            <button
                className="rounded-lg border border-[#374151] bg-[#111827] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
                onClick={onCancel}
                type="button"
            >
              Cancelar
            </button>
            <button
                className="rounded-lg bg-rose-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-rose-500 shadow-md shadow-rose-950/20"
                onClick={() => void onConfirm(device)}
                type="button"
            >
              Confirmar Revogação
            </button>
          </div>
        </div>
      </div>
  )
}