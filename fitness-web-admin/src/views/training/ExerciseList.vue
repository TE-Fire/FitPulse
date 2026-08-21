<template>
  <div class="ex-page">
    <!-- 顶部统计条 -->
    <div class="ex-hero">
      <div class="ex-hero__bg"></div>
      <div class="ex-hero__content">
        <div class="ex-hero__title">
          <h2>动作库管理</h2>
          <p>精选系统动作 · 支持自定义扩展 · 科学分类管理</p>
        </div>
        <div class="ex-hero__stats">
          <div class="ex-stat">
            <div class="ex-stat__num">{{ totalCount }}</div>
            <div class="ex-stat__label">动作总数</div>
          </div>
          <div class="ex-stat">
            <div class="ex-stat__num ex-stat__num--cyan">{{ systemCount }}</div>
            <div class="ex-stat__label">系统预置</div>
          </div>
          <div class="ex-stat">
            <div class="ex-stat__num ex-stat__num--orange">{{ customCount }}</div>
            <div class="ex-stat__label">自定义</div>
          </div>
          <div class="ex-stat">
            <div class="ex-stat__num ex-stat__num--purple">{{ categoryUsed }}</div>
            <div class="ex-stat__label">分类覆盖</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="ex-toolbar">
      <div class="ex-toolbar__filters">
        <el-input
          v-model="query.name"
          placeholder="搜索动作名称…"
          clearable
          size="default"
          style="width: 220px"
          @keyup.enter="fetchList(1)"
          @clear="fetchList(1)"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <el-select
          v-model="query.category"
          placeholder="全部分类"
          clearable
          size="default"
          style="width: 140px"
          @change="fetchList(1)"
        >
          <el-option
            v-for="c in CATEGORY_OPTIONS"
            :key="c.value"
            :label="c.label"
            :value="c.value"
          >
            <div class="ex-opt">
              <span class="ex-opt__dot" :style="{ background: c.color }"></span>
              {{ c.label }}
            </div>
          </el-option>
        </el-select>

        <el-select
          v-model="query.difficulty"
          placeholder="全部难度"
          clearable
          size="default"
          style="width: 120px"
          @change="fetchList(1)"
        >
          <el-option
            v-for="d in DIFFICULTY_OPTIONS"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          >
            <div class="ex-opt">
              <el-tag :color="d.color" effect="dark" size="small" style="margin-right:6px">
                {{ '★'.repeat(d.value) }}
              </el-tag>
              {{ d.label }}
            </div>
          </el-option>
        </el-select>
      </div>

      <div class="ex-toolbar__actions">
        <button class="ex-btn ex-btn--primary" @click="openCreate">
          <el-icon><Plus /></el-icon>
          <span>新建动作</span>
        </button>
      </div>
    </div>

    <!-- 主表格 -->
    <div class="ex-card ex-table-wrap">
      <el-table
        :data="list"
        stripe
        style="width: 100%"
        row-key="id"
        empty-text="暂无动作数据"
      >
        <el-table-column type="index" label="#" width="56" align="center" />
        <el-table-column prop="name" label="动作名称" min-width="160">
          <template #default="{ row }">
            <div class="ex-ex-name">
              <div class="ex-ex-name__title">
                {{ row.name }}
                <el-tag v-if="row.isSystem" size="small" class="ex-tag--system">系统</el-tag>
              </div>
              <div v-if="row.description" class="ex-ex-name__desc">{{ row.description }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="110" align="center">
          <template #default="{ row }">
            <span
              class="ex-chip"
              :style="{ background: catColor(row.category) + '18', color: catColor(row.category) }"
            >
              {{ catLabel(row.category) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="110" align="center">
          <template #default="{ row }">
            <span class="ex-diff">
              <span
                v-for="n in 3"
                :key="n"
                class="ex-diff__star"
                :class="{ 'is-on': n <= row.difficulty }"
              >★</span>
              <small>{{ diffLabel(row.difficulty) }}</small>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="muscleGroup" label="目标肌群" min-width="160">
          <template #default="{ row }">
            <span v-if="row.muscleGroup" class="ex-muscle">{{ row.muscleGroup }}</span>
            <span v-else class="ex-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="equipment" label="器械" width="110">
          <template #default="{ row }">
            <span v-if="row.equipment">{{ equipLabel(row.equipment) }}</span>
            <span v-else class="ex-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <button class="ex-link-btn" @click="openDetail(row)">查看</button>
            <span class="ex-sep">·</span>
            <button class="ex-link-btn" @click="openEdit(row)">编辑</button>
            <span class="ex-sep">·</span>
            <el-popconfirm
              :disabled="row.isSystem"
              title="确认删除该动作？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <button
                  class="ex-link-btn ex-link-btn--danger"
                  :class="{ 'is-disabled': row.isSystem }"
                  :disabled="row.isSystem"
                  :title="row.isSystem ? '系统预置动作不可删除' : '删除'"
                >
                  删除
                </button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="ex-pagination">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="fetchList()"
          @size-change="fetchList(1)"
        />
      </div>
    </div>

    <!-- 新建/编辑 Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑动作' : '新建动作'"
      width="640px"
      destroy-on-close
      top="6vh"
      class="ex-dialog"
    >
      <!-- 顶部装饰条 -->
      <div class="ex-dialog__deco">
        <div class="ex-dialog__deco-bar"></div>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-position="top"
        class="ex-form"
      >
        <!-- 分组：基本信息 -->
        <div class="ex-form-section">
          <div class="ex-form-section__title">
            <span class="ex-form-section__bar"></span>
            <span class="ex-form-section__text">基本信息</span>
          </div>
          <div class="ex-form-section__body">
            <div class="ex-form-row">
              <el-form-item label="动作名称" prop="name" class="ex-form-item--full">
                <el-input
                  v-model="form.name"
                  placeholder="输入一个清晰的动作名称"
                  maxlength="50"
                  show-word-limit
                  class="ex-input"
                >
                  <template #prefix>
                    <el-icon class="ex-input__icon"><Edit /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            <div class="ex-form-row ex-form-row--2col">
              <el-form-item label="动作分类" prop="category">
                <el-select
                  v-model="form.category"
                  placeholder="选择动作分类"
                  style="width: 100%"
                  class="ex-select"
                >
                  <el-option
                    v-for="c in CATEGORY_OPTIONS"
                    :key="c.value"
                    :label="c.label"
                    :value="c.value"
                  >
                    <div class="ex-opt">
                      <span class="ex-opt__dot" :style="{ background: c.color }"></span>
                      {{ c.label }}
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="器械" prop="equipment">
                <el-select
                  v-model="form.equipment"
                  placeholder="选择器械（可选）"
                  clearable
                  style="width: 100%"
                  class="ex-select"
                >
                  <el-option
                    v-for="e in EQUIPMENT_OPTIONS"
                    :key="e.value"
                    :label="e.label"
                    :value="e.value"
                  />
                </el-select>
              </el-form-item>
            </div>
          </div>
        </div>

        <!-- 分组：难度与目标 -->
        <div class="ex-form-section">
          <div class="ex-form-section__title">
            <span class="ex-form-section__bar"></span>
            <span class="ex-form-section__text">难度与目标</span>
          </div>
          <div class="ex-form-section__body">
            <el-form-item label="难度等级" prop="difficulty">
              <div class="ex-difficulty-picker">
                <div
                  v-for="d in DIFFICULTY_OPTIONS"
                  :key="d.value"
                  class="ex-diff-option"
                  :class="{ 'is-active': form.difficulty === d.value }"
                  :style="{
                    '--diff-color': d.color,
                    '--diff-bg': d.color + '15'
                  }"
                  @click="form.difficulty = d.value"
                >
                  <span class="ex-diff-option__stars">
                    <span
                      v-for="i in 3"
                      :key="i"
                      class="ex-diff-option__star"
                      :class="{ 'is-on': i <= d.value }"
                    >★</span>
                  </span>
                  <span class="ex-diff-option__label">{{ d.label }}</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="目标肌群" prop="muscleGroup">
              <el-input
                v-model="form.muscleGroup"
                placeholder="例：胸大肌、肱三头肌、股四头肌"
                maxlength="80"
                class="ex-input"
              >
                <template #prefix>
                  <el-icon class="ex-input__icon"><Aim /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </div>
        </div>

        <!-- 分组：详细信息 -->
        <div class="ex-form-section">
          <div class="ex-form-section__title">
            <span class="ex-form-section__bar"></span>
            <span class="ex-form-section__text">详细信息</span>
          </div>
          <div class="ex-form-section__body">
            <el-form-item label="动作示意图 URL" prop="imageUrl">
              <el-input
                v-model="form.imageUrl"
                placeholder="https://...（可选）"
                class="ex-input"
              >
                <template #prefix>
                  <el-icon class="ex-input__icon"><Link /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="动作描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="4"
                placeholder="详细描述动作要领、发力感觉、注意事项等..."
                maxlength="400"
                show-word-limit
                class="ex-textarea"
              />
            </el-form-item>
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="ex-dialog__footer">
          <button class="ex-btn ex-btn--ghost" @click="dialogVisible = false">
            取消
          </button>
          <button class="ex-btn ex-btn--primary" @click="submitForm">
            <el-icon><Check /></el-icon>
            {{ isEdit ? '保存修改' : '创建动作' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情 Drawer -->
    <el-drawer
      v-model="detailVisible"
      title="动作详情"
      size="420px"
      direction="rtl"
      destroy-on-close
    >
      <template v-if="detail">
        <div class="ex-detail">
          <div class="ex-detail__header">
            <h3>{{ detail.name }}</h3>
            <div class="ex-detail__tags">
              <span
                class="ex-chip"
                :style="{ background: catColor(detail.category) + '18', color: catColor(detail.category) }"
              >{{ catLabel(detail.category) }}</span>
              <el-tag
                v-if="detail.isSystem"
                class="ex-tag--system"
              >系统预置</el-tag>
              <span class="ex-diff-sm">
                <span
                  v-for="n in 3"
                  :key="n"
                  class="ex-diff__star"
                  :class="{ 'is-on': n <= detail.difficulty }"
                >★</span>
                {{ diffLabel(detail.difficulty) }}
              </span>
            </div>
          </div>

          <div class="ex-detail__row">
            <div class="ex-detail__cell">
              <span class="ex-detail__k">目标肌群</span>
              <span class="ex-detail__v">{{ detail.muscleGroup || '—' }}</span>
            </div>
            <div class="ex-detail__cell">
              <span class="ex-detail__k">器械</span>
              <span class="ex-detail__v">{{ equipLabel(detail.equipment) || '—' }}</span>
            </div>
          </div>

          <div v-if="detail.description" class="ex-detail__desc">
            <h4>动作要领</h4>
            <p>{{ detail.description }}</p>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Edit, Aim, Link, Check
} from '@element-plus/icons-vue'
import {
  getExerciseList,
  getExerciseDetail,
  createExercise,
  updateExercise,
  deleteExercise,
  CATEGORY_OPTIONS,
  DIFFICULTY_OPTIONS,
  EQUIPMENT_OPTIONS
} from '@/api/training'

// ========== 查询 & 列表 ==========
const query = reactive({
  page: 1,
  size: 10,
  name: '',
  category: '',
  difficulty: ''
})

const list  = ref([])
const total = ref(0)

async function fetchList(resetPage) {
  if (resetPage) query.page = 1
  try {
    const res = await getExerciseList({ ...query })
    list.value  = res.records || []
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载失败')
  }
}

// 顶部统计（基于全量 mock 近似）
const totalCount  = computed(() => total.value)
const systemCount = computed(() => list.value.filter(x => x.isSystem).length)
const customCount = computed(() => list.value.filter(x => !x.isSystem).length)
const categoryUsed = computed(() => new Set(list.value.map(x => x.category)).size)

// ========== 枚举映射 ==========
function catLabel(v) { return CATEGORY_OPTIONS.find(x => x.value === v)?.label || v }
function catColor(v) { return CATEGORY_OPTIONS.find(x => x.value === v)?.color || '#999' }
function diffLabel(v){ return DIFFICULTY_OPTIONS.find(x => x.value === v)?.label || v }
function equipLabel(v){ return EQUIPMENT_OPTIONS.find(x => x.value === v)?.label || v }

// ========== 新建 / 编辑 Dialog ==========
const dialogVisible = ref(false)
const isEdit        = ref(false)
const formRef       = ref(null)
const form = reactive({
  id: '',
  name: '',
  category: '',
  difficulty: 1,
  muscleGroup: '',
  equipment: '',
  description: '',
  imageUrl: ''
})
const formRules = {
  name: [
    { required: true, message: '请输入动作名称', trigger: 'blur' },
    { min: 1, max: 50, message: '1-50 字符', trigger: 'blur' }
  ],
  category:   [{ required: true, message: '请选择分类', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

function resetForm() {
  form.id = ''
  form.name = ''
  form.category = ''
  form.difficulty = 1
  form.muscleGroup = ''
  form.equipment = ''
  form.description = ''
  form.imageUrl = ''
  formRef.value?.clearValidate()
}

function openCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row) {
  try {
    const d = await getExerciseDetail(row.id)
    isEdit.value = true
    Object.assign(form, {
      id: d.id,
      name: d.name,
      category: d.category,
      difficulty: d.difficulty,
      muscleGroup: d.muscleGroup || '',
      equipment: d.equipment || '',
      description: d.description || '',
      imageUrl: d.imageUrl || ''
    })
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

async function submitForm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch { return }

  const payload = {
    name: form.name.trim(),
    category: form.category,
    difficulty: Number(form.difficulty),
    muscleGroup: form.muscleGroup.trim(),
    equipment: form.equipment,
    description: form.description.trim(),
    imageUrl: form.imageUrl || null
  }

  try {
    if (isEdit.value) {
      await updateExercise(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createExercise(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e) {
    const msgMap = {
      EXERCISE_NAME_DUPLICATED: '动作名称已存在',
      EXERCISE_NOT_FOUND: '动作不存在'
    }
    ElMessage.error(msgMap[e?.message] || e?.message || '操作失败')
  }
}

// ========== 删除 ==========
async function handleDelete(row) {
  try {
    await deleteExercise(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (e) {
    const msgMap = {
      EXERCISE_NOT_FOUND: '动作不存在',
      EXERCISE_SYSTEM_CANNOT_DELETE: '系统预置动作不可删除',
      EXERCISE_IN_USE: '动作已被训练计划或记录引用，不可删除'
    }
    ElMessage.error(msgMap[e?.message] || e?.message || '删除失败')
  }
}

// ========== 详情 Drawer ==========
const detailVisible = ref(false)
const detail = ref(null)
async function openDetail(row) {
  try {
    detail.value = await getExerciseDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    ElMessage.error(e?.message || '获取详情失败')
  }
}

onMounted(fetchList)
</script>

<style scoped>
/* ===== Athletic Precision 主题：深海蓝 + 电光青绿 + 暖橙 ===== */
.ex-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* —— 顶部 Hero —— */
.ex-hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  padding: 28px 32px;
  color: #fff;
  isolation: isolate;
  box-shadow: 0 10px 30px -10px rgba(15, 52, 96, 0.25);
}
.ex-hero__bg {
  position: absolute; inset: 0; z-index: -1;
  background:
    radial-gradient(ellipse at 85% 20%, rgba(34, 211, 238, 0.35) 0%, transparent 55%),
    radial-gradient(ellipse at 10% 90%, rgba(255, 140, 105, 0.25) 0%, transparent 50%),
    linear-gradient(135deg, #0a2540 0%, #0e3a5c 45%, #0d4e6e 100%);
}
.ex-hero__bg::after {
  content: '';
  position: absolute; inset: 0;
  background-image:
    repeating-linear-gradient(120deg, rgba(255,255,255,0.04) 0 1px, transparent 1px 28px),
    repeating-linear-gradient(60deg,  rgba(255,255,255,0.03) 0 1px, transparent 1px 28px);
  mix-blend-mode: overlay;
}
.ex-hero__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  flex-wrap: wrap;
}
.ex-hero__title h2 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.01em;
}
.ex-hero__title p {
  margin: 0;
  font-size: 13px;
  opacity: 0.78;
  letter-spacing: 0.02em;
}
.ex-hero__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(100px, 1fr));
  gap: 12px;
}
.ex-stat {
  background: rgba(255,255,255,0.08);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.12);
  padding: 12px 18px;
  border-radius: 12px;
  text-align: center;
  min-width: 100px;
}
.ex-stat__num {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 4px;
}
.ex-stat__num--cyan   { color: #22d3ee; text-shadow: 0 0 18px rgba(34,211,238,0.5); }
.ex-stat__num--orange { color: #ff8c69; text-shadow: 0 0 18px rgba(255,140,105,0.5); }
.ex-stat__num--purple { color: #c4b5fd; text-shadow: 0 0 18px rgba(196,181,253,0.45); }
.ex-stat__label {
  font-size: 12px;
  opacity: 0.78;
  letter-spacing: 0.05em;
}

/* —— 筛选栏 —— */
.ex-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 18px;
}
.ex-toolbar__filters { display: flex; gap: 10px; flex-wrap: wrap; }
.ex-toolbar__actions { display: flex; gap: 10px; }

/* 通用按钮 */
.ex-btn {
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
}
.ex-btn:hover { border-color: var(--fit-brand, #7c5cff); color: var(--fit-brand, #7c5cff); }
.ex-btn--primary {
  background: linear-gradient(135deg, #0d4e6e 0%, #22d3ee 110%);
  border: none;
  color: #fff;
  box-shadow: 0 6px 18px -6px rgba(34,211,238,0.55);
}
.ex-btn--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px -6px rgba(34,211,238,0.7);
  color: #fff;
}
.ex-btn--primary:active { transform: translateY(0); }

/* —— 主卡片 & 表格 —— */
.ex-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
  padding: 14px 18px 10px;
}
.ex-table-wrap :deep(.el-table) {
  --el-table-border-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-row-hover-bg-color: var(--bg-soft);
}
.ex-table-wrap :deep(.el-table th.el-table__cell) {
  color: var(--text-muted);
  font-weight: 600;
  font-size: 12.5px;
  background: transparent;
  border-bottom: 1px solid var(--border);
}
.ex-table-wrap :deep(.el-table td.el-table__cell) {
  border-bottom: 1px dashed var(--border);
  font-size: 13.5px;
}
.ex-table-wrap :deep(.el-table tr:last-child td) { border-bottom: none; }

.ex-ex-name__title {
  display: flex; align-items: center; gap: 8px;
  font-weight: 600; color: var(--text);
}
.ex-ex-name__desc {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.ex-tag--system {
  background: linear-gradient(135deg, rgba(196,181,253,0.2), rgba(124,92,255,0.12));
  border: 1px solid rgba(124,92,255,0.35);
  color: #7c5cff;
  font-size: 10.5px;
  padding: 1px 7px;
  border-radius: 5px;
  height: 18px;
  line-height: 16px;
  font-weight: 600;
}

.ex-chip {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.ex-diff {
  display: inline-flex; flex-direction: column; align-items: center; gap: 2px;
}
.ex-diff__star {
  color: #d9d9d9;
  font-size: 12px;
  letter-spacing: 1px;
}
.ex-diff__star.is-on { color: #faad14; }
.ex-diff small { font-size: 11px; color: var(--text-muted); }

.ex-diff-sm {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--text-muted);
}
.ex-diff-sm .ex-diff__star { font-size: 12px; }

.ex-muscle {
  font-size: 12.5px;
  color: var(--text-soft);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.ex-muted { color: var(--text-muted); opacity: 0.6; }

.ex-opt { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.ex-opt__dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 0 0 2px rgba(255,255,255,0.1);
}

/* 操作按钮组 */
.ex-link-btn {
  background: none; border: none; padding: 2px 4px;
  font-size: 13px; color: #22d3ee; cursor: pointer;
  transition: color 0.15s;
}
.ex-link-btn:hover { color: #0ea5b7; text-decoration: underline; }
.ex-link-btn--danger { color: #ff6b6b; }
.ex-link-btn--danger:hover { color: #ee5253; }
.ex-link-btn.is-disabled {
  color: var(--text-muted); cursor: not-allowed; opacity: 0.5;
  text-decoration: none !important;
}
.ex-sep { color: var(--border); margin: 0 2px; }

/* 分页 */
.ex-pagination {
  display: flex; justify-content: flex-end;
  padding: 14px 2px 6px;
}

/* —— Dialog 表单 —— */
.ex-dialog :deep(.el-dialog) {
  border-radius: 20px;
  box-shadow: 0 25px 60px -15px rgba(10, 37, 64, 0.4);
  overflow: hidden;
}
.ex-dialog :deep(.el-dialog__header) {
  padding: 20px 28px 16px;
  border-bottom: 1px solid var(--border);
  margin-right: 0;
  position: relative;
}
.ex-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}
.ex-dialog :deep(.el-dialog__body) {
  padding: 0 28px 24px;
}
.ex-dialog :deep(.el-dialog__footer) {
  padding: 16px 28px 24px;
  border-top: 1px solid var(--border);
}

/* 顶部装饰条 */
.ex-dialog__deco {
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  margin: 0 0 4px;
}
.ex-dialog__deco-bar {
  height: 100%;
}

/* 表单容器 */
.ex-form {
  padding: 8px 0;
}

/* 表单分组 */
.ex-form-section {
  margin-bottom: 20px;
}
.ex-form-section:last-child {
  margin-bottom: 0;
}
.ex-form-section__title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--border);
}
.ex-form-section__bar {
  width: 4px;
  height: 16px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 2px;
}
.ex-form-section__text {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  letter-spacing: 0.02em;
}
.ex-form-section__body {
  padding: 0 4px;
}

/* 表单行 */
.ex-form-row {
  margin-bottom: 4px;
}
.ex-form-row--2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* 表单项标签样式 */
.ex-form :deep(.el-form-item__label) {
  font-size: 12.5px;
  font-weight: 500;
  color: var(--text-soft);
  padding-bottom: 4px !important;
}

/* 输入框样式 */
.ex-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  background: var(--bg-soft);
  border: 1.5px solid transparent;
  box-shadow: none !important;
  transition: all 0.2s ease;
}
.ex-input :deep(.el-input__wrapper:hover) {
  background: var(--bg);
  border-color: rgba(102, 126, 234, 0.3);
}
.ex-input :deep(.el-input__wrapper.is-focus) {
  background: var(--bg);
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}
.ex-input__icon {
  color: var(--text-muted);
  font-size: 16px;
}

/* 选择框样式 */
.ex-select :deep(.el-select__wrapper) {
  border-radius: 10px;
  background: var(--bg-soft);
  border: 1.5px solid transparent;
  box-shadow: none !important;
  transition: all 0.2s ease;
}
.ex-select :deep(.el-select__wrapper:hover) {
  background: var(--bg);
  border-color: rgba(102, 126, 234, 0.3);
}
.ex-select :deep(.el-select__wrapper.is-focused) {
  background: var(--bg);
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}

/* 文本域样式 */
.ex-textarea :deep(.el-textarea__wrapper) {
  border-radius: 12px;
  background: var(--bg-soft);
  border: 1.5px solid transparent;
  box-shadow: none !important;
  transition: all 0.2s ease;
  padding: 10px 14px;
}
.ex-textarea :deep(.el-textarea__wrapper:hover) {
  background: var(--bg);
  border-color: rgba(102, 126, 234, 0.3);
}
.ex-textarea :deep(.el-textarea__wrapper.is-focus) {
  background: var(--bg);
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12) !important;
}

/* 难度选择器 */
.ex-difficulty-picker {
  display: flex;
  gap: 12px;
}
.ex-diff-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 10px;
  border-radius: 12px;
  background: var(--bg-soft);
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}
.ex-diff-option:hover {
  background: var(--bg);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.ex-diff-option.is-active {
  background: var(--diff-bg);
  border-color: var(--diff-color);
  box-shadow: 0 4px 16px color-mix(in srgb, var(--diff-color) 20%, transparent);
}
.ex-diff-option__stars {
  display: flex;
  gap: 2px;
}
.ex-diff-option__star {
  font-size: 16px;
  color: #e5e7eb;
  transition: color 0.2s ease;
}
.ex-diff-option__star.is-on {
  color: var(--diff-color);
}
.ex-diff-option__label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text);
}
.ex-diff-option.is-active .ex-diff-option__label {
  color: var(--diff-color);
}

/* Dialog 底部按钮 */
.ex-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 按钮样式扩展 */
.ex-btn--ghost {
  background: transparent;
  color: var(--text-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 9px 20px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.ex-btn--ghost:hover {
  background: var(--bg-soft);
  color: var(--text);
}

/* —— 详情 Drawer —— */
.ex-detail { padding: 4px 4px 20px; }
.ex-detail__header { margin-bottom: 18px; }
.ex-detail__header h3 {
  margin: 0 0 10px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
}
.ex-detail__tags {
  display: flex; gap: 8px; flex-wrap: wrap; align-items: center;
}
.ex-detail__row {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 12px; margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 12px;
  background: var(--bg-soft);
}
.ex-detail__cell { display: flex; flex-direction: column; gap: 4px; }
.ex-detail__k { font-size: 11.5px; color: var(--text-muted); letter-spacing: 0.05em; }
.ex-detail__v { font-size: 14px; color: var(--text); font-weight: 500; }
.ex-detail__desc h4 {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--text);
  font-weight: 600;
  padding-left: 10px;
  border-left: 3px solid #22d3ee;
}
.ex-detail__desc p {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.75;
  color: var(--text-soft);
  padding: 10px 14px;
  background: var(--bg-soft);
  border-radius: 10px;
  white-space: pre-wrap;
}

@media (max-width: 720px) {
  .ex-hero__stats { grid-template-columns: repeat(2, 1fr); width: 100%; }
  .ex-form-row--2col { grid-template-columns: 1fr; }
}
</style>
