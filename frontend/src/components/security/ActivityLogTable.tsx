import type {ActivityLogEntry} from '../../types/security'
import {formatActivityAction, formatActivityMetadata,} from '../../utils/formatActivityAction'
import {formatSecurityDate} from '../../utils/formatSecurityDate'
import {formatUserAgent} from '../../utils/formatUserAgent'
import {ActivityResultBadge} from './ActivityResultBadge'

type ActivityLogTableProps = {
  entries: ActivityLogEntry[]
}

export function ActivityLogTable({ entries }: ActivityLogTableProps) {
  if (entries.length === 0) {
    return (
        <div className="mt-5 rounded-lg border border-[#374151] bg-[#111827] p-8 text-center text-sm text-[#9CA3AF]">
          Nenhuma atividade encontrada para os filtros atuais.
        </div>
    )
  }

  return (
      <div className="mt-5 overflow-x-auto rounded-xl border border-[#374151] bg-[#111827] shadow-inner">
        <table className="min-w-full divide-y divide-[#374151]/60 text-left text-sm">
          <thead className="bg-[#0B1120] text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
          <tr>
            <th className="px-5 py-3.5 font-semibold">Data / Hora</th>
            <th className="px-5 py-3.5 font-semibold">Ação</th>
            <th className="px-5 py-3.5 font-semibold">Resultado</th>
            <th className="px-5 py-3.5 font-semibold">Origem (IP / Dispositivo)</th>
            <th className="px-5 py-3.5 font-semibold">Detalhes Técnicos</th>
          </tr>
          </thead>
          <tbody className="divide-y divide-[#374151]/40 bg-[#111827]/40">
          {entries.map((entry) => (
              <tr className="transition-colors hover:bg-[#1F2937]/30" key={entry.id}>
                <td className="whitespace-nowrap px-5 py-3.5 text-xs text-[#D1D5DB] font-mono">
                  {formatSecurityDate(entry.timestampUtc)}
                </td>
                <td className="px-5 py-3.5 font-bold text-[#F9FAFB] tracking-tight">
                  {formatActivityAction(entry.action)}
                </td>
                <td className="px-5 py-3.5">
                  <ActivityResultBadge result={entry.result} />
                </td>
                <td className="px-5 py-3.5 text-[#D1D5DB]">
                  <span className="block font-mono text-xs font-semibold text-[#67E8F9]">{entry.ip ?? 'IP indisponível'}</span>
                  <span className="mt-0.5 block max-w-xs truncate text-[11px] text-[#9CA3AF]" title={entry.userAgent ?? ''}>
                  {formatUserAgent(entry.userAgent)}
                </span>
                </td>
                <td className="px-5 py-3.5 text-xs text-[#9CA3AF] max-w-xs truncate font-mono">
                  {formatActivityMetadata(entry.metadata)}
                </td>
              </tr>
          ))}
          </tbody>
        </table>
      </div>
  )
}
