import {AlertTriangle, X} from 'lucide-react'
import type {ActiveSession} from '../../types/security'
import {formatDeviceName} from '../../utils/formatUserAgent'

type RevokeSessionDialogProps = {
  isSubmitting: boolean
  session: ActiveSession | null
  onCancel: () => void
  onConfirm: (session: ActiveSession) => Promise<void>
}

export function RevokeSessionDialog({ isSubmitting, session, onCancel, onConfirm }: RevokeSessionDialogProps) {
  if (!session) return null

  return (
      <div
          aria-labelledby="revoke-session-title"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#020617]/70 backdrop-blur-sm px-4 animate-fade-in"
          role="dialog"
      >
        <div className="relative w-full max-w-md rounded-xl border border-[#374151] bg-[#111827] p-6 shadow-2xl transition-all duration-300">
          <button
              className="absolute right-4 top-4 text-[#6B7280] hover:text-[#F9FAFB] transition-colors"
              onClick={onCancel}
              disabled={isSubmitting}
          >
            <X className="h-4 w-4" />
          </button>

          <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-red-500/30 bg-red-500/10 text-red-400 mb-4">
            <AlertTriangle className="h-5 w-5" />
          </div>

          <h2 className="text-lg font-bold text-[#F9FAFB] tracking-tight" id="revoke-session-title">
            Encerrar sessão ativa?
          </h2>

          <p className="mt-2 text-sm leading-relaxed text-[#9CA3AF]">
            A sessão ativa no dispositivo <strong className="text-[#22D3EE] font-medium">{formatDeviceName(session.deviceInfo, session.userAgent)}</strong> será imediatamente revogada.
            {session.current && (
                <span className="block mt-2 rounded border border-red-500/20 bg-red-500/5 p-2 text-xs text-red-400">
              Atenção: Esta é a sua sessão atual. Ao confirmar, você será desconectado da aplicação.
            </span>
            )}
          </p>

          <div className="mt-6 flex justify-end gap-3 border-t border-[#374151]/40 pt-4">
            <button
                className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB] focus:outline-none"
                disabled={isSubmitting}
                onClick={onCancel}
                type="button"
            >
              Cancelar
            </button>
            <button
                className="rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-500 focus:outline-none focus:ring-4 focus:ring-red-500/20 disabled:opacity-50"
                disabled={isSubmitting}
                onClick={() => void onConfirm(session)}
                type="button"
            >
              {isSubmitting ? 'Encerrando sessão...' : 'Confirmar encerramento'}
            </button>
          </div>
        </div>
      </div>
  )
}
