type TotpSecretFieldProps = {
  secret: string
}

export function TotpSecretField({ secret }: TotpSecretFieldProps) {
  async function copySecret() {
    await navigator.clipboard.writeText(secret.replace(/-/g, ''))
  }

  return (
    <div className="rounded-lg border border-[#374151] bg-[#111827] p-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-sm font-medium text-[#F9FAFB]">Chave manual</p>
          <code className="mt-2 block break-all text-sm text-cyan-100">
            {secret}
          </code>
        </div>
        <button
          className="rounded-lg border border-[#374151] px-4 py-2 text-sm font-medium text-[#D1D5DB] transition hover:border-[#06B6D4] hover:text-white"
          onClick={() => void copySecret()}
          type="button"
        >
          Copiar
        </button>
      </div>
    </div>
  )
}

