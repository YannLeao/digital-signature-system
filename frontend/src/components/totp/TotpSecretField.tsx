import {Check, Copy, KeyRound} from 'lucide-react'
import {useState} from 'react'

type TotpSecretFieldProps = {
  secret: string
}

export function TotpSecretField({ secret }: TotpSecretFieldProps) {
  const [copied, setCopied] = useState(false)

  async function copySecret() {
    await navigator.clipboard.writeText(secret.replace(/-/g, ''))
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
      <div className="rounded-lg border border-[#374151] bg-[#111827] p-4 group transition-colors hover:border-[#374151]/80">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-start gap-3">
            <KeyRound className="h-4 w-4 text-[#06B6D4] mt-0.5 shrink-0" />
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-[#9CA3AF]">Chave de configuração manual</p>
              <code className="mt-1 block break-all font-mono text-sm font-medium text-[#22D3EE]">
                {secret}
              </code>
            </div>
          </div>
          <button
              className="flex h-9 items-center justify-center gap-2 rounded-lg border border-[#374151] px-3.5 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/10 shrink-0"
              onClick={() => void copySecret()}
              type="button"
          >
            {copied ? <Check className="h-3.5 w-3.5 text-emerald-400" /> : <Copy className="h-3.5 w-3.5" />}
            <span>{copied ? 'Copiado' : 'Copiar'}</span>
          </button>
        </div>
      </div>
  )
}
