import { BackupCodesCard } from '../../components/totp/BackupCodesCard'
import { TotpSetupCard } from '../../components/totp/TotpSetupCard'
import { useTotp } from '../../hooks/useTotp'

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
    <section className="max-w-4xl">
      <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
        Seguranca da conta
      </p>
      <h1 className="text-3xl font-semibold text-[#F9FAFB]">
        Autenticacao em duas etapas
      </h1>
      <p className="mt-4 max-w-2xl text-base leading-7 text-[#9CA3AF]">
        Proteja sua conta com codigos temporarios gerados no seu aplicativo
        autenticador.
      </p>

      <div className="mt-8 space-y-5">
        {isStatusLoading ? (
          <div className="rounded-xl border border-[#374151] bg-[#1F2937] p-5 text-sm text-[#D1D5DB]">
            Carregando status do 2FA...
          </div>
        ) : null}
        {backupCodes.length > 0 ? (
          <BackupCodesCard codes={backupCodes} />
        ) : null}
        {isEnabled && backupCodes.length === 0 ? (
          <div className="rounded-xl border border-[#10B981]/40 bg-[#10B981]/10 p-5">
            <h2 className="text-lg font-semibold text-[#F9FAFB]">
              Autenticacao em duas etapas ativa
            </h2>
            <p className="mt-2 text-sm leading-6 text-emerald-100">
              Esta conta ja exige codigo TOTP ou codigo de recuperacao apos o
              login por senha.
            </p>
          </div>
        ) : (
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
        )}
      </div>
    </section>
  )
}

