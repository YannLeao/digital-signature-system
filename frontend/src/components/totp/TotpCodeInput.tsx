import { useEffect, useRef, type ClipboardEvent } from 'react'

type TotpCodeInputProps = {
  disabled?: boolean
  label?: string
  onChange: (code: string) => void
  onComplete?: (code: string) => void
  value: string
}

export function TotpCodeInput({
  disabled,
  label = 'Codigo de autenticacao',
  onChange,
  onComplete,
  value,
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
    <label className="block">
      <span className="mb-2 block text-sm font-medium text-[#F9FAFB]">
        {label}
      </span>
      <input
        autoComplete="one-time-code"
        className="w-full rounded-lg border border-[#374151] bg-[#111827] px-4 py-3 text-center font-mono text-2xl tracking-[0.35em] text-[#F9FAFB] outline-none transition placeholder:text-[#6B7280] focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/30 disabled:cursor-not-allowed disabled:opacity-60"
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

