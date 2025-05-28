import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  base: '/ukuk/',
  server: {
    host: '0.0.0.0',
    port: 10001,
    strictPort: true,
    open: true
  },
  preview: {
    host: '0.0.0.0',
    port: 10001,
    strictPort: true
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
    rollupOptions: {
      output: {
        manualChunks: undefined
      }
    }
  }
});