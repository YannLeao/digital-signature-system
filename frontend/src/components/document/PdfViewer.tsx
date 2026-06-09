import { useState } from 'react'
import { Document, Page, pdfjs } from 'react-pdf'
import 'react-pdf/dist/Page/AnnotationLayer.css'
import 'react-pdf/dist/Page/TextLayer.css'

import type { PdfPageSize, StampPosition } from '../../types/document'
import { PageSelector } from './PageSelector'
import { StampPositionOverlay } from './StampPositionOverlay'

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString()

type PdfViewerProps = {
  file: File | null
  position: StampPosition | null
  signerEmail: string | null
  onPositionChange: (position: StampPosition) => void
}

export function PdfViewer({
  file,
  position,
  signerEmail,
  onPositionChange,
}: PdfViewerProps) {
  const [numPages, setNumPages] = useState(0)
  const [currentPage, setCurrentPage] = useState(1)
  const [scale, setScale] = useState(1)
  const [pageSize, setPageSize] = useState<PdfPageSize | null>(null)

  function handleLoadSuccess(document: { numPages: number }) {
    setNumPages(document.numPages)
    setCurrentPage(1)
    setPageSize(null)
  }

  if (!file) {
    return (
      <section className="flex min-h-[420px] items-center justify-center rounded-xl border border-dashed border-[#374151] bg-[#111827] p-8 text-center text-sm text-[#9CA3AF]">
        Selecione um PDF para visualizar e posicionar o selo.
      </section>
    )
  }

  return (
    <section className="rounded-xl border border-[#374151] bg-[#111827] p-5">
      <div className="flex flex-col gap-4 border-b border-[#374151] pb-4 lg:flex-row lg:items-center lg:justify-between">
        <PageSelector
          currentPage={currentPage}
          numPages={numPages || 1}
          onPageChange={(page) => {
            const nextPage = Math.min(Math.max(page || 1, 1), numPages || 1)
            setCurrentPage(nextPage)
            setPageSize(null)
          }}
        />
        <label className="flex items-center gap-3 text-sm text-[#D1D5DB]">
          Zoom
          <input
            className="w-40 accent-[#06B6D4]"
            max={1.4}
            min={0.7}
            onChange={(event) => setScale(Number(event.target.value))}
            step={0.1}
            type="range"
            value={scale}
          />
          <span className="w-12 text-[#9CA3AF]">{Math.round(scale * 100)}%</span>
        </label>
      </div>

      <div className="mt-5 overflow-auto rounded-lg bg-[#0B1120] p-4">
        <Document
          error={<p className="text-sm text-[#EF4444]">Nao foi possivel carregar o PDF.</p>}
          file={file}
          loading={<p className="text-sm text-[#9CA3AF]">Carregando preview...</p>}
          onLoadSuccess={handleLoadSuccess}
        >
          <div className="relative inline-block overflow-hidden bg-white shadow-2xl">
            <Page
              canvasBackground="white"
              error={<p className="p-6 text-sm text-[#EF4444]">Pagina indisponivel.</p>}
              loading={<p className="p-6 text-sm text-[#9CA3AF]">Renderizando pagina...</p>}
              onRenderSuccess={(page) => {
                setPageSize({
                  height: page.height / scale,
                  width: page.width / scale,
                })
              }}
              pageNumber={currentPage}
              renderAnnotationLayer={false}
              renderTextLayer={false}
              scale={scale}
            />
            {pageSize ? (
              <StampPositionOverlay
                currentPage={currentPage}
                onPositionChange={onPositionChange}
                pageSize={pageSize}
                position={position}
                scale={scale}
                signerEmail={signerEmail}
              />
            ) : null}
          </div>
        </Document>
      </div>
    </section>
  )
}
