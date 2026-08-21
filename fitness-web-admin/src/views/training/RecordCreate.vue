<template>
  <div class="rc-page">
    <!-- 顶栏 -->
    <div class="rc-topbar">
      <button class="rc-back" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回记录列表</span>
      </button>
      <div class="rc-topbar__title">
        <h2>记录训练</h2>
        <small v-if="saving">正在保存…</small>
      </div>
      <div class="rc-topbar__actions">
        <button class="rc-btn" @click="goBack">取消</button>
        <button class="rc-btn rc-btn--primary" :disabled="saving" @click="submit">
          <el-icon v-if="!saving"><Check /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <span>{{ saving ? '保存中…' : '提交记录' }}</span>
        </button>
      </div>
    </div>

    <div class="rc-body">
      <!-- ===== 左：基本信息 + 容量预览 ===== -->
      <aside class="rc-sidecard">
        <div class="rc-card">
          <h3 class="rc-card__title">基本信息</h3>
          <el-form :model="form" label-width="80px" label-position="right">
            <el-form-item label="训练计划">
              <el-select
                v-model="form.planId"
                placeholder="选择训练计划（可选）"
                clearable
                filterable
                style="width: 100%"
                @change="onPlanChange"
              >
                <el-option
                  v-for="p in planOptions"
                  :key="p.id"
                  :label="p.name"
                  :value="p.id"
                >
                  <div class="rc-opt">
                    <strong>{{ p.name }}</strong>
                    <span>{{ (p.items || []).length }} 动作</span>
                  </div>
                </el-option>
              </el-select>
              <div v-if="form.planId && currentPlan" class="rc-plan-hint">
                <el-icon><InfoFilled /></el-icon>
                已选择「{{ currentPlan.name }}」，{{ currentPlan.items?.length }} 个动作已带入下方
              </div>
            </el-form-item>

            <el-form-item label="训练日期">
              <el-date-picker
                v-model="form.recordDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择训练日期"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="训练时长">
              <div class="rc-duration">
                <el-input-number
                  v-model="form.durationMin"
                  :min="0"
                  :max="600"
                  size="default"
                  controls-position="right"
                />
                <span>分钟</span>
              </div>
            </el-form-item>

            <el-form-item label="训练备注">
              <el-input
                v-model="form.note"
                type="textarea"
                :rows="4"
                placeholder="记录训练状态、身体感受、进步心得…（选填）"
                maxlength="400"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>

        <!-- 容量预览 -->
        <div class="rc-card rc-sticky">
          <h3 class="rc-card__title">实时汇总</h3>

          <div class="rc-preview">
            <div class="rc-preview__item rc-preview__item--v">
              <label>累计容量</label>
              <strong>{{ formatNum(liveVolume) }} <small>kg</small></strong>
              <div class="rc-preview__bar">
                <div class="rc-preview__fill rc-preview__fill--v" :style="{ width: barPercent(liveVolume, 15000) }"></div>
              </div>
            </div>
            <div class="rc-preview__row">
              <div class="rc-preview__item rc-preview__item--s">
                <label>总组数</label>
                <strong>{{ liveSets }}</strong>
              </div>
              <div class="rc-preview__item rc-preview__item--r">
                <label>总次数</label>
                <strong>{{ liveReps }}</strong>
              </div>
            </div>
            <div class="rc-preview__row">
              <div class="rc-preview__item rc-preview__item--e">
                <label>动作数量</label>
                <strong>{{ exerciseBlocks.length }}</strong>
              </div>
              <div class="rc-preview__item rc-preview__item--d">
                <label>预估强度</label>
                <strong>{{ avgRpeLabel }}</strong>
              </div>
            </div>
          </div>

          <div class="rc-actions-bottom">
            <button class="rc-btn rc-btn--block" @click="addExerciseBlock">
              <el-icon><Plus /></el-icon>
              <span>添加动作</span>
            </button>
            <button class="rc-btn rc-btn--primary rc-btn--block" :disabled="saving" @click="submit">
              {{ saving ? '保存中…' : '提交训练记录' }}
            </button>
          </div>
        </div>
      </aside>

      <!-- ===== 右：按动作分组录入 ===== -->
      <section class="rc-main">
        <div v-if="exerciseBlocks.length === 0" class="rc-empty-box">
          <div class="rc-empty-box__icon">
            <el-icon :size="42"><Flag /></el-icon>
          </div>
          <h4>开始记录这次训练</h4>
          <p>先从左侧选择训练计划，或点击下方按钮直接添加动作</p>
          <button class="rc-btn rc-btn--primary" @click="addExerciseBlock">
            <el-icon><Plus /></el-icon>添加第一个动作
          </button>
        </div>

        <TransitionGroup v-else name="list" tag="div" class="rc-blocks">
          <div
            v-for="(block, bIdx) in exerciseBlocks"
            :key="block._key"
            class="rc-block"
          >
            <!-- 动作标题栏 -->
            <div class="rc-block__head">
              <div class="rc-block__no">
                <span>{{ bIdx + 1 }}</span>
                <small>#{{ bIdx + 1 }}</small>
              </div>
              <div class="rc-block__exerciseselect">
                <el-select
                  v-model="block.exerciseId"
                  filterable
                  placeholder="选择动作…"
                  size="default"
                  style="min-width: 240px"
                  @change="(v) => onBlockExerciseChange(bIdx, v)"
                >
                  <el-option
                    v-for="ex in exerciseOptions"
                    :key="ex.id"
                    :label="ex.name"
                    :value="ex.id"
                  >
                    <div class="rc-opt rc-opt--ex">
                      <span class="rc-opt__dot" :style="{ background: catColor(ex.category) }"></span>
                      <strong>{{ ex.name }}</strong>
                      <span class="rc-opt__diff">
                        <i v-for="n in 3" :key="n" :class="{ on: n <= ex.difficulty }">★</i>
                      </span>
                    </div>
                  </el-option>
                </el-select>
              </div>
              <div class="rc-block__actions">
                <button class="rc-ghost-btn" title="向下移动" :disabled="bIdx === exerciseBlocks.length - 1" @click="moveBlock(bIdx, 1)">
                  <el-icon><ArrowDown /></el-icon>
                </button>
                <button class="rc-ghost-btn" title="向上移动" :disabled="bIdx === 0" @click="moveBlock(bIdx, -1)">
                  <el-icon><ArrowUp /></el-icon>
                </button>
                <button class="rc-ghost-btn rc-ghost-btn--danger" title="移除动作" @click="removeBlock(bIdx)">
                  <el-icon><Delete /></el-icon>
                </button>
              </div>
            </div>

            <!-- 组明细 -->
            <div class="rc-block__body">
              <table class="rc-sets-table">
                <thead>
                  <tr>
                    <th style="width:64px">组次</th>
                    <th>重量 (kg)</th>
                    <th>次数</th>
                    <th style="width:86px">容量 (kg)</th>
                    <th style="width:150px">RPE (1-10)</th>
                    <th style="width:110px">间歇 (秒)</th>
                    <th style="width:110px">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(s, sIdx) in block.sets" :key="s._key">
                    <td class="is-setno">
                      <span class="rc-setno">{{ sIdx + 1 }}</span>
                    </td>
                    <td>
                      <el-input-number
                        v-model="s.weightKg"
                        :min="0"
                        :max="999"
                        :step="2.5"
                        :precision="1"
                        size="small"
                        controls-position="right"
                        style="width: 130px"
                      />
                    </td>
                    <td>
                      <el-input-number
                        v-model="s.reps"
                        :min="1"
                        :max="999"
                        size="small"
                        controls-position="right"
                        style="width: 110px"
                      />
                    </td>
                    <td class="is-volume">
                      {{ formatNum((Number(s.weightKg) || 0) * (Number(s.reps) || 0)) }}
                    </td>
                    <td>
                      <el-slider
                        v-model="s.rpe"
                        :min="0"
                        :max="10"
                        :step="0.5"
                        :show-tooltip="true"
                        :marks="rpeMarks"
                        size="small"
                        style="margin: 0 6px"
                      />
                    </td>
                    <td>
                      <el-input-number
                        v-model="s.restSeconds"
                        :min="0"
                        :max="600"
                        :step="15"
                        size="small"
                        controls-position="right"
                        style="width: 100px"
                      />
                    </td>
                    <td>
                      <div class="rc-setops">
                        <button
                          class="rc-mini-btn"
                          title="复制上一组"
                          :disabled="sIdx === 0"
                          @click="copyPrevSet(bIdx, sIdx)"
                        >复制</button>
                        <button
                          class="rc-mini-btn rc-mini-btn--danger"
                          title="删除该组"
                          :disabled="block.sets.length <= 1"
                          @click="removeSet(bIdx, sIdx)"
                        >删除</button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div class="rc-block__footer">
                <button class="rc-btn rc-btn--ghost" @click="addSet(bIdx)">
                  <el-icon><Plus /></el-icon>
                  <span>新增一组（+1）</span>
                </button>
                <div class="rc-block__summary">
                  <span>{{ block.sets.length }} 组</span>
                  <span>·</span>
                  <span>容量 {{ formatNum(blockVolume(block)) }}kg</span>
                </div>
              </div>
            </div>
          </div>
        </TransitionGroup>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Check, Loading, Plus, ArrowUp, ArrowDown, Delete,
  InfoFilled, Flag
} from '@element-plus/icons-vue'
import {
  createRecord,
  getAllExercises,
  getAllPlans,
  CATEGORY_OPTIONS
} from '@/api/training'

