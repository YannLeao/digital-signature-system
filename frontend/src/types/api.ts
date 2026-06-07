export type ApiFieldError = {
  field: string
  message: string
}

export type ApiErrorResponse = {
  code: string
  message: string
  timestamp: string
  fields?: ApiFieldError[]
}

export type ApiError = {
  code?: string
  message: string
  status?: number
  fields?: ApiFieldError[]
}

