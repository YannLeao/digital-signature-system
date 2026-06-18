import type {
  ActivityFilters,
  ActivityHistoryState,
} from '../../types/security'
import { ActivityLogFilters } from './ActivityLogFilters'
import { ActivityLogTable } from './ActivityLogTable'

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
    ? `Pagina ${history.page + 1} de ${Math.max(history.totalPages, 1)}`
    : 'Pagina 1 de 1'
  const canGoPrevious = !!history && history.page > 0
  const canGoNext = !!history && history.page + 1 < history.totalPages

  return (
    <section className="rounded-xl border border-[#374151] bg-[#1F2937] p-5">
      <div>
        <p className="text-sm font-medium uppercase text-[#06B6D4]">
          Atividade
        </p>
        <h2 className="mt-1 text-2xl font-semibold text-[#F9FAFB]">
          Historico da conta
        </h2>
      </div>

      <ActivityLogFilters
        disabled={isLoading}
        filters={filters}
        onApply={onApplyFilters}
        onReset={onResetFilters}
      />

      {error ? (
        <div className="mt-5 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
          {error}
        </div>
      ) : null}

      {activity?.status === 'unavailable' ? (
        <div className="mt-5 rounded-lg border border-[#F59E0B]/40 bg-[#F59E0B]/10 px-4 py-3 text-sm text-amber-100">
          {activity.message}
        </div>
      ) : null}

      {isLoading ? (
        <div className="mt-5 rounded-lg border border-[#374151] bg-[#111827] p-5 text-sm text-[#9CA3AF]">
          Carregando historico...
        </div>
      ) : null}

      {!isLoading && history ? (
        <>
          <ActivityLogTable entries={history.items} />
          <div className="mt-4 flex flex-col gap-3 text-sm text-[#9CA3AF] sm:flex-row sm:items-center sm:justify-between">
            <span>
              {pageLabel} - {history.totalElements} registro(s)
            </span>
            <div className="flex gap-2">
              <button
                className="rounded-lg border border-[#374151] px-3 py-2 font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                disabled={!canGoPrevious || isLoading}
                onClick={onPreviousPage}
                type="button"
              >
                Anterior
              </button>
              <button
                className="rounded-lg border border-[#374151] px-3 py-2 font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white disabled:cursor-not-allowed disabled:opacity-50"
                disabled={!canGoNext || isLoading}
                onClick={onNextPage}
                type="button"
              >
                Proxima
              </button>
            </div>
          </div>
        </>
      ) : null}
    </section>
  )
}
