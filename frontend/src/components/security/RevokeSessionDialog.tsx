import type { ActiveSession } from '../../types/security'
import { formatDeviceName } from '../../utils/formatUserAgent'

type RevokeSessionDialogProps = {
  isSubmitting: boolean
  session: ActiveSession | null
  onCancel: () => void
  onConfirm: (session: ActiveSession) => Promise<void>
}

export function RevokeSessionDialog({
  isSubmitting,
  session,
  onCancel,
  onConfirm,
}: RevokeSessionDialogProps) {
  if (!session) {
    return null
  }

  return (
    <div
      aria-labelledby="revoke-session-title"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-[#020617]/80 px-4"
      role="dialog"
    >
      <div className="w-full max-w-md rounded-xl border border-[#374151] bg-[#111827] p-6 shadow-2xl">
        <h2
          className="text-lg font-semibold text-[#F9FAFB]"
          id="revoke-session-title"
        >
          Encerrar sessao
        </h2>
        <p className="mt-3 text-sm leading-6 text-[#9CA3AF]">
          A sessao em {formatDeviceName(session.deviceInfo, session.userAgent)}{' '}
          sera revogada.
          {session.current
            ? ' Como esta e a sessao atual, voce voltara para o login.'
            : null}
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <button
            className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
            disabled={isSubmitting}
            onClick={onCancel}
            type="button"
          >
            Cancelar
          </button>
          <button
            className="rounded-lg bg-[#EF4444] px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-400 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={isSubmitting}
            onClick={() => void onConfirm(session)}
            type="button"
          >
            {isSubmitting ? 'Encerrando...' : 'Encerrar'}
          </button>
        </div>
      </div>
    </div>
  )
}
