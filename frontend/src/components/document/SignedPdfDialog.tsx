import {CheckCircle, Download, RefreshCw} from 'lucide-react'
import type {SignedDocument} from '../../types/document'

type SignedPdfDialogProps = {
  signedDocument: SignedDocument | null
  onDownload: () => void
  onReset: () => void
}

function formatSignedAt(isoString: string) {
  try {
    return new Date(isoString).toLocaleString('pt-BR')
  } catch {
    return isoString
  }
}

export function SignedPdfDialog({
                                  signedDocument,
                                  onDownload,
                                  onReset,
                                }: SignedPdfDialogProps) {
  if (!signedDocument) {
    return null
  }

  const signedAt = formatSignedAt(signedDocument.metadata.signedAt)

  return (
      <section className="rounded-xl border border-[#10B981]/40 bg-[#10B981]/5 p-5 border-l-4 border-l-[#10B981] animate-fade-in">
        <div className="flex items-center gap-2 text-sm font-semibold text-emerald-300">
          <CheckCircle className="h-4 w-4 text-[#10B981]" />
          <span>Documento assinado com sucesso!</span>
        </div>

        <dl className="mt-4 grid gap-3 text-xs bg-[#0B1120]/60 p-3 rounded-lg border border-[#374151]/40 text-[#A7F3D0] sm:grid-cols-2">
          <div>
            <dt className="text-[#6EE7B7] font-medium uppercase tracking-wider text-[10px]">ID da Assinatura</dt>
            <dd className="truncate mt-0.5 text-white font-mono">{signedDocument.metadata.signatureId}</dd>
          </div>
          <div>
            <dt className="text-[#6EE7B7] font-medium uppercase tracking-wider text-[10px]">Assinado em</dt>
            <dd className="mt-0.5 text-white">{signedAt}</dd>
          </div>
        </dl>

        <div className="mt-4 flex flex-wrap gap-3">
          <button
              className="inline-flex items-center gap-1.5 rounded-lg bg-[#10B981] px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-400 shadow-md shadow-emerald-950/20"
              onClick={onDownload}
              type="button"
          >
            <Download className="h-4 w-4" />
            Baixar PDF
          </button>
          <button
              className="inline-flex items-center gap-1.5 rounded-lg border border-[#374151] bg-[#111827] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
              onClick={onReset}
              type="button"
          >
            <RefreshCw className="h-3.5 w-3.5 text-[#9CA3AF]" />
            Nova Assinatura
          </button>
        </div>
      </section>
  )
}
