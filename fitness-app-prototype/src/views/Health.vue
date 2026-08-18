<template>
  <div class="page health-page">
    <header class="page-header">
      <h1 class="page-title">健康看板</h1>
      <p class="page-sub">体重 · 摄入 · 饮水</p>
    </header>

    <div v-if="loading" class="loading">加载中…</div>

    <template v-else-if="data">
      <!-- A 维度:体重 + 体脂 + 30天趋势 -->
      <section class="card weight-card">
        <div class="card-head">
          <span class="card-label">身体指标</span>
          <span class="dim-tag dim-purple">A · 体重体脂</span>
        </div>
        <div class="weight-grid">
          <div class="weight-item">
            <p class="weight-label">最新体重</p>
            <div class="weight-value-row">
              <span class="weight-value">{{ data.latestWeight }}</span>
              <span class="weight-unit">kg</span>
            </div>
          </div>
          <div class="weight-divider"></div>
          <div class="weight-item">
            <p class="weight-label">最新体脂率</p>
            <div class="weight-value-row">
              <span class="weight-value">{{ data.latestBodyFat }}</span>
              <span class="weight-unit">%</span>
            </div>
          </div>
        </div>
        <div ref="weightChartRef" class="chart-container"></div>
        <p class="chart-caption">30 天体重趋势 · 累计下降 {{ weightDelta }} kg</p>
      </section>

      <!-- B 维度:今日热量摄入 + 7天柱图 -->
      <section class="card calories-card">
        <div class="card-head">
          <span class="card-label">今日摄入</span>
          <span class="dim-tag dim-orange">B · 热量</span>
        </div>
        <div class="calories-main">
          <div>
            <p class="big-number">{{ formatNumber(data.caloriesToday) }}</p>
            <p class="big-unit">kcal</p>
          </div>
          <div class="protein-mini">
            <p class="protein-label">蛋白质</p>
            <p class="protein-value">{{ data.proteinTodayG }}<span class="protein-unit">g</span></p>
          </div>
        </div>
        <div ref="caloriesChartRef" class="chart-container small"></div>
        <p class="chart-caption">近 7 天热量摄入</p>
      </section>

      <!-- B 维度:今日饮水 + 进度条 -->
      <section class="card water-card">
        <div class="card-head">
          <span class="card-label">今日饮水</span>
          <span class="dim-tag dim-orange">B · 饮水</span>
        </div>
        <div class="water-main">
          <div class="water-numbers">
            <span class="water-current">{{ data.waterTodayMl }}</span>
            <span class="water-target">/ {{ data.waterGoalMl }} ml</span>
          </div>
          <p class="water-pct">{{ waterPct }}%</p>
        </div>
        <div class="water-bar">
          <div class="water-fill" :style="{ width: waterPct + '%' }"></div>
        </div>
        <div class="water-actions">
          <button class="water-btn" @click="addWater(250)">+ 250ml</button>
          <button class="water-btn" @click="addWater(500)">+ 500ml</button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import { getHealthDashboard } from '@/api/dashboard'

const loading = ref(true)
const data = ref(null)
const weightChartRef = ref(null)
const caloriesChartRef = ref(null)
let weightChart = null
let caloriesChart = null

const weightDelta = computed(() => {
  if (!data.value?.weightTrend30d?.length) return '0.0'
  const trend = data.value.weightTrend30d
  const first = trend[0].value
  const last = trend[trend.length - 1].value
  return Math.max(0, first - last).toFixed(1)
})

const waterPct = computed(() => {
  if (!data.value) return 0
  return Math.min(100, Math.round((data.value.waterTodayMl / data.value.waterGoalMl) * 100))
})

