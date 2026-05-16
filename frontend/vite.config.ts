import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const environment = loadEnv(mode, process.cwd(), 'VITE_')
  const apiBaseUrl = environment.VITE_API_BASE_URL

  if (!apiBaseUrl) {
    throw new Error('Missing required environment variable: VITE_API_BASE_URL')
  }

  const apiOrigin = new URL(apiBaseUrl).origin

  return {
    plugins: [react(), tailwindcss()],
    server: {
      proxy: {
        '/api': {
          target: apiOrigin,
          changeOrigin: true,
        },
      },
    },
  }
})
