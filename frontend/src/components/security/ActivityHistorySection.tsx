import {AlertTriangle, ChevronLeft, ChevronRight, History} from 'lucide-react'
import type {ActivityFilters, ActivityHistoryState} from '../../types/security'
import {ActivityLogFilters} from './ActivityLogFilters'
import {ActivityLogTable} from './ActivityLogTable'

type ActivityHistorySectionProps = {
  activity: ActivityHistoryState | null
  error: string | null
  filters: ActivityFilters
  isLoading: boolean
  onApplyFilters: (filters: Partial<ActivityFilters>) => void
  onNextPage: () => void
  onPreviousPage: () => void
  onResetFilters: () => void
}

export function ActivityHistorySection({
                                         activity,
                                         error,
                                         filters,
                                         isLoading,
                                         onApplyFilters,
                                         onNextPage,
                                         onPreviousPage,
                                         onResetFilters,
                                       }: ActivityHistorySectionProps) {
  const history = activity?.status === 'available' ? activity.data : null
  const pageLabel = history
      ? `Página ${history.page + 1} de ${Math.max(history.totalPages, 1)}`
      : 'Página 1 de 1'
  const canGoPrevious = !!history && history.page > 0
  const canGoNext = !!history && history.page + 1 < history.totalPages

  return (
      <section className="rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-xl shadow-black/10 animate-fade-in">
        <div className="flex items-center gap-3 border-b border-[#374151]/50 pb-4">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 text-[#67E8F9]">
            <History className="h-5 w-5" />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4]">
              Trilha de Auditoria
            </p>
            <h2 className="mt-0.5 text-xl font-bold text-[#F9FAFB] tracking-tight">
              Histórico de Atividades da Conta
            </h2>
          </div>
        </div>

        <ActivityLogFilters
            disabled={isLoading}
            filters={filters}
            onApply={onApplyFilters}
            onReset={onResetFilters}
        />

        {error && (
            <div className="mt-5 flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/5 px-4 py-3 text-sm text-red-400">
              <AlertTriangle className="h-4 w-4 shrink-0" />
              <span>{error}</span>
            </div>
        )}

        {activity?.status === 'unavailable' && (
            <div className="mt-5 flex items-center gap-2 rounded-lg border border-amber-500/40 bg-amber-500/5 px-4 py-3 text-sm text-amber-400">
              <AlertTriangle className="h-4 w-4 shrink-0" />
              <span>{activity.message}</span>
            </div>
        )}

        {isLoading ? (
            <div className="mt-5 flex items-center justify-center gap-2 rounded-lg border border-[#374151] bg-[#111827] p-8 text-sm text-[#9CA3AF] animate-pulse">
              Buscando registros no ledger de segurança...
            </div>
        ) : (
            !error && history && (
                <>
                  <ActivityLogTable entries={history.items} />

                  <div className="mt-5 flex flex-col gap-3 text-xs font-medium text-[#9CA3AF] sm:flex-row sm:items-center sm:justify-between border-t border-[#374151]/40 pt-4">
              <span className="bg-[#111827] border border-[#374151]/60 px-3 py-1.5 rounded-md font-mono">
                {pageLabel} — <strong className="text-[#22D3EE]">{history.totalElements}</strong> registro(s) encontrado(s)
              </span>

                    <div className="flex gap-2">
                      <button
                          className="flex items-center gap-1 rounded-lg border border-[#374151] bg-[#111827] px-3 py-2 text-sm font-semibold text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB] focus:outline-none disabled:cursor-not-allowed disabled:opacity-30"
                          disabled={!canGoPrevious || isLoading}
                          onClick={onPreviousPage}
                          type="button"
                      >
                        <ChevronLeft className="h-4 w-4" />
                        Anterior
                      </button>
                      <button
                          className="flex items-center gap-1 rounded-lg border border-[#374151] bg-[#111827] px-3 py-2 text-sm font-semibold text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB] focus:outline-none disabled:cursor-not-allowed disabled:opacity-30"
                          disabled={!canGoNext || isLoading}
                          onClick={onNextPage}
                          type="button"
                      >
                        Próxima
                        <ChevronRight className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </>
            )
        )}
      </section>
  )
}
