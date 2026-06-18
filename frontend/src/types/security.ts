export type ActiveSession = {
  sessionId: string
  deviceInfo: string
  ip: string
  userAgent: string | null
  createdAt: string
  lastSeenAt: string
  current: boolean
}

export type ActivityAction =
  | 'LOGIN'
  | 'LOGOUT'
  | 'AUTH_FAIL'
  | 'TOKEN_ISSUED'
  | 'DOC_SIGNED'
  | 'DOC_VERIFIED'
  | 'PASSWORD_CHANGED'
  | string

export type ActivityResult = 'SUCCESS' | 'FAILURE' | string

export type ActivityLogEntry = {
  id: string
  timestampUtc: string
  ip: string | null
  userAgent: string | null
  action: ActivityAction
  result: ActivityResult
  metadata: Record<string, unknown> | null
}

export type ActivityFilters = {
  action: string
  result: string
  from: string
  to: string
  page: number
  size: number
}

export type ActivityLogFilters = ActivityFilters

export type PaginatedActivityLog = {
  items: ActivityLogEntry[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type ActivityHistoryState =
  | { status: 'available'; data: PaginatedActivityLog }
  | { status: 'unavailable'; message: string }
