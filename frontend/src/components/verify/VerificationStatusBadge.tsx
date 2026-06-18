import type { VerifyStatus } from '../../types/verify'

type VerificationStatusBadgeProps = {
  status: VerifyStatus
}

const statusClasses: Record<VerifyStatus, string> = {
  VALID: 'border-[#10B981]/40 bg-[#10B981]/10 text-[#A7F3D0]',
  TAMPERED: 'border-[#EF4444]/40 bg-[#EF4444]/10 text-[#FCA5A5]',
  NOT_FOUND: 'border-[#F59E0B]/40 bg-[#F59E0B]/10 text-[#FCD34D]',
}

const statusLabels: Record<VerifyStatus, string> = {
  VALID: 'Valido',
  TAMPERED: 'Adulterado',
  NOT_FOUND: 'Nao encontrado',
}

export function VerificationStatusBadge({
  status,
}: VerificationStatusBadgeProps) {
  return (
    <span
      className={`inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold uppercase ${statusClasses[status]}`}
    >
      {statusLabels[status]}
    </span>
  )
}
