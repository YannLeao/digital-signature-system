import { Link, Outlet } from 'react-router-dom'

export function BaseLayout() {
  return (
    <div className="min-h-screen bg-[#0B1120] text-[#F9FAFB]">
      <header className="border-b border-[#374151] bg-[#111827]/90 backdrop-blur">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <Link className="text-sm font-semibold uppercase text-[#F9FAFB]" to="/">
            Projeto Seguranca
          </Link>
          <nav className="flex items-center gap-2 text-sm">
            <Link
              className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
              to="/documents/sign"
            >
              Assinar PDF
            </Link>
            <Link
              className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
              to="/login"
            >
              Login
            </Link>
            <Link
              className="rounded-lg bg-[#06B6D4] px-3 py-2 font-medium text-white transition hover:bg-cyan-400"
              to="/register"
            >
              Cadastro
            </Link>
          </nav>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl px-4 py-10 sm:px-6">
        <Outlet />
      </main>
    </div>
  )
}
