type ActivityResultBadgeProps = {
    result: string
}

export function ActivityResultBadge({ result }: ActivityResultBadgeProps) {
    const normalized = result.toUpperCase()
    const isSuccess = normalized === 'SUCCESS'

    return (
        <span
            className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold border ${
                isSuccess
                    ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-400'
                    : 'border-red-500/30 bg-red-500/10 text-red-400'
            }`}
        >
      <span className={`mr-1.5 h-1.5 w-1.5 rounded-full ${isSuccess ? 'bg-emerald-400' : 'bg-red-400'}`} />
            {isSuccess ? 'Sucesso' : 'Falha'}
    </span>
    )
}
