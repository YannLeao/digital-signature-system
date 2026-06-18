import {Check, ClipboardCopy, Download, ShieldAlert} from 'lucide-react'
import {useState} from 'react'
import {downloadBackupCodes} from '../../utils/downloadBackupCodes'

type BackupCodesCardProps = {
    codes: string[]
}

export function BackupCodesCard({ codes }: BackupCodesCardProps) {
    const [copied, setCopied] = useState(false)

    async function copyCodes() {
        await navigator.clipboard.writeText(codes.join('\n'))
        setCopied(true)
        setTimeout(() => setCopied(false), 2000)
    }

    return (
        <div className="rounded-xl border border-emerald-500/20 bg-emerald-500/5 p-6 animate-fade-in">
            <div className="flex items-start gap-3">
                <div className="flex h-9 w-9 items-center justify-center rounded-lg border border-emerald-500/30 bg-emerald-500/10 text-emerald-400 shrink-0">
                    <ShieldAlert className="h-5 w-5" />
                </div>
                <div>
                    <h2 className="text-lg font-bold text-[#F9FAFB] tracking-tight">
                        Códigos de Recuperação
                    </h2>
                    <p className="mt-1 text-sm leading-relaxed text-emerald-400/80">
                        Estes códigos aparecem uma única vez. Guarde-os em um local seguro e criptografado antes de sair desta tela.
                    </p>
                </div>
            </div>

            <div className="mt-5 grid gap-2.5 sm:grid-cols-2">
                {codes.map((code) => (
                    <code
                        className="rounded-lg border border-[#374151] bg-[#0B1120] px-4 py-2.5 text-center font-mono text-sm font-semibold text-[#22D3EE] tracking-wider transition-colors hover:border-[#06B6D4]/40"
                        key={code}
                    >
                        {code}
                    </code>
                ))}
            </div>

            <div className="mt-6 flex flex-wrap gap-3 border-t border-emerald-500/10 pt-4">
                <button
                    className="flex items-center gap-2 rounded-lg bg-[#06B6D4] px-4 py-2.5 text-sm font-semibold text-[#111827] transition hover:bg-[#22D3EE] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/20"
                    onClick={() => void copyCodes()}
                    type="button"
                >
                    {copied ? <Check className="h-4 w-4" /> : <ClipboardCopy className="h-4 w-4" />}
                    {copied ? 'Copiado!' : 'Copiar todos'}
                </button>
                <button
                    className="flex items-center gap-2 rounded-lg border border-[#374151] px-4 py-2.5 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/10"
                    onClick={() => downloadBackupCodes(codes)}
                    type="button"
                >
                    <Download className="h-4 w-4" />
                    Baixar .txt
                </button>
            </div>
        </div>
    )
}
