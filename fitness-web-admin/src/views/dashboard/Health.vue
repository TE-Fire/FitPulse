<template>
  <div class="fp-page">
    <div class="fp-page__header">
      <div>
        <h1 class="fp-page__title">健康看板</h1>
        <p class="fp-page__desc">今日健康概览 · A 维度（体重/体脂）+ B 维度（热量/饮水）</p>
      </div>
      <div class="fp-page__actions">
        <el-button plain @click="load" :loading="loading">
          <el-icon><Refresh /></el-icon><span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 顶部指标卡：A 重点 + B 重点 + 辅助 -->
    <div class="fp-grid">
      <StatCard dim="A" label="最新体重 (A)" :value="fmtWeight(overview.latestWeight)" unit="kg" :sub="`目标 80.0kg · 还差 ${(80.0 - (overview.latestWeight || 0)).toFixed(1)}kg`" />
      <StatCard dim="A" label="体脂率 (A)" :value="fmtWeight(overview.latestBodyFat)" unit="%" :sub="`目标 15.0% · 健康区间 10-20%`" />
      <StatCard dim="Bo" label="今日热量 (B)" :value="fmtNum(overview.caloriesToday)" unit="kcal" :sub="`目标 ${overview.dailyCaloriesGoal || 2200}kcal · ${calPct}`" />
      <StatCard dim="Bo" label="今日饮水 (B)" :value="fmtNum(overview.waterTodayMl)" unit="ml" :sub="`目标 ${overview.waterGoalMl}ml · ${waterPct}`" />
      <StatCard dim="muted" label="蛋白质摄入" :value="overview.proteinTodayG" unit="g" sub="建议 1.6g/kg 体重" />
    </div>

    <!-- 饮水进度条（独立卡） -->
    <div class="fp-card fp-water-card">
      <div class="fp-section-title">
        <el-icon><Coffee /></el-icon>
        饮水进度 (B)
      </div>
      <div class="fp-water-row">
        <div class="fp-water-row__info">
          <strong>{{ overview.waterTodayMl }}</strong>
          <span>/ {{ overview.waterGoalMl }} ml</span>
          <small>{{ waterPct }} 完成</small>
        </div>
        <div class="fp-progress" style="flex:1;">
          <i :style="{ width: waterPctNum + '%', background: 'var(--dim-Bo)' }" />
        </div>
      </div>
    </div>

    <!-- 图表：30 天体重折线 (A 紫) + 7 天热量柱 (B 橙) -->
    <div class="fp-grid fp-grid--charts" style="margin-top: 16px;">
      <ChartLine
        title="30 天体重趋势 (A)"
        :dim="'A'"
        :series="[{ name: '体重', data: weightValues }]"
        :xData="weightDates"
        yUnit="kg"
        :smooth="true"
        :area="true"
        :loading="loading"
      />
      <ChartBar
        title="7 天热量摄入 (B)"
        :dim="'Bo'"
        :series="[{ name: '热量', data: calValues }]"
        :xData="calDates"
        yUnit="kcal"
        :loading="loading"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * 健康看板
 * - 数据来源：api/dashboard.js getHealthOverview() → HealthOverview
 * - 维度：A（体重/体脂，紫）/ B（热量/饮水，橙）/ 辅助 muted（蛋白质）
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import ChartLine from '@/components/ChartLine.vue'
import ChartBar from '@/components/ChartBar.vue'
import { getHealthOverview } from '@/api/dashboard'

const loading = ref(false)
const overview = ref({
  latestWeight: 0,
  latestBodyFat: 0,
  weightTrend30d: [],
  caloriesToday: 0,
  waterTodayMl: 0,
  waterGoalMl: 2000,
  proteinTodayG: 0,
  caloriesLast7d: []
})

const weightDates = computed(() => overview.value.weightTrend30d.map(t => t.date.slice(5)))
const weightValues = computed(() => overview.value.weightTrend30d.map(t => t.value))
const calDates = computed(() => overview.value.caloriesLast7d.map(t => t.date.slice(5)))
const calValues = computed(() => overview.value.caloriesLast7d.map(t => t.value))

const calPct = computed(() => pct(overview.value.caloriesToday, 2200))
const waterPctNum = computed(() => pctNum(overview.value.waterTodayMl, overview.value.waterGoalMl))
const waterPct = computed(() => waterPctNum.value + '%')

function fmtNum(v) { return (v ?? 0).toLocaleString('zh-CN') }
function fmtWeight(v) { return (v ?? 0).toFixed(1) }
function pctNum(cur, target) {
  if (!target) return 0
  return Math.min(100, Math.round((cur / target) * 100))
}
function pct(cur, target) { return pctNum(cur, target) + '%' }

async function load() {
  loading.value = true
  try {
    const data = await getHealthOverview()
    overview.value = { ...overview.value, ...data }
  } catch (e) {
    ElMessage.error('加载健康看板失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.fp-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.fp-page__title { margin: 0; font-size: 22px; color: var(--text); }
.fp-page__desc  { margin: 4px 0 0; font-size: 13px; color: var(--text-soft); }

.fp-water-card { margin-top: 16px; padding: 16px 20px; }
.fp-water-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.fp-water-row__info {
  display: flex;
  align-items: baseline;
  gap: 6px;
  white-space: nowrap;
}
.fp-water-row__info strong { font-size: 22px; color: var(--dim-Bo); font-variant-numeric: tabular-nums; }
.fp-water-row__info span { color: var(--text-soft); font-size: 13px; }
.fp-water-row__info small { color: var(--text-muted); font-size: 12px; margin-left: 4px; }
</style>
