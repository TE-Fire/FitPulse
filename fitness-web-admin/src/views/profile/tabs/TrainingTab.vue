<template>
  <div v-loading="loading" class="fp-tab">
    <!-- 累计指标卡 -->
    <div class="fp-grid">
      <StatCard dim="B" label="累计训练次数" :value="fmtNum(stats.totalWorkouts)" unit="次" :sub="`日均 ${(stats.totalWorkouts / 30).toFixed(1)} 次`" />
      <StatCard dim="B" label="累计训练容量" :value="fmtNum(stats.totalVolume)" unit="kg" sub="所有训练总和" />
      <StatCard dim="C" label="当前连续打卡" :value="stats.streakDays" unit="天" sub="保持势头！" />
      <StatCard dim="C" label="历史最长连续" :value="stats.longestStreak" unit="天" :sub="`截至 ${stats.lastWorkoutAt?.slice(0,10) || '--'}`" />
    </div>

    <!-- 次级指标：总组数 / 总次数 -->
    <div class="fp-grid" style="margin-top: 12px;">
      <StatCard dim="muted" label="累计组数" :value="fmtNum(stats.totalSets)" unit="组" />
      <StatCard dim="muted" label="累计次数" :value="fmtNum(stats.totalReps)" unit="次" />
      <StatCard dim="muted" label="平均单组容量" :value="avgVolumePerSet" unit="kg" sub="总容量 / 总组数" />
      <StatCard dim="muted" label="平均单次容量" :value="avgVolumePerWorkout" unit="kg" sub="总容量 / 总次数" />
    </div>

    <!-- 6 月柱图 -->
    <div style="margin-top: 16px;">
      <ChartBar
        title="近 6 月训练容量趋势"
        :dim="'B'"
        :series="[{ name: '月度容量', data: monthlyVolumes }]"
        :xData="monthlyLabels"
        yUnit="kg"
        :loading="loading"
      />
    </div>

    <!-- 上次训练信息 -->
    <div class="fp-card fp-last-card">
      <div class="fp-section-title">
        <el-icon><Clock /></el-icon>
        上次训练
      </div>
      <div class="fp-last-row">
        <strong>{{ stats.lastWorkoutAt || '暂无记录' }}</strong>
        <span v-if="stats.lastWorkoutAt" class="fp-hint">距今 {{ daysFromNow(stats.lastWorkoutAt) }} 天</span>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 训练统计 Tab
 * - 4 个累计指标卡（B 训练次数/容量 + C 当前连续/最长连续）
 * - 4 个辅助卡（总组数/总次数/平均单组/平均单次）
 * - 6 月容量趋势柱图（ChartBar B 维蓝）
 * - 上次训练时间
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import ChartBar from '@/components/ChartBar.vue'
import { getMyTrainingStats } from '@/api/user'

const loading = ref(false)
const stats = ref({
  totalWorkouts: 0, totalVolume: 0, totalSets: 0, totalReps: 0,
  streakDays: 0, longestStreak: 0, lastWorkoutAt: '',
  monthlySummary: []
})

const monthlyLabels = computed(() => stats.value.monthlySummary.map(m => m.month))
const monthlyVolumes = computed(() => stats.value.monthlySummary.map(m => m.volume))
const avgVolumePerSet = computed(() => stats.value.totalSets ? (stats.value.totalVolume / stats.value.totalSets).toFixed(1) : '0.0')
const avgVolumePerWorkout = computed(() => stats.value.totalWorkouts ? (stats.value.totalVolume / stats.value.totalWorkouts).toFixed(1) : '0.0')

function fmtNum(v) { return (v ?? 0).toLocaleString('zh-CN') }

function daysFromNow(dateStr) {
  const d = new Date(dateStr.replace(' ', 'T'))
  const diff = (Date.now() - d.getTime()) / 86400000
  return Math.max(0, Math.floor(diff))
}

async function load() {
  loading.value = true
  try {
    stats.value = await getMyTrainingStats()
  } catch (e) {
    ElMessage.error('加载训练统计失败')
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.fp-tab { padding: 4px 0; }
.fp-last-card { margin-top: 16px; padding: 16px 20px; }
.fp-last-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.fp-last-row strong { font-size: 18px; color: var(--text); font-variant-numeric: tabular-nums; }
.fp-hint { color: var(--text-muted); font-size: 12px; }
</style>
