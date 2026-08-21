<template>
  <div class="pe-page">
    <!-- 顶栏 -->
    <div class="pe-topbar">
      <button class="pe-back" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回计划列表</span>
      </button>
      <div class="pe-topbar__title">
        <h2>{{ isEdit ? '编辑训练计划' : '新建训练计划' }}</h2>
        <span v-if="form.name" class="pe-topbar__name">「{{ form.name }}」</span>
      </div>
      <div class="pe-topbar__actions">
        <button class="pe-btn" @click="goBack">取消</button>
        <button class="pe-btn pe-btn--primary" :disabled="saving" @click="submit">
          <el-icon v-if="!saving"><Check /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <span>{{ saving ? '保存中…' : (isEdit ? '保存修改' : '创建计划') }}</span>
        </button>
      </div>
    </div>

    <div class="pe-body">
      <!-- ====== 左侧：动作选择器 ====== -->
      <aside class="pe-sidebar">
        <div class="pe-section">
          <h3>动作库</h3>
          <el-input
            v-model="searchKw"
            placeholder="搜索动作…"
            clearable
            size="default"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>

          <div class="pe-cat-tabs">
            <button
              class="pe-cat-tab"
              :class="{ 'is-on': catFilter === '' }"
              @click="catFilter = ''"
            >全部</button>
            <button
              v-for="c in CATEGORY_OPTIONS"
              :key="c.value"
              class="pe-cat-tab"
              :class="{ 'is-on': catFilter === c.value }"
              :style="catFilter === c.value ? { borderColor: c.color, color: c.color, background: c.color + '14' } : {}"
              @click="catFilter = c.value"
            >
              <span class="pe-cat-tab__dot" :style="{ background: c.color }"></span>
              {{ c.label }}
            </button>
          </div>
        </div>

        <div class="pe-section pe-section--scroll">
          <div v-if="filteredExercises.length === 0" class="pe-empty-sm">
            没有符合条件的动作
          </div>
          <TransitionGroup name="list">
            <div
              v-for="ex in filteredExercises"
              :key="ex.id"
              class="pe-ex-card"
              :class="{ 'is-in-plan': isInPlan(ex.id) }"
              @click="toggleExercise(ex)"
            >
              <div class="pe-ex-card__head">
                <span
                  class="pe-ex-card__chip"
                  :style="{ background: catColor(ex.category) + '18', color: catColor(ex.category) }"
                >{{ catLabel(ex.category) }}</span>
                <span class="pe-ex-card__diff">
                  <span
                    v-for="n in 3"
                    :key="n"
                    :class="{ 'is-on': n <= ex.difficulty }"
                  >★</span>
                </span>
              </div>
              <div class="pe-ex-card__name">
                {{ ex.name }}
                <el-tag v-if="ex.isSystem" size="small" effect="plain" class="pe-tag">系统</el-tag>
              </div>
              <div v-if="ex.muscleGroup" class="pe-ex-card__mg">{{ ex.muscleGroup }}</div>
              <div class="pe-ex-card__add">
                <template v-if="isInPlan(ex.id)">
                  <el-icon color="#52c41a"><CircleCheckFilled /></el-icon>
                  <span style="color:#52c41a">已加入 · 再点移除</span>
                </template>
                <template v-else>
                  <el-icon><Plus /></el-icon>
                  <span>加入计划</span>
                </template>
              </div>
            </div>
          </TransitionGroup>
        </div>
      </aside>

      <!-- ====== 右侧：已选 & 基本信息 ====== -->
      <section class="pe-main">
        <!-- 基本信息 -->
        <div class="pe-card pe-card--featured">
          <div class="pe-card-accent"></div>
          <h3 class="pe-card__title pe-card__title--featured">
            <span class="pe-title__num">1</span>
            基本信息
            <small>定义你的训练计划</small>
          </h3>
          <el-form
            ref="basicFormRef"
            :model="form"
            :rules="basicRules"
            label-position="top"
            class="pe-form"
          >
            <div class="pe-form-section">
              <div class="pe-form-section__head">
                <el-icon class="pe-form-icon"><EditPen /></el-icon>
                <span>计划标识</span>
              </div>
              <div class="pe-form-section__body">
                <el-form-item label="计划名称" prop="name" class="pe-form-item--full">
                  <el-input
                    v-model="form.name"
                    placeholder="给这个计划取一个有意义的名字，如：推日A / 上肢增肌日"
                    maxlength="50"
                    show-word-limit
                    class="pe-input"
                  >
                    <template #prefix>
                      <el-icon class="pe-input__icon"><Calendar /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="计划描述" prop="description">
                  <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="3"
                    maxlength="200"
                    placeholder="简要说明训练重点、适用场景、目标肌群等（选填）"
                    show-word-limit
                    class="pe-textarea"
                  />
                </el-form-item>
              </div>
            </div>
          </el-form>
        </div>

        <!-- 已选动作 -->
        <div class="pe-card">
          <div class="pe-card__header">
            <h3 class="pe-card__title">
              <span class="pe-title__num">2</span>
              动作编排
              <small>{{ form.items.length }} 个动作</small>
            </h3>
            <div v-if="form.items.length" class="pe-card__quick">
              <div class="pe-quick">
                <span>总组数：<strong>{{ totalSets }}</strong></span>
                <span>预计时长：<strong>~{{ estDuration }} 分钟</strong></span>
              </div>
            </div>
          </div>

          <div v-if="form.items.length === 0" class="pe-empty-box">
            <div class="pe-empty-box__icon">
              <el-icon :size="40"><Files /></el-icon>
            </div>
            <h4>还没有添加动作</h4>
            <p>在左侧动作库中点击动作卡片加入编排</p>
          </div>

          <TransitionGroup v-else name="list" tag="div" class="pe-items">
            <div
              v-for="(item, index) in form.items"
              :key="item._key"
              class="pe-item"
            >
              <!-- 拖拽条 -->
              <div class="pe-item__drag">
                <div class="pe-item__order">
                  <span>{{ index + 1 }}</span>
                  <small>#{{ sortOf(index) }}</small>
                </div>
                <div class="pe-item__drag-handle" title="拖拽排序">
                  <el-icon><Rank /></el-icon>
                </div>
                <div class="pe-item__drag-btns">
                  <button
                    :disabled="index === 0"
                    title="上移"
                    @click="moveItem(index, -1)"
                  ><el-icon><ArrowUp /></el-icon></button>
                  <button
                    :disabled="index === form.items.length - 1"
                    title="下移"
                    @click="moveItem(index, 1)"
                  ><el-icon><ArrowDown /></el-icon></button>
                </div>
              </div>

              <!-- 主体 -->
              <div class="pe-item__body">
                <div class="pe-item__head">
                  <div class="pe-item__name">
                    <span class="pe-item__catdot" :style="{ background: catColor(exerciseById(item.exerciseId)?.category) }"></span>
                    {{ item.exerciseName || exerciseById(item.exerciseId)?.name || '未知动作' }}
                  </div>
                  <button class="pe-item__remove" title="移除动作" @click="removeItem(index)">
                    <el-icon><Delete /></el-icon>
                    移除
                  </button>
                </div>

                <div class="pe-item__params">
                  <div class="pe-param">
                    <label>目标组数</label>
                    <el-input-number
                      v-model="item.targetSets"
                      :min="1"
                      :max="20"
                      size="small"
                      controls-position="right"
                    />
                  </div>
                  <div class="pe-param">
                    <label>每组次数</label>
                    <el-input-number
                      v-model="item.targetReps"
                      :min="1"
                      :max="999"
                      size="small"
                      controls-position="right"
                    />
                  </div>
                  <div class="pe-param">
                    <label>间歇 (秒)</label>
                    <el-input-number
                      v-model="item.restSeconds"
                      :min="0"
                      :max="600"
                      :step="30"
                      size="small"
                      controls-position="right"
                    />
                  </div>
                </div>
              </div>
            </div>
          </TransitionGroup>
        </div>

        <!-- 底部保存条（移动端） -->
        <div class="pe-savebar">
          <span class="pe-savebar__tip">
            共 <strong>{{ form.items.length }}</strong> 个动作 ·
            <strong>{{ totalSets }}</strong> 组 ·
            预计 <strong>~{{ estDuration }}</strong> 分钟
          </span>
          <div style="display:flex;gap:8px">
            <button class="pe-btn" @click="goBack">取消</button>
            <button class="pe-btn pe-btn--primary" :disabled="saving" @click="submit">
              {{ saving ? '保存中…' : (isEdit ? '保存修改' : '创建计划') }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, unref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Check, Loading, Search, Plus, CircleCheckFilled,
  Files, Rank, ArrowUp, ArrowDown, Delete, EditPen, Calendar
} from '@element-plus/icons-vue'
import {
  getPlanDetail,
  createPlan,
  updatePlan,
  getAllExercises,
  CATEGORY_OPTIONS
} from '@/api/training'

