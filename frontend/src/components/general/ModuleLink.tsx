import { type ElementType } from 'react'
import { Link } from 'react-router-dom'

type ModuleLinkProps = {
  icon: ElementType
  description: string
  title: string
  to: string
}

export function ModuleLink({ icon: Icon, description, title, to }: ModuleLinkProps) {
  return (
    <Link
      className="group rounded-xl border border-[#374151] bg-[#111827] p-5 transition-all duration-200 hover:border-[#06B6D4] hover:bg-[#111827]/80 hover:shadow-lg hover:shadow-[#06B6D4]/5"
      to={to}
    >
      <span className="flex h-10 w-10 items-center justify-center rounded-lg border border-[#06B6D4]/40 bg-[#06B6D4]/10 text-[#67E8F9] transition-colors group-hover:border-[#67E8F9] group-hover:bg-[#06B6D4]/20">
        <Icon className="h-5 w-5" />
      </span>

      <h2 className="mt-4 text-base font-semibold text-white transition-colors group-hover:text-[#67E8F9]">
        {title}
      </h2>
      <p className="mt-2 text-sm leading-6 text-[#9CA3AF]">{description}</p>
    </Link>
  )
}
