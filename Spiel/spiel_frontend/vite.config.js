// vite.config.js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // WebSocket-Proxy (bleibt exakt so → für dein Spiel / Socket.io etc.)
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      },

      // HTTP-Proxy für alle /api-Calls → geht an Gateway (Port 8090)
      // → wichtig: alle deine /api/shop_catalogue/... und /api/wallet/... gehen über Gateway → so soll es sein
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
        rewrite: path => path.replace(/^\/api/, '/api') // optional, aber macht es stabil
      }
    }
  }
});
