import { type ChangeEvent, useId } from 'react'
import { AlertCircle, FileSearch, FileText, Trash2 } from 'lucide-react'

import { formatFileSize } from '../../utils/formatFileSize'

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
      <label className="block text-sm font-semibold text-[#F9FAFB]" htmlFor={inputId}>
        Documento PDF para auditoria
      </label>
      <p className="mt-1 text-sm text-[#9CA3AF]">
        Envie o arquivo para validar assinaturas criptograficas.
      </p>

      <div className="mt-4">
        {!file ? (
          <div className="group relative flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-[#374151] bg-[#0B1120] p-6 text-center transition-colors hover:border-[#06B6D4]/60">
            <input
              accept="application/pdf,.pdf"
              aria-describedby={error ? errorId : undefined}
              className="absolute inset-0 h-full w-full cursor-pointer opacity-0 disabled:cursor-not-allowed"
              disabled={disabled}
              id={inputId}
              onChange={handleChange}
              type="file"
            />
            <FileSearch className="h-8 w-8 text-[#6B7280] transition-colors group-hover:text-[#06B6D4]" />
            <p className="mt-2 text-sm text-[#D1D5DB]">
              <span className="font-semibold text-[#06B6D4]">Selecione o arquivo assinado</span> ou arraste-o aqui
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-3 rounded-lg border border-[#374151] bg-[#0B1120] p-4 animate-fade-in sm:flex-row sm:items-center sm:justify-between">
            <div className="flex min-w-0 items-center gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-[#06B6D4]/10 text-[#67E8F9]">
                <FileText className="h-5 w-5" />
              </div>
              <div className="min-w-0">
                <p className="truncate text-sm font-medium text-white">{file.name}</p>
                <p className="text-xs text-[#9CA3AF]">{formatFileSize(file.size)}</p>
              </div>
            </div>

            <button
              className="inline-flex items-center justify-center gap-1.5 rounded-lg border border-red-500/30 px-3 py-1.5 text-xs font-medium text-red-400 transition hover:bg-red-500/10 disabled:opacity-50"
              disabled={disabled}
              onClick={() => onFileChange(null)}
              type="button"
            >
              <Trash2 className="h-3.5 w-3.5" />
              Remover
            </button>
          </div>
        )}
      </div>

      {error ? (
        <div
          className="mt-3 flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/10 px-3 py-2.5 text-sm font-medium text-red-300"
          id={errorId}
        >
          <AlertCircle className="h-4 w-4 shrink-0 text-red-500" />
          <span>{error}</span>
        </div>
      ) : null}
    </section>
  )
}
