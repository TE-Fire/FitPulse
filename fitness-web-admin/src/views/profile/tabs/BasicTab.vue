<template>
  <div v-loading="loading" class="fp-profile-editor">
    <!-- ====== 个人名片卡 ====== -->
    <div class="fp-hero-card">
      <div class="fp-hero-card__glow" />
      <div class="fp-hero-card__content">
        <div class="fp-avatar-wrap">
          <div class="fp-avatar-lg">
            <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="avatar" />
            <span v-else>{{ letterAvatar }}</span>
          </div>
          <el-upload :show-file-list="false" :http-request="onAvatarUpload" accept="image/*">
            <el-button class="fp-avatar-btn" :loading="uploading" circle size="small">
              <el-icon><Camera /></el-icon>
            </el-button>
          </el-upload>
        </div>
        <div class="fp-hero-info">
          <h2 class="fp-hero-name">
            {{ form.nickname || 'FitPulse 用户' }}
            <el-tag v-if="form.fitnessLevel" :type="levelTagType" effect="dark" size="small" class="fp-level-tag">
              {{ levelText }}
            </el-tag>
          </h2>
          <p class="fp-hero-bio">{{ form.bio || '这个人很懒，还没有介绍自己' }}</p>
          <div class="fp-hero-meta">
            <span v-if="form.gender" class="fp-hero-chip">
              <el-icon><component :is="form.gender === 1 ? 'Male' : 'Female'" /></el-icon>
              {{ form.gender === 1 ? '男' : '女' }}
            </span>
            <span v-if="form.birthday" class="fp-hero-chip">
              <el-icon><Calendar /></el-icon>
              {{ form.birthday }}
            </span>
            <span v-if="form.heightCm" class="fp-hero-chip">
              <el-icon><Ruler /></el-icon>
              {{ form.heightCm }} cm
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 基本信息 ====== -->
    <div class="fp-section">
      <div class="fp-section-header">
        <el-icon><User /></el-icon>
        <span>基本信息</span>
      </div>
      <div class="fp-section-body">
        <el-form :model="form" label-position="top" class="fp-form-grid">
          <el-form-item label="昵称" required>
            <el-input v-model="form.nickname" maxlength="32" show-word-limit placeholder="给自己起一个响亮的名字" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender" class="fp-seg">
              <el-radio-button :value="1">男</el-radio-button>
              <el-radio-button :value="2">女</el-radio-button>
              <el-radio-button :value="0">未知</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="生日">
            <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
          </el-form-item>
          <el-form-item label="健身等级">
            <el-select v-model="form.fitnessLevel" placeholder="选择你的健身水平" style="width:100%">
              <el-option :value="1" label="入门" />
              <el-option :value="2" label="进阶" />
              <el-option :value="3" label="达人" />
              <el-option :value="4" label="专业" />
            </el-select>
          </el-form-item>
          <el-form-item label="简介" class="fp-form-item--full">
            <el-input v-model="form.bio" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="一句话介绍自己的训练理念" />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ====== 身体数据 ====== -->
    <div class="fp-section">
      <div class="fp-section-header fp-section-header--A">
        <el-icon><DataLine /></el-icon>
        <span>身体数据</span>
        <span class="fp-section-hint">（体重/体脂由身体数据模块自动同步）</span>
      </div>
      <div class="fp-section-body">
        <el-form :model="form" label-position="top" class="fp-form-grid">
          <el-form-item label="身高 (cm)">
            <div class="fp-modern-number">
              <el-input-number v-model="form.heightCm" :min="100" :max="250" :precision="1" />
            </div>
          </el-form-item>
          <el-form-item label="体重 (kg)">
            <div class="fp-modern-number">
              <el-input-number v-model="form.weightKg" :min="30" :max="200" :precision="1" disabled />
            </div>
          </el-form-item>
          <!-- 体脂率：支持估算 + 手动覆盖 -->
          <el-form-item label="体脂率 (%)" class="fp-form-item--bodyfat">
            <div class="fp-bodyfat-wrap">
              <div class="fp-modern-number" style="flex:1;">
                <el-input-number
                  v-model="form.bodyFatPct"
                  :min="3" :max="60" :precision="1"
                  :placeholder="bodyFatPlaceholder"
                />
              </div>
              <el-button
                v-if="estimatedBodyFat && form.bodyFatPct !== estimatedBodyFat"
                size="small"
                type="primary"
                plain
                @click="applyEstimatedBodyFat"
              >
                填入 {{ estimatedBodyFat }}%
              </el-button>
            </div>
            <div v-if="estimatedBodyFat && form.bodyFatPct !== estimatedBodyFat" class="fp-bodyfat-hint">
              💡 根据身高 {{ form.heightCm }}cm / 体重 {{ form.weightKg }}kg / {{ form.gender === 1 ? '男' : '女' }} / {{ age }}岁 估算
            </div>
            <div class="fp-bodyfat-disclaimer">
              <el-icon><Warning /></el-icon>
              基于 BMI 法估算，仅供参考。准确值请使用体脂秤测量。
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- ====== 操作按钮 ====== -->
    <div class="fp-actions">
      <el-button type="primary" size="large" :loading="saving" class="fp-btn-modern" @click="onSaveProfile">
        <el-icon><Check /></el-icon>保存资料
      </el-button>
      <el-button size="large" class="fp-btn-modern-plain" @click="load">
        <el-icon><Refresh /></el-icon>重置
      </el-button>
    </div>

    <!-- ====== 训练目标（视觉占位，后端 user_goal 尚未开发） ====== -->
    <div class="fp-section fp-section--goal">
      <div class="fp-section-header fp-section-header--C">
        <el-icon><Aim /></el-icon>
        <span>训练目标</span>
        <span class="fp-section-hint">（功能开发中，暂不可保存）</span>
      </div>
      <div class="fp-section-body fp-section-body--disabled">
        <el-form label-position="top" class="fp-form-grid">
          <el-form-item label="目标类型">
            <el-select model-value="" disabled style="width:100%">
              <el-option :value="2" label="增肌" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标体重 (kg)">
            <div class="fp-modern-number">
              <el-input-number :model-value="null" :min="30" :max="200" :precision="1" disabled />
            </div>
          </el-form-item>
          <el-form-item label="目标体脂 (%)">
            <div class="fp-modern-number">
              <el-input-number :model-value="null" :min="3" :max="60" :precision="1" disabled />
            </div>
          </el-form-item>
          <el-form-item label="每周训练 (次)">
            <div class="fp-modern-number">
              <el-input-number :model-value="null" :min="1" :max="14" disabled />
            </div>
          </el-form-item>
          <el-form-item label="每日热量 (kcal)">
            <div class="fp-modern-number">
              <el-input-number :model-value="null" :min="1000" :max="5000" :step="50" disabled />
            </div>
          </el-form-item>
          <el-form-item label="每日饮水 (ml)">
            <div class="fp-modern-number">
              <el-input-number :model-value="null" :min="500" :max="5000" :step="100" disabled />
            </div>
          </el-form-item>
          <el-form-item label="目标日期">
            <el-date-picker :model-value="null" type="date" value-format="YYYY-MM-DD" disabled style="width:100%" />
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 基本资料 Tab（视觉重构 + 体脂率估算）
 *
 * 视觉：卡片式分区布局
 *   - 个人名片卡：渐变光晕背景 + 大头像 + 等级徽章 + 资料简介 + 芯片式元信息
 *   - 基本信息区：栅格布局 + 分段控件
 *   - 身体数据区：A 紫色分区标识 + 体脂率估算交互
 *   - 训练目标区：C 绿色分区标识 + 独立保存
 *
 * 体脂率估算：Deurenberg 公式（见 devlog §9.3.1）
 *   - 触发：身高/体重/性别/生日四字段齐全时实时计算
 *   - 显示：placeholder 显示"建议 xx.x%"，一键填入按钮
 *   - 限制：3-60%，仅作参考
 */
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfile, updateMyProfile, uploadAvatar } from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)

