import {ChevronLeft, ChevronRight} from 'lucide-react'

type PageSelectorProps = {
    currentPage: number
    numPages: number
    onPageChange: (page: number) => void
}

export function PageSelector({
                                 currentPage,
                                 numPages,
                                 onPageChange,
                             }: PageSelectorProps) {
    const canGoBack = currentPage > 1
    const canGoForward = currentPage < numPages

    return (
        <div className="flex flex-wrap items-center gap-2">
            <button
                aria-label="Página anterior"
                className="flex h-9 w-9 items-center justify-center rounded-lg border border-[#374151] text-[#D1D5DB] transition enabled:hover:border-[#06B6D4] enabled:hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
                disabled={!canGoBack}
                onClick={() => onPageChange(currentPage - 1)}
                type="button"
            >
                <ChevronLeft className="h-4 w-4" />
            </button>

            <label className="flex items-center gap-2 text-sm text-[#D1D5DB]">
                Página
                <input
                    className="w-16 rounded-lg border border-[#374151] bg-[#0B1120] px-2.5 py-1.5 text-center text-sm text-white outline-none transition focus:border-[#06B6D4] focus:ring-2 focus:ring-[#06B6D4]/20"
                    max={numPages}
                    min={1}
                    onChange={(event) => onPageChange(Number(event.target.value))}
                    type="number"
                    value={currentPage}
                />
            </label>
            <span className="text-sm text-[#9CA3AF]">de {numPages}</span>

            <button
                aria-label="Próxima página"
                className="flex h-9 w-9 items-center justify-center rounded-lg border border-[#374151] text-[#D1D5DB] transition enabled:hover:border-[#06B6D4] enabled:hover:text-white disabled:cursor-not-allowed disabled:opacity-40"
                disabled={!canGoForward}
                onClick={() => onPageChange(currentPage + 1)}
                type="button"
            >
                <ChevronRight className="h-4 w-4" />
            </button>
        </div>
    )
}
