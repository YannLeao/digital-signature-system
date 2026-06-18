import type { ActiveSession } from '../../types/security'
import { SessionCard } from './SessionCard'

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
    <section className="rounded-xl border border-[#374151] bg-[#1F2937] p-5">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm font-medium uppercase text-[#06B6D4]">
            Sessoes
          </p>
          <h2 className="mt-1 text-2xl font-semibold text-[#F9FAFB]">
            Dispositivos conectados
          </h2>
        </div>
        <button
          className="rounded-lg bg-[#EF4444] px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-400 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={isLoading || isRevoking || sessions.length === 0}
          onClick={onRevokeAll}
          type="button"
        >
          Sair de todos
        </button>
      </div>

      {error ? (
        <div className="mt-5 rounded-lg border border-[#EF4444]/40 bg-[#EF4444]/10 px-4 py-3 text-sm text-red-200">
          {error}
        </div>
      ) : null}

      {isLoading ? (
        <div className="mt-5 rounded-lg border border-[#374151] bg-[#111827] p-5 text-sm text-[#9CA3AF]">
          Carregando sessoes...
        </div>
      ) : null}

      {!isLoading && sessions.length === 0 ? (
        <div className="mt-5 rounded-lg border border-[#374151] bg-[#111827] p-5 text-sm text-[#9CA3AF]">
          Nenhuma sessao ativa encontrada.
        </div>
      ) : null}

      {!isLoading && sessions.length > 0 ? (
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
      ) : null}
    </section>
  )
}
