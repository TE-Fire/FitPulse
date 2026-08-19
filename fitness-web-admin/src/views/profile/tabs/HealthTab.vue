<template>
  <div v-loading="loading" class="fp-tab">
    <!-- 主要指标卡：A 体重 / A 体脂 / B 热量 / B 饮水 -->
    <div class="fp-grid">
      <StatCard dim="A" label="最新体重" :value="fmtWeight(ov.latestWeight)" unit="kg" :sub="`30 天 ${fmtChange(ov.weightChange30d)} kg`" />
      <StatCard dim="A" label="最新体脂率" :value="fmtWeight(ov.latestBodyFat)" unit="%" :sub="`30 天 ${fmtChange(ov.bodyFatChange30d)} %`" />
      <StatCard dim="Bo" label="今日热量" :value="fmtNum(ov.caloriesToday)" unit="kcal" :sub="`目标 ${ov.caloriesGoal} kcal · ${pct(ov.caloriesToday, ov.caloriesGoal)}`" />
      <StatCard dim="Bo" label="今日饮水" :value="fmtNum(ov.waterTodayMl)" unit="ml" :sub="`目标 ${ov.waterGoalMl} ml · ${pct(ov.waterTodayMl, ov.waterGoalMl)}`" />
    </div>

    <!-- 进度条区：热量 + 饮水 -->
    <div class="fp-card fp-progress-card">
      <div class="fp-progress-row">
        <div class="fp-progress-row__info">
          <span class="fp-progress-row__label">热量摄入进度</span>
          <strong>{{ ov.caloriesToday }} / {{ ov.caloriesGoal }} kcal · {{ pct(ov.caloriesToday, ov.caloriesGoal) }}</strong>
        </div>
        <div class="fp-progress" style="flex:1;">
          <i :style="{ width: pctNum(ov.caloriesToday, ov.caloriesGoal) + '%', background: 'var(--dim-Bo)' }" />
        </div>
      </div>
      <div class="fp-progress-row" style="margin-top: 12px;">
        <div class="fp-progress-row__info">
          <span class="fp-progress-row__label">饮水进度</span>
          <strong>{{ ov.waterTodayMl }} / {{ ov.waterGoalMl }} ml · {{ pct(ov.waterTodayMl, ov.waterGoalMl) }}</strong>
        </div>
        <div class="fp-progress" style="flex:1;">
          <i :style="{ width: pctNum(ov.waterTodayMl, ov.waterGoalMl) + '%', background: 'var(--dim-B)' }" />
        </div>
      </div>
    </div>

    <!-- 次级指标：蛋白质 + 睡眠 -->
    <div class="fp-grid" style="margin-top: 12px;">
      <StatCard dim="muted" label="今日蛋白质摄入" :value="ov.proteinTodayG" unit="g" :sub="`目标 ${ov.proteinGoalG} g · ${pct(ov.proteinTodayG, ov.proteinGoalG)}`" />
      <StatCard dim="muted" label="昨晚睡眠时长" :value="fmtSleep(ov.sleepHoursLastNight)" unit="h" sub="建议 7-9 小时" />
      <StatCard dim="muted" label="30 天体重变化" :value="fmtChange(ov.weightChange30d)" unit="kg" sub="负数=减重" />
      <StatCard dim="muted" label="30 天体脂变化" :value="fmtChange(ov.bodyFatChange30d)" unit="%" sub="负数=减脂" />
    </div>
  </div>
</template>

<script setup>
/**
 * 健康概览 Tab
 * - 4 个主指标卡（A 体重/体脂 + B 热量/饮水）
 * - 独立进度条卡（热量 + 饮水，与看板页保持一致风格）
 * - 4 个辅助卡（蛋白质/睡眠/30天变化）
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import { getMyHealthOverview } from '@/api/user'

const loading = ref(false)
const ov = ref({
  latestWeight: 0, latestBodyFat: 0,
  weightChange30d: 0, bodyFatChange30d: 0,
  caloriesToday: 0, caloriesGoal: 2200,
  waterTodayMl: 0, waterGoalMl: 2000,
  proteinTodayG: 0, proteinGoalG: 140,
  sleepHoursLastNight: 0
})

function fmtNum(v) { return (v ?? 0).toLocaleString('zh-CN') }
function fmtWeight(v) { return (v ?? 0).toFixed(1) }
function fmtSleep(v) { return (v ?? 0).toFixed(1) }
function fmtChange(v) { return (v ?? 0) > 0 ? '+' + (v ?? 0).toFixed(1) : (v ?? 0).toFixed(1) }
function pctNum(cur, target) {
  if (!target) return 0
  return Math.min(100, Math.round((cur / target) * 100))
}
function pct(cur, target) { return pctNum(cur, target) + '%' }

async function load() {
  loading.value = true
  try {
    ov.value = { ...ov.value, ...await getMyHealthOverview() }
  } catch (e) {
    ElMessage.error('加载健康概览失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.fp-tab { padding: 4px 0; }
.fp-progress-card { margin-top: 16px; padding: 18px 20px; }
.fp-progress-row {
  display: flex;
  align-items: center;
  gap: 16px;
}
.fp-progress-row__info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  white-space: nowrap;
  min-width: 240px;
}
.fp-progress-row__label { font-size: 12px; color: var(--text-soft); }
.fp-progress-row__info strong { font-size: 14px; color: var(--text); font-variant-numeric: tabular-nums; }
</style>
