type SignProgressProps = {
  isSigning: boolean
}

export function SignProgress({ isSigning }: SignProgressProps) {
  if (!isSigning) {
    return null
  }

  return (
    <div className="rounded-lg border border-[#06B6D4]/50 bg-[#06B6D4]/10 px-4 py-3 text-sm text-[#CFFAFE]">
      <div className="flex items-center gap-3">
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-[#06B6D4] border-t-transparent" />
        Assinando documento...
      </div>
    </div>
  )
}
