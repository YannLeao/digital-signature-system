import { useActionState } from 'react'

type AddPasskeyButtonProps = {
  disabled?: boolean
  onAdd: (deviceName: string) => Promise<void>
}

export function AddPasskeyButton({ disabled, onAdd }: AddPasskeyButtonProps) {
  const [, action, isPending] = useActionState(
      async (_: unknown, formData: FormData) => {
        const deviceName = (formData.get('deviceName') as string).trim()
        if (deviceName) await onAdd(deviceName)
        return null
      },
      null,
  )

  return (
      <form
          action={action}
          className="flex flex-col gap-3 border-t border-[#374151] pt-5 sm:flex-row"
      >
        <label className="flex-1">
        <span className="mb-2 block text-sm font-medium text-[#F9FAFB]">
          Nome do dispositivo
        </span>
          <input
              autoComplete="off"
              className="w-full rounded-lg border border-[#374151] bg-[#111827] px-3 py-3 text-[#F9FAFB] outline-none transition placeholder:text-[#6B7280] focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/30"
              disabled={disabled || isPending}
              maxLength={80}
              name="deviceName"
              placeholder="Notebook pessoal"
              type="text"
          />
        </label>
        <button
            className="self-end rounded-lg bg-[#06B6D4] px-4 py-3 text-sm font-semibold text-white transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={disabled || isPending}
            type="submit"
        >
          {isPending ? 'Aguardando autenticador...' : 'Adicionar passkey'}
        </button>
      </form>
  )
}