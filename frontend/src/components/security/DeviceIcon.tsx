import {Laptop, Monitor, Smartphone} from 'lucide-react'

type DeviceIconProps = {
  userAgent: string | null
}

export function DeviceIcon({ userAgent }: DeviceIconProps) {
  const value = userAgent?.toLowerCase() ?? ''

  const isMobile = /mobile|android|iphone|ipad/.test(value)
  const isTablet = /ipad|tablet/.test(value)

  return (
      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/10 text-[#67E8F9]">
      {isMobile ? (
          <Smartphone className="h-5 w-5" />
      ) : isTablet ? (
          <Monitor className="h-5 w-5" />
      ) : (
          <Laptop className="h-5 w-5" />
      )}
    </span>
  )
}
