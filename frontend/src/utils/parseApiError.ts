import axios, { type AxiosError } from 'axios'

import { apiErrorResponseSchema } from '../schemas/apiSchemas'
import type { ApiError, ApiErrorResponse } from '../types/api'

const defaultConnectionError =
  'Nao foi possivel conectar ao servidor. Tente novamente.'
const invalidResponseMessage = 'Resposta inesperada do servidor.'

export class InvalidApiResponseError extends Error {
  constructor() {
    super(invalidResponseMessage)
    this.name = 'InvalidApiResponseError'
  }
}

export function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  return apiErrorResponseSchema.safeParse(value).success
}

export function isApiAxiosError(
  error: unknown,
): error is AxiosError<ApiErrorResponse> {
  return axios.isAxiosError(error) && isApiErrorResponse(error.response?.data)
}

export function parseApiError(
  error: unknown,
  fallbackMessage = defaultConnectionError,
): ApiError {
  if (error instanceof InvalidApiResponseError) {
    return {
      code: 'SYS_001',
      message: invalidResponseMessage,
    }
  }

  if (!axios.isAxiosError(error)) {
    return {
      message: defaultConnectionError,
    }
  }

  const status = error.response?.status
  const parsedError = apiErrorResponseSchema.safeParse(error.response?.data)

  if (!parsedError.success) {
    return {
      message: fallbackMessage,
      status,
    }
  }

  return {
    code: parsedError.data.code,
    fields: parsedError.data.fields,
    message: parsedError.data.message,
    status,
  }
}
