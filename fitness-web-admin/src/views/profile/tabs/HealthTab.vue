<template>
  <div v-loading="loading" class="fp-tab">
    <!-- 主要指标卡（对齐后端 HealthOverviewVO 4 字段） -->
    <div class="fp-grid">
      <StatCard dim="A" label="最新体重" :value="fmtWeight(ov.latestWeight)" unit="kg" sub="来自身体数据模块" />
      <StatCard dim="A" label="最新体脂率" :value="fmtWeight(ov.latestBodyFat)" unit="%" sub="来自身体数据模块" />
      <StatCard dim="Bo" label="今日热量" :value="fmtNum(ov.todayCalories)" unit="kcal" sub="今日已摄入" />
      <StatCard dim="Bo" label="今日饮水" :value="fmtNum(ov.todayWaterMl)" unit="ml" sub="今日已饮用" />
    </div>

    <!-- 提示卡：数据来源说明 -->
    <div class="fp-card fp-hint-card">
      <div class="fp-hint-text">
        <el-icon><InfoFilled /></el-icon>
        <span>健康数据由身体数据、饮食记录、饮水记录模块聚合，需在对应模块录入后才会显示。</span>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 健康概览 Tab（对齐后端 HealthOverviewVO）
 * 后端返回 4 字段：latestWeight / latestBodyFat / todayCalories / todayWaterMl
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import StatCard from '@/components/StatCard.vue'
import { getMyHealthOverview } from '@/api/user'

const loading = ref(false)
const ov = ref({
  latestWeight: 0,
  latestBodyFat: 0,
  todayCalories: 0,
  todayWaterMl: 0
})

function fmtNum(v) { return (v ?? 0).toLocaleString('zh-CN') }
function fmtWeight(v) { return (v ?? 0).toFixed(1) }

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
.fp-hint-card {
  margin-top: 16px;
  padding: 14px 20px;
}
.fp-hint-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.6;
}
</style>
