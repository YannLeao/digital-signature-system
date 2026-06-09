import { useId, useState, type ChangeEvent } from 'react'

import { pdfFileSchema } from '../../schemas/signDocumentSchemas'

type PdfUploaderProps = {
  file: File | null
  onFileChange: (file: File | null) => void
}

export function PdfUploader({ file, onFileChange }: PdfUploaderProps) {
  const inputId = useId()
  const [error, setError] = useState<string | null>(null)

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const selectedFile = event.target.files?.[0] ?? null

    if (!selectedFile) {
      onFileChange(null)
      setError(null)
      return
    }

    const parsedFile = pdfFileSchema.safeParse(selectedFile)

    if (!parsedFile.success) {
      onFileChange(null)
      setError(parsedFile.error.issues[0]?.message ?? 'Documento PDF invalido.')
      event.target.value = ''
      return
    }

    setError(null)
    onFileChange(parsedFile.data)
  }

  return (
    <section className="rounded-xl border border-[#374151] bg-[#111827] p-5">
      <label
        className="block text-sm font-semibold text-[#F9FAFB]"
        htmlFor={inputId}
      >
        PDF para assinatura
      </label>
      <p className="mt-1 text-sm text-[#9CA3AF]">
        Envie um arquivo PDF de ate 20MB. O navegador nao modifica nem assina o
        documento.
      </p>

      <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center">
        <input
          accept="application/pdf,.pdf"
          className="block w-full cursor-pointer rounded-lg border border-[#374151] bg-[#0B1120] text-sm text-[#D1D5DB] file:mr-4 file:border-0 file:bg-[#06B6D4] file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-cyan-400"
          id={inputId}
          onChange={handleChange}
          type="file"
        />
        {file ? (
          <button
            className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
            onClick={() => {
              onFileChange(null)
              setError(null)
            }}
            type="button"
          >
            Trocar
          </button>
        ) : null}
      </div>

      {file ? (
        <p className="mt-3 text-sm text-[#10B981]">
          {file.name} selecionado.
        </p>
      ) : null}
      {error ? <p className="mt-3 text-sm text-[#EF4444]">{error}</p> : null}
    </section>
  )
}
