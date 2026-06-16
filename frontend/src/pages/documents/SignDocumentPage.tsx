import { useMemo, useState } from 'react'

import { PdfUploader } from '../../components/document/PdfUploader'
import { PdfViewer } from '../../components/document/PdfViewer'
import { SignButton } from '../../components/document/SignButton'
import { SignedPdfDialog } from '../../components/document/SignedPdfDialog'
import { SignProgress } from '../../components/document/SignProgress'
import { useAuth } from '../../hooks/useAuth'
import { usePdfSigning } from '../../hooks/usePdfSigning'
import { signDocumentRequestSchema } from '../../schemas/signDocumentSchemas'
import type { StampPosition } from '../../types/document'

export function SignDocumentPage() {
  const { email } = useAuth()
  const signing = usePdfSigning()
  const [file, setFile] = useState<File | null>(null)
  const [position, setPosition] = useState<StampPosition | null>(null)
  const request = useMemo(
    () =>
      position
        ? {
            sealPage: position.page,
            sealX: position.x,
            sealY: position.y,
          }
        : null,
    [position],
  )
  const requestIsValid = request
    ? signDocumentRequestSchema.safeParse(request).success
    : false

  function handleFileChange(nextFile: File | null) {
    setFile(nextFile)
    setPosition(null)
    signing.reset()
  }

  async function handleSign() {
    if (!file || !request || !requestIsValid || signing.isSigning) {
      return
    }

    await signing.submit(file, request)
  }

  return (
    <section className="space-y-6">
      <div>
        <p className="text-sm font-medium uppercase text-[#06B6D4]">
          Documentos
        </p>
        <h1 className="mt-2 text-3xl font-semibold text-[#F9FAFB]">
          Assinar PDF
        </h1>
      </div>

      <PdfUploader file={file} onFileChange={handleFileChange} />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
        <PdfViewer
          file={file}
          onPositionChange={setPosition}
          position={position}
          signerEmail={email}
        />

        <aside className="space-y-4">
          <SignProgress isSigning={signing.isSigning} />

          {signing.error ? (
            <div className="rounded-lg border border-[#EF4444]/50 bg-[#EF4444]/10 px-4 py-3 text-sm text-[#FCA5A5]">
              {signing.error}
            </div>
          ) : null}

          <SignedPdfDialog
            onDownload={signing.download}
            onReset={() => {
              handleFileChange(null)
            }}
            signedDocument={signing.signedDocument}
          />

          <SignButton
            disabled={!file || !requestIsValid || Boolean(signing.signedDocument)}
            isSigning={signing.isSigning}
            onClick={handleSign}
          />
        </aside>
      </div>
    </section>
  )
}