const router = useRouter()

// ===== 状态 =====
const saving        = ref(false)
const allExercises  = ref([])
const planOptions   = ref([])

const form = reactive({
  planId: '',
  recordDate: new Date().toISOString().slice(0, 10),
  durationMin: 60,
  note: ''
})

const rpeMarks = { 5: '5 轻松', 7: '7 吃力', 9: '9 极限', 10: '10 力竭' }

// 每个动作分组：{ _key, exerciseId, exerciseName, sets:[{_key,setNo,weightKg,reps,rpe,restSeconds}] }
const exerciseBlocks = ref([])
let _blockKey = 0
let _setKey   = 0

const exerciseOptions = computed(() => allExercises.value)
const currentPlan = computed(() =>
  form.planId ? planOptions.value.find(p => p.id === form.planId) : null
)

// ===== 工具 =====
function catColor(v) { return CATEGORY_OPTIONS.find(x => x.value === v)?.color || '#999' }
function formatNum(n) {
  const x = Number(n) || 0
  return x.toLocaleString('en-US', { maximumFractionDigits: 1 })
}
function barPercent(v, max) {
  const p = Math.min(100, (Number(v) || 0) / max * 100)
  return p + '%'
}

// ===== 初始化 =====
onMounted(async () => {
  try {
    const [exs, plans] = await Promise.all([getAllExercises(), getAllPlans()])
    allExercises.value = exs
    planOptions.value = plans
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  }
})

