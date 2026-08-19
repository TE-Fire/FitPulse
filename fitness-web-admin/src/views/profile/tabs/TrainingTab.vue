<template>
  <div v-loading="loading" class="fp-tab">
    <!-- 累计指标卡（对齐后端 TrainingStatsVO 4 字段） -->
    <div class="fp-grid">
      <StatCard dim="B" label="累计训练次数" :value="fmtNum(stats.totalWorkouts)" unit="次" sub="所有训练总和" />
      <StatCard dim="B" label="累计训练容量" :value="fmtNum(stats.totalVolume)" unit="kg" sub="历史总量" />
      <StatCard dim="C" label="当前连续打卡" :value="stats.currentStreak ?? 0" unit="天" sub="保持势头！" />
      <StatCard dim="C" label="平均单次容量" :value="avgVolumePerWorkout" unit="kg" sub="总容量 / 总次数" />
    </div>

    <!-- 上次训练信息 -->
    <div class="fp-card fp-last-card">
      <div class="fp-section-title">
        <el-icon><Clock /></el-icon>
        上次训练
      </div>
      <div class="fp-last-row">
        <strong>{{ stats.lastWorkoutDate || '暂无记录' }}</strong>
        <span v-if="stats.lastWorkoutDate" class="fp-hint">距今 {{ daysFromNow(stats.lastWorkoutDate) }} 天</span>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 训练统计 Tab（对齐后端 TrainingStatsVO）
 * 后端返回 4 字段：totalWorkouts / totalVolume / currentStreak / lastWorkoutDate
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import { getMyTrainingStats } from '@/api/user'

const loading = ref(false)
const stats = ref({
  totalWorkouts: 0,
  totalVolume: 0,
  currentStreak: 0,
  lastWorkoutDate: null
})

const avgVolumePerWorkout = computed(() =>
  stats.value.totalWorkouts ? (stats.value.totalVolume / stats.value.totalWorkouts).toFixed(1) : '0.0'
)

function fmtNum(v) { return (v ?? 0).toLocaleString('zh-CN') }

function daysFromNow(dateStr) {
  if (!dateStr) return 0
  const d = new Date(dateStr)
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
