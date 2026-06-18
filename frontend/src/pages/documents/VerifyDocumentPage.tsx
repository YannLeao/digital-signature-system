import { useEffect, useRef } from 'react'
import { ClipboardCheck, RotateCcw, Search } from 'lucide-react'

import { VerifyPdfUploader } from '../../components/verify/VerifyPdfUploader'
import { VerifyResultCard } from '../../components/verify/VerifyResultCard'
import { useVerifyDocument } from '../../hooks/useVerifyDocument'

export function VerifyDocumentPage() {
  const verification = useVerifyDocument()
  const resultRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    if (verification.result) {
      resultRef.current?.focus()
    }
  }, [verification.result])

  return (
    <section className="mx-auto max-w-4xl space-y-6 animate-fade-in">
      <div className="max-w-2xl">
        <div className="flex items-center gap-2 text-sm font-medium uppercase tracking-wider text-[#06B6D4]">
          <ClipboardCheck className="h-4 w-4" />
          <span>Verificacao publica</span>
        </div>
        <h1 className="mt-1 text-3xl font-bold text-[#F9FAFB] sm:text-4xl tracking-tight">
          Verificar autenticidade de documento
        </h1>
        <p className="mt-3 text-base leading-relaxed text-[#9CA3AF]">
          Envie um PDF assinado pela plataforma para verificar sua integridade e autenticidade.
        </p>
      </div>

      <VerifyPdfUploader
        disabled={verification.isVerifying}
        error={verification.error}
        file={verification.file}
        onFileChange={verification.selectFile}
      />

      <div className="flex flex-wrap gap-3">
        <button
          className="inline-flex items-center gap-2 rounded-lg bg-[#06B6D4] px-5 py-2.5 text-sm font-semibold text-[#0B1120] transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={!verification.file || verification.isVerifying}
          onClick={() => void verification.submit()}
          type="button"
        >
          {verification.isVerifying ? (
            <>
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-[#0B1120] border-t-transparent" />
              Verificando...
            </>
          ) : (
            <>
              <Search className="h-4 w-4" />
              Verificar PDF
            </>
          )}
        </button>

        {verification.file || verification.result || verification.error ? (
          <button
            className="inline-flex items-center gap-2 rounded-lg border border-[#374151] px-5 py-2.5 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white disabled:cursor-not-allowed disabled:opacity-60"
            disabled={verification.isVerifying}
            onClick={verification.reset}
            type="button"
          >
            <RotateCcw className="h-3.5 w-3.5 text-[#9CA3AF]" />
            Limpar
          </button>
        ) : null}
      </div>

      {verification.result ? (
        <div ref={resultRef} tabIndex={-1}>
          <VerifyResultCard result={verification.result} />
        </div>
      ) : null}
    </section>
  )
}