const form = reactive({
  nickname: '', avatarUrl: '', gender: 0,
  birthday: '', heightCm: 170, weightKg: 0, bodyFatPct: 0,
  fitnessLevel: 1, bio: ''
})

/* ========== 体脂率估算逻辑 ========== */

function calcAge(birthday) {
  if (!birthday) return null
  const birth = new Date(birthday)
  if (isNaN(birth.getTime())) return null
  const now = new Date()
  let age = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--
  return age
}

/**
 * Deurenberg 公式估算体脂率（BMI 法，仅供参考）
 * bodyFat = 1.2*BMI + 0.23*age - 10.8*gender - 5.4
 * gender: 男=1, 女=0
 */
function estimateBodyFat(weightKg, heightCm, gender, birthday) {
  if (!weightKg || !heightCm || weightKg <= 0 || heightCm <= 0) return null
  if (!gender || gender === 0 || !birthday) return null
  const g = gender === 1 ? 1 : 0
  const age = calcAge(birthday)
  if (!age || age < 10 || age > 100) return null
  const bmi = weightKg / Math.pow(heightCm / 100, 2)
  const bodyFat = (1.2 * bmi) + (0.23 * age) - (10.8 * g) - 5.4
  const clamped = Math.max(3, Math.min(60, bodyFat))
  return Number(clamped.toFixed(1))
}

const estimatedBodyFat = computed(() =>
  estimateBodyFat(form.weightKg, form.heightCm, form.gender, form.birthday)
)

const age = computed(() => calcAge(form.birthday) ?? '—')

