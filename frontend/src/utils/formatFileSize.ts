export function formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes'

    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']

    const i = Math.floor(Math.log(bytes) / Math.log(k))

    const index = Math.min(i, sizes.length - 1)

    const formattedValue = parseFloat((bytes / Math.pow(k, index)).toFixed(2))

    return `${formattedValue} ${sizes[index]}`
}
