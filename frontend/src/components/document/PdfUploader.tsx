import {type ChangeEvent, useId, useState} from 'react'
import {AlertCircle, FileText, FileUp, XCircle} from 'lucide-react'
import {pdfFileSchema} from '../../schemas/signDocumentSchemas'

type PdfUploaderProps = {
    file: File | null
    onFileChange: (file: File | null) => void
}

export function PdfUploader({ file, onFileChange }: PdfUploaderProps) {
    const inputId = useId()
    const [error, setError] = useState<string | null>(null)

    function handleChange(event: ChangeEvent<HTMLInputElement>) {
        const selectedFile = event.target.files?.[0] ?? null

        if (!selectedFile) {
            onFileChange(null)
            setError(null)
            return
        }

        const parsedFile = pdfFileSchema.safeParse(selectedFile)

        if (!parsedFile.success) {
            onFileChange(null)
            setError(parsedFile.error.issues[0]?.message ?? 'Documento PDF inválido.')
            event.target.value = ''
            return
        }

        setError(null)
        onFileChange(parsedFile.data)
    }

    return (
        <section className="rounded-xl border border-[#374151] bg-[#111827] p-5">
            <label className="block text-sm font-semibold text-[#F9FAFB]" htmlFor={inputId}>
                PDF para assinatura
            </label>
            <p className="mt-1 text-sm text-[#9CA3AF]">Arquivo PDF de até 20MB.</p>

            <div className="mt-4">
                {!file ? (
                    /* Estado Vazio - Sem arquivo */
                    <div className="relative flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-[#374151] bg-[#0B1120] p-6 text-center transition-colors hover:border-[#06B6D4]/60 group">
                        <input
                            accept="application/pdf,.pdf"
                            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                            id={inputId}
                            onChange={handleChange}
                            type="file"
                        />
                        <FileUp className="h-8 w-8 text-[#6B7280] group-hover:text-[#06B6D4] transition-colors" />
                        <p className="mt-2 text-sm text-[#D1D5DB]">
                            <span className="font-semibold text-[#06B6D4]">Clique para fazer upload</span> ou arraste o arquivo aqui
                        </p>
                    </div>
                ) : (
                    /* Estado Ativo - Arquivo Selecionado */
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between rounded-lg border border-[#06B6D4]/30 bg-[#06B6D4]/5 p-4 animate-fade-in">
                        <div className="flex items-center gap-3 min-w-0">
                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-[#06B6D4]/10 text-[#67E8F9]">
                                <FileText className="h-5 w-5" />
                            </div>
                            <div className="min-w-0">
                                <p className="truncate text-sm font-medium text-white">{file.name}</p>
                                <p className="text-xs text-[#9CA3AF]">{(file.size / (1024 * 1024)).toFixed(2)} MB</p>
                            </div>
                        </div>
                        <button
                            className="inline-flex items-center justify-center gap-1.5 rounded-lg border border-red-500/30 px-3 py-1.5 text-xs font-medium text-red-400 transition hover:bg-red-500/10"
                            onClick={() => onFileChange(null)}
                            type="button"
                        >
                            <XCircle className="h-3.5 w-3.5" />
                            Remover arquivo
                        </button>
                    </div>
                )}
            </div>

            {error ? (
                <div className="mt-3 flex items-center gap-2 rounded-lg border border-red-500/40 bg-red-500/10 px-3 py-2.5 text-xs font-medium text-red-300">
                    <AlertCircle className="h-4 w-4 shrink-0 text-red-500" />
                    <span>{error}</span>
                </div>
            ) : null}
        </section>
    )
}
