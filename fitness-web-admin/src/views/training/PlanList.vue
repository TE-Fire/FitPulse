<template>
  <div class="pl-page">
    <!-- 统计卡片 -->
    <div class="pl-stats">
      <div class="pl-stat-card">
        <div class="pl-stat-card__value">{{ stats.total }}</div>
        <div class="pl-stat-card__label">总计划数</div>
      </div>
      <div class="pl-stat-card pl-stat-card--draft">
        <div class="pl-stat-card__value">{{ stats.draft }}</div>
        <div class="pl-stat-card__label">草稿</div>
      </div>
      <div class="pl-stat-card pl-stat-card--progress">
        <div class="pl-stat-card__value">{{ stats.progress }}</div>
        <div class="pl-stat-card__label">进行中</div>
      </div>
      <div class="pl-stat-card pl-stat-card--done">
        <div class="pl-stat-card__value">{{ stats.completed }}</div>
        <div class="pl-stat-card__label">已完成</div>
      </div>
      <div class="pl-stat-card pl-stat-card--cancel">
        <div class="pl-stat-card__value">{{ stats.cancelled }}</div>
        <div class="pl-stat-card__label">已取消</div>
      </div>
    </div>

    <!-- 筛选区 -->
    <div class="pl-filter">
      <el-input
        v-model="query.keyword"
        placeholder="搜索计划名称…"
        clearable
        class="pl-filter__search"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>

      <el-select
        v-model="query.status"
        placeholder="状态"
        clearable
        class="pl-filter__select"
      >
        <el-option label="草稿" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>

      <el-select
        v-model="query.planType"
        placeholder="类型"
        clearable
        class="pl-filter__select"
      >
        <el-option label="力量" :value="1" />
        <el-option label="有氧" :value="2" />
        <el-option label="混合" :value="3" />
      </el-select>

      <button class="pl-btn pl-btn--ghost" @click="resetFilter">
        <el-icon><RefreshLeft /></el-icon>
        <span>重置</span>
      </button>
    </div>

    <!-- 列表 -->
    <div v-if="loading" class="pl-cards">
      <el-skeleton :rows="4" animated />
    </div>

    <div v-else class="pl-cards">
      <div
        v-for="plan in filteredList"
        :key="plan.id"
        class="pl-card"
        @click="openDetail(plan)"
      >
        <div class="pl-card__glow"></div>
        <div class="pl-card__header">
          <div class="pl-card__index">
            {{ String(plan.exerciseCount || 0).padStart(2, '0') }}
            <small>动作</small>
          </div>
          <div class="pl-card__tags">
            <span
              class="pl-status-tag"
              :class="statusClass(plan.status)"
            >{{ plan.statusText || statusText(plan.status) }}</span>
            <span
              v-if="plan.planType"
              class="pl-type-tag"
            >{{ plan.planTypeLabel || planTypeText(plan.planType) }}</span>
          </div>
        </div>
        <div class="pl-card__body">
          <h3 class="pl-card__name">{{ plan.name }}</h3>
          <p class="pl-card__desc">
            {{ plan.description || '暂无描述' }}
          </p>
          <div v-if="plan.userName" class="pl-card__user">
            <el-icon><User /></el-icon>
            <span>{{ plan.userName }}</span>
          </div>
        </div>
        <div class="pl-card__footer">
          <span class="pl-card__date">
            <el-icon><Clock /></el-icon>
            {{ formatDate(plan.createdAt) }}
          </span>
          <div class="pl-card__btns" @click.stop>
            <button class="pl-chip-btn" @click="openDetail(plan)">查看详情</button>
            <el-dropdown trigger="click" @command="(c) => onCardCmd(c, plan)">
              <button class="pl-chip-btn pl-chip-btn--more">
                <el-icon><MoreFilled /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item divided command="delete" class="danger">
                    <el-icon><Delete /></el-icon>删除计划
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <div v-if="filteredList.length === 0" class="pl-empty">
        <div class="pl-empty__icon">
          <svg viewBox="0 0 64 64" width="56" height="56" fill="currentColor"><path d="M18 14h28a4 4 0 0 1 4 4v32a4 4 0 0 1-4 4H18a4 4 0 0 1-4-4V18a4 4 0 0 1 4-4zm0 4v8h28v-8H18zm0 12v6h28v-6H18zm0 10v6h18v-6H18z"/></svg>
        </div>
        <h4>暂无训练计划</h4>
        <p>移动端用户创建的计划将在此展示</p>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pl-pagination">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[8, 12, 24]"
        :total="total"
        layout="total, prev, pager, next"
        background
        @current-change="fetchList"
        @size-change="fetchList(1)"
      />
    </div>

    <!-- 详情 Drawer -->
    <el-drawer
      v-model="detailVisible"
      size="480px"
      direction="rtl"
      destroy-on-close
    >
      <template #header>
        <div class="pl-drawer-title">
          <h3>{{ detail?.name }}</h3>
          <span
            v-if="detail"
            class="pl-status-tag"
            :class="statusClass(detail.status)"
          >{{ statusText(detail.status) }}</span>
        </div>
      </template>

      <template v-if="detail">
        <div class="pl-detail">
          <div class="pl-detail__meta">
            <div class="pl-detail__meta-row">
              <span>所属用户</span>
              <strong>{{ detail.userName || '—' }}</strong>
            </div>
            <div class="pl-detail__meta-row">
              <span>动作数量</span>
              <strong>{{ detail.exercises?.length || 0 }} 个</strong>
            </div>
            <div class="pl-detail__meta-row">
              <span>计划类型</span>
              <strong>{{ detail.planTypeLabel || planTypeText(detail.planType) }}</strong>
            </div>
            <div class="pl-detail__meta-row">
              <span>预计时长</span>
              <strong>{{ detail.estimatedMin ? detail.estimatedMin + ' 分钟' : '—' }}</strong>
            </div>
          </div>

          <div class="pl-detail__desc">
            <h4>计划说明</h4>
            <p>{{ detail.description || '暂无描述' }}</p>
          </div>

          <div v-if="detail.actualDurationSec" class="pl-detail__info">
            <h4>训练信息</h4>
            <div class="pl-detail__info-row">
              <span>实际时长</span>
              <strong>{{ Math.round(detail.actualDurationSec / 60) }} 分钟</strong>
            </div>
            <div v-if="detail.startedAt" class="pl-detail__info-row">
              <span>开始时间</span>
              <strong>{{ formatDateTime(detail.startedAt) }}</strong>
            </div>
            <div v-if="detail.completedAt" class="pl-detail__info-row">
              <span>完成时间</span>
              <strong>{{ formatDateTime(detail.completedAt) }}</strong>
            </div>
          </div>

          <div class="pl-detail__items">
            <h4>
              动作编排
              <small>{{ detail.exercises?.length || 0 }} 个动作</small>
            </h4>
            <div v-if="!detail.exercises?.length" class="pl-empty-sm">暂无动作</div>
            <div
              v-for="(item, idx) in detail.exercises"
              :key="item.id"
              class="pl-ex-row"
            >
              <div class="pl-ex-row__no">{{ idx + 1 }}</div>
              <div class="pl-ex-row__info">
                <div class="pl-ex-row__name">{{ item.exerciseName }}</div>
                <div class="pl-ex-row__params">
                  <span>{{ item.targetSets }} 组</span>
                  <span>×</span>
                  <span>{{ item.targetReps }} 次</span>
                  <span v-if="item.targetWeightKg" class="pl-weight">
                    建议 {{ item.targetWeightKg }}kg
                  </span>
                  <span v-if="item.restSec" class="pl-rest">
                    间歇 {{ item.restSec }}s
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, MoreFilled, Delete, Clock, User, RefreshLeft } from '@element-plus/icons-vue'
import {
  getPlanList,
  getPlanDetail,
  deletePlan,
  PLAN_STATUS,
  PLAN_STATUS_TEXT,
  PLAN_TYPE_OPTIONS
} from '@/api/training'

