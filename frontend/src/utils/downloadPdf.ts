export function downloadPdf(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')

  anchor.href = url
  anchor.download = filename
  anchor.rel = 'noopener'
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()

  window.setTimeout(() => URL.revokeObjectURL(url), 0)
}

export function signedFilename(originalName: string): string {
  const cleanName = originalName.trim() || 'documento'
  const withoutPdf = cleanName.toLowerCase().endsWith('.pdf')
    ? cleanName.slice(0, -4)
    : cleanName

  return `${withoutPdf}-assinado.pdf`
}
