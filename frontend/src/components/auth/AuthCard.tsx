import type { ReactNode } from 'react'

type AuthCardProps = {
  title: string
  children: ReactNode
  footer: ReactNode
}

export function AuthCard({ title, children, footer }: AuthCardProps) {
  return (
    <section className="mx-auto w-full max-w-md rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-2xl shadow-black/30">
      <div className="mb-6">
        <p className="mb-2 text-sm font-medium uppercase text-[#06B6D4]">
          Autenticação
        </p>
        <h1 className="text-2xl font-semibold text-[#F9FAFB]">{title}</h1>
      </div>
      {children}
      <div className="mt-6 border-t border-[#374151] pt-4 text-sm text-[#9CA3AF]">
        {footer}
      </div>
    </section>
  )
}
