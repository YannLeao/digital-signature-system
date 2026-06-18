import {useAuth} from '../hooks/useAuth'
import {FileCheck, FileSignature, Fingerprint, KeySquare, ShieldAlert} from 'lucide-react'
import {ModuleLink} from '../components/general/ModuleLink.tsx'

export function ProtectedPage() {
  const { email } = useAuth()

  return (
      <section className="space-y-8 animate-fade-in">
        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
          <div>
            <p className="mb-3 text-sm font-medium uppercase tracking-wider text-[#06B6D4]">
              Painel
            </p>
            <h1 className="text-3xl font-semibold text-[#F9FAFB] sm:text-4xl tracking-tight">
              Assinador Digital
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-[#9CA3AF]">
              {email ? `Conta: ${email}.` : 'Conta autenticada.'}
            </p>
          </div>
        </div>

        <div className="grid gap-4 lg:grid-cols-2">
          <ModuleLink
              icon={FileSignature}
              description="Upload, selo visual e download do documento assinado."
              title="Assinar documento"
              to="/documents/sign"
          />
          <ModuleLink
              icon={FileCheck}
              description="Conferir integridade e autenticidade de um PDF."
              title="Verificar assinatura"
              to="/verificar"
          />
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <ModuleLink
              icon={ShieldAlert}
              description="Sessões ativas, revogação e histórico da conta."
              title="Segurança"
              to="/settings/security"
          />
          <ModuleLink
              icon={KeySquare}
              description="Código temporário e backup codes."
              title="2FA / TOTP"
              to="/settings/2fa"
          />
          <ModuleLink
              icon={Fingerprint}
              description="Credenciais WebAuthn do dispositivo."
              title="Passkeys"
              to="/security/passkeys"
          />
        </div>
      </section>
  )
}