// ===== 计划切换：带入动作 =====
function onPlanChange(planId) {
  if (!planId) return
  const plan = planOptions.value.find(p => p.id === planId)
  if (!plan || !plan.items?.length) return

  // 若当前已有内容，二次确认
  if (exerciseBlocks.value.length > 0) {
    ElMessageBox.confirm(
      '选择训练计划后，会按计划的动作和目标组数重建下方录入表单，当前已有内容将被覆盖。是否继续？',
      '带入训练计划？',
      { confirmButtonText: '覆盖并带入', cancelButtonText: '取消', type: 'warning' }
    )
      .then(() => applyPlan(plan))
      .catch(() => { /* 用户取消 */ })
  } else {
    applyPlan(plan)
  }
}

function applyPlan(plan) {
  exerciseBlocks.value = (plan.items || [])
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map(it => {
      const targetSets = Math.max(1, it.targetSets || 3)
      return {
        _key: ++_blockKey,
        exerciseId: it.exerciseId,
        exerciseName: it.exerciseName,
        sets: Array.from({ length: targetSets }, (_, i) => makeSet({
          restSeconds: it.restSeconds
        }))
      }
    })
}

function makeSet(partial = {}) {
  return {
    _key: ++_setKey,
    setNo: 0,
    weightKg: partial.weightKg ?? 20,
    reps: partial.reps ?? 10,
    rpe: partial.rpe ?? 7,
    restSeconds: partial.restSeconds ?? 90
  }
}

