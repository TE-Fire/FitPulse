<template>
  <div class="page home-page">
    <!-- Header -->
    <header class="home-header">
      <div>
        <p class="hello">你好,{{ userStore.username || '健身达人' }}</p>
        <h1 class="page-title">今日训练</h1>
      </div>
      <div class="streak-badge">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
        </svg>
        <span>{{ data?.streakDays ?? '-' }} 天连续</span>
      </div>
    </header>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中…</div>

    <!-- 数据 -->
    <template v-else-if="data">
      <!-- B 维度:本周训练容量(大号高亮卡) -->
      <section class="card volume-card">
        <div class="card-head">
          <span class="card-label">本周训练容量</span>
          <span class="dim-tag dim-blue">B · 容量</span>
        </div>
        <div class="big-value">
          <span class="value">{{ formatNumber(data.totalVolumeThisWeek) }}</span>
          <span class="unit">kg</span>
        </div>
        <div class="mini-grid">
          <div class="mini">
            <p class="mini-label">总组数</p>
            <p class="mini-value blue">{{ data.totalSetsThisWeek }}</p>
          </div>
          <div class="mini">
            <p class="mini-label">总次数</p>
            <p class="mini-value blue">{{ data.totalRepsThisWeek }}</p>
          </div>
        </div>
      </section>

      <!-- C 维度:7天容量趋势 + 完成率 -->
      <section class="card trend-card">
        <div class="card-head">
          <span class="card-label">近 7 天容量趋势</span>
          <span class="dim-tag dim-green">C · 趋势</span>
        </div>
        <div ref="chartRef" class="chart-container"></div>
        <div class="progress-row">
          <div class="progress-item">
            <p class="progress-label">完成率</p>
            <div class="progress-bar">
              <div class="progress-fill green" :style="{ width: data.completionRate7d + '%' }"></div>
            </div>
            <p class="progress-text">{{ data.completionRate7d }}%</p>
          </div>
        </div>
      </section>

      <!-- 辅助指标:本周训练次数 / 计划数 -->
      <section class="aux-grid">
        <div class="card aux-card">
          <p class="aux-label">本周训练次数</p>
          <p class="aux-value">{{ data.totalWorkoutsThisWeek }}<span class="aux-unit">次</span></p>
        </div>
        <div class="card aux-card">
          <p class="aux-label">我的训练计划</p>
          <p class="aux-value">{{ data.totalPlans }}<span class="aux-unit">个</span></p>
        </div>
      </section>

      <!-- 今日训练推荐 -->
      <section class="card today-card">
        <p class="today-label">今日推荐</p>
        <div class="today-content">
          <div>
            <p class="today-title">推 Day · 胸 + 三头</p>
            <p class="today-sub">5 个动作 · 约 65 分钟</p>
          </div>
          <button class="start-btn">开始训练</button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTrainingDashboard } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(true)
const data = ref(null)
const chartRef = ref(null)
let chart = null

function formatNumber(n) {
  if (!n) return '0'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getTrainingDashboard()
    await nextTick()
    initChart()
  } finally {
    loading.value = false
  }
}

function initChart() {
  if (!chartRef.value || !data.value) return
  chart = echarts.init(chartRef.value)
  const trend = data.value.weeklyVolumeTrend
  chart.setOption({
    grid: { left: 8, right: 8, top: 16, bottom: 24, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>容量: <b>${p.value.toLocaleString()} kg</b>`
      }
    },
    xAxis: {
      type: 'category',
      data: trend.map(i => i.date),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      show: false
    },
    series: [
      {
        type: 'line',
        data: trend.map(i => i.volume),
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#43A047', width: 3 },
        itemStyle: { color: '#43A047', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(67,160,71,0.25)' },
            { offset: 1, color: 'rgba(67,160,71,0)' }
          ])
        }
      }
    ]
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.home-page {
  padding: 0 16px 80px 16px;
  background: #f6f7fb;
  min-height: 100vh;
}

/* Header */
.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 4px 20px 4px;
}
.hello {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 4px 0;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}
.streak-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
}
.streak-badge svg {
  width: 14px;
  height: 14px;
}

/* 通用卡片 */
.card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 14px -4px rgba(30, 27, 75, 0.06);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.card-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}
.dim-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}
.dim-blue {
  background: rgba(30, 136, 229, 0.1);
  color: #1E88E5;
}
.dim-green {
  background: rgba(67, 160, 71, 0.1);
  color: #43A047;
}

/* B 容量卡 */
.volume-card {
  background: linear-gradient(135deg, #1E88E5 0%, #1976D2 100%);
  color: white;
}
.volume-card .card-label,
.volume-card .mini-label {
  color: rgba(255, 255, 255, 0.85);
}
.volume-card .dim-blue {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}
.big-value {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin: 8px 0 16px 0;
}
.value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}
.unit {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}
.mini-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.mini {
  background: rgba(255, 255, 255, 0.1);
  padding: 10px 12px;
  border-radius: 10px;
}
.mini-label {
  font-size: 11px;
  margin: 0 0 4px 0;
}
.mini-value {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
}
.mini-value.blue {
  color: white;
}

/* 趋势卡 */
.trend-card .chart-container {
  width: 100%;
  height: 180px;
  margin: 4px 0 12px 0;
}
.progress-row {
  margin-top: 8px;
}
.progress-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.progress-label {
  font-size: 12px;
  color: #64748b;
  margin: 0;
  width: 56px;
}
.progress-bar {
  flex: 1;
  height: 8px;
  background: #f1f5f9;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.4s ease;
}
.progress-fill.green {
  background: linear-gradient(90deg, #43A047, #66BB6A);
}
.progress-text {
  font-size: 12px;
  font-weight: 600;
  color: #43A047;
  margin: 0;
  width: 44px;
  text-align: right;
}

/* 辅助卡 */
.aux-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}
.aux-card {
  margin: 0;
  text-align: center;
}
.aux-label {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 6px 0;
}
.aux-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}
.aux-unit {
  font-size: 14px;
  font-weight: 500;
  color: #94a3b8;
  margin-left: 2px;
}

/* 今日推荐 */
.today-card {
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
  border: none;
}
.today-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 6px 0;
}
.today-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.today-title {
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 4px 0;
}
.today-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}
.start-btn {
  padding: 10px 20px;
  background: white;
  color: #7c5cff;
  font-size: 14px;
  font-weight: 600;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.15s ease;
}
.start-btn:active {
  transform: scale(0.96);
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
  font-size: 14px;
}
</style>
