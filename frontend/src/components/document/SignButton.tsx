import {FileSignature} from 'lucide-react'

type SignButtonProps = {
  disabled: boolean
  isSigning: boolean
  onClick: () => void
}

export function SignButton({ disabled, isSigning, onClick }: SignButtonProps) {
  return (
      <button
          className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-[#06B6D4] px-5 py-3 text-sm font-semibold text-[#0B1120] transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:bg-[#374151] disabled:text-[#9CA3AF] shadow-lg shadow-[#06B6D4]/10"
          disabled={disabled || isSigning}
          onClick={onClick}
          type="button"
      >
        {isSigning ? (
            <>
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-[#0B1120] border-t-transparent" />
              Assinando documento...
            </>
        ) : (
            <>
              <FileSignature className="h-4 w-4" />
              Assinar PDF
            </>
        )}
      </button>
  )
}
