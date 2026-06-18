import type { VerifySignatureData } from '../../types/verify'
import { formatVerificationDate } from '../../utils/formatVerificationDate'

type VerificationDetailsProps = {
  signature: VerifySignatureData
}

export function VerificationDetails({ signature }: VerificationDetailsProps) {
  return (
    <dl className="grid gap-3 text-sm sm:grid-cols-3">
      <div>
        <dt className="text-[#9CA3AF]">Assinante</dt>
        <dd className="mt-1 break-words font-medium text-[#F9FAFB]">
          {signature.signerName}
        </dd>
      </div>
      <div>
        <dt className="text-[#9CA3AF]">Assinado em</dt>
        <dd className="mt-1 font-medium text-[#F9FAFB]">
          {formatVerificationDate(signature.signedAt)}
        </dd>
      </div>
      <div>
        <dt className="text-[#9CA3AF]">Identificador</dt>
        <dd className="mt-1 break-all font-mono text-xs text-[#D1D5DB]">
          {signature.signatureId}
        </dd>
      </div>
    </dl>
  )
}
