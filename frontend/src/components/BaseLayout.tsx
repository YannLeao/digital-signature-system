import { Outlet } from 'react-router-dom'

export function BaseLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <span className="text-sm font-semibold uppercase tracking-wide text-slate-700">
            Projeto Seguranca
          </span>
          <span className="rounded bg-emerald-50 px-2 py-1 text-xs font-medium text-emerald-700">
            Setup frontend
          </span>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl px-4 py-10 sm:px-6">
        <Outlet />
      </main>
    </div>
  )
}