// ====== 列表 ======
const query = reactive({
  page: 1,
  size: 8,
  keyword: '',
  status: null,
  planType: null
})
const list = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchList(resetPage) {
  if (resetPage) query.page = 1
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size
    }
    if (query.status !== null) params.status = query.status
    if (query.planType !== null) params.planType = query.planType
    if (query.keyword) params.keyword = query.keyword

    const res = await getPlanList(params)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  query.keyword = ''
  query.status = null
  query.planType = null
  fetchList(true)
}

// 前端搜索过滤
const filteredList = computed(() => {
  if (!query.keyword) return list.value
  const kw = query.keyword.toLowerCase()
  return list.value.filter(p =>
    p.name?.toLowerCase().includes(kw) ||
    p.description?.toLowerCase().includes(kw)
  )
})

// 统计
const stats = computed(() => {
  const s = { total: total.value, draft: 0, progress: 0, completed: 0, cancelled: 0 }
  list.value.forEach(p => {
    if (p.status === 0) s.draft++
    else if (p.status === 1) s.progress++
    else if (p.status === 2) s.completed++
    else if (p.status === 3) s.cancelled++
  })
  return s
})

// 计划类型选项
const planTypeOptions = PLAN_TYPE_OPTIONS

// ====== 格式化 ======
function formatDate(s) {
  if (!s) return '—'
  return String(s).slice(0, 10).replace('T', ' ')
}

