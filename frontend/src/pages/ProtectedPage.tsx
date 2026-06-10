import { Link } from 'react-router-dom'

import { useAuth } from '../hooks/useAuth'

export function ProtectedPage() {
  const { email } = useAuth()

  return (
    <section className="space-y-8">
      <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
        Area autenticada
      </p>
      <h1 className="text-3xl font-semibold text-[#F9FAFB]">
        Painel do assinador digital
      </h1>
      <p className="mt-4 text-base leading-7 text-[#9CA3AF]">
        {email
          ? `Sessao ativa para ${email}.`
          : 'Sessao ativa.'}{' '}
        Acesse os modulos implementados para a demonstracao.
      </p>
      <div className="grid gap-4 md:grid-cols-3">
        <ModuleLink
          description="Enviar PDF, posicionar selo e baixar documento assinado."
          title="Assinar PDF"
          to="/documents/sign"
        />
        <ModuleLink
          description="Adicionar, listar e revogar credenciais WebAuthn."
          title="Passkeys"
          to="/security/passkeys"
        />
        <ModuleLink
          description="Ativar TOTP, confirmar codigo e gerar backup codes."
          title="2FA / TOTP"
          to="/settings/2fa"
        />
      </div>
    </section>
  )
}

type ModuleLinkProps = {
  description: string
  title: string
  to: string
}

function ModuleLink({ description, title, to }: ModuleLinkProps) {
  return (
    <Link
      className="rounded-lg border border-[#374151] bg-[#111827] p-4 transition hover:border-[#06B6D4]"
      to={to}
    >
      <h2 className="text-base font-semibold text-white">{title}</h2>
      <p className="mt-2 text-sm leading-6 text-[#9CA3AF]">{description}</p>
    </Link>
  )
}
