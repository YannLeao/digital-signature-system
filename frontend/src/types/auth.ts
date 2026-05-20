export type RegisterUserRequest = {
  email: string
  password: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type AuthMessageResponse = {
  message: string
}

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
