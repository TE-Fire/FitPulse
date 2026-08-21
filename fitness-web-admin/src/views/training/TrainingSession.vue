<template>
  <div class="ts-page">
    <!-- 顶栏 -->
    <div class="ts-topbar">
      <button class="ts-back" @click="onBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <div class="ts-topbar__title">
        <h2>{{ plan?.name || '训练中' }}</h2>
        <span v-if="plan?.description" class="ts-topbar__desc">{{ plan.description }}</span>
      </div>
      <div class="ts-topbar__actions">
        <button class="ts-btn ts-btn--ghost" @click="showCancelDialog = true">
          <el-icon><CloseBold /></el-icon>
          <span>放弃</span>
        </button>
        <button class="ts-btn ts-btn--primary" :disabled="completing" @click="onFinish">
          <el-icon v-if="!completing"><Check /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <span>{{ completing ? '提交中…' : '结束训练' }}</span>
        </button>
      </div>
    </div>

    <!-- 计时器区域 -->
    <div class="ts-timer-card">
      <div class="ts-timer" :class="{ 'is-paused': paused }">
        <span class="ts-timer__minutes">{{ formattedTime.minutes }}</span>
        <span class="ts-timer__sep">:</span>
        <span class="ts-timer__seconds">{{ formattedTime.seconds }}</span>
        <span class="ts-timer__ms">.{{ formattedTime.ms }}</span>
      </div>
      <div class="ts-timer__controls">
        <button
          class="ts-ctrl-btn"
          :class="{ 'is-paused': paused }"
          @click="togglePause"
        >
          <el-icon v-if="paused"><VideoPlay /></el-icon>
          <el-icon v-else><VideoPause /></el-icon>
          <span>{{ paused ? '继续' : '暂停' }}</span>
        </button>
      </div>
    </div>

    <!-- 实时汇总 -->
    <div class="ts-summary">
      <div class="ts-summary__item">
        <span class="ts-summary__value">{{ totalVolume.toFixed(0) }}</span>
        <span class="ts-summary__label">训练容量 (kg)</span>
      </div>
      <div class="ts-summary__divider"></div>
      <div class="ts-summary__item">
        <span class="ts-summary__value">{{ totalSets }}</span>
        <span class="ts-summary__label">已完成组数</span>
      </div>
      <div class="ts-summary__divider"></div>
      <div class="ts-summary__item">
        <span class="ts-summary__value">{{ totalReps }}</span>
        <span class="ts-summary__label">总次数</span>
      </div>
      <div class="ts-summary__divider"></div>
      <div class="ts-summary__item">
        <span class="ts-summary__value">{{ completedItemCount }}</span>
        <span class="ts-summary__label">完成动作</span>
      </div>
    </div>

    <!-- 动作列表 -->
    <div class="ts-body">
      <div v-if="loading" class="ts-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载计划数据…</span>
      </div>

      <template v-else>
        <TransitionGroup name="list">
          <div
            v-for="(item, itemIdx) in planItems"
            :key="item.id"
            class="ts-ex-card"
            :class="{ 'is-completed': isItemCompleted(item) }"
          >
            <div class="ts-ex-card__head">
              <div class="ts-ex-card__info">
                <span class="ts-ex-card__num">{{ itemIdx + 1 }}</span>
                <span class="ts-ex-card__name">{{ item.exerciseName }}</span>
                <span class="ts-ex-card__target">目标 {{ item.targetSets }}×{{ item.targetReps }}</span>
              </div>
              <div class="ts-ex-card__toggle" @click="toggleItem(item)">
                <el-icon><component :is="expandedItems.has(item.id) ? 'ArrowDown' : 'ArrowRight'" /></el-icon>
              </div>
            </div>

            <div v-if="expandedItems.has(item.id)" class="ts-ex-card__body">
              <div class="ts-sets-table">
                <div class="ts-sets-table__head">
                  <span>#</span>
                  <span>重量 (kg)</span>
                  <span>次数</span>
                  <span>RPE</span>
                  <span>休息 (秒)</span>
                  <span>状态</span>
                </div>
                <div
                  v-for="(set, setIdx) in getSets(item)"
                  :key="setIdx"
                  class="ts-sets-row"
                  :class="{ 'is-done': set.done }"
                >
                  <span class="ts-sets-row__num">{{ setIdx + 1 }}</span>
                  <el-input-number
                    v-model="set.weightKg"
                    :precision="1"
                    :step="2.5"
                    :min="0"
                    :max="500"
                    size="small"
                    controls-position="right"
                  />
                  <el-input-number
                    v-model="set.reps"
                    :min="1"
                    :max="99"
                    size="small"
                    controls-position="right"
                  />
                  <el-select
                    v-model="set.rpe"
                    placeholder="-"
                    size="small"
                    class="ts-rpe-select"
                  >
                    <el-option
                      v-for="r in RPE_OPTIONS"
                      :key="r"
                      :label="r"
                      :value="r"
                    />
                  </el-select>
                  <el-input-number
                    v-model="set.restSeconds"
                    :min="0"
                    :max="600"
                    size="small"
                    controls-position="right"
                  />
                  <div class="ts-sets-row__action">
                    <button
                      v-if="!set.done"
                      class="ts-done-btn"
                      @click="markSetDone(item, set)"
                    >
                      <el-icon><Check /></el-icon>
                    </button>
                    <el-icon v-else class="ts-done-icon"><CheckFilled /></el-icon>
                  </div>
                </div>
              </div>

              <div class="ts-ex-card__footer">
                <button class="ts-add-set-btn" @click="addSet(item)">
                  <el-icon><Plus /></el-icon>
                  <span>加一组</span>
                </button>
                <span class="ts-ex-card__progress">
                  完成 {{ completedSets(item) }} / {{ item.targetSets }} 组
                </span>
              </div>
            </div>
          </div>
        </TransitionGroup>

        <!-- 添加自由动作 -->
        <div class="ts-free-add">
          <button class="ts-free-add-btn" @click="showAddExercise = true">
            <el-icon><Plus /></el-icon>
            <span>添加自由动作</span>
          </button>
        </div>

        <!-- 训练备注 -->
        <div class="ts-note-card">
          <div class="ts-note-card__head">
            <el-icon><EditPen /></el-icon>
            <span>训练备注</span>
          </div>
          <el-input
            v-model="note"
            type="textarea"
            :rows="3"
            placeholder="记录训练状态、身体感受、进步心得…"
            maxlength="400"
            show-word-limit
          />
        </div>
      </template>
    </div>

    <!-- 添加自由动作 Dialog -->
    <el-dialog
      v-model="showAddExercise"
      title="添加自由动作"
      width="520px"
      destroy-on-close
    >
      <div class="ts-dialog-search">
        <el-input
          v-model="searchKw"
          placeholder="搜索动作…"
          clearable
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div class="ts-dialog-list">
        <div
          v-for="ex in filteredExercises"
          :key="ex.id"
          class="ts-ex-option"
          @click="selectFreeExercise(ex)"
        >
          <span
            class="ts-ex-option__cat"
            :style="{ background: catColor(ex.category) + '18', color: catColor(ex.category) }"
          >{{ catLabel(ex.category) }}</span>
          <span class="ts-ex-option__name">{{ ex.name }}</span>
          <span class="ts-ex-option__diff">
            <span
              v-for="n in 3"
              :key="n"
              :class="{ 'is-on': n <= ex.difficulty }"
            >★</span>
          </span>
        </div>
        <div v-if="filteredExercises.length === 0" class="ts-empty">
          没有符合条件的动作
        </div>
      </div>
    </el-dialog>

    <!-- 结束训练确认 Dialog -->
    <el-dialog
      v-model="showFinishDialog"
      title="确认结束训练"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="ts-confirm">
        <div class="ts-confirm__timer">
          <span>训练时长</span>
          <strong>{{ formatDuration(totalSeconds) }}</strong>
        </div>
        <div v-if="totalSeconds < 300" class="ts-confirm__warn">
          <el-icon><Warning /></el-icon>
          <span>训练时长不足 5 分钟，不计入记录</span>
        </div>
        <div class="ts-confirm__stats">
          <div class="ts-confirm__stat">
            <strong>{{ totalVolume.toFixed(0) }}</strong>
            <span>容量 kg</span>
          </div>
          <div class="ts-confirm__stat">
            <strong>{{ totalSets }}</strong>
            <span>组数</span>
          </div>
          <div class="ts-confirm__stat">
            <strong>{{ totalReps }}</strong>
            <span>次数</span>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="ts-btn ts-btn--ghost" @click="showFinishDialog = false">继续训练</button>
        <button
          class="ts-btn ts-btn--primary"
          :disabled="completing || (totalSeconds < 300 && totalSets === 0)"
          @click="confirmFinish"
        >
          <span>{{ completing ? '提交中…' : '确认结束' }}</span>
        </button>
      </template>
    </el-dialog>

    <!-- 放弃训练确认 Dialog -->
    <el-dialog
      v-model="showCancelDialog"
      title="确认放弃训练"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="ts-cancel-warn">
        <el-icon><Warning /></el-icon>
        <p>放弃后本次训练将不生成记录，计划状态将回到草稿。</p>
      </div>
      <template #footer>
        <button class="ts-btn ts-btn--ghost" @click="showCancelDialog = false">继续训练</button>
        <button
          class="ts-btn ts-btn--danger"
          :disabled="cancelling"
          @click="confirmCancel"
        >
          <span>{{ cancelling ? '处理中…' : '确认放弃' }}</span>
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, CloseBold, Check, Loading, VideoPlay, VideoPause,
  Plus, Search, EditPen, Warning, CheckFilled
} from '@element-plus/icons-vue'
import {
  getPlanDetail, startPlan, completePlan, cancelPlan, getInProgressPlan,
  getAllExercises, CATEGORY_OPTIONS
} from '@/api/training'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const plan = ref(null)
const planItems = ref([])

