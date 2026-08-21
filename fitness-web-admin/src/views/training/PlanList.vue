<template>
  <div class="pl-page">
    <!-- Hero 区 -->
    <div class="pl-hero">
      <div class="pl-hero__bg"></div>
      <div class="pl-hero__content">
        <div>
          <h2>训练计划管理</h2>
          <p>自由组合动作 · 科学编排组数次数 · 打造专属训练方案</p>
        </div>
        <div class="pl-hero__actions">
          <button class="pl-btn pl-btn--primary" @click="goCreate">
            <el-icon><Plus /></el-icon>
            <span>新建训练计划</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 列表卡片 -->
    <div v-if="loading" class="pl-cards">
      <el-skeleton :rows="4" animated />
    </div>

    <div v-else class="pl-cards">
      <div
        v-for="plan in list"
        :key="plan.id"
        class="pl-card"
        @click="openDetail(plan)"
      >
        <div class="pl-card__glow"></div>
        <div class="pl-card__header">
          <div class="pl-card__index">
            {{ String(plan.itemCount).padStart(2, '0') }}
            <small>动作</small>
          </div>
          <div class="pl-card__menu" @click.stop>
            <el-dropdown trigger="click" @command="(c) => onCardCmd(c, plan)">
              <button class="pl-icon-btn">
                <el-icon><MoreFilled /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>编辑计划
                  </el-dropdown-item>
                  <el-dropdown-item divided command="delete" class="danger">
                    <el-icon><Delete /></el-icon>删除计划
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div class="pl-card__body">
          <h3 class="pl-card__name">{{ plan.name }}</h3>
          <p class="pl-card__desc">
            {{ plan.description || '暂未添加描述' }}
          </p>
        </div>
        <div class="pl-card__footer">
          <span class="pl-card__date">
            <el-icon><Clock /></el-icon>
            {{ formatDate(plan.createdAt) }}
          </span>
          <div class="pl-card__btns" @click.stop>
            <button class="pl-chip-btn" @click="openDetail(plan)">详情</button>
            <button class="pl-chip-btn pl-chip-btn--accent" @click="goEdit(plan.id)">编辑</button>
          </div>
        </div>
      </div>

      <div v-if="list.length === 0" class="pl-empty">
        <div class="pl-empty__icon">
          <svg viewBox="0 0 64 64" width="56" height="56" fill="currentColor"><path d="M18 14h28a4 4 0 0 1 4 4v32a4 4 0 0 1-4 4H18a4 4 0 0 1-4-4V18a4 4 0 0 1 4-4zm0 4v8h28v-8H18zm0 12v6h28v-6H18zm0 10v6h18v-6H18z"/></svg>
        </div>
        <h4>还没有训练计划</h4>
        <p>从第一个动作组合开始，打造属于你的训练方案</p>
        <button class="pl-btn pl-btn--primary" @click="goCreate">
          <el-icon><Plus /></el-icon>新建计划
        </button>
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
      size="460px"
      direction="rtl"
      destroy-on-close
    >
      <template #header>
        <div class="pl-drawer-title">
          <h3>{{ detail?.name }}</h3>
          <button v-if="detail" class="pl-btn pl-btn--primary" style="padding:6px 12px;font-size:12px" @click="goEdit(detail.id)">
            <el-icon><Edit /></el-icon>编辑
          </button>
        </div>
      </template>

      <template v-if="detail">
        <div class="pl-detail">
          <div class="pl-detail__meta">
            <div class="pl-detail__meta-row">
              <span>动作数量</span>
              <strong>{{ detail.items?.length || 0 }} 个</strong>
            </div>
            <div class="pl-detail__meta-row">
              <span>总组数</span>
              <strong>{{ totalSets }} 组</strong>
            </div>
            <div class="pl-detail__meta-row">
              <span>预计时长</span>
              <strong>~{{ estDuration }} 分钟</strong>
            </div>
          </div>

          <div v-if="detail.description" class="pl-detail__desc">
            <h4>计划说明</h4>
            <p>{{ detail.description }}</p>
          </div>

          <div class="pl-detail__items">
            <h4>
              动作编排
              <small>{{ detail.items?.length || 0 }} 个动作</small>
            </h4>
            <div v-if="!detail.items?.length" class="pl-empty-sm">暂无动作</div>
            <div
              v-for="(item, idx) in detail.items"
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
                  <span v-if="item.restSeconds" class="pl-rest">
                    间歇 {{ item.restSeconds }}s
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled, Edit, Delete, Clock } from '@element-plus/icons-vue'
import {
  getPlanList,
  getPlanDetail,
  deletePlan
} from '@/api/training'