const route  = useRoute()
const router = useRouter()

// ===== 基本状态 =====
const planId     = computed(() => route.params.id)
const isEdit     = computed(() => !!planId.value && planId.value !== 'new')
const saving     = ref(false)
const loading    = ref(false)

const basicFormRef = ref(null)
const basicRules = {
  name: [
    { required: true, message: '请输入计划名称', trigger: 'blur' },
    { min: 1, max: 50, message: '1-50 字符', trigger: 'blur' }
  ]
}

// 基本表单
const form = reactive({
  name: '',
  description: '',
  items: []  // 每项：{ _key, id?, exerciseId, exerciseName, sortOrder, targetSets, targetReps, restSeconds }
})

// ===== 动作库 =====
const allExercises = ref([])
const searchKw  = ref('')
const catFilter = ref('')

const filteredExercises = computed(() => {
  const kw = searchKw.value.trim().toLowerCase()
  return allExercises.value.filter(ex => {
    if (catFilter.value && ex.category !== catFilter.value) return false
    if (kw && !ex.name.toLowerCase().includes(kw)) return false
    return true
  })
})

function catLabel(v) { return CATEGORY_OPTIONS.find(x => x.value === v)?.label || v }
function catColor(v) { return CATEGORY_OPTIONS.find(x => x.value === v)?.color || '#999' }
function exerciseById(id) { return allExercises.value.find(e => e.id === id) }

