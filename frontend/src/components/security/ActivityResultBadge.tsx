type ActivityResultBadgeProps = {
  result: string
}

export function ActivityResultBadge({ result }: ActivityResultBadgeProps) {
  const normalized = result.toUpperCase()
  const isSuccess = normalized === 'SUCCESS'

  return (
    <span
      className={
        isSuccess
          ? 'rounded-full border border-[#10B981]/40 bg-[#10B981]/10 px-2 py-0.5 text-xs font-medium text-emerald-200'
          : 'rounded-full border border-[#EF4444]/40 bg-[#EF4444]/10 px-2 py-0.5 text-xs font-medium text-red-200'
      }
    >
      {isSuccess ? 'Sucesso' : 'Falha'}
    </span>
  )
}
