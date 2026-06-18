import {ShieldAlert, X} from 'lucide-react'

type RevokeAllSessionsDialogProps = {
  isOpen: boolean
  isSubmitting: boolean
  onCancel: () => void
  onConfirm: () => Promise<void>
}

export function RevokeAllSessionsDialog({
                                          isOpen,
                                          isSubmitting,
                                          onCancel,
                                          onConfirm,
                                        }: RevokeAllSessionsDialogProps) {
  if (!isOpen) return null

  return (
      <div
          aria-labelledby="revoke-all-sessions-title"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-[#020617]/70 backdrop-blur-sm px-4 animate-fade-in"
          role="dialog"
      >
        <div className="relative w-full max-w-md rounded-xl border border-[#374151] bg-[#111827] p-6 shadow-2xl">
          <button
              className="absolute right-4 top-4 text-[#6B7280] hover:text-[#F9FAFB] transition-colors"
              onClick={onCancel}
              disabled={isSubmitting}
          >
            <X className="h-4 w-4" />
          </button>

          <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-red-500/30 bg-red-500/10 text-red-400 mb-4">
            <ShieldAlert className="h-5 w-5" />
          </div>

          <h2 className="text-lg font-bold text-[#F9FAFB] tracking-tight" id="revoke-all-sessions-title">
            Revogar todos os acessos?
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-[#9CA3AF]">
            Esta ação forçará o encerramento de <strong className="text-red-400 font-medium">todas as sessões ativas</strong> vinculadas a esta conta. Todos os dispositivos conectados perderão o token de autenticação instantaneamente.
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
                onClick={() => void onConfirm()}
                type="button"
            >
              {isSubmitting ? 'Revogando credenciais...' : 'Confirmar e sair de todos'}
            </button>
          </div>
        </div>
      </div>
  )
}
