import type { ActivityLogEntry } from '../../types/security'
import {
  formatActivityAction,
  formatActivityMetadata,
} from '../../utils/formatActivityAction'
import { formatSecurityDate } from '../../utils/formatSecurityDate'
import { formatUserAgent } from '../../utils/formatUserAgent'
import { ActivityResultBadge } from './ActivityResultBadge'

type ActivityLogTableProps = {
  entries: ActivityLogEntry[]
}

export function ActivityLogTable({ entries }: ActivityLogTableProps) {
  if (entries.length === 0) {
    return (
      <div className="mt-5 rounded-lg border border-[#374151] bg-[#111827] p-5 text-sm text-[#9CA3AF]">
        Nenhuma atividade encontrada para os filtros atuais.
      </div>
    )
  }

  return (
    <div className="mt-5 overflow-x-auto rounded-xl border border-[#374151] bg-[#111827]">
      <table className="min-w-full divide-y divide-[#374151] text-left text-sm">
        <thead className="bg-[#0B1120] text-xs uppercase text-[#9CA3AF]">
          <tr>
            <th className="px-4 py-3 font-medium">Data</th>
            <th className="px-4 py-3 font-medium">Acao</th>
            <th className="px-4 py-3 font-medium">Resultado</th>
            <th className="px-4 py-3 font-medium">Origem</th>
            <th className="px-4 py-3 font-medium">Detalhes</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-[#374151]">
          {entries.map((entry) => (
            <tr key={entry.id}>
              <td className="whitespace-nowrap px-4 py-3 text-[#D1D5DB]">
                {formatSecurityDate(entry.timestampUtc)}
              </td>
              <td className="px-4 py-3 font-medium text-[#F9FAFB]">
                {formatActivityAction(entry.action)}
              </td>
              <td className="px-4 py-3">
                <ActivityResultBadge result={entry.result} />
              </td>
              <td className="px-4 py-3 text-[#D1D5DB]">
                <span className="block">{entry.ip ?? 'IP indisponivel'}</span>
                <span className="mt-1 block max-w-xs truncate text-xs text-[#9CA3AF]">
                  {formatUserAgent(entry.userAgent)}
                </span>
              </td>
              <td className="px-4 py-3 text-[#D1D5DB]">
                {formatActivityMetadata(entry.metadata)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
