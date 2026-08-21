<template>
  <div class="rc-page">
    <!-- Hero 区 -->
    <div class="rc-hero">
      <div class="rc-hero__bg"></div>
      <div class="rc-hero__content">
        <div>
          <h2>训练记录</h2>
          <p>移动端同步的训练数据 · 辅助用户量化训练成果</p>
        </div>
        <div class="rc-hero__stats">
          <div class="rc-stat">
            <div class="rc-stat__num">{{ totalRecords }}</div>
            <div class="rc-stat__label">训练次数</div>
          </div>
          <div class="rc-stat">
            <div class="rc-stat__num rc-stat__num--orange">{{ totalVolume }}<small>kg</small></div>
            <div class="rc-stat__label">累计容量</div>
          </div>
          <div class="rc-stat">
            <div class="rc-stat__num rc-stat__num--cyan">{{ totalReps }}</div>
            <div class="rc-stat__label">完成次数</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="rc-toolbar">
      <div class="rc-toolbar__filters">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="default"
          style="width: 280px"
          @change="fetchList(1)"
        />
        <el-select
          v-model="query.userId"
          placeholder="全部用户"
          clearable
          size="default"
          style="width: 140px"
          @change="fetchList(1)"
        >
          <el-option
            v-for="u in userOptions"
            :key="u.value"
            :label="u.label"
            :value="u.value"
          />
        </el-select>
        <button class="rc-chip-btn" @click="resetFilter">
          <el-icon><Refresh /></el-icon>重置
        </button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="rc-card rc-table-wrap">
      <el-table
        :data="list"
        stripe
        style="width: 100%"
        empty-text="暂无训练记录"
        @row-click="openDetail"
        v-loading="loading"
      >
        <el-table-column label="日期" width="130" align="center">
          <template #default="{ row }">
            <div class="rc-date">
              <strong>{{ row.recordDate?.slice(5) }}</strong>
              <span>{{ row.recordDate?.slice(0, 4) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="planName" label="训练内容" min-width="180">
          <template #default="{ row }">
            <div class="rc-plan">
              <div class="rc-plan__name">
                <span class="rc-plan__dot"></span>
                {{ row.planName || '自由训练' }}
              </div>
              <div v-if="row.note" class="rc-plan__note">
                {{ row.note }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="110">
          <template #default="{ row }">
            <span class="rc-user">
              <el-icon><User /></el-icon>
              {{ row.userName || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="110" align="center">
          <template #default="{ row }">
            <span class="rc-chip rc-chip--blue">
              {{ formatDuration(row.durationSec) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="总容量" width="130" align="center">
          <template #default="{ row }">
            <div class="rc-volume">
              <strong>{{ formatNum(row.totalVolume) }}</strong>
              <span>kg</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="组数" width="80" align="center">
          <template #default="{ row }">
            <span class="rc-num">{{ row.totalSets }}</span>
          </template>
        </el-table-column>
        <el-table-column label="次数" width="80" align="center">
          <template #default="{ row }">
            <span class="rc-num rc-num--orange">{{ row.totalReps }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <button class="rc-link-btn" @click.stop="openDetail(row)">查看</button>
          </template>
        </el-table-column>
      </el-table>

      <div class="rc-pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="fetchList"
          @size-change="fetchList(1)"
        />
      </div>
    </div>

    <!-- 详情 Drawer -->
    <el-drawer
      v-model="detailVisible"
      size="560px"
      direction="rtl"
      destroy-on-close
      class="rc-drawer"
    >
      <template #header>
        <div v-if="detail" class="rc-detail-header">
          <div>
            <h3>{{ detail.planName || '自由训练' }}</h3>
            <p>
              <el-icon><Calendar /></el-icon>
              {{ detail.recordDate }}
              <span class="rc-dot">·</span>
              <el-icon><Timer /></el-icon>
              {{ formatDuration(detail.durationSec) }}
            </p>
          </div>
        </div>
      </template>

      <div v-if="detail">
        <!-- 顶部指标 -->
        <div class="rc-summary">
          <div class="rc-summary__item rc-summary__item--1">
            <label>总容量</label>
            <strong>{{ formatNum(detail.totalVolume) }} <small>kg</small></strong>
          </div>
          <div class="rc-summary__item rc-summary__item--2">
            <label>总组数</label>
            <strong>{{ detail.totalSets }}</strong>
          </div>
          <div class="rc-summary__item rc-summary__item--3">
            <label>总次数</label>
            <strong>{{ detail.totalReps }}</strong>
          </div>
        </div>

        <!-- 备注 -->
        <div v-if="detail.note" class="rc-note">
          <h4><el-icon><ChatDotRound /></el-icon>训练备注</h4>
          <p>{{ detail.note }}</p>
        </div>

        <!-- 按动作分组展示 -->
        <div class="rc-groups">
          <h4>
            <el-icon><Collection /></el-icon>
            详细记录
            <small>{{ exerciseGroups.length }} 个动作 · {{ detail.totalSets }} 组</small>
          </h4>
          <div
            v-for="(grp, idx) in exerciseGroups"
            :key="grp.exerciseId"
            class="rc-group"
          >
            <div class="rc-group__head">
              <span class="rc-group__no">{{ idx + 1 }}</span>
              <span class="rc-group__name">{{ grp.exerciseName }}</span>
              <span class="rc-group__sub">
                {{ grp.sets.length }} 组 ·
                总容量 {{ formatNum(grp.volume) }}kg
              </span>
            </div>
            <table class="rc-set-table">
              <thead>
                <tr>
                  <th style="width:56px">组次</th>
                  <th>重量 (kg)</th>
                  <th>次数</th>
                  <th>容量 (kg)</th>
                  <th>RPE</th>
                  <th style="width:64px">标记</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in grp.sets" :key="s.id" :class="{ 'is-warmup': s.isWarmup }">
                  <td class="is-setno">
                    <span class="rc-setno">{{ s.setNo }}</span>
                  </td>
                  <td class="is-strong">{{ s.weightKg }}</td>
                  <td class="is-strong">{{ s.reps }}</td>
                  <td>{{ formatNum((Number(s.weightKg) || 0) * (Number(s.reps) || 0)) }}</td>
                  <td>
                    <el-tag v-if="s.rpe" size="small" :type="rpeTagType(s.rpe)">
                      RPE {{ s.rpe }}
                    </el-tag>
                    <span v-else class="rc-muted">—</span>
                  </td>
                  <td>
                    <el-tag v-if="s.isWarmup" size="small" type="warning" effect="plain">热身</el-tag>
                    <span v-else class="rc-muted">—</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh, Calendar, Timer, ChatDotRound, Collection, User
} from '@element-plus/icons-vue'
import {
  getRecordList,
  getRecordDetail,
  MOCK_USER_OPTIONS
} from '@/api/training'

// ===== 列表查询 =====
const query = reactive({ page: 1, size: 10, userId: '' })
const dateRange = ref([])

const list    = ref([])
const total   = ref(0)
const loading = ref(false)

// 用户筛选选项（来自 mock，真实场景应调用后端）
const userOptions = MOCK_USER_OPTIONS

async function fetchList(resetPage) {
  if (resetPage) query.page = 1
  loading.value = true
  const params = { ...query }
  if (!query.userId) delete params.userId
  if (dateRange.value?.length === 2) {
    params.startDate = dateRange.value[0]
    params.endDate   = dateRange.value[1]
  }
  try {
    const res = await getRecordList(params)
    list.value  = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  dateRange.value = []
  query.userId = ''
  fetchList(1)
}

// ===== 统计（Hero 区显示当前筛选下的总量近似）=====
const totalRecords = computed(() => total.value)
const totalVolume  = computed(() =>
  formatNum(list.value.reduce((s, x) => s + (Number(x.totalVolume) || 0), 0))
)
const totalReps    = computed(() =>
  list.value.reduce((s, x) => s + (Number(x.totalReps) || 0), 0)
)

// ===== 工具 =====
function formatDuration(sec) {
  const s = Number(sec) || 0
  if (s < 60) return s + 's'
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m} 分钟`
}
function formatNum(n) {
  const x = Number(n) || 0
  return x.toLocaleString('en-US', { maximumFractionDigits: 1 })
}
function rpeTagType(r) {
  const n = Number(r)
  if (n >= 9) return 'danger'
  if (n >= 7) return 'warning'
  if (n >= 5) return 'success'
  return 'info'
}

// ===== 详情 Drawer =====
const detailVisible = ref(false)
const detail = ref(null)

const exerciseGroups = computed(() => {
  if (!detail.value?.sets) return []
  const map = new Map()
  for (const s of detail.value.sets) {
    if (!map.has(s.exerciseId)) {
      map.set(s.exerciseId, {
        exerciseId: s.exerciseId,
        exerciseName: s.exerciseName,
        sets: [],
        volume: 0
      })
    }
    const g = map.get(s.exerciseId)
    g.sets.push(s)
    g.volume += (Number(s.weightKg) || 0) * (Number(s.reps) || 0)
  }
  return Array.from(map.values())
})

async function openDetail(row) {
  try {
    detail.value = await getRecordDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    const msgMap = { RECORD_NOT_FOUND: '记录不存在' }
    ElMessage.error(msgMap[e?.message] || e?.message || '获取详情失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.rc-page { display: flex; flex-direction: column; gap: 16px; }

/* ===== Hero ===== */
.rc-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  padding: 26px 32px;
  color: #fff;
  isolation: isolate;
  box-shadow: 0 10px 30px -10px rgba(10, 37, 64, 0.3);
}
.rc-hero__bg {
  position: absolute; inset: 0; z-index: -1;
  background:
    radial-gradient(ellipse at 15% 20%, rgba(34, 211, 238, 0.4) 0%, transparent 50%),
    radial-gradient(ellipse at 90% 90%, rgba(255, 140, 105, 0.35) 0%, transparent 55%),
    linear-gradient(135deg, #2d1b69 0%, #3d2b8f 45%, #5b3fc1 100%);
}
.rc-hero__bg::after {
  content: '';
  position: absolute; inset: 0;
  background:
    repeating-linear-gradient(45deg, rgba(255,255,255,0.035) 0 2px, transparent 2px 18px);
}
.rc-hero__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
  flex-wrap: wrap;
}
.rc-hero__content h2 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.01em;
}
.rc-hero__content > div > p {
  margin: 0;
  font-size: 13px;
  opacity: 0.8;
}
.rc-hero__stats { display: flex; gap: 14px; }
.rc-stat {
  padding: 12px 20px;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.14);
  border-radius: 12px;
  text-align: center;
  backdrop-filter: blur(8px);
  min-width: 96px;
}
.rc-stat__num {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 4px;
  letter-spacing: -0.02em;
}
.rc-stat__num small { font-size: 12px; font-weight: 500; margin-left: 2px; opacity: 0.8; }
.rc-stat__num--orange { color: #ffb26b; text-shadow: 0 0 18px rgba(255,178,107,0.5); }
.rc-stat__num--cyan   { color: #6ef3ff; text-shadow: 0 0 18px rgba(110,243,255,0.5); }
.rc-stat__label {
  font-size: 11.5px;
  opacity: 0.8;
  letter-spacing: 0.08em;
}

/* ===== 按钮 ===== */
.rc-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.16s cubic-bezier(.2,.8,.2,1);
}
.rc-btn:hover { border-color: #22d3ee; color: #22d3ee; }
.rc-btn--primary {
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b6b 100%);
  border: none;
  color: #fff;
  box-shadow: 0 8px 20px -6px rgba(255, 107, 107, 0.55);
}
.rc-btn--primary:hover {
  transform: translateY(-1px);
  color: #fff;
  box-shadow: 0 12px 26px -6px rgba(255, 107, 107, 0.7);
}

/* ===== 筛选栏 ===== */
.rc-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 16px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
}
.rc-toolbar__filters { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.rc-chip-btn {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-soft);
  font-size: 12.5px;
  cursor: pointer;
  transition: all 0.15s;
}
.rc-chip-btn:hover { border-color: #22d3ee; color: #22d3ee; }

/* ===== 列表 ===== */
.rc-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 18px 10px;
}
.rc-table-wrap :deep(.el-table) {
  --el-table-border-color: transparent;
}
.rc-table-wrap :deep(.el-table th.el-table__cell) {
  background: transparent;
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12.5px;
  border-bottom: 1px solid var(--border);
}
.rc-table-wrap :deep(.el-table td.el-table__cell) {
  border-bottom: 1px dashed var(--border);
  font-size: 13.5px;
  cursor: pointer;
}

.rc-date {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  padding: 4px 0;
  line-height: 1.1;
}
.rc-date strong {
  font-size: 17px;
  font-weight: 700;
  color: #22d3ee;
  letter-spacing: 0.02em;
}
.rc-date span {
  font-size: 10.5px;
  color: var(--text-muted);
}

.rc-plan__name {
  display: inline-flex; align-items: center; gap: 8px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}
.rc-plan__dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff8c69, #ff6b6b);
  box-shadow: 0 0 0 3px rgba(255, 140, 105, 0.14);
}
.rc-plan__note {
  font-size: 12px;
  color: var(--text-muted);
  padding-left: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rc-chip {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.rc-chip--blue {
  background: rgba(34, 211, 238, 0.14);
  color: #0ea5b7;
}

.rc-volume {
  display: inline-flex; align-items: baseline; gap: 3px;
}
.rc-volume strong {
  font-size: 16px;
  font-weight: 700;
  color: #ff8c69;
  letter-spacing: -0.01em;
}
.rc-volume span {
  font-size: 11px;
  color: var(--text-muted);
}

.rc-num {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
}
.rc-num--orange { color: #ff8c69; }

.rc-user {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-soft);
}
.rc-user .el-icon { color: #22d3ee; }

/* 热身组标记 */
.rc-set-table tr.is-warmup td {
  background: rgba(245, 158, 11, 0.06);
  color: var(--text-soft);
}

.rc-link-btn {
  background: none; border: none;
  padding: 2px 6px;
  font-size: 13px;
  color: #22d3ee;
  cursor: pointer;
}
.rc-link-btn:hover { text-decoration: underline; color: #0ea5b7; }

.rc-pagination {
  display: flex; justify-content: flex-end;
  padding: 14px 2px 6px;
}

/* ===== 详情 Drawer ===== */
.rc-detail-header { width: 100%; padding-right: 16px; }
.rc-detail-header h3 {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
}
.rc-detail-header p {
  margin: 0;
  font-size: 12.5px;
  color: var(--text-muted);
  display: inline-flex; align-items: center; gap: 4px;
}
.rc-dot { margin: 0 4px; }

.rc-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 16px 0 20px;
}
.rc-summary__item {
  padding: 16px 14px;
  border-radius: 12px;
  display: flex; flex-direction: column; gap: 6px;
  position: relative;
  overflow: hidden;
  isolation: isolate;
}
.rc-summary__item::before {
  content: '';
  position: absolute; inset: 0; z-index: -1;
  border-radius: 12px;
}
.rc-summary__item--1 {
  color: #fff;
  background: linear-gradient(135deg, #0d4e6e, #22d3ee);
}
.rc-summary__item--2 {
  background: var(--bg-soft);
  color: var(--text);
}
.rc-summary__item--3 {
  color: #fff;
  background: linear-gradient(135deg, #c2410c, #ff8c69);
}
.rc-summary__item label {
  font-size: 11.5px;
  opacity: 0.85;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.rc-summary__item strong {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.1;
}
.rc-summary__item small { font-size: 12px; font-weight: 500; margin-left: 2px; }

.rc-note {
  padding: 14px 16px;
  background: var(--bg-soft);
  border-radius: 12px;
  margin-bottom: 20px;
}
.rc-note h4 {
  margin: 0 0 8px;
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}
.rc-note p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--text-soft);
  white-space: pre-wrap;
}

.rc-groups h4 {
  margin: 0 0 12px;
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  padding-left: 10px;
  border-left: 3px solid #5b3fc1;
  width: 100%;
}
.rc-groups h4 small {
  margin-left: auto;
  font-size: 11.5px;
  color: var(--text-muted);
  font-weight: 400;
  border: none;
  padding: 0;
}

.rc-group {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  margin-bottom: 14px;
  overflow: hidden;
}
.rc-group__head {
  padding: 12px 16px;
  background: var(--bg-soft);
  display: flex; align-items: center; gap: 10px;
  border-bottom: 1px solid var(--border);
}
.rc-group__no {
  width: 26px; height: 26px;
  border-radius: 8px;
  background: linear-gradient(135deg, #5b3fc1, #22d3ee);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  display: inline-flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.rc-group__name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  flex: 1;
}
.rc-group__sub {
  font-size: 12px;
  color: var(--text-muted);
}

.rc-set-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.rc-set-table th, .rc-set-table td {
  padding: 10px 14px;
  text-align: center;
  border-bottom: 1px dashed var(--border);
}
.rc-set-table th {
  font-size: 11.5px;
  color: var(--text-muted);
  font-weight: 600;
  background: transparent;
  letter-spacing: 0.05em;
}
.rc-set-table tr:last-child td { border-bottom: none; }
.rc-set-table td.is-setno { width: 56px; }
.rc-set-table td.is-strong {
  font-weight: 700;
  color: var(--text);
  font-size: 14px;
}
.rc-setno {
  display: inline-block;
  width: 22px; height: 22px;
  border-radius: 6px;
  background: rgba(34, 211, 238, 0.12);
  color: #0ea5b7;
  font-weight: 700;
  font-size: 12px;
  line-height: 22px;
}
.rc-muted { color: var(--text-muted); opacity: 0.5; }

@media (max-width: 720px) {
  .rc-hero__stats { width: 100%; }
  .rc-hero__stats .rc-stat { flex: 1; min-width: 0; }
  .rc-summary { grid-template-columns: repeat(3, 1fr); gap: 6px; }
  .rc-summary__item { padding: 12px 8px; }
  .rc-summary__item strong { font-size: 18px; }
}
</style>
