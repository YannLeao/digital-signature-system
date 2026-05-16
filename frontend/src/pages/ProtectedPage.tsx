export function ProtectedPage() {
  return (
    <section className="max-w-xl">
      <p className="mb-3 text-sm font-medium uppercase tracking-wide text-emerald-700">
        Area privada
      </p>
      <h1 className="text-3xl font-semibold text-slate-950">
        Rota protegida inicial.
      </h1>
      <p className="mt-4 text-base leading-7 text-slate-600">
        Esta pagina so deve renderizar quando houver token local disponivel.
      </p>
    </section>
  )
}
