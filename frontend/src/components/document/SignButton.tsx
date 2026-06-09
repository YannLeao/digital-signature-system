type SignButtonProps = {
  disabled: boolean
  isSigning: boolean
  onClick: () => void
}

export function SignButton({ disabled, isSigning, onClick }: SignButtonProps) {
  return (
    <button
      className="inline-flex items-center justify-center rounded-lg bg-[#06B6D4] px-5 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:bg-[#374151] disabled:text-[#9CA3AF]"
      disabled={disabled || isSigning}
      onClick={onClick}
      type="button"
    >
      {isSigning ? 'Assinando documento...' : 'Assinar PDF'}
    </button>
  )
}