const router = useRouter()

// ====== 列表 ======
const query = reactive({ page: 1, size: 8 })
const list    = ref([])
const total   = ref(0)
const loading = ref(false)

async function fetchList(resetPage) {
  if (resetPage) query.page = 1
  loading.value = true
  try {
    const res = await getPlanList({ ...query })
    list.value  = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function formatDate(s) {
  if (!s) return '—'
  return String(s).slice(0, 10).replace('T', ' ')
}

// ====== 导航 ======
function goCreate() { router.push('/training/plans/new') }
function goEdit(id)  { router.push(`/training/plans/${id}/edit`) }

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

const totalSets = computed(() =>
  (detail.value?.items || []).reduce((s, x) => s + (Number(x.targetSets) || 0), 0)
)
const estDuration = computed(() => {
  const items = detail.value?.items || []
  let total = 0
  for (const it of items) {
    const sets = it.targetSets || 0
    const rest = it.restSeconds ?? 90
    // 每组按 40s 工作时间估算
    total += sets * 40 + Math.max(0, sets - 1) * rest
  }
  return Math.round(total / 60)
})

// ====== 卡片操作 ======
function onCardCmd(cmd, plan) {
  if (cmd === 'edit') goEdit(plan.id)
  else if (cmd === 'delete') handleDelete(plan)
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
    const msgMap = { PLAN_NOT_FOUND: '计划不存在' }
    ElMessage.error(msgMap[e?.message] || e?.message || '删除失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
.pl-page { display: flex; flex-direction: column; gap: 16px; }

/* ===== Hero ===== */
.pl-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  padding: 26px 32px;
  color: #fff;
  isolation: isolate;
  box-shadow: 0 10px 30px -10px rgba(10, 37, 64, 0.3);
}
.pl-hero__bg {
  position: absolute; inset: 0; z-index: -1;
  background:
    radial-gradient(ellipse at 80% 10%, rgba(255, 140, 105, 0.35) 0%, transparent 50%),
    radial-gradient(ellipse at 0% 100%, rgba(34, 211, 238, 0.3) 0%, transparent 55%),
    linear-gradient(135deg, #131f42 0%, #1b2a63 45%, #2a3f8f 100%);
}
.pl-hero__bg::after {
  content: '';
  position: absolute; inset: 0;
  background-image:
    radial-gradient(circle at 30% 50%, rgba(255,255,255,0.06) 1.5px, transparent 1.5px);
  background-size: 22px 22px;
}
.pl-hero__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.pl-hero__content h2 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.01em;
}
.pl-hero__content p {
  margin: 0;
  font-size: 13px;
  opacity: 0.8;
}

/* ===== 按钮 ===== */
.pl-btn {
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
  transition: all 0.16s cubic-bezier(.2,.8,.2,1);
}
.pl-btn:hover { border-color: #22d3ee; color: #22d3ee; }
.pl-btn--primary {
  background: linear-gradient(135deg, #ff8c69 0%, #ff6b6b 100%);
  border: none;
  color: #fff;
  box-shadow: 0 8px 20px -6px rgba(255, 107, 107, 0.55);
}
.pl-btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 26px -6px rgba(255, 107, 107, 0.7);
  color: #fff;
}
.pl-icon-btn {
  width: 32px; height: 32px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.pl-icon-btn:hover { background: var(--bg-soft); color: var(--text); border-color: var(--border); }

/* ===== 卡片列表 ===== */
.pl-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.pl-card {
  position: relative;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 20px 20px 16px;
  cursor: pointer;
  transition: all 0.22s cubic-bezier(.2,.8,.2,1);
  overflow: hidden;
}
.pl-card__glow {
  position: absolute;
  inset: -2px;
  border-radius: 18px;
  background: linear-gradient(135deg, transparent 40%, rgba(34, 211, 238, 0.15) 60%, rgba(255, 140, 105, 0.12) 100%);
  opacity: 0;
  transition: opacity 0.25s;
  z-index: -1;
}
.pl-card:hover {
  transform: translateY(-3px);
  border-color: rgba(34, 211, 238, 0.35);
  box-shadow: 0 16px 32px -14px rgba(10, 37, 64, 0.22);
}
.pl-card:hover .pl-card__glow { opacity: 1; }

.pl-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}
.pl-card__index {
  width: 54px; height: 54px;
  border-radius: 14px;
  background: linear-gradient(135deg, #0d4e6e 0%, #22d3ee 100%);
  color: #fff;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  font-weight: 700;
  font-size: 20px;
  line-height: 1;
  box-shadow: 0 8px 18px -6px rgba(34, 211, 238, 0.55);
}
.pl-card__index small {
  font-size: 10px;
  opacity: 0.85;
  font-weight: 500;
  letter-spacing: 0.1em;
  margin-top: 3px;
}

.pl-card__body { margin-bottom: 14px; }
.pl-card__name {
  margin: 0 0 6px;
  font-size: 17px;
  font-weight: 700;
  color: var(--text);
  line-height: 1.3;
}
.pl-card__desc {
  margin: 0;
  font-size: 12.5px;
  color: var(--text-muted);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.4em;
}

.pl-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px dashed var(--border);
}
.pl-card__date {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 12px; color: var(--text-muted);
}
.pl-card__btns { display: flex; gap: 8px; }

.pl-chip-btn {
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  font-size: 12px;
  color: var(--text-soft);
  cursor: pointer;
  transition: all 0.15s;
}
.pl-chip-btn:hover { border-color: #22d3ee; color: #22d3ee; }
.pl-chip-btn--accent {
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.15), rgba(34, 211, 238, 0.05));
  border-color: rgba(34, 211, 238, 0.35);
  color: #0ea5b7;
}
.pl-chip-btn--accent:hover {
  background: rgba(34, 211, 238, 0.25);
  color: #0d9488;
}

/* ===== 空态 ===== */
.pl-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px 50px;
  background: var(--card);
  border: 1px dashed var(--border);
  border-radius: 16px;
  color: var(--text-muted);
}
.pl-empty__icon {
  display: inline-block;
  color: #c4b5fd;
  margin-bottom: 14px;
  opacity: 0.85;
}
.pl-empty h4 {
  margin: 0 0 6px;
  font-size: 16px;
  color: var(--text);
}
.pl-empty p { margin: 0 0 18px; font-size: 13px; }

.pl-empty-sm {
  text-align: center;
  padding: 24px 0;
  color: var(--text-muted);
  font-size: 13px;
}

/* ===== 分页 ===== */
.pl-pagination {
  display: flex; justify-content: center;
  padding: 12px 0 4px;
}

/* ===== 详情 Drawer ===== */
.pl-drawer-title {
  display: flex; align-items: center; justify-content: space-between;
  width: 100%;
}
.pl-drawer-title h3 { margin: 0; font-size: 19px; font-weight: 700; }

.pl-detail { padding: 4px 4px 20px; }
.pl-detail__meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}
.pl-detail__meta-row {
  padding: 14px 12px;
  background: var(--bg-soft);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.pl-detail__meta-row span {
  font-size: 11.5px;
  color: var(--text-muted);
  letter-spacing: 0.05em;
}
.pl-detail__meta-row strong {
  font-size: 18px;
  font-weight: 700;
  color: #22d3ee;
}

.pl-detail__desc { margin-bottom: 20px; }
.pl-detail__desc h4,
.pl-detail__items h4 {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  padding-left: 10px;
  border-left: 3px solid #ff8c69;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pl-detail__desc h4 small,
.pl-detail__items h4 small {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-muted);
  border: none;
  padding: 0;
}
.pl-detail__desc p {
  margin: 0;
  padding: 12px 14px;
  background: var(--bg-soft);
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-soft);
  line-height: 1.7;
}

.pl-ex-row {
  display: flex; align-items: center; gap: 14px;
  padding: 12px 14px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 12px;
  margin-bottom: 10px;
  transition: all 0.15s;
}
.pl-ex-row:hover { border-color: rgba(34, 211, 238, 0.4); }
.pl-ex-row__no {
  width: 30px; height: 30px; flex: 0 0 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #0d4e6e, #22d3ee);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  display: flex; align-items: center; justify-content: center;
}
.pl-ex-row__info { flex: 1; min-width: 0; }
.pl-ex-row__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
}
.pl-ex-row__params {
  display: flex; align-items: center; gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
}
.pl-ex-row__params span:not(.pl-rest) { font-weight: 600; color: var(--text-soft); }
.pl-rest {
  margin-left: 6px;
  padding: 1px 8px;
  border-radius: 999px;
  background: rgba(196, 181, 253, 0.18);
  color: #7c5cff;
  font-weight: 500;
}

/* dropdown danger */
:deep(.el-dropdown-menu__item.danger) { color: #ff4d4f; }
</style>
