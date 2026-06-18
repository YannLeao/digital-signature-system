import {type FormEvent, useState} from 'react'
import {Filter, RotateCcw} from 'lucide-react'
import type {ActivityFilters} from '../../types/security'

type ActivityLogFiltersProps = {
  disabled: boolean
  filters: ActivityFilters
  onApply: (filters: Partial<ActivityFilters>) => void
  onReset: () => void
}

export function ActivityLogFilters({ disabled, filters, onApply, onReset }: ActivityLogFiltersProps) {
  const [draft, setDraft] = useState(filters)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onApply(draft)
  }

  function handleReset() {
    setDraft({ action: '', result: '', from: '', to: '', page: 0, size: 10 })
    onReset()
  }

  const selectStyle = "mt-1.5 w-full rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-sm text-[#F9FAFB] outline-none transition-all focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/10 disabled:opacity-50"

  return (
      <form
          className="mt-5 grid gap-4 rounded-xl border border-[#374151] bg-[#111827] p-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-[1fr_1fr_1fr_1fr_auto_auto] items-end"
          onSubmit={handleSubmit}
      >
        <label className="block text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
          Ação Audita
          <select
              className={selectStyle}
              disabled={disabled}
              onChange={(e) => setDraft((c) => ({ ...c, action: e.target.value }))}
              value={draft.action}
          >
            <option value="">Todas as ações</option>
            <option value="LOGIN">Autenticação (Login)</option>
            <option value="LOGOUT">Desconexão (Logout)</option>
            <option value="AUTH_FAIL">Falha Crítica de Login</option>
            <option value="DOC_SIGNED">Documento Assinado</option>
            <option value="DOC_VERIFIED">Documento Verificado</option>
            <option value="TWO_FACTOR_CHANGED">Modificação 2FA</option>
          </select>
        </label>

        <label className="block text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
          Resultado
          <select
              className={selectStyle}
              disabled={disabled}
              onChange={(e) => setDraft((c) => ({ ...c, result: e.target.value }))}
              value={draft.result}
          >
            <option value="">Todos</option>
            <option value="SUCCESS">Sucesso</option>
            <option value="FAILURE">Bloqueio/Falha</option>
          </select>
        </label>

        <label className="block text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
          Período Inicial
          <input
              className={selectStyle}
              disabled={disabled}
              onChange={(e) => setDraft((c) => ({ ...c, from: e.target.value }))}
              type="date"
              value={draft.from}
          />
        </label>

        <label className="block text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
          Período Final
          <input
              className={selectStyle}
              disabled={disabled}
              onChange={(e) => setDraft((c) => ({ ...c, to: e.target.value }))}
              type="date"
              value={draft.to}
          />
        </label>

        <label className="block text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
          Registros
          <select
              className={selectStyle}
              disabled={disabled}
              onChange={(e) => setDraft((c) => ({ ...c, size: Number(e.target.value) }))}
              value={draft.size}
          >
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
        </label>

        <div className="flex gap-2 w-full sm:w-auto">
          <button
              className="flex-1 sm:flex-initial flex items-center justify-center gap-1.5 h-[38px] rounded-lg bg-[#06B6D4] px-4 py-2 text-sm font-semibold text-[#111827] transition hover:bg-[#22D3EE] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/20 disabled:opacity-50"
              disabled={disabled}
              type="submit"
          >
            <Filter className="h-3.5 w-3.5" />
            Filtrar
          </button>
          <button
              className="flex-1 sm:flex-initial flex items-center justify-center gap-1.5 h-[38px] rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB] focus:outline-none disabled:opacity-50"
              disabled={disabled}
              onClick={handleReset}
              type="button"
          >
            <RotateCcw className="h-3.5 w-3.5" />
            Limpar
          </button>
        </div>
      </form>
  )
}
