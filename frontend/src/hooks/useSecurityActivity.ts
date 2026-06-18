import axios from 'axios'
import { useCallback, useEffect, useMemo, useState } from 'react'

import {
  getActiveSessions,
  getActivityHistory,
  revokeAllSessions,
  revokeSession,
} from '../services/securityService'
import type {
  ActiveSession,
  ActivityFilters,
  ActivityHistoryState,
} from '../types/security'
import { clearAccessToken } from '../utils/authTokenStore'
import { currentSessionIdFromAccessToken } from '../utils/currentSession'
import { parseApiError } from '../utils/parseApiError'

const initialFilters: ActivityFilters = {
  action: '',
  result: '',
  from: '',
  to: '',
  page: 0,
  size: 10,
}

type UseSecurityActivityResult = {
  activity: ActivityHistoryState | null
  activityError: string | null
  filters: ActivityFilters
  isActivityLoading: boolean
  isRevoking: boolean
  isRevokingAll: boolean
  isSessionsLoading: boolean
  message: string | null
  sessions: ActiveSession[]
  sessionsError: string | null
  clearMessage: () => void
  goToNextPage: () => void
  goToPreviousPage: () => void
  refreshActivity: () => Promise<void>
  refreshSessions: () => Promise<void>
  resetFilters: () => void
  revokeAll: () => Promise<'signed-out' | 'kept-session'>
  revokeOne: (session: ActiveSession) => Promise<'signed-out' | 'kept-session'>
  updateFilters: (filters: Partial<ActivityFilters>) => void
}

export function useSecurityActivity(): UseSecurityActivityResult {
  const [sessions, setSessions] = useState<ActiveSession[]>([])
  const [activity, setActivity] = useState<ActivityHistoryState | null>(null)
  const [filters, setFilters] = useState<ActivityFilters>(initialFilters)
  const [sessionsError, setSessionsError] = useState<string | null>(null)
  const [activityError, setActivityError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [isSessionsLoading, setIsSessionsLoading] = useState(false)
  const [isActivityLoading, setIsActivityLoading] = useState(false)
  const [isRevoking, setIsRevoking] = useState(false)
  const [isRevokingAll, setIsRevokingAll] = useState(false)

  const currentSessionId = useMemo(() => currentSessionIdFromAccessToken(), [])

  const refreshSessions = useCallback(async () => {
    setIsSessionsLoading(true)
    setSessionsError(null)

    try {
      const activeSessions = await getActiveSessions()
      setSessions(
        activeSessions.map((session) => ({
          ...session,
          current: session.sessionId === currentSessionId,
        })),
      )
    } catch (error) {
      setSessionsError(
        parseApiError(error, 'Nao foi possivel carregar as sessoes ativas.')
          .message,
      )
    } finally {
      setIsSessionsLoading(false)
    }
  }, [currentSessionId])

  const refreshActivity = useCallback(async () => {
    setIsActivityLoading(true)
    setActivityError(null)

    try {
      const history = await getActivityHistory(filters)
      setActivity({ status: 'available', data: history })
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        setActivity({
          status: 'unavailable',
          message: 'Historico de atividade ainda nao esta disponivel.',
        })
      } else {
        setActivityError(
          parseApiError(error, 'Nao foi possivel carregar o historico.')
            .message,
        )
      }
    } finally {
      setIsActivityLoading(false)
    }
  }, [filters])

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void refreshSessions()
    }, 0)

    return () => window.clearTimeout(timeoutId)
  }, [refreshSessions])

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void refreshActivity()
    }, 0)

    return () => window.clearTimeout(timeoutId)
  }, [refreshActivity])

  const updateFilters = useCallback((newFilters: Partial<ActivityFilters>) => {
    setFilters((currentFilters) => ({
      ...currentFilters,
      ...newFilters,
      page: newFilters.page ?? 0,
    }))
  }, [])

  const resetFilters = useCallback(() => {
    setFilters(initialFilters)
  }, [])

  const goToPreviousPage = useCallback(() => {
    setFilters((currentFilters) => ({
      ...currentFilters,
      page: Math.max(currentFilters.page - 1, 0),
    }))
  }, [])

  const goToNextPage = useCallback(() => {
    setFilters((currentFilters) => ({
      ...currentFilters,
      page: currentFilters.page + 1,
    }))
  }, [])

  const revokeOne = useCallback(
    async (session: ActiveSession) => {
      setIsRevoking(true)
      setMessage(null)
      setSessionsError(null)

      try {
        const responseMessage = await revokeSession(session.sessionId)
        setMessage(responseMessage)

        if (session.current) {
          clearAccessToken()
          return 'signed-out'
        }

        await refreshSessions()
        await refreshActivity()
        return 'kept-session'
      } catch (error) {
        setSessionsError(
          parseApiError(error, 'Nao foi possivel encerrar a sessao.').message,
        )
        return 'kept-session'
      } finally {
        setIsRevoking(false)
      }
    },
    [refreshActivity, refreshSessions],
  )

  const revokeAll = useCallback(async () => {
    setIsRevokingAll(true)
    setMessage(null)
    setSessionsError(null)

    try {
      const responseMessage = await revokeAllSessions()
      setMessage(responseMessage)
      clearAccessToken()
      return 'signed-out'
    } catch (error) {
      setSessionsError(
        parseApiError(error, 'Nao foi possivel sair dos dispositivos.')
          .message,
      )
      return 'kept-session'
    } finally {
      setIsRevokingAll(false)
    }
  }, [])

  return {
    activity,
    activityError,
    filters,
    isActivityLoading,
    isRevoking,
    isRevokingAll,
    isSessionsLoading,
    message,
    sessions,
    sessionsError,
    clearMessage: () => setMessage(null),
    goToNextPage,
    goToPreviousPage,
    refreshActivity,
    refreshSessions,
    resetFilters,
    revokeAll,
    revokeOne,
    updateFilters,
  }
}
