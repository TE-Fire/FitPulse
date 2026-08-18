/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        // FitPulse 品牌色
        pulse: {
          DEFAULT: '#7c5cff',   // 主品牌紫
          cyan: '#22d3ee',      // 副青
          accent: '#f59e0b',    // 点缀琥珀
          success: '#10b981'    // 成功绿
        }
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', 'sans-serif']
      },
      boxShadow: {
        card: '0 24px 60px -12px rgba(30, 27, 75, 0.18)',
        soft: '0 8px 24px -4px rgba(30, 27, 75, 0.08)'
      },
      keyframes: {
        heartbeat: {
          '0%, 100%': { transform: 'scale(1)' },
          '25%': { transform: 'scale(1.12)' },
          '50%': { transform: 'scale(0.96)' },
          '75%': { transform: 'scale(1.06)' }
        },
        'fade-up': {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' }
        },
        'pulse-ring': {
          '0%': { transform: 'scale(0.8)', opacity: '0.6' },
          '100%': { transform: 'scale(2)', opacity: '0' }
        }
      },
      animation: {
        heartbeat: 'heartbeat 1.2s ease-in-out infinite',
        'fade-up': 'fade-up 0.5s ease-out both',
        'pulse-ring': 'pulse-ring 1.8s ease-out infinite'
      }
    }
  },
  // 关闭 preflight 与 Element Plus 全局样式冲突的部分：保留默认即可，Element Plus 样式优先级足够
  corePlugins: {
    preflight: true
  }
}
