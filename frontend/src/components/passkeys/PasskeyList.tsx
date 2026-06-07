import type { PasskeyDevice } from '../../types/passkey'
import { PasskeyItem } from './PasskeyItem'

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
      <div className="rounded-lg border border-[#374151] bg-[#111827] p-5 text-sm text-[#9CA3AF]">
        Carregando passkeys...
      </div>
    )
  }

  if (devices.length === 0) {
    return (
      <div className="rounded-lg border border-[#374151] bg-[#111827] p-5 text-sm text-[#9CA3AF]">
        Nenhuma passkey cadastrada ainda.
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