// ===== 左侧选择器 =====
function isInPlan(exId) {
  return form.items.some(x => x.exerciseId === exId)
}

let _itemKey = 0
function toggleExercise(ex) {
  if (isInPlan(ex.id)) {
    const idx = form.items.findIndex(x => x.exerciseId === ex.id)
    if (idx >= 0) form.items.splice(idx, 1)
  } else {
    form.items.push({
      _key: ++_itemKey,
      id: '',
      exerciseId: ex.id,
      exerciseName: ex.name,
      sortOrder: form.items.length + 1,
      targetSets: 3,
      targetReps: 10,
      restSeconds: 90
    })
  }
  refreshSortOrder()
}

function removeItem(index) { form.items.splice(index, 1); refreshSortOrder() }
function moveItem(index, step) {
  const t = index + step
  if (t < 0 || t >= form.items.length) return
  const [moved] = form.items.splice(index, 1)
  form.items.splice(t, 0, moved)
  refreshSortOrder()
}
function refreshSortOrder() {
  form.items.forEach((x, i) => { x.sortOrder = i + 1 })
}
function sortOf(index) { return form.items[index]?.sortOrder ?? index + 1 }

// ===== 汇总 =====
const totalSets = computed(() =>
  form.items.reduce((s, x) => s + (Number(x.targetSets) || 0), 0)
)
const estDuration = computed(() => {
  let total = 0
  for (const it of form.items) {
    const sets = it.targetSets || 0
    const rest = it.restSeconds ?? 90
    total += sets * 40 + Math.max(0, sets - 1) * rest
  }
  return Math.round(total / 60)
})

