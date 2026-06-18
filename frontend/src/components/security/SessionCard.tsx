import {Clock, Globe, ShieldX} from 'lucide-react'
import type {ActiveSession} from '../../types/security'
import {formatSecurityDate} from '../../utils/formatSecurityDate'
import {formatDeviceName, formatUserAgent} from '../../utils/formatUserAgent'
import {DeviceIcon} from './DeviceIcon'

type SessionCardProps = {
  disabled: boolean
  session: ActiveSession
  onRevoke: (session: ActiveSession) => void
}

export function SessionCard({ disabled, session, onRevoke }: SessionCardProps) {
  return (
      <li className="rounded-xl border border-[#374151] bg-[#111827] p-4 transition-all hover:border-[#374151]/80 hover:bg-[#111827]/60 group">
        <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div className="flex min-w-0 gap-3.5">
            <DeviceIcon userAgent={session.userAgent} />
            <div className="min-w-0 flex-1">
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="font-bold text-[#F9FAFB] tracking-tight">
                  {formatDeviceName(session.deviceInfo, session.userAgent)}
                </h3>
                {session.current && (
                    <span className="rounded-full border border-emerald-500/30 bg-emerald-500/10 px-2.5 py-0.5 text-xs font-semibold text-emerald-400">
                  Sessão atual
                </span>
                )}
              </div>
              <p className="mt-1 wrap-break-word text-xs text-[#9CA3AF] font-mono opacity-80">
                {formatUserAgent(session.userAgent)}
              </p>

              <div className="mt-4 grid gap-3 text-xs text-[#D1D5DB] sm:grid-cols-3 border-t border-[#374151]/40 pt-3">
              <span className="flex flex-col gap-0.5">
                <span className="flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider text-[#9CA3AF]">
                  <Globe className="h-3 w-3 text-[#06B6D4]" /> IP Address
                </span>
                <span className="font-medium text-[#F9FAFB]">{session.ip}</span>
              </span>
                <span className="flex flex-col gap-0.5">
                <span className="flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider text-[#9CA3AF]">
                  <Clock className="h-3 w-3 text-[#06B6D4]" /> Criada em
                </span>
                <span className="text-[#9CA3AF]">{formatSecurityDate(session.createdAt)}</span>
              </span>
                <span className="flex flex-col gap-0.5">
                <span className="flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider text-[#9CA3AF]">
                  <Clock className="h-3 w-3 text-[#06B6D4]" /> Último Uso
                </span>
                <span className="text-[#9CA3AF]">{formatSecurityDate(session.lastSeenAt)}</span>
              </span>
              </div>
            </div>
          </div>

          <button
              className="flex items-center justify-center gap-1.5 rounded-lg border border-red-500/30 px-3.5 py-2 text-xs font-semibold text-red-400 transition hover:bg-red-500/10 focus:outline-none focus:ring-4 focus:ring-red-500/10 disabled:cursor-not-allowed disabled:opacity-40 self-end md:self-start shrink-0"
              disabled={disabled}
              onClick={() => onRevoke(session)}
              type="button"
          >
            <ShieldX className="h-3.5 w-3.5" />
            Encerrar
          </button>
        </div>
      </li>
  )
}
