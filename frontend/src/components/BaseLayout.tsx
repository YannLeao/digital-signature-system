import {Link, NavLink, Outlet, useNavigate} from 'react-router-dom'
import {type ElementType, useEffect, useRef, useState} from 'react'
import {ChevronDown, Fingerprint, KeySquare, LogOut, Shield, ShieldAlert} from 'lucide-react'

import {useAuth} from '../hooks/useAuth'

export function BaseLayout() {
  const navigate = useNavigate()
  const { email, isAuthenticated, logout } = useAuth()
  const [accountMenuIsOpen, setAccountMenuIsOpen] = useState(false)
  const accountMenuRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    function handlePointerDown(event: MouseEvent) {
      if (!accountMenuRef.current?.contains(event.target as Node)) {
        setAccountMenuIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    return () => document.removeEventListener('mousedown', handlePointerDown)
  }, [])

  async function handleLogout() {
    await logout()
    setAccountMenuIsOpen(false)
    navigate('/login', {
      replace: true,
      state: { authMessage: 'Logout realizado com sucesso.' },
    })
  }

  return (
      <div className="min-h-screen bg-[#0B1120] text-[#F9FAFB] antialiased">
        <header className="border-b border-[#374151] bg-[#111827]/90 backdrop-blur sticky top-0 z-50">
          <div className="mx-auto flex w-full max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-4 sm:px-6">

            <Link
                className="flex items-center gap-3 text-sm font-semibold uppercase tracking-wider text-[#F9FAFB] group"
                to={isAuthenticated ? '/app' : '/'}
            >
            <span className="flex h-9 w-9 items-center justify-center rounded-lg border border-[#06B6D4]/40 bg-[#06B6D4]/10 text-[#67E8F9] transition-colors group-hover:border-[#67E8F9]">
              <Shield className="h-4 w-4" />
            </span>
              <span>Assinador Digital</span>
            </Link>

            <nav className="flex flex-wrap items-center gap-2 text-sm">
              {isAuthenticated ? (
                  <>
                    <HeaderLink to="/app">Painel</HeaderLink>
                    <HeaderLink to="/documents/sign">Assinar</HeaderLink>
                    <HeaderLink to="/verificar">Verificar</HeaderLink>

                    {/* Botão do Menu da Conta */}
                    <div className="relative" ref={accountMenuRef}>
                      <button
                          aria-expanded={accountMenuIsOpen}
                          className="flex items-center gap-2 rounded-lg border border-[#374151] bg-[#111827] px-3 py-2 font-medium text-[#F9FAFB] transition hover:border-[#06B6D4] group"
                          onClick={() => setAccountMenuIsOpen((current) => !current)}
                          type="button"
                      >
                    <span className="flex h-6 w-6 items-center justify-center rounded-md bg-[#374151] text-xs font-bold text-[#D1D5DB] group-hover:bg-[#06B6D4]/20 group-hover:text-[#67E8F9] transition-colors">
                      {email?.slice(0, 1).toUpperCase() ?? 'U'}
                    </span>
                        <span className="hidden max-w-36 truncate md:inline text-sm">
                      {email ?? 'Conta'}
                    </span>
                        <ChevronDown className={`h-3.5 w-3.5 text-[#9CA3AF] transition-transform duration-200 ${accountMenuIsOpen ? 'rotate-180' : ''}`} />
                      </button>

                      {accountMenuIsOpen ? (
                          <div className="absolute right-0 z-40 mt-2 w-64 rounded-xl border border-[#374151] bg-[#111827] p-1.5 shadow-2xl animate-fade-in">
                            <AccountMenuLink
                                icon={ShieldAlert}
                                onClick={() => setAccountMenuIsOpen(false)}
                                to="/settings/security"
                            >
                              Sessões e atividade
                            </AccountMenuLink>
                            <AccountMenuLink
                                icon={KeySquare}
                                onClick={() => setAccountMenuIsOpen(false)}
                                to="/settings/2fa"
                            >
                              Autenticação de 2 etapas
                            </AccountMenuLink>
                            <AccountMenuLink
                                icon={Fingerprint}
                                onClick={() => setAccountMenuIsOpen(false)}
                                to="/security/passkeys"
                            >
                              Passkeys (Biometria)
                            </AccountMenuLink>

                            <div className="h-px bg-[#374151] my-1" />

                            <button
                                className="flex w-full items-center gap-2.5 rounded-lg px-3 py-2 text-left text-sm font-medium text-rose-400 transition hover:bg-rose-500/10"
                                onClick={() => void handleLogout()}
                                type="button"
                            >
                              <LogOut className="h-4 w-4" />
                              Sair da conta
                            </button>
                          </div>
                      ) : null}
                    </div>
                  </>
              ) : (
                  <>
                    <HeaderLink to="/verificar">Verificar</HeaderLink>
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

type HeaderLinkProps = {
  children: string
  to: string
}

function HeaderLink({ children, to }: HeaderLinkProps) {
  return (
      <NavLink
          className={({ isActive }) =>
              [
                'rounded-lg border px-3 py-2 transition text-sm font-medium',
                isActive
                    ? 'border-[#06B6D4] bg-[#06B6D4]/10 text-[#67E8F9]'
                    : 'border-[#374151] text-[#D1D5DB] hover:border-[#06B6D4] hover:text-[#F9FAFB]',
              ].join(' ')
          }
          to={to}
      >
        {children}
      </NavLink>
  )
}

type AccountMenuLinkProps = {
  children: string
  icon: ElementType
  onClick: () => void
  to: string
}

function AccountMenuLink({ children, icon: Icon, onClick, to }: AccountMenuLinkProps) {
  return (
      <NavLink
          className={({ isActive }) =>
              [
                'flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm transition font-medium',
                isActive
                    ? 'bg-[#06B6D4]/10 text-[#67E8F9]'
                    : 'text-[#D1D5DB] hover:bg-[#1F2937] hover:text-white',
              ].join(' ')
          }
          onClick={onClick}
          to={to}
      >
        <Icon className="h-4 w-4 opacity-80" />
        {children}
      </NavLink>
  )
}
