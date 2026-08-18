/** @type {import('tailwindcss').Config} */
// 复用 fitness-web-admin 的 pulse tokens + 增加看板维度重点色(设计契约 5.1/5.2)
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        // FitPulse 品牌色(与 fitness-web-admin 完全一致)
        pulse: {
          DEFAULT: '#7c5cff',   // 主品牌紫
          cyan: '#22d3ee',      // 副青
          accent: '#f59e0b',    // 点缀琥珀
          success: '#10b981'    // 成功绿
        },
        // 看板维度重点色(设计契约 5.1/5.2 固化)
        dim: {
          blue: '#1E88E5',     // B 训练容量/组数/次数
          green: '#43A047',    // C 完成率/趋势
          purple: '#8E24AA',   // A 体重/体脂
          orange: '#FF6F00'    // B 摄入/饮水
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
        }
      },
      animation: {
        heartbeat: 'heartbeat 1.2s ease-in-out infinite',
        'fade-up': 'fade-up 0.5s ease-out both'
      }
    }
  }
}