// 计时器
const elapsedSec = ref(0)
const paused = ref(false)
let timerInterval = null

// 训练数据
const expandedItems = ref(new Set())
const showAddExercise = ref(false)
const showFinishDialog = ref(false)
const showCancelDialog = ref(false)
const completing = ref(false)
const cancelling = ref(false)
const note = ref('')

// 自由动作搜索
const searchKw = ref('')
const exercises = ref([])

// RPE 选项
const RPE_OPTIONS = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

// ========== 计时器相关 ==========
const totalSeconds = computed(() => elapsedSec.value)

const formattedTime = computed(() => {
  const total = Math.floor(totalSeconds.value)
  const minutes = String(Math.floor(total / 60)).padStart(2, '0')
  const seconds = String(total % 60).padStart(2, '0')
  const ms = String(Math.floor((Date.now() % 1000) / 100)).padStart(1, '0')
  return { minutes, seconds, ms }
})

function startTimer(startFrom = 0) {
  elapsedSec.value = startFrom
  timerInterval = setInterval(() => {
    if (!paused.value) {
      elapsedSec.value++
    }
  }, 1000)
}

function stopTimer() {
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

function togglePause() {
  paused.value = !paused.value
}

function formatDuration(sec) {
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// ========== 训练数据相关 ==========
function getSets(item) {
  return item._sets || []
}

function initSets(item) {
  if (!item._sets) {
    item._sets = Array.from({ length: item.targetSets }, () => ({
      weightKg: 0,
      reps: item.targetReps,
      rpe: null,
      restSeconds: item.restSeconds || 60,
      done: false
    }))
  }
}

function addSet(item) {
  initSets(item)
  const lastSet = item._sets[item._sets.length - 1]
  const newSet = {
    weightKg: lastSet?.weightKg || 0,
    reps: lastSet?.reps || item.targetReps,
    rpe: lastSet?.rpe || null,
    restSeconds: lastSet?.restSeconds || 60,
    done: false
  }
  item._sets.push(newSet)
}

function markSetDone(item, set) {
  if (set.weightKg <= 0 || set.reps <= 0) {
    ElMessage.warning('请先填写重量和次数')
    return
  }
  set.done = true
  // 自动展开下一组的重量/次数
  setTimeout(() => {
    const idx = item._sets.indexOf(set)
    const nextSet = item._sets[idx + 1]
    if (nextSet && nextSet.weightKg === 0) {
      nextSet.weightKg = set.weightKg
      nextSet.reps = set.reps
      nextSet.rpe = set.rpe
    }
  }, 100)
}

function completedSets(item) {
  initSets(item)
  return item._sets.filter(s => s.done).length
}

function isItemCompleted(item) {
  initSets(item)
  const done = item._sets.filter(s => s.done).length
  return done >= item.targetSets
}

function toggleItem(item) {
  initSets(item)
  if (expandedItems.value.has(item.id)) {
    expandedItems.value.delete(item.id)
  } else {
    expandedItems.value.add(item.id)
  }
}

// ========== 汇总计算 ==========
const totalVolume = computed(() => {
  return planItems.value.reduce((sum, item) => {
    initSets(item)
    return sum + item._sets.reduce((s, set) => s + (set.done ? (Number(set.weightKg) || 0) * (Number(set.reps) || 0) : 0), 0)
  }, 0)
})

const totalSets = computed(() => {
  return planItems.value.reduce((sum, item) => {
    initSets(item)
    return sum + item._sets.filter(s => s.done).length
  }, 0)
})

const totalReps = computed(() => {
  return planItems.value.reduce((sum, item) => {
    initSets(item)
    return sum + item._sets.filter(s => s.done).reduce((s, set) => s + (Number(set.reps) || 0), 0)
  }, 0)
})

const completedItemCount = computed(() => {
  return planItems.value.filter(isItemCompleted).length
})

// ========== 添加自由动作 ==========
const filteredExercises = computed(() => {
  const kw = searchKw.value.toLowerCase()
  if (!kw) return exercises.value
  return exercises.value.filter(ex => ex.name.toLowerCase().includes(kw))
})

function catColor(cat) {
  const opt = CATEGORY_OPTIONS.find(c => c.value === cat)
  return opt?.color || '#666'
}

function catLabel(cat) {
  const opt = CATEGORY_OPTIONS.find(c => c.value === cat)
  return opt?.label || cat
}

async function selectFreeExercise(ex) {
  const newItem = {
    id: 'free_' + Date.now(),
    exerciseId: ex.id,
    exerciseName: ex.name,
    targetSets: 1,
    targetReps: 10,
    restSeconds: 60,
    isFree: true
  }
  planItems.value.push(newItem)
  expandedItems.value.add(newItem.id)
  showAddExercise.value = false
  searchKw.value = ''
  ElMessage.success(`已添加「${ex.name}」`)
}

// ========== 生命周期 ==========
async function loadPlan() {
  const planId = route.params.id
  if (!planId) {
    // 尝试恢复进行中的训练
    try {
      const res = await getInProgressPlan()
      if (res.hasActivePlan) {
        router.replace(`/training/session/${res.planId}`)
        return
      }
    } catch {
      // ignore
    }
    ElMessage.error('未找到进行中的训练')
    router.replace('/training/plans')
    return
  }

  loading.value = true
  try {
    // 调用 start 接口开始训练
    await startPlan(planId)
    const detail = await getPlanDetail(planId)
    plan.value = detail
    planItems.value = (detail.items || []).map(it => ({ ...it }))
    // 初始化展开第一个动作
    if (planItems.value.length > 0) {
      expandedItems.value.add(planItems.value[0].id)
    }
    // 加载自由动作选项
    exercises.value = await getAllExercises()
    // 启动计时器
    startTimer()
  } catch (err) {
    console.error('加载计划失败:', err)
    if (err.message?.includes('ALREADY_IN_PROGRESS')) {
      ElMessage.info('计划已在进行中，继续训练')
      const inProgress = await getInProgressPlan()
      if (inProgress.hasActivePlan) {
        startTimer(inProgress.elapsedSec || 0)
      }
    } else {
      ElMessage.error('加载计划失败')
      router.replace('/training/plans')
    }
  } finally {
    loading.value = false
  }
}

function onBack() {
  ElMessageBox.confirm(
    '训练进行中，确定离开吗？建议使用"放弃"或"结束训练"按钮。',
    '提示',
    {
      confirmButtonText: '强制离开',
      cancelButtonText: '继续训练',
      type: 'warning'
    }
  ).then(() => {
    router.push('/training/plans')
  }).catch(() => {
    // stay
  })
}

function onFinish() {
  showFinishDialog.value = true
}

async function confirmFinish() {
  if (totalSeconds.value < 300) {
    ElMessage.warning('训练时长不足5分钟，不计入记录')
    showFinishDialog.value = false
    return
  }

  // 收集实际完成的组
  const actualSets = []
  planItems.value.forEach(item => {
    initSets(item)
    item._sets.forEach((set, idx) => {
      if (set.done && set.weightKg > 0 && set.reps > 0) {
        actualSets.push({
          exerciseId: item.exerciseId,
          setNo: idx + 1,
          weightKg: Number(set.weightKg),
          reps: Number(set.reps),
          rpe: set.rpe || null,
          restSeconds: set.restSeconds || 0
        })
      }
    })
  })

  if (actualSets.length === 0) {
    ElMessage.warning('请至少完成一组训练')
    return
  }

  completing.value = true
  try {
    const planId = route.params.id
    const result = await completePlan(planId, {
      durationSec: totalSeconds.value,
      note: note.value,
      actualSets
    })
    ElMessage.success(`训练完成！容量 ${result.totalVolume}kg，记录已生成`)
    stopTimer()
    router.push('/training/records')
  } catch (err) {
    console.error('完成训练失败:', err)
    if (err.message?.includes('DURATION_TOO_SHORT')) {
      ElMessage.error('训练时长不足5分钟')
    } else if (err.message?.includes('NO_SETS')) {
      ElMessage.error('请至少完成一组训练')
    } else {
      ElMessage.error('完成训练失败')
    }
  } finally {
    completing.value = false
    showFinishDialog.value = false
  }
}

async function confirmCancel() {
  cancelling.value = true
  try {
    const planId = route.params.id
    await cancelPlan(planId)
    ElMessage.info('已放弃训练，计划回到草稿状态')
    stopTimer()
    router.push('/training/plans')
  } catch (err) {
    console.error('放弃训练失败:', err)
    ElMessage.error('放弃训练失败')
  } finally {
    cancelling.value = false
    showCancelDialog.value = false
  }
}

onMounted(() => {
  loadPlan()
})

onUnmounted(() => {
  stopTimer()
})
</script>

<style scoped>
.ts-page {
  max-width: 880px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

/* ============ 顶栏 ============ */
.ts-topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 24px;
}

.ts-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: transparent;
  border: 1px solid #ddd;
  border-radius: 10px;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-back:hover {
  background: #f5f5f5;
  color: #333;
}

.ts-topbar__title {
  flex: 1;
  min-width: 0;
}
.ts-topbar__title h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ts-topbar__desc {
  font-size: 12px;
  color: #999;
}

.ts-topbar__actions {
  display: flex;
  gap: 10px;
}

.ts-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-btn--ghost {
  background: #f5f5f5;
  color: #666;
}
.ts-btn--ghost:hover {
  background: #eee;
  color: #333;
}
.ts-btn--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.ts-btn--primary:hover:not(:disabled) {
  opacity: 0.9;
}
.ts-btn--primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.ts-btn--danger {
  background: #ff4d4f;
  color: #fff;
}
.ts-btn--danger:hover:not(:disabled) {
  opacity: 0.9;
}

/* ============ 计时器 ============ */
.ts-timer-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 32px 24px;
  margin-bottom: 24px;
  text-align: center;
  color: #fff;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.ts-timer {
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  font-size: 56px;
  font-weight: 700;
  letter-spacing: 4px;
  line-height: 1;
  margin-bottom: 16px;
  color: #fff;
}
.ts-timer.is-paused {
  opacity: 0.7;
}
.ts-timer__ms {
  font-size: 28px;
  color: rgba(255, 255, 255, 0.7);
  margin-left: 4px;
}

.ts-timer__controls {
  display: flex;
  justify-content: center;
}

.ts-ctrl-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 28px;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-radius: 30px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-ctrl-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}
.ts-ctrl-btn.is-paused {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

/* ============ 汇总 ============ */
.ts-summary {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.ts-summary__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.ts-summary__value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.ts-summary__label {
  font-size: 12px;
  color: #999;
}

.ts-summary__divider {
  width: 1px;
  height: 32px;
  background: #eee;
}

/* ============ 动作卡片 ============ */
.ts-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ts-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}

.ts-ex-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.2s;
}
.ts-ex-card.is-completed {
  border-color: #52c41a;
}

