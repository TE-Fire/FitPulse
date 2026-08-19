<template>
  <div class="fp-page">
    <!-- 顶部说明 -->
    <div class="fp-page__header">
      <div>
        <h1 class="fp-page__title">训练看板</h1>
        <p class="fp-page__desc">本周训练概览 · B 维度（容量/组数/次数）+ C 维度（趋势/完成率）</p>
      </div>
      <div class="fp-page__actions">
        <el-button plain @click="load" :loading="loading">
          <el-icon><Refresh /></el-icon><span>刷新</span>
        </el-button>
      </div>
    </div>

    <!-- 指标卡：B 重点三卡 + C 重点两卡 + 辅助两卡 -->
    <div class="fp-grid">
      <StatCard dim="B" label="本周训练容量 (B)" :value="fmtNum(overview.totalVolumeThisWeek)" unit="kg" :sub="`目标周累计 40000kg · 完成度 ${pct(overview.totalVolumeThisWeek, 40000)}`" />
      <StatCard dim="B" label="总组数 (B)" :value="overview.totalSetsThisWeek" unit="组" :sub="`周均 ${avgSets}/天`" />
      <StatCard dim="B" label="总次数 (B)" :value="overview.totalRepsThisWeek" unit="次" :sub="`周均 ${avgReps}/天`" />
      <StatCard dim="C" label="完成率 (C)" :value="fmtPct(overview.completionRate7d)" :sub="`本周 ${overview.totalWorkoutsThisWeek} 次训练`" />
      <StatCard dim="C" label="连续打卡 (C)" :value="overview.streakDays" unit="天" sub="保持势头！" />
      <StatCard dim="muted" label="本周训练次数" :value="overview.totalWorkoutsThisWeek" unit="次" :sub="`目标 4 次 · 完成度 ${pct(overview.totalWorkoutsThisWeek, 4)}`" />
      <StatCard dim="muted" label="训练计划总数" :value="overview.totalPlans" unit="个" sub="管理训练模板" />
    </div>

    <!-- 图表 + 明细表 -->
    <div class="fp-grid fp-grid--charts" style="margin-top: 16px;">
      <ChartLine
        title="7 天容量趋势 (B)"
        :dim="'B'"
        :series="[{ name: '训练容量', data: trendVolumes }]"
        :xData="trendDates"
        yUnit="kg"
        :loading="loading"
      />

      <div class="fp-card fp-table-wrap">
        <div class="fp-section-title">7 天明细</div>
        <el-table :data="overview.weeklyVolumeTrend" :loading="loading" stripe size="default" style="width:100%;">
          <el-table-column prop="date" label="日期" min-width="120" />
          <el-table-column label="容量 (kg)">
            <template #default="{ row }">
              <span class="fp-cell-b">{{ fmtNum(row.volume) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sets" label="组数" min-width="80" />
          <el-table-column label="平均单组容量">
            <template #default="{ row }">
              {{ row.sets ? (row.volume / row.sets).toFixed(1) : '0.0' }} kg
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 训练看板
 * - 数据来源：api/dashboard.js getTrainingOverview() → TrainingOverview
 * - 维度：B（容量/组数/次数，蓝）/ C（完成率/连续打卡，绿）/ 辅助 muted
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import ChartLine from '@/components/ChartLine.vue'
import { getTrainingOverview } from '@/api/dashboard'

const loading = ref(false)
const overview = ref({
  totalWorkoutsThisWeek: 0,
  totalVolumeThisWeek: 0,
  totalSetsThisWeek: 0,
  totalRepsThisWeek: 0,
  completionRate7d: 0,
  streakDays: 0,
  totalPlans: 0,
  weeklyVolumeTrend: []
})

const trendDates = computed(() => overview.value.weeklyVolumeTrend.map(t => t.date.slice(5)))
const trendVolumes = computed(() => overview.value.weeklyVolumeTrend.map(t => t.volume))
const avgSets = computed(() => (overview.value.totalSetsThisWeek / 7).toFixed(1))
const avgReps = computed(() => (overview.value.totalRepsThisWeek / 7).toFixed(0))

function fmtNum(v) { return (v ?? 0).toLocaleString('zh-CN') }
function fmtPct(v) { return ((v ?? 0) * 100).toFixed(0) + '%' }
function pct(cur, target) {
  if (!target) return '0%'
  return Math.min(100, Math.round((cur / target) * 100)) + '%'
}

async function load() {
  loading.value = true
  try {
    overview.value = await getTrainingOverview()
  } catch (e) {
    ElMessage.error('加载训练看板失败')
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
.fp-table-wrap { padding: 14px 16px; }
.fp-cell-b { color: var(--dim-B); font-weight: 600; font-variant-numeric: tabular-nums; }
</style>