// ===== 提交 =====
async function submit() {
  if (basicFormRef.value) {
    try { await basicFormRef.value.validate() } catch { return }
  }
  if (form.items.length === 0) {
    ElMessage.warning('请至少添加 1 个动作')
    return
  }

  saving.value = true
  const payload = {
    name: form.name.trim(),
    description: form.description.trim(),
    items: form.items.map(it => ({
      id: it.id || undefined,
      exerciseId: it.exerciseId,
      sortOrder: it.sortOrder,
      targetSets: Number(it.targetSets),
      targetReps: Number(it.targetReps),
      restSeconds: Number(it.restSeconds)
    }))
  }

  try {
    if (isEdit.value) {
      await updatePlan(planId.value, payload)
      ElMessage.success('计划已更新')
    } else {
      await createPlan(payload)
      ElMessage.success('计划已创建')
    }
    router.push('/training/plans')
  } catch (e) {
    const msgMap = {
      PLAN_NOT_FOUND: '计划不存在',
      PLAN_NAME_DUPLICATED: '计划名称已存在',
      PLAN_ITEM_EMPTY: '请至少包含 1 个动作'
    }
    ElMessage.error(msgMap[e?.message] || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() { router.push('/training/plans') }

// ===== 初始化 =====
onMounted(async () => {
  loading.value = true
  try {
    allExercises.value = await getAllExercises()
    if (isEdit.value) {
      const detail = await getPlanDetail(planId.value)
      form.name = detail.name || ''
      form.description = detail.description || ''
      form.items = (detail.items || []).map(it => ({
        _key: ++_itemKey,
        id: it.id || '',
        exerciseId: it.exerciseId,
        exerciseName: it.exerciseName,
        sortOrder: it.sortOrder,
        targetSets: it.targetSets,
        targetReps: it.targetReps,
        restSeconds: it.restSeconds ?? 60
      }))
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
    if (isEdit.value) router.push('/training/plans')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.pe-page {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 100px);
}

/* ===== 顶栏 ===== */
.pe-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  margin-bottom: 16px;
}
.pe-back {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-soft);
  font-size: 12.5px;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
}
.pe-back:hover { border-color: #22d3ee; color: #22d3ee; }
.pe-topbar__title {
  flex: 1;
  display: flex; align-items: baseline; gap: 10px;
  min-width: 0;
}
.pe-topbar__title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}
.pe-topbar__name {
  font-size: 13px;
  color: #22d3ee;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pe-topbar__actions { display: flex; gap: 8px; flex-shrink: 0; }

.pe-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--text);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.16s;
}
.pe-btn:hover { border-color: #22d3ee; color: #22d3ee; }
.pe-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none !important; }
.pe-btn--primary {
  background: linear-gradient(135deg, #131f42 0%, #2a3f8f 50%, #22d3ee 120%);
  border: none;
  color: #fff;
  box-shadow: 0 6px 18px -6px rgba(42, 63, 143, 0.5);
}
.pe-btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px -6px rgba(42, 63, 143, 0.65);
  color: #fff;
}
.is-loading { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ===== 主体布局 ===== */
.pe-body {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 16px;
  align-items: start;
  flex: 1;
}

/* ===== 侧边栏 ===== */
.pe-sidebar {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 136px;
  max-height: calc(100vh - 152px);
  overflow: hidden;
}
.pe-section { padding: 16px; border-bottom: 1px solid var(--border); }
.pe-section:last-child { border-bottom: none; }
.pe-section--scroll { flex: 1; padding: 12px 14px 16px; overflow: auto; }
.pe-section h3 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  padding-left: 10px;
  border-left: 3px solid #22d3ee;
}

.pe-cat-tabs {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.pe-cat-tab {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  font-size: 12px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-soft);
  cursor: pointer;
  transition: all 0.15s;
}
.pe-cat-tab:hover { border-color: rgba(34, 211, 238, 0.5); }
.pe-cat-tab.is-on { font-weight: 600; }
.pe-cat-tab__dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  display: inline-block;
}

/* 动作小卡片 */
.pe-ex-card {
  padding: 12px 12px 10px;
  background: var(--bg-soft);
  border: 1px solid transparent;
  border-radius: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.18s cubic-bezier(.2,.8,.2,1);
}
.pe-ex-card:hover {
  border-color: rgba(34, 211, 238, 0.4);
  background: var(--card);
  transform: translateX(2px);
  box-shadow: 0 4px 12px -4px rgba(10, 37, 64, 0.12);
}
.pe-ex-card.is-in-plan {
  border-color: rgba(82, 196, 26, 0.35);
  background: linear-gradient(135deg, rgba(82,196,26,0.08), transparent);
}
.pe-ex-card__head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 6px;
}
.pe-ex-card__chip {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 600;
}
.pe-ex-card__diff {
  font-size: 11px;
  letter-spacing: 1px;
  color: #d9d9d9;
}
.pe-ex-card__diff .is-on { color: #faad14; }
.pe-ex-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 3px;
  display: flex; align-items: center; gap: 6px;
}
.pe-tag {
  --el-tag-border-color: rgba(124,92,255,0.35);
  --el-tag-text-color: #7c5cff;
  font-size: 10px;
  padding: 0 5px;
  height: 16px;
  line-height: 14px;
}
.pe-ex-card__mg {
  font-size: 11.5px;
  color: var(--text-muted);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.pe-ex-card__add {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 12px;
  font-weight: 500;
  color: #22d3ee;
}

.pe-empty-sm {
  text-align: center;
  padding: 30px 0;
  color: var(--text-muted);
  font-size: 12.5px;
}

/* ===== 右侧主区 ===== */
.pe-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.pe-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 18px 20px;
}
.pe-card__header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 4px;
  flex-wrap: wrap;
  gap: 10px;
}
.pe-card__title {
  margin: 0 0 14px;
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
  display: inline-flex;
  align-items: center;
  gap: 10px;
}
.pe-card__title small {
  font-size: 12px;
  font-weight: 400;
  color: var(--text-muted);
}
.pe-title__num {
  width: 24px; height: 24px;
  border-radius: 8px;
  background: linear-gradient(135deg, #131f42, #22d3ee);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: inline-flex; align-items: center; justify-content: center;
}
.pe-quick {
  display: flex; gap: 16px;
  padding: 6px 14px;
  background: var(--bg-soft);
  border-radius: 999px;
  font-size: 12.5px;
  color: var(--text-muted);
}
.pe-quick strong { color: #ff8c69; margin-left: 3px; }

/* ===== 特色卡片（基本信息） ===== */
.pe-card--featured {
  position: relative;
  overflow: hidden;
  padding: 20px 24px 24px;
  border: 1.5px solid rgba(102, 126, 234, 0.25);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}
.pe-card-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
}
.pe-card__title--featured {
  margin: 0 0 18px;
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--border);
}
.pe-card__title--featured small {
  font-size: 13px;
  font-weight: 400;
  color: var(--text-muted);
  margin-left: 8px;
}

