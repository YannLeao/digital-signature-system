import { User, Calendar, Fingerprint } from 'lucide-react'
import type { VerifySignatureData } from '../../types/verify'
import { formatVerificationDate } from '../../utils/formatVerificationDate'

type VerificationDetailsProps = {
    signature: VerifySignatureData
}

export function VerificationDetails({ signature }: VerificationDetailsProps) {
    return (
        <dl className="grid gap-4 text-sm sm:grid-cols-3 bg-[#0B1120]/40 p-4 rounded-lg border border-[#374151]/40">
            <div className="space-y-1">
                <dt className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
                    <User className="h-3.5 w-3.5 text-[#06B6D4]" />
                    Assinante
                </dt>
                <dd className="wrap-break-word font-medium text-[#F9FAFB]">
                    {signature.signerName}
                </dd>
            </div>
            <div className="space-y-1">
                <dt className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
                    <Calendar className="h-3.5 w-3.5 text-[#06B6D4]" />
                    Assinado em
                </dt>
                <dd className="font-medium text-[#F9FAFB]">
                    {formatVerificationDate(signature.signedAt)}
                </dd>
            </div>
            <div className="space-y-1 sm:col-span-1">
                <dt className="flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wider text-[#9CA3AF]">
                    <Fingerprint className="h-3.5 w-3.5 text-[#06B6D4]" />
                    Identificador
                </dt>
                <dd className="break-all font-mono text-xs text-[#D1D5DB]">
                    {signature.signatureId}
                </dd>
            </div>
        </dl>
    )
}
