import {type ElementType, useState} from 'react'
import type {FieldError, UseFormRegisterReturn} from 'react-hook-form'
import {AlertTriangle, Eye, EyeOff, Lock, Mail} from 'lucide-react'

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

  const Icon: ElementType = isPasswordField ? Lock : Mail

  return (
      <label className="block group">
      <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-[#9CA3AF] group-focus-within:text-[#06B6D4] transition-colors">
        {label}
      </span>
        <div className="relative">
          <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#6B7280] group-focus-within:text-[#06B6D4] transition-colors">
            <Icon className="h-4 w-4" />
          </div>

          <input
              aria-describedby={error ? errorId : undefined}
              aria-invalid={error ? 'true' : 'false'}
              autoComplete={autoComplete}
              className={`w-full rounded-lg border bg-[#111827] py-3 pl-11 pr-4 text-sm text-[#F9FAFB] outline-none transition-all placeholder:text-[#6B7280] focus:ring-4 ${
                  error
                      ? 'border-red-500/50 focus:border-red-500 focus:ring-red-500/10'
                      : 'border-[#374151] focus:border-[#06B6D4] focus:ring-[#06B6D4]/10'
              } ${isPasswordField ? 'pr-12' : ''}`}
              placeholder={placeholder}
              type={inputType}
              {...registration}
          />

          {isPasswordField ? (
              <button
                  aria-label={isPasswordVisible ? 'Ocultar senha' : 'Mostrar senha'}
                  className="absolute right-2 top-1/2 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-md text-[#9CA3AF] transition hover:bg-[#1F2937] hover:text-[#F9FAFB] focus:outline-none"
                  onClick={() => setIsPasswordVisible((current) => !current)}
                  title={isPasswordVisible ? 'Ocultar senha' : 'Mostrar senha'}
                  type="button"
              >
                {isPasswordVisible ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
          ) : null}
        </div>

        {error ? (
            <span className="mt-2 flex items-center gap-1.5 text-xs font-medium text-red-400" id={errorId}>
          <AlertTriangle className="h-3.5 w-3.5 shrink-0" />
              {error.message}
        </span>
        ) : null}
      </label>
  )
}