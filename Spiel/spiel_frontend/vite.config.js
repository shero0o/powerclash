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
            '/api': [
                {
                    // erstes Ziel
                    target: 'http://localhost:8092',
                    changeOrigin: true,
                    secure: false,
                    rewrite: path => path.replace(/^\/api/, '/api')
                },
                {
                    // zweites Ziel
                    target: 'http://localhost:8090',
                    changeOrigin: true,
                    secure: false,
                    rewrite: path => path.replace(/^\/api/, '/api')
                }
            ]
        }
    }
})

