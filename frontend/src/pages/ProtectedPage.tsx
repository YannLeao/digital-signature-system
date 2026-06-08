import { Link } from 'react-router-dom'

export function ProtectedPage() {
  return (
    <section className="max-w-xl">
      <p className="mb-3 text-sm font-medium uppercase text-[#06B6D4]">
        Area privada
      </p>
      <h1 className="text-3xl font-semibold text-[#F9FAFB]">
        Rota protegida inicial.
      </h1>
      <p className="mt-4 text-base leading-7 text-[#9CA3AF]">
        Esta pagina so deve renderizar quando houver token local disponivel.
      </p>
      <Link
        className="mt-6 inline-flex rounded-lg bg-[#06B6D4] px-4 py-2 text-sm font-medium text-white transition hover:bg-cyan-400"
        to="/security/passkeys"
      >
        Gerenciar passkeys
      </Link>
      <Link
        className="ml-3 mt-6 inline-flex rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
        to="/settings/2fa"
      >
        Configurar 2FA
      </Link>
    </section>
  )
}
