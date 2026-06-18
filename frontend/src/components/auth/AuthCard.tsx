import type {ReactNode} from 'react'
import {LockKeyhole} from 'lucide-react'

type AuthCardProps = {
    title: string
    children: ReactNode
    footer: ReactNode
}

export function AuthCard({ title, children, footer }: AuthCardProps) {
    return (
        <section className="mx-auto w-full max-w-md rounded-xl border border-[#374151] bg-[#1F2937] p-6 shadow-2xl shadow-black/30 animate-fade-in">
            <div className="mb-6 flex flex-col items-center text-center sm:items-start sm:text-left">
                <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 text-[#67E8F9]">
                    <LockKeyhole className="h-5 w-5" />
                </div>
                <p className="text-xs font-semibold uppercase tracking-wider text-[#06B6D4]">
                    Autenticação Digital
                </p>
                <h1 className="mt-1 text-2xl font-bold text-[#F9FAFB] tracking-tight">{title}</h1>
            </div>

            {children}

            <div className="mt-6 border-t border-[#374151] pt-4 text-center text-sm text-[#9CA3AF]">
                {footer}
            </div>
        </section>
    )
}