// ===== 动作块操作 =====
function addExerciseBlock() {
  if (exerciseOptions.value.length === 0) {
    ElMessage.warning('动作库未加载，请稍后再试')
    return
  }
  const first = exerciseOptions.value[0]
  exerciseBlocks.value.push({
    _key: ++_blockKey,
    exerciseId: first.id,
    exerciseName: first.name,
    sets: [makeSet()]
  })
}
function removeBlock(idx) {
  ElMessageBox.confirm(`确认移除动作「${exerciseBlocks.value[idx]?.exerciseName || ''}」？`, '移除动作', {
    confirmButtonText: '移除', cancelButtonText: '取消', type: 'warning'
  }).then(() => exerciseBlocks.value.splice(idx, 1)).catch(() => {})
}
function moveBlock(idx, step) {
  const t = idx + step
  if (t < 0 || t >= exerciseBlocks.value.length) return
  const [m] = exerciseBlocks.value.splice(idx, 1)
  exerciseBlocks.value.splice(t, 0, m)
}
function onBlockExerciseChange(idx, newId) {
  const ex = exerciseOptions.value.find(e => e.id === newId)
  if (ex) {
    exerciseBlocks.value[idx].exerciseId   = ex.id
    exerciseBlocks.value[idx].exerciseName = ex.name
  }
}

// ===== 组操作 =====
function addSet(bIdx) {
  const block = exerciseBlocks.value[bIdx]
  const last  = block.sets[block.sets.length - 1]
  block.sets.push(makeSet(last ? {
    weightKg: last.weightKg,
    reps: last.reps,
    rpe: last.rpe,
    restSeconds: last.restSeconds
  } : {}))
}
function removeSet(bIdx, sIdx) {
  exerciseBlocks.value[bIdx].sets.splice(sIdx, 1)
}
function copyPrevSet(bIdx, sIdx) {
  if (sIdx === 0) return
  const prev = exerciseBlocks.value[bIdx].sets[sIdx - 1]
  const cur  = exerciseBlocks.value[bIdx].sets[sIdx]
  cur.weightKg    = prev.weightKg
  cur.reps        = prev.reps
  cur.rpe         = prev.rpe
  cur.restSeconds = prev.restSeconds
  ElMessage.success({ message: '已复制上一组数据', duration: 1200 })
}

// ===== 实时汇总 =====
function blockVolume(block) {
  return (block.sets || []).reduce((s, x) => s + (Number(x.weightKg) || 0) * (Number(x.reps) || 0), 0)
}
const liveVolume = computed(() =>
  exerciseBlocks.value.reduce((s, b) => s + blockVolume(b), 0)
)
const liveSets = computed(() =>
  exerciseBlocks.value.reduce((s, b) => s + (b.sets?.length || 0), 0)
)
const liveReps = computed(() =>
  exerciseBlocks.value.reduce((s, b) =>
    s + (b.sets || []).reduce((ss, x) => ss + (Number(x.reps) || 0), 0), 0)
)
const avgRpeLabel = computed(() => {
  const all = []
  for (const b of exerciseBlocks.value) {
    for (const s of b.sets || []) if (s.rpe) all.push(Number(s.rpe))
  }
  if (all.length === 0) return '—'
  const avg = all.reduce((s, x) => s + x, 0) / all.length
  if (avg < 5.5) return '轻松'
  if (avg < 7.5) return '中等'
  if (avg < 9)   return '吃力'
  return '极限'
})

