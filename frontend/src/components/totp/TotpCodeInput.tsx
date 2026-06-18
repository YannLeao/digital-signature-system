import {type ClipboardEvent, useEffect, useRef} from 'react'

type TotpCodeInputProps = {
  disabled?: boolean
  label?: string
  onChange: (code: string) => void
  onComplete?: (code: string) => void
  value: string
  error?: boolean
}

export function TotpCodeInput({
                                disabled,
                                label = 'Código de autenticação',
                                onChange,
                                onComplete,
                                value,
                                error
                              }: TotpCodeInputProps) {
  const inputRef = useRef<HTMLInputElement | null>(null)

  useEffect(() => {
    inputRef.current?.focus()
  }, [])

  function updateCode(nextValue: string) {
    const normalized = nextValue.replace(/\D/g, '').slice(0, 6)
    onChange(normalized)

    if (normalized.length === 6) {
      onComplete?.(normalized)
    }
  }

  function handlePaste(event: ClipboardEvent<HTMLInputElement>) {
    event.preventDefault()
    updateCode(event.clipboardData.getData('text'))
  }

  return (
      <label className="block group">
      <span className="mb-2 block text-xs font-semibold uppercase tracking-wide text-[#9CA3AF] group-focus-within:text-[#06B6D4] transition-colors">
        {label}
      </span>
        <input
            autoComplete="one-time-code"
            className={`w-full rounded-lg border bg-[#111827] px-4 py-3.5 text-center font-mono text-2xl font-bold tracking-[0.4em] text-[#F9FAFB] outline-none transition-all placeholder:text-[#6B7280] focus:ring-4 disabled:cursor-not-allowed disabled:opacity-50 ${
                error
                    ? 'border-red-500/50 focus:border-red-500 focus:ring-red-500/10'
                    : 'border-[#374151] focus:border-[#06B6D4] focus:ring-[#06B6D4]/10'
            }`}
            disabled={disabled}
            inputMode="numeric"
            maxLength={6}
            onChange={(event) => updateCode(event.target.value)}
            onPaste={handlePaste}
            pattern="[0-9]*"
            placeholder="000000"
            ref={inputRef}
            type="text"
            value={value}
        />
      </label>
  )
}