const bodyFatPlaceholder = computed(() => {
  if (!estimatedBodyFat.value) return '体脂率 (%)'
  return `建议 ${estimatedBodyFat.value}%（由 BMI 估算）`
})

function applyEstimatedBodyFat() {
  if (estimatedBodyFat.value) {
    form.bodyFatPct = estimatedBodyFat.value
  }
}

/* ========== 等级徽章 ========== */
const levelText = computed(() => {
  const map = { 1: '入门', 2: '进阶', 3: '达人', 4: '专业' }
  return map[form.fitnessLevel] || ''
})
const levelTagType = computed(() => {
  const map = { 1: 'info', 2: '', 3: 'warning', 4: 'danger' }
  return map[form.fitnessLevel] || ''
})

const letterAvatar = computed(() => (form.nickname || 'F').charAt(0).toUpperCase())

/* ========== 数据加载与保存 ========== */

async function load() {
  loading.value = true
  try {
    const data = await getMyProfile()
    // 后端返回 UserProfileVO：扁平 user 字段 + 嵌套 profile 对象
    const p = data.profile || {}
    Object.assign(form, {
      nickname: p.nickname ?? '',
      avatarUrl: p.avatarUrl ?? '',
      gender: p.gender ?? 0,
      birthday: p.birthday ?? '',
      heightCm: p.heightCm ?? 170,
      weightKg: p.weightKg ?? 0,
      bodyFatPct: p.bodyFatPct ?? 0,
      fitnessLevel: p.fitnessLevel ?? 1,
      bio: p.bio ?? ''
    })
  } catch (e) {
    ElMessage.error('加载资料失败')
  } finally {
    loading.value = false
  }
}

async function onAvatarUpload(opt) {
  uploading.value = true
  try {
    const res = await uploadAvatar(opt.file)
    form.avatarUrl = res.avatarUrl
    ElMessage.success('头像已更新')
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

async function onSaveProfile() {
  saving.value = true
  try {
    // 后端 UpdateProfileReq 不含 avatarUrl（由 POST /user/avatar 内部回写）
    const payload = {
      nickname: form.nickname,
      gender: form.gender,
      birthday: form.birthday,
      heightCm: form.heightCm,
      weightKg: form.weightKg,
      bodyFatPct: form.bodyFatPct,
      fitnessLevel: form.fitnessLevel,
      bio: form.bio
    }
    await updateMyProfile(payload)
    ElMessage.success('资料已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.fp-profile-editor {
  padding: 0 0 8px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========== 个人名片卡 ========== */
.fp-hero-card {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  background: var(--card);
  border: 1px solid var(--border);
  padding: 28px 32px;
  box-shadow: var(--shadow-soft);
}
.fp-hero-card__glow {
  position: absolute;
  top: -40px; right: -40px;
  width: 220px; height: 220px;
  background: radial-gradient(circle, rgba(124,92,255,0.25), transparent 70%);
  pointer-events: none;
}
.fp-hero-card__content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 28px;
}

.fp-avatar-wrap { position: relative; flex-shrink: 0; }
.fp-avatar-lg {
  width: 96px; height: 96px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c5cff, #22d3ee);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 40px; font-weight: 700;
  overflow: hidden;
  border: 3px solid var(--card);
  box-shadow: 0 8px 24px -4px rgba(124, 92, 255, 0.45);
}
.fp-avatar-lg img { width: 100%; height: 100%; object-fit: cover; }
.fp-avatar-btn {
  position: absolute;
  bottom: -4px; right: -4px;
  background: var(--fit-brand);
  border: 3px solid var(--card);
  color: #fff;
  width: 32px; height: 32px;
  z-index: 2;
}
.fp-avatar-btn:hover { background: #6a48e6; }

.fp-hero-info { flex: 1; min-width: 0; }
.fp-hero-name {
  margin: 0;
  font-size: 22px;
  color: var(--text);
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.fp-level-tag { font-size: 11px; }
.fp-hero-bio {
  margin: 6px 0 10px;
  font-size: 13px;
  color: var(--text-soft);
  line-height: 1.5;
}
.fp-hero-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.fp-hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--bg-soft);
  color: var(--text-soft);
  font-size: 12px;
  border: 1px solid var(--border);
}

/* ========== 分区卡 ========== */
.fp-section {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 16px;
  box-shadow: var(--shadow-soft);
  overflow: hidden;
}
.fp-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
  border-bottom: 1px solid var(--border);
  background: var(--card-2);
}
.fp-section-header .el-icon { color: var(--fit-brand); font-size: 16px; }
.fp-section-header--A .el-icon { color: var(--dim-A); }
.fp-section-header--C .el-icon { color: var(--dim-C); }
.fp-section-hint { font-weight: 400; font-size: 12px; color: var(--text-muted); margin-left: auto; }
.fp-section-body { padding: 20px 24px; }
.fp-section-body--disabled { opacity: 0.55; pointer-events: none; }
.fp-section-footer {
  padding: 12px 24px;
  border-top: 1px solid var(--border);
  display: flex;
  justify-content: flex-end;
  background: var(--card-2);
}

/* ========== 表单栅格 ========== */
.fp-form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0 20px;
}
.fp-form-grid .fp-form-item--full { grid-column: 1 / -1; }
.fp-form-grid .fp-form-item--bodyfat { grid-column: 1 / -1; }

/* 分段控件美化 */
.fp-seg :deep(.el-radio-button__inner) {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-soft);
}
.fp-seg :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--fit-brand);
  color: #fff;
  border-color: var(--fit-brand);
  box-shadow: 0 2px 8px -2px rgba(124, 92, 255, 0.5);
}

