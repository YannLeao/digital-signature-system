import type { ActiveSession } from '../../types/security'
import { formatSecurityDate } from '../../utils/formatSecurityDate'
import { formatDeviceName, formatUserAgent } from '../../utils/formatUserAgent'
import { DeviceIcon } from './DeviceIcon'

type SessionCardProps = {
  disabled: boolean
  session: ActiveSession
  onRevoke: (session: ActiveSession) => void
}

export function SessionCard({
  disabled,
  session,
  onRevoke,
}: SessionCardProps) {
  return (
    <li className="rounded-xl border border-[#374151] bg-[#111827] p-4">
      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div className="flex min-w-0 gap-3">
          <DeviceIcon userAgent={session.userAgent} />
          <div className="min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <h3 className="font-semibold text-[#F9FAFB]">
                {formatDeviceName(session.deviceInfo, session.userAgent)}
              </h3>
              {session.current ? (
                <span className="rounded-full border border-[#10B981]/40 bg-[#10B981]/10 px-2 py-0.5 text-xs font-medium text-emerald-200">
                  Sessao atual
                </span>
              ) : null}
            </div>
            <p className="mt-1 break-words text-sm text-[#9CA3AF]">
              {formatUserAgent(session.userAgent)}
            </p>
            <div className="mt-3 grid gap-2 text-sm text-[#D1D5DB] sm:grid-cols-3">
              <span>
                <strong className="block text-xs font-medium uppercase text-[#9CA3AF]">
                  IP
                </strong>
                {session.ip}
              </span>
              <span>
                <strong className="block text-xs font-medium uppercase text-[#9CA3AF]">
                  Criada em
                </strong>
                {formatSecurityDate(session.createdAt)}
              </span>
              <span>
                <strong className="block text-xs font-medium uppercase text-[#9CA3AF]">
                  Ultimo uso
                </strong>
                {formatSecurityDate(session.lastSeenAt)}
              </span>
            </div>
          </div>
        </div>
        <button
          className="rounded-lg border border-[#EF4444]/50 px-3 py-2 text-sm font-medium text-red-100 transition hover:bg-[#EF4444]/15 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled}
          onClick={() => onRevoke(session)}
          type="button"
        >
          Encerrar
        </button>
      </div>
    </li>
  )
}
