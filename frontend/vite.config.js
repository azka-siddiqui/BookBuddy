import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The dev server runs on 5173 (allowed by the gateway CORS config).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
});
