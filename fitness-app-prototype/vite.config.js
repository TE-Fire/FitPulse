import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// Vite 配置:Vue 3 + @ 别名 + 相对 base(便于离线打开 / Capacitor 打包)
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5174,
    host: '0.0.0.0' // 允许手机扫码访问
  },
  base: './'
})
