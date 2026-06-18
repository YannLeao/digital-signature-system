type DeviceIconProps = {
  userAgent: string | null
}

export function DeviceIcon({ userAgent }: DeviceIconProps) {
  const value = userAgent?.toLowerCase() ?? ''
  const label = /mobile|android|iphone|ipad/.test(value) ? 'MO' : 'PC'

  return (
    <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-[#06B6D4]/40 bg-[#06B6D4]/10 text-xs font-semibold text-[#67E8F9]">
      {label}
    </span>
  )
}
