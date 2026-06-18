import type { FormEvent } from 'react'
import { useState } from 'react'

import type { ActivityFilters } from '../../types/security'

type ActivityLogFiltersProps = {
  disabled: boolean
  filters: ActivityFilters
  onApply: (filters: Partial<ActivityFilters>) => void
  onReset: () => void
}

export function ActivityLogFilters({
  disabled,
  filters,
  onApply,
  onReset,
}: ActivityLogFiltersProps) {
  const [draft, setDraft] = useState(filters)

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    onApply(draft)
  }

  function handleReset() {
    setDraft({
      action: '',
      result: '',
      from: '',
      to: '',
      page: 0,
      size: 10,
    })
    onReset()
  }

  return (
    <form
      className="mt-5 grid gap-3 rounded-xl border border-[#374151] bg-[#111827] p-4 md:grid-cols-[1fr_1fr_1fr_1fr_auto_auto]"
      onSubmit={handleSubmit}
    >
      <label className="text-sm text-[#D1D5DB]">
        Acao
        <select
          className="mt-1 w-full rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-[#F9FAFB] outline-none transition focus:border-[#06B6D4]"
          disabled={disabled}
          onChange={(event) =>
            setDraft((current) => ({ ...current, action: event.target.value }))
          }
          value={draft.action}
        >
          <option value="">Todas</option>
          <option value="LOGIN">Login</option>
          <option value="LOGOUT">Logout</option>
          <option value="AUTH_FAIL">Falha de login</option>
          <option value="DOC_SIGNED">PDF assinado</option>
          <option value="DOC_VERIFIED">PDF verificado</option>
          <option value="TWO_FACTOR_CHANGED">2FA</option>
        </select>
      </label>
      <label className="text-sm text-[#D1D5DB]">
        Resultado
        <select
          className="mt-1 w-full rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-[#F9FAFB] outline-none transition focus:border-[#06B6D4]"
          disabled={disabled}
          onChange={(event) =>
            setDraft((current) => ({ ...current, result: event.target.value }))
          }
          value={draft.result}
        >
          <option value="">Todos</option>
          <option value="SUCCESS">Sucesso</option>
          <option value="FAILURE">Falha</option>
        </select>
      </label>
      <label className="text-sm text-[#D1D5DB]">
        De
        <input
          className="mt-1 w-full rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-[#F9FAFB] outline-none transition focus:border-[#06B6D4]"
          disabled={disabled}
          onChange={(event) =>
            setDraft((current) => ({ ...current, from: event.target.value }))
          }
          type="date"
          value={draft.from}
        />
      </label>
      <label className="text-sm text-[#D1D5DB]">
        Ate
        <input
          className="mt-1 w-full rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-[#F9FAFB] outline-none transition focus:border-[#06B6D4]"
          disabled={disabled}
          onChange={(event) =>
            setDraft((current) => ({ ...current, to: event.target.value }))
          }
          type="date"
          value={draft.to}
        />
      </label>
      <label className="text-sm text-[#D1D5DB]">
        Itens
        <select
          className="mt-1 w-full rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-[#F9FAFB] outline-none transition focus:border-[#06B6D4]"
          disabled={disabled}
          onChange={(event) =>
            setDraft((current) => ({
              ...current,
              size: Number(event.target.value),
            }))
          }
          value={draft.size}
        >
          <option value="10">10</option>
          <option value="20">20</option>
          <option value="50">50</option>
        </select>
      </label>
      <div className="flex items-end gap-2">
        <button
          className="rounded-lg bg-[#06B6D4] px-4 py-2 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled}
          type="submit"
        >
          Filtrar
        </button>
        <button
          className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled}
          onClick={handleReset}
          type="button"
        >
          Limpar
        </button>
      </div>
    </form>
  )
}
