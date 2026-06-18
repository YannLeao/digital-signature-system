import {Loader2} from 'lucide-react'

type SignProgressProps = {
  isSigning: boolean
}

export function SignProgress({ isSigning }: SignProgressProps) {
  if (!isSigning) {
    return null
  }

  return (
      <div className="rounded-lg border border-[#06B6D4]/40 bg-[#06B6D4]/5 px-4 py-3 text-sm text-[#CFFAFE] animate-pulse">
        <div className="flex items-center gap-3 font-medium">
          <Loader2 className="h-4 w-4 animate-spin text-[#06B6D4]" />
          Assinando documento eletronicamente...
        </div>
      </div>
  )
}
