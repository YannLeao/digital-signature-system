import {Loader2, ShieldX} from 'lucide-react'
import type {PasskeyDevice} from '../../types/passkey'
import {PasskeyItem} from './PasskeyItem'

type PasskeyListProps = {
  devices: PasskeyDevice[]
  isLoading: boolean
  onRevoke: (device: PasskeyDevice) => void
}

export function PasskeyList({
                              devices,
                              isLoading,
                              onRevoke,
                            }: PasskeyListProps) {
  if (isLoading) {
    return (
        <div className="flex items-center justify-center gap-3 rounded-xl border border-[#374151]/50 bg-[#111827] p-8 text-sm text-[#9CA3AF]">
          <Loader2 className="h-5 w-5 animate-spin text-[#06B6D4]" />
          Buscando credenciais de segurança...
        </div>
    )
  }

  if (devices.length === 0) {
    return (
        <div className="flex flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-[#374151] bg-[#111827] p-8 text-center text-sm text-[#9CA3AF]">
          <ShieldX className="h-8 w-8 text-[#4B5563]" />
          <p className="font-medium text-[#D1D5DB]">Nenhuma passkey cadastrada ainda.</p>
          <p className="text-xs text-[#6B7280]">Registre chaves do dispositivo abaixo para habilitar login sem senha.</p>
        </div>
    )
  }

  return (
      <ul className="space-y-3">
        {devices.map((device) => (
            <PasskeyItem device={device} key={device.id} onRevoke={onRevoke} />
        ))}
      </ul>
  )
}
