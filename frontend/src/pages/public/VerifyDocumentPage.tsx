import { useEffect, useRef } from 'react'

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
    <section className="mx-auto max-w-4xl space-y-6">
      <div className="max-w-2xl">
        <p className="text-sm font-medium uppercase text-[#06B6D4]">
          Verificacao publica
        </p>
        <h1 className="mt-2 text-3xl font-semibold text-[#F9FAFB] sm:text-4xl">
          Verificar autenticidade de documento
        </h1>
        <p className="mt-4 text-base leading-7 text-[#9CA3AF]">
          Envie um PDF assinado pela plataforma para verificar se ele esta
          integro e autentico.
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
          className="rounded-lg bg-[#06B6D4] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
          disabled={!verification.file || verification.isVerifying}
          onClick={() => void verification.submit()}
          type="button"
        >
          {verification.isVerifying ? 'Verificando...' : 'Verificar PDF'}
        </button>

        {verification.file || verification.result || verification.error ? (
          <button
            className="rounded-lg border border-[#374151] px-5 py-2.5 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white disabled:cursor-not-allowed disabled:opacity-60"
            disabled={verification.isVerifying}
            onClick={verification.reset}
            type="button"
          >
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
