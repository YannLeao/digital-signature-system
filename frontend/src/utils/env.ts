type RequiredVariable = 'VITE_API_BASE_URL'

type AppEnvironment = Record<RequiredVariable, string>

function readRequiredVariable(name: RequiredVariable): string {
  const value = import.meta.env[name]

  if (typeof value !== 'string' || value.trim() === '') {
    throw new Error(`Missing required environment variable: ${name}`)
  }

  return value
}

export const env: AppEnvironment = {
  VITE_API_BASE_URL: readRequiredVariable('VITE_API_BASE_URL'),
}