import { defineConfig } from 'vite'
import preact from '@preact/preset-vite'

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      react: "preact-compat",
      "react-dom": "preact-compat"
    },
  },
  build: {
    manifest: true,
    rollupOptions: {
      // overwrite default .html entry
      input: './src/main.tsx'
    }
  },
  plugins: [preact()],
  server: {
    host: true,
    port: 3001,
  },
})