function formatDateTime(s) {
  if (!s) return '—'
  return String(s).slice(0, 16).replace('T', ' ')
}

// ====== 状态 ======
function statusText(status) {
  return PLAN_STATUS_TEXT[status] || '未知'
}

function statusClass(status) {
  const map = {
    0: 'is-draft',
    1: 'is-progress',
    2: 'is-completed',
    3: 'is-cancelled'
  }
  return map[status] || ''
}

function planTypeText(type) {
  const map = { 1: '力量', 2: '有氧', 3: '混合' }
  return map[type] || '—'
}

// ====== 详情 Drawer ======
const detailVisible = ref(false)
const detail = ref(null)

async function openDetail(row) {
  try {
    detail.value = await getPlanDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

// ====== 卡片操作 ======
function onCardCmd(cmd, plan) {
  if (cmd === 'delete') handleDelete(plan)
}

async function handleDelete(plan) {
  try {
    await ElMessageBox.confirm(
      `确认删除计划「${plan.name}」？此操作不可撤销。`,
      '删除计划',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await deletePlan(plan.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    if (e === 'cancel') return
    const msgMap = {
      PLAN_NOT_FOUND: '计划不存在',
      PLAN_IN_PROGRESS: '计划正在进行中，无法删除',
      PLAN_COMPLETED_CANNOT_DELETE: '已完成的计划无法删除'
    }
    ElMessage.error(msgMap[e?.message] || e?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.pl-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ===== 统计卡片 ===== */
.pl-stats {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.pl-stat-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 18px 16px;
  text-align: center;
  transition: all 0.2s;
}
.pl-stat-card:hover {
  border-color: rgba(102, 126, 234, 0.3);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.pl-stat-card__value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 6px;
}

.pl-stat-card__label {
  font-size: 12px;
  color: #999;
}

.pl-stat-card--draft .pl-stat-card__value { color: #faad14; }
.pl-stat-card--progress .pl-stat-card__value { color: #1890ff; }
.pl-stat-card--done .pl-stat-card__value { color: #52c41a; }
.pl-stat-card--cancel .pl-stat-card__value { color: #999; }

/* ===== 筛选区 ===== */
.pl-filter {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 14px 16px;
  align-items: center;
}

.pl-filter__search {
  width: 240px;
}

.pl-filter__select {
  width: 120px;
}

.pl-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid #ddd;
  background: #fff;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.pl-btn:hover {
  border-color: #667eea;
  color: #667eea;
}
.pl-btn--ghost {
  background: #f5f5f5;
}

/* ===== 卡片列表 ===== */
.pl-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.pl-card {
  position: relative;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 14px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
}
.pl-card:hover {
  transform: translateY(-2px);
  border-color: rgba(102, 126, 234, 0.4);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.pl-card__glow {
  position: absolute;
  inset: -2px;
  border-radius: 16px;
  background: linear-gradient(135deg, transparent 40%, rgba(102, 126, 234, 0.08) 60%, rgba(118, 75, 162, 0.06) 100%);
  opacity: 0;
  transition: opacity 0.25s;
  z-index: -1;
}
.pl-card:hover .pl-card__glow { opacity: 1; }

.pl-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.pl-card__index {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  line-height: 1;
}
.pl-card__index small {
  font-size: 9px;
  opacity: 0.85;
  font-weight: 500;
  letter-spacing: 0.1em;
  margin-top: 3px;
}

.pl-card__tags {
  display: flex;
  gap: 6px;
  align-items: center;
}

.pl-status-tag {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 999px;
  font-weight: 500;
}
.pl-status-tag.is-draft {
  background: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}
.pl-status-tag.is-progress {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}
.pl-status-tag.is-completed {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}
.pl-status-tag.is-cancelled {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #d9d9d9;
}

.pl-type-tag {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 999px;
  background: #f0f5ff;
  color: #2f54eb;
  border: 1px solid #adc6ff;
  font-weight: 500;
}

.pl-card__body {
  margin-bottom: 12px;
}

.pl-card__name {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.3;
}

.pl-card__desc {
  margin: 0 0 8px;
  font-size: 12px;
  color: #999;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pl-card__user {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #667eea;
  background: #f0f5ff;
  padding: 3px 8px;
  border-radius: 6px;
}
.pl-card__user .el-icon {
  font-size: 12px;
}

.pl-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px dashed #eee;
}

.pl-card__date {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #999;
}

.pl-card__btns {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pl-chip-btn {
  padding: 5px 12px;
  border-radius: 8px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}
.pl-chip-btn:hover {
  border-color: #667eea;
  color: #667eea;
}
.pl-chip-btn--more {
  padding: 5px 8px;
}

/* ===== 空态 ===== */
.pl-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px 50px;
  background: #fff;
  border: 1px dashed #eee;
  border-radius: 14px;
  color: #999;
}
.pl-empty h4 {
  margin: 0 0 6px;
  font-size: 16px;
  color: #333;
}
.pl-empty p {
  margin: 0;
  font-size: 13px;
}

.pl-empty-sm {
  text-align: center;
  padding: 24px 0;
  color: #999;
  font-size: 13px;
}

/* ===== 分页 ===== */
.pl-pagination {
  display: flex;
  justify-content: center;
  padding: 12px 0 4px;
}

/* ===== 详情 Drawer ===== */
.pl-drawer-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 12px;
}
.pl-drawer-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.pl-detail {
  padding: 4px 4px 20px;
}

.pl-detail__meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.pl-detail__meta-row {
  padding: 12px;
  background: #f8f9fb;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.pl-detail__meta-row span {
  font-size: 11px;
  color: #999;
}
.pl-detail__meta-row strong {
  font-size: 15px;
  font-weight: 600;
  color: #667eea;
}

.pl-detail__desc {
  margin-bottom: 16px;
}
.pl-detail__desc h4,
.pl-detail__items h4,
.pl-detail__info h4 {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding-left: 10px;
  border-left: 3px solid #667eea;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pl-detail__items h4 small {
  font-size: 12px;
  font-weight: 400;
  color: #999;
  border: none;
  padding: 0;
}

.pl-detail__desc p {
  margin: 0;
  padding: 10px 12px;
  background: #f8f9fb;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.pl-detail__info {
  margin-bottom: 16px;
}

.pl-detail__info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fb;
  border-radius: 8px;
  margin-bottom: 6px;
}
.pl-detail__info-row span {
  font-size: 13px;
  color: #999;
}
.pl-detail__info-row strong {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.pl-ex-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 10px;
  margin-bottom: 8px;
}
.pl-ex-row:hover {
  border-color: rgba(102, 126, 234, 0.3);
}

.pl-ex-row__no {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border-radius: 7px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pl-ex-row__info { flex: 1; min-width: 0; }

.pl-ex-row__name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 3px;
}

.pl-ex-row__params {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #999;
}
.pl-ex-row__params span:not(.pl-rest) {
  font-weight: 600;
  color: #666;
}

.pl-rest {
  margin-left: 4px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #f0f5ff;
  color: #2f54eb;
  font-weight: 500;
  font-size: 11px;
}

:deep(.el-dropdown-menu__item.danger) { color: #ff4d4f; }
</style>
