export function formatSecurityDate(value: string | null | undefined): string {
  if (!value) {
    return 'Indisponivel'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return 'Indisponivel'
  }

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(date)
}
