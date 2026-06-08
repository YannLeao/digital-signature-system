import { BackupCodesCard } from '../../components/totp/BackupCodesCard'
import { TotpSetupCard } from '../../components/totp/TotpSetupCard'
import { useTotp } from '../../hooks/useTotp'

export function TwoFactorPage() {
  const {
    backupCodes,
    clearBackupCodes,
    code,
    error,
    isConfirming,
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
        {backupCodes.length > 0 ? (
          <BackupCodesCard codes={backupCodes} onClear={clearBackupCodes} />
        ) : null}
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
      </div>
    </section>
  )
}

