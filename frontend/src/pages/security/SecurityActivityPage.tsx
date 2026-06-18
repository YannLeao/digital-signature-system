import {useState} from 'react'
import {useNavigate} from 'react-router-dom'
import {CheckCircle2} from 'lucide-react'

import {ActiveSessionsSection} from '../../components/security/ActiveSessionsSection'
import {ActivityHistorySection} from '../../components/security/ActivityHistorySection'
import {RevokeAllSessionsDialog} from '../../components/security/RevokeAllSessionsDialog'
import {RevokeSessionDialog} from '../../components/security/RevokeSessionDialog'
import {useSecurityActivity} from '../../hooks/useSecurityActivity'
import type {ActiveSession} from '../../types/security'

export function SecurityActivityPage() {
  const navigate = useNavigate()
  const [sessionToRevoke, setSessionToRevoke] = useState<ActiveSession | null>(null)
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
        state: { authMessage: 'Sessão encerrada com sucesso. Faça login novamente.' },
      })
    }
  }

  async function handleConfirmRevokeAll() {
    const result = await revokeAll()
    setIsRevokeAllOpen(false)

    if (result === 'signed-out') {
      navigate('/login', {
        replace: true,
        state: { authMessage: 'Todas as sessões foram finalizadas por segurança. Faça login novamente.' },
      })
    }
  }

  return (
      <section className="max-w-6xl p-6 mx-auto space-y-6">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4]">
            Políticas de Segurança
          </p>
          <h1 className="mt-1 text-3xl font-bold text-[#F9FAFB] tracking-tight">
            Controle de Sessões e Atividades
          </h1>
          <p className="mt-2 text-sm text-[#9CA3AF] max-w-2xl">
            Monitore o histórico de acessos da sua conta, audite alterações cadastrais recentes e gerencie permissões de tokens de hardware ativos.
          </p>
        </div>

        {message && (
            <div className="flex items-center justify-between gap-4 rounded-lg border border-emerald-500/20 bg-emerald-500/5 px-4 py-3 text-sm text-emerald-400 animate-fade-in">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 shrink-0" />
                <span>{message}</span>
              </div>
              <button
                  className="text-xs font-bold uppercase tracking-wider text-emerald-400 hover:text-emerald-300 transition-colors focus:outline-none"
                  onClick={clearMessage}
                  type="button"
              >
                Dispensar
              </button>
            </div>
        )}

        <div className="grid gap-6">
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
        </div>

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