.ts-ex-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.ts-ex-card__info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ts-ex-card__num {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.ts-ex-card__name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.ts-ex-card__target {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 3px 8px;
  border-radius: 6px;
}

.ts-ex-card__toggle {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-ex-card__toggle:hover {
  background: #f5f5f5;
  color: #666;
}

.ts-ex-card__body {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* ============ 组数表格 ============ */
.ts-sets-table {
  overflow-x: auto;
}

.ts-sets-table__head,
.ts-sets-row {
  display: grid;
  grid-template-columns: 30px 1fr 1fr 60px 1fr 40px;
  gap: 8px;
  align-items: center;
  padding: 8px 4px;
}

.ts-sets-table__head {
  font-size: 12px;
  color: #999;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

.ts-sets-row {
  font-size: 13px;
  border-radius: 8px;
  transition: all 0.2s;
}
.ts-sets-row.is-done {
  background: #f6ffed;
}

.ts-sets-row__num {
  font-weight: 600;
  color: #666;
  text-align: center;
}

.ts-sets-row__action {
  display: flex;
  align-items: center;
  justify-content: center;
}

.ts-done-btn {
  width: 32px;
  height: 32px;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.ts-done-btn:hover {
  background: #52c41a;
  color: #fff;
}

.ts-done-icon {
  font-size: 20px;
  color: #52c41a;
}

.ts-rpe-select {
  width: 100%;
}

/* ============ 底部 ============ */
.ts-ex-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #f0f0f0;
}

.ts-add-set-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #f5f7fa;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-add-set-btn:hover {
  background: #e6f7ff;
  border-color: #667eea;
  color: #667eea;
}

.ts-ex-card__progress {
  font-size: 12px;
  color: #999;
}

/* ============ 添加动作 ============ */
.ts-free-add {
  display: flex;
  justify-content: center;
}

.ts-free-add-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: #f5f7fa;
  border: 1px dashed #d9d9d9;
  border-radius: 12px;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-free-add-btn:hover {
  background: #e6f7ff;
  border-color: #667eea;
  color: #667eea;
}

/* ============ 备注 ============ */
.ts-note-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.ts-note-card__head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* ============ Dialog ============ */
.ts-dialog-search {
  margin-bottom: 16px;
}

.ts-dialog-list {
  max-height: 360px;
  overflow-y: auto;
}

.ts-ex-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.ts-ex-option:hover {
  background: #f5f7fa;
}

.ts-ex-option__cat {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
}

.ts-ex-option__name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.ts-ex-option__diff {
  color: #ddd;
  font-size: 12px;
}
.ts-ex-option__diff .is-on {
  color: #faad14;
}

.ts-empty {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

/* ============ 确认 Dialog ============ */
.ts-confirm {
  text-align: center;
}

.ts-confirm__timer {
  margin-bottom: 20px;
}
.ts-confirm__timer span {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.ts-confirm__timer strong {
  font-size: 36px;
  font-weight: 700;
  font-family: 'SF Mono', monospace;
  color: #667eea;
}

.ts-confirm__warn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  color: #ff4d4f;
  font-size: 13px;
  margin-bottom: 20px;
}

.ts-confirm__stats {
  display: flex;
  justify-content: space-around;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.ts-confirm__stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.ts-confirm__stat strong {
  font-size: 20px;
  font-weight: 700;
  color: #333;
}
.ts-confirm__stat span {
  font-size: 11px;
  color: #999;
}

.ts-cancel-warn {
  text-align: center;
  padding: 16px;
}
.ts-cancel-warn .el-icon {
  font-size: 36px;
  color: #faad14;
  margin-bottom: 12px;
}
.ts-cancel-warn p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

/* ============ List Transition ============ */
.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>
