import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { ActiveSessionsSection } from '../../components/security/ActiveSessionsSection'
import { ActivityHistorySection } from '../../components/security/ActivityHistorySection'
import { RevokeAllSessionsDialog } from '../../components/security/RevokeAllSessionsDialog'
import { RevokeSessionDialog } from '../../components/security/RevokeSessionDialog'
import { useSecurityActivity } from '../../hooks/useSecurityActivity'
import type { ActiveSession } from '../../types/security'

export function SecurityActivityPage() {
  const navigate = useNavigate()
  const [sessionToRevoke, setSessionToRevoke] = useState<ActiveSession | null>(
    null,
  )
  const [isRevokeAllOpen, setIsRevokeAllOpen] = useState(false)
  const {
    activity,
    activityError,
    clearMessage,
    filters,
    goToNextPage,
    goToPreviousPage,
    isActivityLoading,
    isRevoking,
    isRevokingAll,
    isSessionsLoading,
    message,
    resetFilters,
    revokeAll,
    revokeOne,
    sessions,
    sessionsError,
    updateFilters,
  } = useSecurityActivity()

  async function handleConfirmRevoke(session: ActiveSession) {
    const result = await revokeOne(session)
    setSessionToRevoke(null)

    if (result === 'signed-out') {
      navigate('/login', {
        replace: true,
        state: { authMessage: 'Sessao encerrada. Entre novamente.' },
      })
    }
  }

  async function handleConfirmRevokeAll() {
    const result = await revokeAll()
    setIsRevokeAllOpen(false)

    if (result === 'signed-out') {
      navigate('/login', {
        replace: true,
        state: { authMessage: 'Sessoes encerradas. Entre novamente.' },
      })
    }
  }

  return (
    <section className="space-y-6">
      <div>
        <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
          Seguranca da conta
        </p>
        <h1 className="text-3xl font-semibold text-[#F9FAFB]">
          Sessoes e atividade
        </h1>
      </div>

      {message ? (
        <div className="flex items-center justify-between gap-4 rounded-lg border border-[#10B981]/40 bg-[#10B981]/10 px-4 py-3 text-sm text-emerald-100">
          <span>{message}</span>
          <button
            className="font-medium text-emerald-50 underline-offset-4 hover:underline"
            onClick={clearMessage}
            type="button"
          >
            Fechar
          </button>
        </div>
      ) : null}

      <ActiveSessionsSection
        error={sessionsError}
        isLoading={isSessionsLoading}
        isRevoking={isRevoking || isRevokingAll}
        onRevoke={setSessionToRevoke}
        onRevokeAll={() => setIsRevokeAllOpen(true)}
        sessions={sessions}
      />

      <ActivityHistorySection
        activity={activity}
        error={activityError}
        filters={filters}
        isLoading={isActivityLoading}
        onApplyFilters={updateFilters}
        onNextPage={goToNextPage}
        onPreviousPage={goToPreviousPage}
        onResetFilters={resetFilters}
      />

      <RevokeSessionDialog
        isSubmitting={isRevoking}
        onCancel={() => setSessionToRevoke(null)}
        onConfirm={handleConfirmRevoke}
        session={sessionToRevoke}
      />

      <RevokeAllSessionsDialog
        isOpen={isRevokeAllOpen}
        isSubmitting={isRevokingAll}
        onCancel={() => setIsRevokeAllOpen(false)}
        onConfirm={handleConfirmRevokeAll}
      />
    </section>
  )
}
