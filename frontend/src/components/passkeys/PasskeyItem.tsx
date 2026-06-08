import type { PasskeyDevice } from '../../types/passkey'

type PasskeyItemProps = {
  device: PasskeyDevice
  onRevoke: (device: PasskeyDevice) => void
}

export function PasskeyItem({ device, onRevoke }: PasskeyItemProps) {
  return (
    <li className="flex flex-col gap-4 rounded-lg border border-[#374151] bg-[#111827] p-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <div className="flex flex-wrap items-center gap-2">
          <h3 className="font-medium text-[#F9FAFB]">{device.deviceName}</h3>
          <span className="rounded-md border border-[#10B981]/40 px-2 py-1 text-xs text-emerald-200">
            {device.active ? 'Ativa' : 'Inativa'}
          </span>
        </div>
        <p className="mt-2 text-sm text-[#9CA3AF]">
          Criada em {formatDate(device.createdAt)}
        </p>
        <p className="mt-1 text-sm text-[#6B7280]">
          Ultimo uso: {device.lastUsed ? formatDate(device.lastUsed) : 'Nunca'}
        </p>
      </div>
      <button
        className="rounded-lg border border-[#EF4444]/50 px-4 py-2 text-sm font-medium text-red-200 transition hover:bg-[#EF4444]/10"
        onClick={() => onRevoke(device)}
        type="button"
      >
        Revogar
      </button>
    </li>
  )
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

