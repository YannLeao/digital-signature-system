import {ShieldCheck} from 'lucide-react'

type SignatureStampPreviewProps = {
    signerEmail: string | null
}

export function SignatureStampPreview({
                                          signerEmail,
                                      }: SignatureStampPreviewProps) {
    const signer = signerEmail || 'usuário autenticado'

    return (
        <div className="flex h-full w-full items-center gap-2 border-2 border-[#06B6D4] bg-[#0B1120] px-2.5 text-left shadow-xl shadow-cyan-950/40 rounded-sm">
            <div className="flex h-7 w-7 shrink-0 items-center justify-center bg-[#06B6D4] text-[#0B1120]">
                <ShieldCheck className="h-5 w-5 stroke-[2.5]" />
            </div>

            <div className="min-w-0 flex-1">
                <p className="truncate text-[10px] font-bold uppercase tracking-wide text-white">
                    Assinado Digitalmente
                </p>
                <p className="truncate text-[9px] font-medium leading-none text-[#67E8F9] my-0.5">
                    {signer}
                </p>
                <p className="truncate text-[8px] tracking-tight text-[#9CA3AF]">
                    Integridade Garantida por SHA-256
                </p>
            </div>
        </div>
    )
}