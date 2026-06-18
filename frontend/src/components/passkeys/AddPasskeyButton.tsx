import {useActionState} from 'react'
import {KeyRound, Loader2} from 'lucide-react'

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
            className="flex flex-col gap-3 border-t border-[#374151]/60 pt-6 sm:flex-row sm:items-end"
        >
            <label className="flex-1">
        <span className="mb-2 block text-sm font-medium text-[#F9FAFB]">
          Nome do dispositivo
        </span>
                <input
                    autoComplete="off"
                    className="w-full rounded-lg border border-[#374151] bg-[#111827] px-3.5 py-2.5 text-[#F9FAFB] text-sm outline-none transition placeholder:text-[#6B7280] focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/20"
                    disabled={disabled || isPending}
                    maxLength={80}
                    name="deviceName"
                    placeholder="Ex: Macbook Pro Pro, Windows Hello Celular"
                    type="text"
                />
            </label>

            <button
                className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#06B6D4] px-5 py-2.5 text-sm font-semibold text-[#0B1120] transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:bg-[#374151] disabled:text-[#9CA3AF] shadow-lg shadow-[#06B6D4]/5 h-[42px]"
                disabled={disabled || isPending}
                type="submit"
            >
                {isPending ? (
                    <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Aguardando autenticador...
                    </>
                ) : (
                    <>
                        <KeyRound className="h-4 w-4" />
                        Adicionar passkey
                    </>
                )}
            </button>
        </form>
    )
}
