import { downloadBackupCodes } from '../../utils/downloadBackupCodes'

type BackupCodesCardProps = {
  codes: string[]
}

export function BackupCodesCard({ codes }: BackupCodesCardProps) {
  async function copyCodes() {
    await navigator.clipboard.writeText(codes.join('\n'))
  }

  return (
    <div className="rounded-xl border border-[#10B981]/40 bg-[#10B981]/10 p-5">
      <h2 className="text-lg font-semibold text-[#F9FAFB]">
        Codigos de recuperacao
      </h2>
      <p className="mt-2 text-sm leading-6 text-emerald-100">
        Estes codigos aparecem uma unica vez. Guarde em um local seguro antes de
        sair desta tela.
      </p>
      <div className="mt-4 grid gap-2 sm:grid-cols-2">
        {codes.map((code) => (
          <code
            className="rounded-lg border border-[#374151] bg-[#0B1120] px-3 py-2 text-center text-sm text-[#F9FAFB]"
            key={code}
          >
            {code}
          </code>
        ))}
      </div>
      <div className="mt-5 flex flex-wrap gap-3">
        <button
          className="rounded-lg bg-[#06B6D4] px-4 py-2 text-sm font-semibold text-white transition hover:bg-cyan-400"
          onClick={() => void copyCodes()}
          type="button"
        >
          Copiar todos
        </button>
        <button
          className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
          onClick={() => downloadBackupCodes(codes)}
          type="button"
        >
          Baixar .txt
        </button>
      </div>
    </div>
  )
}

