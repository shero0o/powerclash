// vite.config.js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // WebSocket-Proxy (falls du z.B. Live-Daten o.ä. via WS nutzt):
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      },
      // HTTP-Proxy: Weiterleitung aller /api-Aufrufe zum Spring-Gateway auf Port 8080
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