/* ========== 现代化数字输入框 (Segmented Control Style) ========== */
.fp-modern-number {
  :deep(.el-input-number) {
    width: 100% !important;
    --el-input-number-height: 36px;
  }
  
  :deep(.el-input-number__decrease),
  :deep(.el-input-number__increase) {
    width: 36px;
    height: 36px;
    border-radius: 24px;
    background: var(--bg-soft);
    border: none;
    color: var(--text-soft);
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    margin: 2px;
    
    &:hover {
      background: var(--fit-brand);
      color: #fff;
      transform: scale(1.05);
      box-shadow: 0 2px 8px -2px rgba(124, 92, 255, 0.4);
    }
    
    &:active {
      transform: scale(0.95);
    }
  }

  :deep(.el-input-number__decrease) {
    position: absolute;
    left: 4px;
    top: 50%;
    transform: translateY(-50%);
    
    &:hover { transform: translateY(-50%) scale(1.05); }
    &:active { transform: translateY(-50%) scale(0.95); }
  }

  :deep(.el-input-number__increase) {
    position: absolute;
    right: 4px;
    top: 50%;
    transform: translateY(-50%);
    
    &:hover { transform: translateY(-50%) scale(1.05); }
    &:active { transform: translateY(-50%) scale(0.95); }
  }

  :deep(.el-input__wrapper) {
    border-radius: 24px !important;
    padding: 0 40px !important; /* 为左右按钮留出空间 */
    background: var(--bg-soft);
    box-shadow: none !important;
    border: 1px solid var(--border);
    transition: all 0.2s ease;
    
    &:hover {
      border-color: var(--fit-brand-light);
      background: var(--bg-soft-hover);
    }
    
    &.is-focus {
      border-color: var(--fit-brand) !important;
      box-shadow: 0 0 0 3px rgba(124, 92, 255, 0.15) !important;
      background: var(--card);
    }
    
    &.is-disabled {
      background: var(--card);
      opacity: 0.7;
    }
  }

  :deep(.el-input__inner) {
    text-align: center !important;
    font-weight: 600;
    font-size: 14px;
    color: var(--text);
    height: 34px;
    line-height: 34px;
  }
}

/* ========== 体脂率估算区 ========== */
.fp-bodyfat-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.fp-bodyfat-wrap .el-input-number { flex: 1; }
.fp-bodyfat-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--dim-A);
  display: flex;
  align-items: center;
  gap: 4px;
}
.fp-bodyfat-disclaimer {
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 6px;
  background: var(--bg-soft);
  border: 1px dashed var(--border);
}

/* ========== 操作按钮 ========== */
.fp-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

/* 现代化按钮样式 */
.fp-btn-modern {
  border-radius: 12px !important;
  padding: 12px 28px !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #7c5cff, #6a48e6) !important;
  border: none !important;
  box-shadow: 0 4px 12px -2px rgba(124, 92, 255, 0.4);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px -4px rgba(124, 92, 255, 0.5);
    background: linear-gradient(135deg, #8b6fff, #7c5cff) !important;
  }
  
  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 8px -2px rgba(124, 92, 255, 0.4);
  }
}

.fp-btn-modern-plain {
  border-radius: 12px !important;
  padding: 12px 24px !important;
  font-weight: 500 !important;
  background: var(--card) !important;
  border: 1px solid var(--border) !important;
  color: var(--text-soft) !important;
  transition: all 0.2s ease !important;
  
  &:hover {
    color: var(--fit-brand) !important;
    border-color: var(--fit-brand) !important;
    background: var(--bg-soft) !important;
  }
}

/* 响应式：窄屏栅格单列 */
@media (max-width: 720px) {
  .fp-form-grid { grid-template-columns: 1fr; }
  .fp-hero-card__content { flex-direction: column; text-align: center; }
  .fp-hero-meta { justify-content: center; }
  .fp-section-hint { display: none; }
}
</style>
