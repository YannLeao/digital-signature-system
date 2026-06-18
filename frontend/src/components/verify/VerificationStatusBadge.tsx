import {AlertOctagon, CheckCircle2, HelpCircle} from 'lucide-react'
import type {VerifyStatus} from '../../types/verify'
import type {ElementType} from 'react'

type VerificationStatusBadgeProps = {
  status: VerifyStatus
}

const statusConfig: Record<VerifyStatus, { classes: string; label: string; icon: ElementType }> = {
  VALID: {
    classes: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-300',
    label: 'Válido',
    icon: CheckCircle2,
  },
  TAMPERED: {
    classes: 'border-rose-500/40 bg-rose-500/10 text-rose-300',
    label: 'Adulterado',
    icon: AlertOctagon,
  },
  NOT_FOUND: {
    classes: 'border-amber-500/40 bg-amber-500/10 text-amber-300',
    label: 'Não encontrado',
    icon: HelpCircle,
  },
}

export function VerificationStatusBadge({
                                          status,
                                        }: VerificationStatusBadgeProps) {
  const config = statusConfig[status]
  const Icon = config.icon

  return (
      <span
          className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-wider ${config.classes}`}
      >
      <Icon className="h-3.5 w-3.5" />
        {config.label}
    </span>
  )
}
