import {type FormEvent} from 'react'
import {AlertTriangle, Laptop, ShieldCheck} from 'lucide-react'
import {TotpCodeInput} from './TotpCodeInput'
import {TotpQrCode} from './TotpQrCode'
import {TotpSecretField} from './TotpSecretField'

type TotpSetupCardProps = {
    code: string
    error?: string | null
    isConfirming?: boolean
    isStarting?: boolean
    manualSecret: string
    onCodeChange: (code: string) => void
    onConfirm: (code: string) => Promise<void>
    onStart: () => Promise<void>
    otpauthUrl?: string
}

export function TotpSetupCard({
                                  code,
                                  error,
                                  isConfirming,
                                  isStarting,
                                  manualSecret,
                                  onCodeChange,
                                  onConfirm,
                                  onStart,
                                  otpauthUrl,
                              }: TotpSetupCardProps) {
    async function submitConfirmation(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        await onConfirm(code)
    }

    if (!otpauthUrl) {
        return (
            <div className="rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-xl shadow-black/20 animate-fade-in">
                <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 text-[#67E8F9]">
                    <ShieldCheck className="h-5 w-5" />
                </div>
                <h2 className="text-xl font-bold text-[#F9FAFB] tracking-tight">
                    Autenticação em duas etapas
                </h2>
                <p className="mt-2 text-sm leading-relaxed text-[#9CA3AF]">
                    Configure um aplicativo autenticador para adicionar uma camada extra de segurança e proteger sua conta contra acessos não autorizados.
                </p>

                {error && (
                    <div className="mt-4 flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/5 px-4 py-3 text-sm text-red-400">
                        <AlertTriangle className="h-4 w-4 shrink-0" />
                        <span>{error}</span>
                    </div>
                )}

                <button
                    className="mt-6 rounded-lg bg-[#06B6D4] px-5 py-3 text-sm font-semibold text-[#111827] transition hover:bg-[#22D3EE] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/20 disabled:cursor-not-allowed disabled:opacity-50"
                    disabled={isStarting}
                    onClick={() => void onStart()}
                    type="button"
                >
                    {isStarting ? 'Gerando chaves de segurança...' : 'Ativar autenticação em duas etapas'}
                </button>
            </div>
        )
    }

    return (
        <div className="rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-xl shadow-black/20 animate-fade-in">
            <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 text-[#67E8F9]">
                <Laptop className="h-5 w-5" />
            </div>
            <h2 className="text-xl font-bold text-[#F9FAFB] tracking-tight">
                Escaneie o QR Code
            </h2>
            <p className="mt-1 text-sm leading-relaxed text-[#9CA3AF]">
                Abra o Google Authenticator, Microsoft Authenticator, Authy ou app compatível. Se preferir, digite a chave manual.
            </p>

            <div className="mt-6 flex flex-col gap-6 lg:flex-row">
                <TotpQrCode otpauthUrl={otpauthUrl} />
                <div className="flex-1 space-y-4">
                    <TotpSecretField secret={manualSecret} />

                    <form className="space-y-4" onSubmit={submitConfirmation}>
                        <TotpCodeInput
                            disabled={isConfirming}
                            onChange={onCodeChange}
                            value={code}
                            error={!!error}
                        />

                        {error && (
                            <div className="flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/5 px-4 py-3 text-sm text-red-400">
                                <AlertTriangle className="h-4 w-4 shrink-0" />
                                <span>{error}</span>
                            </div>
                        )}

                        <button
                            className="w-full rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-[#111827] transition hover:bg-[#22D3EE] focus:outline-none focus:ring-4 focus:ring-[#06B6D4]/20 disabled:cursor-not-allowed disabled:opacity-50"
                            disabled={isConfirming || code.length !== 6}
                            type="submit"
                        >
                            {isConfirming ? 'Vinculando dispositivo...' : 'Confirmar ativação'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    )
}
