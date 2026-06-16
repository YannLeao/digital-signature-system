import { useId, type ChangeEvent } from 'react'

type VerifyPdfUploaderProps = {
  disabled?: boolean
  error: string | null
  file: File | null
  onFileChange: (file: File | null) => void
}

export function VerifyPdfUploader({
  disabled = false,
  error,
  file,
  onFileChange,
}: VerifyPdfUploaderProps) {
  const inputId = useId()
  const errorId = `${inputId}-error`

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    onFileChange(event.target.files?.[0] ?? null)
    event.target.value = ''
  }

  return (
    <section className="rounded-xl border border-[#374151] bg-[#111827] p-5 shadow-2xl shadow-black/20">
      <label
        className="block text-sm font-semibold text-[#F9FAFB]"
        htmlFor={inputId}
      >
        Documento PDF
      </label>
      <p className="mt-1 text-sm text-[#9CA3AF]">
        Envie um arquivo PDF de ate 20MB.
      </p>

      <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center">
        <input
          accept="application/pdf,.pdf"
          aria-describedby={error ? errorId : undefined}
          className="block w-full cursor-pointer rounded-lg border border-[#374151] bg-[#0B1120] text-sm text-[#D1D5DB] file:mr-4 file:border-0 file:bg-[#06B6D4] file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-cyan-400 focus:border-[#06B6D4] focus:outline-none focus:ring-2 focus:ring-[#06B6D4]/30 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={disabled}
          id={inputId}
          onChange={handleChange}
          type="file"
        />
        {file ? (
          <button
            className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white disabled:cursor-not-allowed disabled:opacity-60"
            disabled={disabled}
            onClick={() => onFileChange(null)}
            type="button"
          >
            Remover
          </button>
        ) : null}
      </div>

      {file ? (
        <div className="mt-4 rounded-lg border border-[#374151] bg-[#0B1120] px-4 py-3 text-sm">
          <p className="break-words font-medium text-[#F9FAFB]">{file.name}</p>
          <p className="mt-1 text-[#9CA3AF]">{formatFileSize(file.size)}</p>
        </div>
      ) : null}

      {error ? (
        <p className="mt-3 text-sm text-[#FCA5A5]" id={errorId}>
          {error}
        </p>
      ) : null}
    </section>
  )
}

function formatFileSize(size: number): string {
  if (size < 1024 * 1024) {
    return `${Math.max(1, Math.round(size / 1024))} KB`
  }

  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}