/* 表单样式 */
.pe-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-soft);
  padding-bottom: 6px !important;
}
.pe-form-section {
  margin-bottom: 16px;
}
.pe-form-section__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 10px 14px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.05));
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  color: #5a67d8;
}
.pe-form-icon {
  font-size: 16px;
  color: #667eea;
}
.pe-form-section__body {
  padding: 0 4px;
}

/* 输入框样式 */
.pe-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #fff;
  border: 1.5px solid var(--border);
  box-shadow: none !important;
  transition: all 0.2s ease;
}
.pe-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(102, 126, 234, 0.4);
}
.pe-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}
.pe-input__icon {
  color: #667eea;
  font-size: 15px;
}

/* 文本域样式 */
.pe-textarea :deep(.el-textarea__wrapper) {
  border-radius: 12px;
  background: #fff;
  border: 1.5px solid var(--border);
  box-shadow: none !important;
  transition: all 0.2s ease;
  padding: 12px 14px;
}
.pe-textarea :deep(.el-textarea__wrapper:hover) {
  border-color: rgba(102, 126, 234, 0.4);
}
.pe-textarea :deep(.el-textarea__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}

/* ===== 动作编排卡 ===== */
.pe-items { display: flex; flex-direction: column; gap: 12px; }

.pe-item {
  display: grid;
  grid-template-columns: 70px 1fr;
  gap: 0;
  background: linear-gradient(180deg, var(--bg-soft), transparent 60%);
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;
  transition: all 0.2s;
}
.pe-item:hover {
  border-color: rgba(34, 211, 238, 0.4);
  box-shadow: 0 8px 20px -10px rgba(10, 37, 64, 0.18);
}

