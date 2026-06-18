import {RefreshCw, ShieldAlert} from 'lucide-react'
import type {ActiveSession} from '../../types/security'
import {SessionCard} from './SessionCard'

type ActiveSessionsSectionProps = {
  error: string | null
  isLoading: boolean
  isRevoking: boolean
  sessions: ActiveSession[]
  onRevoke: (session: ActiveSession) => void
  onRevokeAll: () => void
}

export function ActiveSessionsSection({
                                        error,
                                        isLoading,
                                        isRevoking,
                                        sessions,
                                        onRevoke,
                                        onRevokeAll,
                                      }: ActiveSessionsSectionProps) {
  return (
      <section className="rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-xl shadow-black/10 animate-fade-in">
        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between border-b border-[#374151]/50 pb-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4]">
              Controle de Acessos
            </p>
            <h2 className="mt-0.5 text-xl font-bold text-[#F9FAFB] tracking-tight">
              Dispositivos Conectados
            </h2>
          </div>
          <button
              className="rounded-lg bg-red-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-red-500 focus:outline-none focus:ring-4 focus:ring-red-500/20 disabled:cursor-not-allowed disabled:opacity-40"
              disabled={isLoading || isRevoking || sessions.length === 0}
              onClick={onRevokeAll}
              type="button"
          >
            Revogar todos os acessos
          </button>
        </div>

        {error && (
            <div className="mt-5 flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/5 px-4 py-3 text-sm text-red-400">
              <ShieldAlert className="h-4 w-4 shrink-0" />
              <span>{error}</span>
            </div>
        )}

        {isLoading && (
            <div className="mt-5 flex items-center justify-center gap-2.5 rounded-lg border border-[#374151] bg-[#111827] p-8 text-sm text-[#9CA3AF]">
              <RefreshCw className="h-4 w-4 animate-spin text-[#06B6D4]" />
              Mapeando sessões ativas...
            </div>
        )}

        {!isLoading && sessions.length === 0 && (
            <div className="mt-5 rounded-lg border border-[#374151] bg-[#111827] p-8 text-center text-sm text-[#9CA3AF]">
              Nenhum dispositivo complementar registrado no momento.
            </div>
        )}

        {!isLoading && sessions.length > 0 && (
            <ul className="mt-5 space-y-3">
              {sessions.map((session) => (
                  <SessionCard
                      disabled={isRevoking}
                      key={session.sessionId}
                      onRevoke={onRevoke}
                      session={session}
                  />
              ))}
            </ul>
        )}
      </section>
  )
}
