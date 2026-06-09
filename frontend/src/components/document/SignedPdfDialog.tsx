import type { SignedDocument } from '../../types/document'

type SignedPdfDialogProps = {
  signedDocument: SignedDocument | null
  onDownload: () => void
  onReset: () => void
}

export function SignedPdfDialog({
  signedDocument,
  onDownload,
  onReset,
}: SignedPdfDialogProps) {
  if (!signedDocument) {
    return null
  }

  return (
    <section className="rounded-xl border border-[#10B981]/50 bg-[#10B981]/10 p-5">
      <p className="text-sm font-semibold text-[#D1FAE5]">
        Documento assinado com sucesso.
      </p>
      <dl className="mt-3 grid gap-2 text-xs text-[#A7F3D0] sm:grid-cols-2">
        <div>
          <dt className="text-[#6EE7B7]">Assinatura</dt>
          <dd className="truncate">{signedDocument.metadata.signatureId}</dd>
        </div>
        <div>
          <dt className="text-[#6EE7B7]">Assinado em</dt>
          <dd>{signedDocument.metadata.signedAt}</dd>
        </div>
      </dl>
      <div className="mt-4 flex flex-wrap gap-3">
        <button
          className="rounded-lg bg-[#10B981] px-4 py-2 text-sm font-semibold text-white transition hover:bg-emerald-400"
          onClick={onDownload}
          type="button"
        >
          Baixar PDF
        </button>
        <button
          className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
          onClick={onReset}
          type="button"
        >
          Assinar outro
        </button>
      </div>
    </section>
  )
}
