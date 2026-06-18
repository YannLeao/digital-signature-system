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
  if (!isOpen) {
    return null
  }

  return (
    <div
      aria-labelledby="revoke-all-sessions-title"
      aria-modal="true"
      className="fixed inset-0 z-50 flex items-center justify-center bg-[#020617]/80 px-4"
      role="dialog"
    >
      <div className="w-full max-w-md rounded-xl border border-[#374151] bg-[#111827] p-6 shadow-2xl">
        <h2
          className="text-lg font-semibold text-[#F9FAFB]"
          id="revoke-all-sessions-title"
        >
          Sair de todos os dispositivos
        </h2>
        <p className="mt-3 text-sm leading-6 text-[#9CA3AF]">
          Todas as sessoes ativas serao encerradas e sera necessario entrar
          novamente.
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
            onClick={() => void onConfirm()}
            type="button"
          >
            {isSubmitting ? 'Saindo...' : 'Sair de todos'}
          </button>
        </div>
      </div>
    </div>
  )
}
