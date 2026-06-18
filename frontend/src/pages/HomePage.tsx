import {Link, Navigate, useLocation} from 'react-router-dom'
import {FileSearch, LogIn, ShieldCheck, UserPlus} from 'lucide-react'

import {useAuth} from '../hooks/useAuth'

type HomeState = {
  authMessage?: string
}

export function HomePage() {
  const location = useLocation()
  const state = location.state as HomeState | null
  const { isAuthenticated } = useAuth()

  if (isAuthenticated) {
    return <Navigate replace state={state ?? undefined} to="/app" />
  }

  return (
      <section className="grid gap-10 lg:grid-cols-[minmax(0,1fr)_360px] lg:items-center animate-fade-in">
        {state?.authMessage ? (
            <div className="lg:col-span-2 rounded-lg border border-[#10B981]/40 bg-[#10B981]/10 px-4 py-3 text-sm text-emerald-200">
              {state.authMessage}
            </div>
        ) : null}

        <div>
          {/* Tag Superior */}
          <div className="inline-flex items-center gap-2 mb-4 px-3 py-1 rounded-full border border-[#06B6D4]/30 bg-[#06B6D4]/5 text-xs font-medium text-[#67E8F9]">
            <ShieldCheck className="h-3.5 w-3.5" /> Projeto de Segurança da Informação
          </div>

          <h1 className="max-w-3xl text-4xl font-semibold text-[#F9FAFB] sm:text-5xl tracking-tight leading-tight">
            Assine e verifique PDFs com controles fortes de segurança.
          </h1>

          <p className="mt-5 max-w-2xl text-base leading-7 text-[#9CA3AF]">
            Plataforma para assinatura criptográfica de documentos, verificação
            pública de autenticidade e proteção de acesso com passkeys, TOTP,
            sessões ativas e auditoria.
          </p>

          {/* Botões com Ícones */}
          <div className="mt-8 flex flex-wrap gap-3">
            <Link
                className="inline-flex items-center gap-2 rounded-lg bg-[#06B6D4] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-cyan-400 group"
                to="/register"
            >
              <UserPlus className="h-4 w-4" />
              Criar conta
            </Link>
            <Link
                className="inline-flex items-center gap-2 rounded-lg border border-[#374151] px-5 py-2.5 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
                to="/login"
            >
              <LogIn className="h-4 w-4 text-[#9CA3AF]" />
              Entrar
            </Link>
            <Link
                className="inline-flex items-center gap-2 rounded-lg border border-[#374151] px-5 py-2.5 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
                to="/verificar"
            >
              <FileSearch className="h-4 w-4 text-[#9CA3AF]" />
              Verificar PDF
            </Link>
          </div>
        </div>
      </section>
  )
}
