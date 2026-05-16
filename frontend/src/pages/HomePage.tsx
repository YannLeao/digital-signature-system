import { Link } from 'react-router-dom'

export function HomePage() {
  return (
    <section className="max-w-3xl">
      <p className="mb-3 text-sm font-medium uppercase tracking-wide text-emerald-700">
        Base inicial
      </p>
      <h1 className="text-3xl font-semibold text-slate-950 sm:text-4xl">
        Frontend preparado para as proximas telas seguras.
      </h1>
      <p className="mt-4 text-base leading-7 text-slate-600">
        React, Vite, TypeScript, TailwindCSS, roteamento e cliente HTTP estao
        configurados para os proximos cards.
      </p>
      <div className="mt-8 flex flex-wrap gap-3">
        <Link
          className="rounded bg-slate-950 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
          to="/app"
        >
          Acessar area privada
        </Link>
        <Link
          className="rounded border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-white"
          to="/login"
        >
          Ir para login
        </Link>
      </div>
    </section>
  )
}
