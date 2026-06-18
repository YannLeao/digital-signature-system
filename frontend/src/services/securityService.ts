import { api } from './api'
import {
  activeSessionsSchema,
  activityLogPageSchema,
  securityActionResponseSchema,
} from '../schemas/securitySchemas'
import type {
  ActiveSession,
  ActivityLogFilters,
  PaginatedActivityLog,
} from '../types/security'
import { InvalidApiResponseError } from '../utils/parseApiError'

export async function getActiveSessions(): Promise<ActiveSession[]> {
  const response = await api.get<unknown>('/sessions')
  const parsed = activeSessionsSchema.safeParse(response.data)

  if (!parsed.success) {
    throw new InvalidApiResponseError()
  }

  return parsed.data.map((session) => ({
    ...session,
    current: false,
  }))
}

export async function revokeSession(sessionId: string): Promise<string> {
  const response = await api.delete<unknown>(`/sessions/${sessionId}`)
  const parsed = securityActionResponseSchema.safeParse(response.data)

  return parsed.success ? parsed.data.message : 'Sessao encerrada com sucesso.'
}

export async function revokeAllSessions(): Promise<string> {
  const response = await api.delete<unknown>('/sessions/all')
  const parsed = securityActionResponseSchema.safeParse(response.data)

  return parsed.success
    ? parsed.data.message
    : 'Todas as sessoes foram encerradas com sucesso.'
}

export async function getActivityHistory(
  filters: ActivityLogFilters,
): Promise<PaginatedActivityLog> {
  const params = new URLSearchParams()
  params.set('page', String(filters.page))
  params.set('size', String(filters.size))

  if (filters.action) {
    params.set('action', filters.action)
  }
  if (filters.result) {
    params.set('result', filters.result)
  }
  if (filters.from) {
    params.set('from', filters.from)
  }
  if (filters.to) {
    params.set('to', filters.to)
  }

  const response = await api.get<unknown>(`/audit-log/me?${params.toString()}`)
  const parsed = activityLogPageSchema.safeParse(response.data)

  if (!parsed.success) {
    throw new InvalidApiResponseError()
  }

  return parsed.data
}
