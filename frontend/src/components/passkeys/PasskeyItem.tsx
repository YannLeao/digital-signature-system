import {Activity, Calendar, CheckCircle2, Laptop, Trash2} from 'lucide-react'
import type {PasskeyDevice} from '../../types/passkey'

type PasskeyItemProps = {
  device: PasskeyDevice
  onRevoke: (device: PasskeyDevice) => void
}

export function PasskeyItem({ device, onRevoke }: PasskeyItemProps) {
  return (
      <li className="flex flex-col gap-4 rounded-xl border border-[#374151]/50 bg-[#111827] p-4 sm:flex-row sm:items-center sm:justify-between transition hover:border-[#374151]">
        <div className="flex items-start gap-3.5 min-w-0">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-[#06B6D4]/10 text-[#67E8F9] border border-[#06B6D4]/20">
            <Laptop className="h-5 w-5" />
          </div>

          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h3 className="font-semibold text-[#F9FAFB] tracking-tight">{device.deviceName}</h3>
              {device.active ? (
                  <span className="inline-flex items-center gap-1 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2 py-0.5 text-[11px] font-medium text-emerald-400">
                <CheckCircle2 className="h-3 w-3" />
                Ativa
              </span>
              ) : (
                  <span className="rounded-full border border-gray-500/30 bg-gray-500/10 px-2 py-0.5 text-[11px] font-medium text-gray-400">
                Inativa
              </span>
              )}
            </div>

            <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-[#9CA3AF]">
            <span className="flex items-center gap-1.5">
              <Calendar className="h-3.5 w-3.5 text-[#4B5563]" />
              Criada em {formatDate(device.createdAt)}
            </span>
              <span className="flex items-center gap-1.5">
              <Activity className="h-3.5 w-3.5 text-[#4B5563]" />
              Último uso: {device.lastUsed ? formatDate(device.lastUsed) : 'Nunca'}
            </span>
            </div>
          </div>
        </div>

        <button
            className="inline-flex items-center justify-center gap-1.5 rounded-lg border border-rose-500/30 px-4 py-2 text-sm font-medium text-rose-400 transition hover:bg-rose-500/10"
            onClick={() => onRevoke(device)}
            type="button"
        >
          <Trash2 className="h-4 w-4" />
          Revogar
        </button>
      </li>
  )
}

function formatDate(value: string): string {
  try {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(value))
  } catch {
    return value
  }
}
