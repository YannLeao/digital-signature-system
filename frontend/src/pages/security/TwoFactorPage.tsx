import {CheckCircle2} from 'lucide-react'
import {BackupCodesCard} from '../../components/totp/BackupCodesCard'
import {TotpSetupCard} from '../../components/totp/TotpSetupCard'
import {useTotp} from '../../hooks/useTotp'

export function TwoFactorPage() {
    const {
        backupCodes,
        code,
        error,
        isConfirming,
        isEnabled,
        isStatusLoading,
        isStarting,
        manualSecret,
        otpauthUrl,
        setCode,
        startSetup,
        verifySetup,
    } = useTotp()

    return (
        <section className="max-w-4xl p-6 space-y-6">
            <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4]">
                    Segurança Avançada
                </p>
                <h1 className="mt-1 text-3xl font-bold text-[#F9FAFB] tracking-tight">
                    Autenticação em Duas Etapas (2FA)
                </h1>
                <p className="mt-2 max-w-2xl text-sm leading-relaxed text-[#9CA3AF]">
                    Proteja sua identidade blindando sua conta com códigos temporários dinâmicos adicionais (TOTP) no momento do acesso.
                </p>
            </div>

            <div className="space-y-6">
                {isStatusLoading && (
                    <div className="flex items-center justify-center rounded-xl border border-[#374151] bg-[#1F2937] p-8 text-sm text-[#D1D5DB] animate-pulse">
                        Sincronizando parâmetros de segurança...
                    </div>
                )}

                {backupCodes.length > 0 && (
                    <BackupCodesCard codes={backupCodes} />
                )}

                {isEnabled && backupCodes.length === 0 ? (
                    <div className="rounded-xl border border-emerald-500/20 bg-emerald-500/5 p-6 animate-fade-in flex items-start gap-3.5">
                        <div className="flex h-9 w-9 items-center justify-center rounded-lg border border-emerald-500/30 bg-emerald-500/10 text-emerald-400 shrink-0">
                            <CheckCircle2 className="h-5 w-5" />
                        </div>
                        <div>
                            <h2 className="text-lg font-bold text-[#F9FAFB] tracking-tight">
                                Proteção Ativa
                            </h2>
                            <p className="mt-1 text-sm leading-relaxed text-emerald-400/80">
                                Seu segundo fator via aplicativo autenticador está ativo e operando normalmente. Chaves extras serão exigidas a cada novo login.
                            </p>
                        </div>
                    </div>
                ) : (
                    !isStatusLoading && (
                        <TotpSetupCard
                            code={code}
                            error={error}
                            isConfirming={isConfirming}
                            isStarting={isStarting}
                            manualSecret={manualSecret}
                            onCodeChange={setCode}
                            onConfirm={verifySetup}
                            onStart={startSetup}
                            otpauthUrl={otpauthUrl}
                        />
                    )
                )}
            </div>
        </section>
    )
}