.pe-item__drag {
  background: linear-gradient(180deg, #0d4e6e 0%, #131f42 100%);
  padding: 14px 10px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.pe-item__order {
  display: flex; flex-direction: column; align-items: center;
  line-height: 1;
}
.pe-item__order span {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #22d3ee, #ff8c69);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.pe-item__order small {
  margin-top: 3px;
  font-size: 9px;
  opacity: 0.6;
  letter-spacing: 0.1em;
}
.pe-item__drag-handle {
  width: 28px; height: 28px;
  border-radius: 8px;
  background: rgba(255,255,255,0.08);
  display: flex; align-items: center; justify-content: center;
  cursor: grab;
  color: rgba(255,255,255,0.75);
  transition: background 0.15s;
}
.pe-item__drag-handle:hover { background: rgba(255,255,255,0.16); color: #fff; }
.pe-item__drag-handle:active { cursor: grabbing; }

.pe-item__drag-btns {
  display: flex; flex-direction: column; gap: 2px;
}
.pe-item__drag-btns button {
  width: 28px; height: 22px;
  border-radius: 6px;
  border: none;
  background: rgba(255,255,255,0.06);
  color: rgba(255,255,255,0.65);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.15s;
  font-size: 11px;
}
.pe-item__drag-btns button:hover:not(:disabled) { background: rgba(255,255,255,0.18); color: #fff; }
.pe-item__drag-btns button:disabled { opacity: 0.3; cursor: not-allowed; }

.pe-item__body { padding: 14px 16px; }
.pe-item__head {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 12px;
  gap: 10px;
  flex-wrap: wrap;
}
.pe-item__name {
  display: inline-flex; align-items: center; gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
}
.pe-item__catdot {
  width: 9px; height: 9px;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 0 3px rgba(255,255,255,0.05);
}
.pe-item__remove {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: rgba(255, 107, 107, 0.08);
  color: #ff6b6b;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.pe-item__remove:hover {
  background: rgba(255, 107, 107, 0.16);
  border-color: rgba(255, 107, 107, 0.3);
}

.pe-item__params {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.pe-param { display: flex; flex-direction: column; gap: 5px; }
.pe-param label {
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 0.05em;
}

/* ===== 空态盒 ===== */
.pe-empty-box {
  text-align: center;
  padding: 44px 20px;
  color: var(--text-muted);
  border: 1px dashed var(--border);
  border-radius: 14px;
  background: var(--bg-soft);
}
.pe-empty-box__icon {
  color: #c4b5fd;
  margin-bottom: 12px;
  opacity: 0.85;
}
.pe-empty-box h4 {
  margin: 0 0 6px;
  font-size: 15px;
  color: var(--text);
}
.pe-empty-box p { margin: 0; font-size: 12.5px; }

/* ===== 移动端保存条 ===== */
.pe-savebar {
  display: none;
  position: sticky;
  bottom: 0;
  margin: 4px -24px -32px;
  padding: 12px 20px;
  background: var(--card);
  border-top: 1px solid var(--border);
  align-items: center;
  justify-content: space-between;
  z-index: 5;
}
.pe-savebar__tip { font-size: 12.5px; color: var(--text-muted); }
.pe-savebar__tip strong { color: #ff8c69; }

/* ===== 列表过渡 ===== */
.list-enter-active, .list-leave-active { transition: all 0.25s ease; }
.list-enter-from { opacity: 0; transform: translateX(-8px); }
.list-leave-to   { opacity: 0; transform: translateX(8px); }

/* ===== 响应式 ===== */
@media (max-width: 960px) {
  .pe-body { grid-template-columns: 1fr; }
  .pe-sidebar {
    position: static;
    max-height: none;
  }
  .pe-topbar__actions { display: none; }
  .pe-savebar { display: flex; }
}
@media (max-width: 620px) {
  .pe-item { grid-template-columns: 56px 1fr; }
  .pe-item__params { grid-template-columns: 1fr 1fr; }
  .pe-item__params .pe-param:last-child { grid-column: 1 / -1; }
}
</style>
