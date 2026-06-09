type SignatureStampPreviewProps = {
  signerEmail: string | null
}

export function SignatureStampPreview({
  signerEmail,
}: SignatureStampPreviewProps) {
  const signer = signerEmail || 'usuario autenticado'

  return (
    <div className="h-full w-full rounded-md border border-[#06B6D4] bg-[#0B1120]/95 p-2 text-left shadow-lg shadow-cyan-950/30">
      <p className="truncate text-[10px] font-semibold text-[#F9FAFB]">
        Assinado digitalmente por: {signer}
      </p>
      <p className="mt-1 truncate text-[10px] text-[#D1D5DB]">
        Data/Hora UTC: pre-visualizacao
      </p>
      <p className="mt-1 truncate text-[9px] text-[#9CA3AF]">
        Hash SHA-256: calculado no backend
      </p>
      <p className="mt-1 truncate text-[9px] text-[#9CA3AF]">
        ID da assinatura: gerado no backend
      </p>
    </div>
  )
}
