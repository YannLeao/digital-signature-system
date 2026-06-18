import { VerificationDetails } from './VerificationDetails'
import { VerificationStatusBadge } from './VerificationStatusBadge'
import type { VerifyDocumentResponse, VerifyStatus } from '../../types/verify'

type VerifyResultCardProps = {
  result: VerifyDocumentResponse
}

type StatusContent = {
  description: string
  iconLabel: string
  ringClass: string
  title: string
}

const statusContent: Record<VerifyStatus, StatusContent> = {
  VALID: {
    description: 'O documento confere com o registro da plataforma.',
    iconLabel: 'OK',
    ringClass: 'border-[#10B981]/50 bg-[#10B981]/15 text-[#A7F3D0]',
    title: 'Documento integro e autenticado',
  },
  TAMPERED: {
    description:
      'A integridade do documento foi comprometida. O arquivo pode ter sido alterado apos a assinatura.',
    iconLabel: '!',
    ringClass: 'border-[#EF4444]/50 bg-[#EF4444]/15 text-[#FCA5A5]',
    title: 'Documento adulterado',
  },
  NOT_FOUND: {
    description:
      'Nao encontramos uma assinatura reconhecida pela plataforma neste documento.',
    iconLabel: 'i',
    ringClass: 'border-[#F59E0B]/50 bg-[#F59E0B]/15 text-[#FCD34D]',
    title: 'Assinatura nao encontrada',
  },
}

export function VerifyResultCard({ result }: VerifyResultCardProps) {
  const content = statusContent[result.status]

  return (
    <section
      aria-live="polite"
      className="rounded-xl border border-[#374151] bg-[#111827] p-5 shadow-2xl shadow-black/20"
      tabIndex={-1}
    >
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start">
        <div
          className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-full border text-sm font-bold ${content.ringClass}`}
        >
          {content.iconLabel}
        </div>

        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-3">
            <h2 className="text-xl font-semibold text-[#F9FAFB]">
              {content.title}
            </h2>
            <VerificationStatusBadge status={result.status} />
          </div>
          <p className="mt-2 text-sm leading-6 text-[#D1D5DB]">
            {result.message || content.description}
          </p>
          <p className="mt-1 text-sm leading-6 text-[#9CA3AF]">
            {content.description}
          </p>

          {result.status === 'VALID' && result.signature ? (
            <div className="mt-5 rounded-lg border border-[#374151] bg-[#0B1120] p-4">
              <VerificationDetails signature={result.signature} />
            </div>
          ) : null}
        </div>
      </div>
    </section>
  )
}