function formatNumber(n) {
  if (!n) return '0'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

async function loadData() {
  loading.value = true
  try {
    data.value = await getHealthDashboard()
    await nextTick()
    initWeightChart()
    initCaloriesChart()
  } finally {
    loading.value = false
  }
}

function initWeightChart() {
  if (!weightChartRef.value || !data.value) return
  weightChart = echarts.init(weightChartRef.value)
  const trend = data.value.weightTrend30d
  weightChart.setOption({
    grid: { left: 0, right: 0, top: 16, bottom: 4, containLabel: false },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>体重: <b>${p.value} kg</b>`
      }
    },
    xAxis: { type: 'category', data: trend.map(i => i.date), show: false },
    yAxis: { type: 'value', show: false, scale: true },
    series: [
      {
        type: 'line',
        data: trend.map(i => i.value),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#8E24AA', width: 3 },
        itemStyle: { color: '#8E24AA', borderColor: '#fff', borderWidth: 2 }
      }
    ]
  })
}

function initCaloriesChart() {
  if (!caloriesChartRef.value || !data.value) return
  caloriesChart = echarts.init(caloriesChartRef.value)
  const trend = data.value.caloriesLast7d
  caloriesChart.setOption({
    grid: { left: 0, right: 0, top: 8, bottom: 20, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30,41,59,0.92)',
      borderWidth: 0,
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>摄入: <b>${p.value.toLocaleString()} kcal</b>`
      }
    },
    xAxis: {
      type: 'category',
      data: trend.map(i => i.date),
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisTick: { show: false },
      axisLabel: { color: '#94a3b8', fontSize: 10 }
    },
    yAxis: { type: 'value', show: false },
    series: [
      {
        type: 'bar',
        data: trend.map(i => i.value),
        barWidth: 16,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF6F00' },
            { offset: 1, color: '#FFA726' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  })
}

function addWater(ml) {
  // 原型交互:本地累加 mock 数据,模拟饮水追加
  data.value.waterTodayMl += ml
}

function handleResize() {
  weightChart?.resize()
  caloriesChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  weightChart?.dispose()
  caloriesChart?.dispose()
})
</script>

<style scoped>
.health-page {
  padding: 0 16px 80px 16px;
  background: #f6f7fb;
  min-height: 100vh;
}
.page-header {
  padding: 24px 4px 20px 4px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px 0;
}
.page-sub {
  font-size: 12px;
  color: #64748b;
  margin: 0;
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
  margin-bottom: 12px;
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
.dim-purple {
  background: rgba(142, 36, 170, 0.1);
  color: #8E24AA;
}
.dim-orange {
  background: rgba(255, 111, 0, 0.1);
  color: #FF6F00;
}

/* A 体重体脂卡 */
.weight-card .weight-grid {
  display: grid;
  grid-template-columns: 1fr 1px 1fr;
  gap: 12px;
  margin-bottom: 8px;
}
.weight-item {
  text-align: center;
}
.weight-divider {
  background: #e2e8f0;
}
.weight-label {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 8px 0;
}
.weight-value-row {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}
.weight-value {
  font-size: 28px;
  font-weight: 700;
  color: #8E24AA;
}
.weight-unit {
  font-size: 12px;
  color: #64748b;
}
.chart-container {
  width: 100%;
  height: 140px;
  margin: 8px 0 4px 0;
}
.chart-container.small {
  height: 120px;
}
.chart-caption {
  font-size: 11px;
  color: #94a3b8;
  text-align: center;
  margin: 0;
}

/* B 热量卡 */
.calories-card .calories-main {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 4px;
}
.big-number {
  font-size: 36px;
  font-weight: 700;
  color: #FF6F00;
  margin: 0;
  line-height: 1;
}
.big-unit {
  font-size: 13px;
  color: #FF6F00;
  margin: 0;
}
.protein-mini {
  text-align: right;
}
.protein-label {
  font-size: 11px;
  color: #64748b;
  margin: 0 0 4px 0;
}
.protein-value {
  font-size: 18px;
  font-weight: 600;
  color: #475569;
  margin: 0;
}
.protein-unit {
  font-size: 11px;
  color: #94a3b8;
}

/* B 饮水卡 */
.water-card .water-main {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
}
.water-numbers {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.water-current {
  font-size: 32px;
  font-weight: 700;
  color: #FF6F00;
}
.water-target {
  font-size: 12px;
  color: #94a3b8;
}
.water-pct {
  font-size: 14px;
  font-weight: 600;
  color: #FF6F00;
  margin: 0;
}
.water-bar {
  height: 10px;
  background: #f1f5f9;
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 16px;
}
.water-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6F00, #FFA726);
  border-radius: 999px;
  transition: width 0.4s ease;
}
.water-actions {
  display: flex;
  gap: 8px;
}
.water-btn {
  flex: 1;
  padding: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #FF6F00;
  background: rgba(255, 111, 0, 0.08);
  border: 1px solid rgba(255, 111, 0, 0.2);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.water-btn:active {
  transform: scale(0.97);
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
  font-size: 14px;
}
</style>
