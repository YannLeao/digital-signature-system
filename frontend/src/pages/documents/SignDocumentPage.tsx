import {useMemo, useState} from 'react'

import {PdfUploader} from '../../components/document/PdfUploader'
import {PdfViewer} from '../../components/document/PdfViewer'
import {SignButton} from '../../components/document/SignButton'
import {SignedPdfDialog} from '../../components/document/SignedPdfDialog'
import {SignProgress} from '../../components/document/SignProgress'
import {useAuth} from '../../hooks/useAuth'
import {usePdfSigning} from '../../hooks/usePdfSigning'
import {signDocumentRequestSchema} from '../../schemas/signDocumentSchemas'
import type {StampPosition} from '../../types/document'
import {AlertTriangle, FileSignature} from "lucide-react";

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
       <section className="space-y-6 animate-fade-in">
           <div>
               <div className="flex items-center gap-2 text-sm font-medium uppercase tracking-wider text-[#06B6D4]">
                    <FileSignature className="h-4 w-4" />
                    <span>Documentos</span>
               </div>
               <h1 className="mt-1 text-3xl font-bold text-[#F9FAFB] tracking-tight">
                   Assinar PDF
               </h1>
           </div>
            <PdfUploader file={file} onFileChange={handleFileChange} />
            <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px] items-start">
               <PdfViewer
                   file={file}
                   onPositionChange={setPosition}
                   position={position}
                   signerEmail={email}
               />
                <aside className="space-y-4 lg:sticky lg:top-24">
                   <SignProgress isSigning={signing.isSigning} />
                    {signing.error ? (
                       <div className="flex items-start gap-2 rounded-lg border border-red-500/50 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                           <AlertTriangle className="h-5 w-5 shrink-0 text-red-500 mt-0.5" />
                           <span>{signing.error}</span>
                       </div>
                    ) : null}

                    <SignedPdfDialog
                        onDownload={signing.download}
                        onReset={() => handleFileChange(null)}
                        signedDocument={signing.signedDocument}
                    />

                    {!signing.signedDocument ? (
                        <SignButton
                            disabled={!requestIsValid}
                            isSigning={signing.isSigning}
                            onClick={() => void handleSign()}
                        />
                    ) : null}
                </aside>
            </div>
        </section>
    )
}
