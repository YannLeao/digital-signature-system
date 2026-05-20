import type { FieldError, UseFormRegisterReturn } from 'react-hook-form'
import { useState } from 'react'

type AuthFormFieldProps = {
  label: string
  type: 'email' | 'password'
  autoComplete: string
  placeholder: string
  registration: UseFormRegisterReturn
  error?: FieldError
}

export function AuthFormField({
  label,
  type,
  autoComplete,
  placeholder,
  registration,
  error,
}: AuthFormFieldProps) {
  const errorId = `${registration.name}-error`
  const [isPasswordVisible, setIsPasswordVisible] = useState(false)
  const isPasswordField = type === 'password'
  const inputType = isPasswordField && isPasswordVisible ? 'text' : type

  return (
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-[#F9FAFB]">
        {label}
      </span>
      <div className="relative">
        <input
          aria-describedby={error ? errorId : undefined}
          aria-invalid={error ? 'true' : 'false'}
          autoComplete={autoComplete}
          className={`w-full rounded-lg border border-[#374151] bg-[#111827] px-3 py-3 text-[#F9FAFB] outline-none transition placeholder:text-[#6B7280] focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/30 ${
            isPasswordField ? 'pr-12' : ''
          }`}
          placeholder={placeholder}
          type={inputType}
          {...registration}
        />
        {isPasswordField ? (
          <button
            aria-label={isPasswordVisible ? 'Ocultar senha' : 'Mostrar senha'}
            className="absolute right-2 top-1/2 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-md text-[#9CA3AF] transition hover:bg-[#1F2937] hover:text-[#F9FAFB] focus:outline-none focus:ring-2 focus:ring-[#06B6D4]/40"
            onClick={() => setIsPasswordVisible((current) => !current)}
            title={isPasswordVisible ? 'Ocultar senha' : 'Mostrar senha'}
            type="button"
          >
            {isPasswordVisible ? <EyeOffIcon /> : <EyeIcon />}
          </button>
        ) : null}
      </div>
      {error ? (
        <span className="mt-2 block text-sm text-[#FCA5A5]" id={errorId}>
          {error.message}
        </span>
      ) : null}
    </label>
  )
}

function EyeIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
    >
      <path d="M2.1 12s3.6-6.5 9.9-6.5S21.9 12 21.9 12 18.3 18.5 12 18.5 2.1 12 2.1 12Z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  )
}

function EyeOffIcon() {
  return (
    <svg
      aria-hidden="true"
      className="h-5 w-5"
      fill="none"
      stroke="currentColor"
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth="1.8"
      viewBox="0 0 24 24"
    >
      <path d="M3 3l18 18" />
      <path d="M10.6 10.6a3 3 0 0 0 4.2 4.2" />
      <path d="M9.9 5.7A10.5 10.5 0 0 1 12 5.5c6.3 0 9.9 6.5 9.9 6.5a17.6 17.6 0 0 1-3 3.8" />
      <path d="M6.2 7.1A17.8 17.8 0 0 0 2.1 12s3.6 6.5 9.9 6.5a10.7 10.7 0 0 0 4.1-.8" />
    </svg>
  )
}
