type SignatureStampPreviewProps = {
  signerEmail: string | null
}

export function SignatureStampPreview({
  signerEmail,
}: SignatureStampPreviewProps) {
  const signer = signerEmail || 'usuario autenticado'

  return (
    <div className="flex h-full w-full items-center gap-2 border border-[#06B6D4] bg-[#0B1120] px-2 text-left shadow-lg shadow-cyan-950/30">
      <div className="relative h-7 w-7 shrink-0 bg-[#06B6D4]">
          <svg width="26" height="26" viewBox="0 0 26 26" aria-hidden="true">
              <path d="M7 13 L11.5 17.5 L20 8" fill="none" stroke="#0B1120" strokeWidth="2.6" strokeLinecap="butt" strokeLinejoin="miter"/>
          </svg>
      </div>
      <div className="min-w-0">
        <p className="truncate text-[10px] font-semibold leading-tight text-[#F9FAFB]">
          Assinado digitalmente
        </p>
        <p className="truncate text-[9px] leading-tight text-[#D1D5DB]">
          {signer}
        </p>
        <p className="truncate text-[8px] leading-tight text-[#9CA3AF]">
          SHA-256 registrado
        </p>
      </div>
    </div>
  )
}
