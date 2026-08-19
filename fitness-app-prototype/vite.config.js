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
    host: '0.0.0.0', // 允许手机扫码访问
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 后端 FileController 以 /files/** 暴露上传资源(头像等),
      // 前端 avatarUrl 形如 /files/avatars/xxx.jpg,dev 下必须代理到 8080,
      // 否则会落到 SPA fallback 返回 index.html,导致 img 裂图
      '/files': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  base: './'
})
