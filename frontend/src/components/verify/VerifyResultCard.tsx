import {AlertOctagon, HelpCircle, ShieldCheck} from 'lucide-react'
import {VerificationDetails} from './VerificationDetails'
import {VerificationStatusBadge} from './VerificationStatusBadge'
import type {VerifyDocumentResponse, VerifyStatus} from '../../types/verify'
import type {ElementType} from 'react'

type VerifyResultCardProps = {
  result: VerifyDocumentResponse
}

type StatusContent = {
  description: string
  icon: ElementType
  ringClass: string
  title: string
}

const statusContent: Record<VerifyStatus, StatusContent> = {
  VALID: {
    description: 'O documento confere perfeitamente com o registro criptográfico da plataforma.',
    icon: ShieldCheck,
    ringClass: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400',
    title: 'Documento íntegro e autenticado',
  },
  TAMPERED: {
    description: 'A integridade do documento foi comprometida! O arquivo sofreu alterações não autorizadas após a assinatura.',
    icon: AlertOctagon,
    ringClass: 'border-rose-500/40 bg-rose-500/10 text-rose-400',
    title: 'Documento adulterado',
  },
  NOT_FOUND: {
    description: 'Não encontramos nenhuma assinatura válida reconhecida por esta autoridade de registro.',
    icon: HelpCircle,
    ringClass: 'border-amber-500/40 bg-amber-500/10 text-amber-400',
    title: 'Assinatura não encontrada',
  },
}

export function VerifyResultCard({ result }: VerifyResultCardProps) {
  const content = statusContent[result.status]
  const Icon = content.icon

  return (
      <section
          aria-live="polite"
          className="rounded-xl border border-[#374151] bg-[#111827] p-5 shadow-2xl shadow-black/20 animate-fade-in"
          tabIndex={-1}
      >
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start">
          {/* Ícone de Status do Escudo */}
          <div className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl border shadow-inner ${content.ringClass}`}>
            <Icon className="h-6 w-6" />
          </div>

          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-3">
              <h2 className="text-xl font-bold text-[#F9FAFB] tracking-tight">
                {content.title}
              </h2>
              <VerificationStatusBadge status={result.status} />
            </div>

            <p className="mt-2 text-sm leading-6 text-[#D1D5DB]">
              {result.message || content.description}
            </p>

            {result.status === 'VALID' && result.signature ? (
                <div className="mt-4 pt-4 border-t border-[#374151]/60">
                  <VerificationDetails signature={result.signature} />
                </div>
            ) : null}
          </div>
        </div>
      </section>
  )
}
