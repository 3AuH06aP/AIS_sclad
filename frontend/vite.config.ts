import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

const apiTarget = process.env.API_PROXY_TARGET || 'http://localhost:8080';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: apiTarget,
        changeOrigin: true,
        secure: false
      }
    }
  }
});
