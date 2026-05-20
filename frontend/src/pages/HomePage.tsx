import { Link, useLocation } from 'react-router-dom'

type HomeState = {
  authMessage?: string
}

export function HomePage() {
  const location = useLocation()
  const state = location.state as HomeState | null

  return (
    <section className="max-w-3xl">
      {state?.authMessage ? (
        <div className="mb-6 rounded-lg border border-[#10B981]/40 bg-[#10B981]/10 px-4 py-3 text-sm text-emerald-200">
          {state.authMessage}
        </div>
      ) : null}
      <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
        Plataforma segura
      </p>
      <h1 className="text-3xl font-semibold text-[#F9FAFB] sm:text-4xl">
        Autenticacao inicial pronta para os proximos modulos de seguranca.
      </h1>
      <p className="mt-4 text-base leading-7 text-[#9CA3AF]">
        Cadastre usuarios e valide credenciais contra a API real sem criar
        tokens provisorios, sessoes falsas ou armazenamento sensivel no browser.
      </p>
      <div className="mt-8 flex flex-wrap gap-3">
        <Link
          className="rounded-lg bg-[#06B6D4] px-4 py-2 text-sm font-medium text-white transition hover:bg-cyan-400"
          to="/register"
        >
          Criar conta
        </Link>
        <Link
          className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
          to="/login"
        >
          Entrar
        </Link>
      </div>
    </section>
  )
}