// ===== 提交 =====
async function submit() {
  if (!form.recordDate) {
    ElMessage.warning('请选择训练日期')
    return
  }
  if (exerciseBlocks.value.length === 0) {
    ElMessage.warning('请至少添加 1 个动作')
    return
  }
  // 每个动作至少 1 组（由结构保证）；收集 sets
  const recordSets = []
  let setNoGlobal = 1
  for (const b of exerciseBlocks.value) {
    if (!b.exerciseId) { ElMessage.warning('请为每个动作选择具体动作'); return }
    for (let i = 0; i < b.sets.length; i++) {
      const s = b.sets[i]
      recordSets.push({
        exerciseId: b.exerciseId,
        setNo: i + 1,
        weightKg: Number(s.weightKg) || 0,
        reps: Number(s.reps) || 1,
        rpe: s.rpe ? Number(s.rpe) : null,
        restSeconds: Number(s.restSeconds) || 0
      })
      setNoGlobal++
    }
  }
  if (recordSets.length === 0) {
    ElMessage.warning('至少需要 1 组记录')
    return
  }

  saving.value = true
  try {
    await createRecord({
      planId: form.planId || null,
      recordDate: form.recordDate,
      durationSec: (Number(form.durationMin) || 0) * 60,
      note: form.note.trim(),
      sets: recordSets
    })
    ElMessage.success('训练记录已保存，继续加油！💪')
    router.push('/training/records')
  } catch (e) {
    const msgMap = { RECORD_SET_EMPTY: '至少包含 1 组训练数据' }
    ElMessage.error(msgMap[e?.message] || e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() { router.push('/training/records') }
</script>

<style scoped>
.rc-page { display: flex; flex-direction: column; gap: 16px; }

/* ===== 顶栏 ===== */
.rc-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  position: sticky;
  top: 56px;
  z-index: 5;
  backdrop-filter: blur(10px);
}
.rc-back {
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
.rc-back:hover { border-color: #22d3ee; color: #22d3ee; }
.rc-topbar__title {
  flex: 1;
  display: inline-flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}
.rc-topbar__title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}
.rc-topbar__title small { color: #ff8c69; font-size: 12px; }
.rc-topbar__actions { display: flex; gap: 8px; flex-shrink: 0; }

.rc-btn {
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
  transition: all 0.16s cubic-bezier(.2,.8,.2,1);
  text-decoration: none;
}
.rc-btn:hover { border-color: #22d3ee; color: #22d3ee; }
.rc-btn:disabled { opacity: 0.6; cursor: not-allowed; transform: none !important; }
.rc-btn--primary {
  background: linear-gradient(135deg, #2d1b69 0%, #5b3fc1 50%, #ff8c69 130%);
  border: none;
  color: #fff;
  box-shadow: 0 6px 18px -6px rgba(91, 63, 193, 0.55);
}
.rc-btn--primary:hover {
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 10px 24px -6px rgba(91, 63, 193, 0.7);
}
.rc-btn--ghost {
  background: transparent;
  border: 1px dashed var(--border);
  color: var(--text-muted);
}
.rc-btn--ghost:hover { border-color: #5b3fc1; color: #5b3fc1; border-style: solid; }
.rc-btn--block { width: 100%; justify-content: center; }
.is-loading { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ===== 主体 ===== */
.rc-body {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
  align-items: start;
}

/* ===== 侧栏 ===== */
.rc-sidecard {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 136px;
}
.rc-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 18px 20px;
}
.rc-card__title {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text);
  padding-left: 10px;
  border-left: 3px solid #5b3fc1;
  display: inline-flex;
  align-items: center;
}
.rc-plan-hint {
  margin-top: 8px;
  padding: 8px 12px;
  font-size: 12px;
  color: #0ea5b7;
  background: rgba(34, 211, 238, 0.08);
  border: 1px solid rgba(34, 211, 238, 0.22);
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  line-height: 1.5;
}

.rc-duration {
  display: inline-flex; align-items: center; gap: 8px;
  color: var(--text-muted);
  font-size: 13px;
}
.rc-duration .el-input-number { width: 130px; }

.rc-opt {
  display: flex; align-items: center; justify-content: space-between;
  gap: 8px;
  font-size: 13px;
  width: 100%;
}
.rc-opt strong { color: var(--text); font-weight: 600; }
.rc-opt span  { color: var(--text-muted); font-size: 11.5px; }
.rc-opt--ex { justify-content: flex-start; }
.rc-opt__dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.rc-opt__diff { margin-left: auto; letter-spacing: 1px; color: #d9d9d9; font-size: 11px; }
.rc-opt__diff i.on { color: #faad14; font-style: normal; }

/* ===== 容量预览 ===== */
.rc-sticky {
  background:
    linear-gradient(180deg, rgba(34, 211, 238, 0.04), transparent 60%),
    var(--card);
  border: 1px solid var(--border);
}
.rc-preview { margin-bottom: 14px; }
.rc-preview__item {
  display: flex; flex-direction: column; gap: 5px;
  padding: 12px 12px 14px;
  background: var(--bg-soft);
  border-radius: 12px;
  position: relative;
}
.rc-preview__item--v {
  padding-bottom: 22px;
  margin-bottom: 10px;
  background: linear-gradient(135deg, rgba(91, 63, 193, 0.12), rgba(34, 211, 238, 0.06));
  border: 1px solid rgba(91, 63, 193, 0.2);
}
.rc-preview__item label {
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.rc-preview__item strong {
  font-size: 24px;
  font-weight: 800;
  line-height: 1.1;
  color: var(--text);
  letter-spacing: -0.02em;
}
.rc-preview__item small { font-size: 12px; font-weight: 500; margin-left: 2px; color: var(--text-muted); }
.rc-preview__item--v strong {
  background: linear-gradient(135deg, #22d3ee, #5b3fc1);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.rc-preview__item--s strong { color: #ff8c69; }
.rc-preview__item--r strong { color: #22d3ee; }
.rc-preview__item--e strong { color: #52c41a; }
.rc-preview__item--d strong { color: #faad14; }

.rc-preview__bar {
  position: absolute;
  left: 12px; right: 12px; bottom: 10px;
  height: 5px;
  border-radius: 999px;
  background: rgba(255,255,255,0.06);
  overflow: hidden;
}
.rc-preview__fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.25s ease;
}
.rc-preview__fill--v {
  background: linear-gradient(90deg, #22d3ee, #5b3fc1);
  box-shadow: 0 0 10px rgba(34, 211, 238, 0.5);
}

.rc-preview__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 10px;
}

.rc-actions-bottom {
  display: flex; flex-direction: column; gap: 10px;
  padding-top: 8px;
  border-top: 1px dashed var(--border);
}

/* ===== 主区 ===== */
.rc-main { min-width: 0; }
.rc-empty-box {
  text-align: center;
  padding: 70px 40px;
  background: var(--card);
  border: 1px dashed var(--border);
  border-radius: 14px;
  color: var(--text-muted);
}
.rc-empty-box__icon {
  color: #c4b5fd;
  margin-bottom: 14px;
  opacity: 0.85;
}
.rc-empty-box h4 {
  margin: 0 0 6px;
  font-size: 17px;
  color: var(--text);
  font-weight: 600;
}
.rc-empty-box p { margin: 0 0 18px; font-size: 13px; }

/* ===== 动作块 ===== */
.rc-blocks { display: flex; flex-direction: column; gap: 16px; }

.rc-block {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 16px;
  overflow: hidden;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.rc-block:hover {
  border-color: rgba(91, 63, 193, 0.35);
  box-shadow: 0 10px 24px -12px rgba(91, 63, 193, 0.25);
}

.rc-block__head {
  display: grid;
  grid-template-columns: 70px 1fr auto;
  gap: 14px;
  align-items: center;
  padding: 14px 18px;
  background:
    linear-gradient(90deg, rgba(91, 63, 193, 0.1), rgba(34, 211, 238, 0.04) 60%, transparent 100%),
    var(--bg-soft);
  border-bottom: 1px solid var(--border);
}
.rc-block__no {
  width: 54px; height: 54px;
  border-radius: 14px;
  background: linear-gradient(135deg, #2d1b69 0%, #5b3fc1 100%);
  color: #fff;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  box-shadow: 0 8px 18px -6px rgba(91, 63, 193, 0.6);
}
.rc-block__no span {
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
  background: linear-gradient(135deg, #fff, #6ef3ff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.rc-block__no small {
  margin-top: 2px;
  font-size: 9px;
  letter-spacing: 0.1em;
  opacity: 0.8;
}

.rc-block__actions {
  display: flex; gap: 4px;
}
.rc-ghost-btn {
  width: 32px; height: 32px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.rc-ghost-btn:hover:not(:disabled) {
  background: var(--bg-soft);
  border-color: var(--border);
  color: #22d3ee;
}
.rc-ghost-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.rc-ghost-btn--danger:hover:not(:disabled) { color: #ff6b6b; border-color: rgba(255,107,107,0.3); }

.rc-block__body { padding: 10px 4px 4px; overflow: auto; }

.rc-sets-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.rc-sets-table th,
.rc-sets-table td {
  padding: 8px 14px;
  vertical-align: middle;
  border-bottom: 1px dashed var(--border);
  text-align: center;
}
.rc-sets-table th {
  font-size: 11.5px;
  color: var(--text-muted);
  font-weight: 600;
  letter-spacing: 0.05em;
  background: transparent;
}
.rc-sets-table tbody tr:last-child td { border-bottom: none; }
.rc-sets-table td.is-volume {
  font-weight: 700;
  color: #ff8c69;
  font-size: 14px;
}
.rc-sets-table td.is-setno { width: 64px; }
.rc-setno {
  display: inline-block;
  width: 26px; height: 26px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.18), rgba(34, 211, 238, 0.05));
  color: #0ea5b7;
  font-weight: 700;
  font-size: 12px;
  line-height: 26px;
}

.rc-setops {
  display: inline-flex;
  flex-direction: column;
  gap: 3px;
  min-width: 92px;
}
.rc-mini-btn {
  padding: 3px 8px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-soft);
  font-size: 11.5px;
  cursor: pointer;
  transition: all 0.14s;
}
.rc-mini-btn:hover:not(:disabled) { border-color: #22d3ee; color: #22d3ee; }
.rc-mini-btn--danger:hover:not(:disabled) { border-color: #ff6b6b; color: #ff6b6b; }
.rc-mini-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.rc-block__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 18px 14px;
  gap: 10px;
  flex-wrap: wrap;
}
.rc-block__summary {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  padding: 5px 12px;
  background: var(--bg-soft);
  border-radius: 999px;
}
.rc-block__summary span:nth-child(odd) { color: var(--text-soft); font-weight: 600; }

/* ===== 过渡 ===== */
.list-enter-active, .list-leave-active { transition: all 0.25s ease; }
.list-enter-from { opacity: 0; transform: translateY(-8px); }
.list-leave-to   { opacity: 0; transform: translateY(8px); }

/* ===== 响应式 ===== */
@media (max-width: 1080px) {
  .rc-body { grid-template-columns: 1fr; }
  .rc-sidecard {
    position: static;
  }
  .rc-topbar__actions { display: none; }
}
</style>
