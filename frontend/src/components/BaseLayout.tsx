import { Link, Outlet, useNavigate } from 'react-router-dom'

import { useAuth } from '../hooks/useAuth'

export function BaseLayout() {
  const navigate = useNavigate()
  const { email, isAuthenticated, logout } = useAuth()

  async function handleLogout() {
    await logout()
    navigate('/login', {
      replace: true,
      state: { authMessage: 'Logout realizado com sucesso.' },
    })
  }

  return (
    <div className="min-h-screen bg-[#0B1120] text-[#F9FAFB]">
      <header className="border-b border-[#374151] bg-[#111827]/90 backdrop-blur">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-4 py-4 sm:px-6">
          <Link className="text-sm font-semibold uppercase text-[#F9FAFB]" to={isAuthenticated ? '/app' : '/'}>
            Projeto Seguranca
          </Link>
          <nav className="flex items-center gap-2 text-sm">
            <Link
              className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
              to="/verificar"
            >
              Verificar PDF
            </Link>
            {isAuthenticated ? (
              <>
                <Link
                  className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
                  to="/documents/sign"
                >
                  Assinar PDF
                </Link>
                <Link
                  className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
                  to="/settings/security"
                >
                  Seguranca
                </Link>
                <Link
                  className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
                  to="/settings/2fa"
                >
                  2FA
                </Link>
                <Link
                  className="rounded-lg border border-[#374151] px-3 py-2 text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-[#F9FAFB]"
                  to="/security/passkeys"
                >
                  Passkeys
                </Link>
                {email ? (
                  <span className="hidden max-w-48 truncate text-[#9CA3AF] md:inline">
                    {email}
                  </span>
                ) : null}
                <button
                  className="rounded-lg bg-[#374151] px-3 py-2 font-medium text-white transition hover:bg-[#4B5563]"
                  onClick={() => void handleLogout()}
                  type="button"
                >
                  Sair
                </button>
              </>
            ) : (
              <>
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
              </>
            )}
          </nav>
        </div>
      </header>

      <main className="mx-auto w-full max-w-6xl px-4 py-10 sm:px-6">
        <Outlet />
      </main>
    </div>
  )
}
