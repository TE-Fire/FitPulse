<template>
  <div class="edit-page">
    <!-- 顶部栏 -->
    <header class="topbar">
      <button class="topbar-btn icon-only" @click="onCancel" aria-label="返回">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6" /></svg>
      </button>
      <h1 class="topbar-title">编辑资料</h1>
      <button class="topbar-btn save" :disabled="saving || uploading" @click="onSave">
        <span v-if="!saving">保存</span>
        <span v-else>保存中…</span>
      </button>
    </header>

    <main v-if="loading" class="loading">加载中…</main>

    <form v-else class="form" @submit.prevent="onSave" novalidate>
      <!-- 头像区 -->
      <section class="card hero-card">
        <div class="hero-wrap">
          <label class="hero-avatar" :class="{ 'is-loading': uploading }" for="avatar-file" aria-label="更换头像">
            <span v-if="!form.avatarUrl || avatarBroken">{{ letterAvatar }}</span>
            <img v-else :src="form.avatarUrl" alt="avatar" @error="onAvatarError" />
            <div class="hero-avatar-mask">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
              <span>{{ uploading ? '上传中…' : '更换头像' }}</span>
            </div>
          </label>
          <input id="avatar-file" ref="avatarInputRef" type="file" accept="image/*" style="display:none" @change="onAvatarChange" />
          <p class="hero-hint">点击头像可更换（JPG / PNG，≤ 5MB）</p>
        </div>
      </section>

      <!-- 基本信息 -->
      <section class="card">
        <div class="section-title">基本信息</div>

        <div class="field">
          <label class="field-label" for="nickname">昵称</label>
          <input
            id="nickname"
            v-model.trim="form.nickname"
            maxlength="32"
            placeholder="给自己起一个响亮的名字"
            class="field-input"
          />
          <div class="field-foot">
            <span class="field-hint">显示在首页卡片与评论中</span>
            <span class="field-counter">{{ (form.nickname || '').length }}/32</span>
          </div>
        </div>

        <div class="field">
          <div class="field-label">性别</div>
          <div class="seg-group">
            <button type="button" class="seg" :class="{ active: form.gender === 1 }" @click="setGender(1)">男</button>
            <button type="button" class="seg" :class="{ active: form.gender === 2 }" @click="setGender(2)">女</button>
            <button type="button" class="seg" :class="{ active: form.gender === 0 }" @click="setGender(0)">未知</button>
          </div>
        </div>

        <div class="field">
          <label class="field-label" for="birthday">生日</label>
          <input
            id="birthday"
            v-model="form.birthday"
            type="date"
            class="field-input"
            :max="today"
          />
        </div>

        <div class="field">
          <label class="field-label" for="fitnessLevel">健身等级</label>
          <select id="fitnessLevel" v-model.number="form.fitnessLevel" class="field-input field-select">
            <option :value="null">暂不选择</option>
            <option :value="1">入门（刚开始健身）</option>
            <option :value="2">进阶（持续 3 个月+）</option>
            <option :value="3">达人（1 年以上，有明确目标）</option>
            <option :value="4">专业（备赛/教练级）</option>
          </select>
        </div>

        <div class="field">
          <label class="field-label" for="bio">个人简介</label>
          <textarea
            id="bio"
            v-model.trim="form.bio"
            maxlength="200"
            rows="3"
            placeholder="一句话介绍自己的训练理念"
            class="field-input field-textarea"
          />
          <div class="field-foot field-foot--right">
            <span class="field-counter">{{ (form.bio || '').length }}/200</span>
          </div>
        </div>
      </section>

      <!-- 身体数据 -->
      <section class="card">
        <div class="section-title section-title--hint">
          <span>身体数据</span>
          <span class="section-hint">（后续身体数据模块会自动同步）</span>
        </div>

        <div class="field-grid">
          <div class="field">
            <label class="field-label" for="heightCm">身高 (cm)</label>
            <div class="number-wrap">
              <input
                id="heightCm"
                v-model.number="form.heightCm"
                type="number"
                step="0.1" min="100" max="250"
                placeholder="例如 175.0"
                class="field-input"
              />
              <span class="number-suffix">cm</span>
            </div>
          </div>

          <div class="field">
            <label class="field-label" for="weightKg">体重 (kg)</label>
            <div class="number-wrap">
              <input
                id="weightKg"
                v-model.number="form.weightKg"
                type="number"
                step="0.1" min="30" max="200"
                placeholder="例如 65.0"
                class="field-input"
              />
              <span class="number-suffix">kg</span>
            </div>
          </div>
        </div>

        <div class="field">
          <label class="field-label" for="bodyFatPct">体脂率 (%)</label>
          <div class="bodyfat-wrap">
            <div class="number-wrap">
              <input
                id="bodyFatPct"
                v-model.number="form.bodyFatPct"
                type="number"
                step="0.1" min="3" max="60"
                :placeholder="bodyFatPlaceholder"
                class="field-input"
              />
              <span class="number-suffix">%</span>
            </div>
            <button
              v-if="estimatedBodyFat != null && form.bodyFatPct !== estimatedBodyFat"
              type="button"
              class="btn-estimate"
              @click="applyEstimatedBodyFat"
            >
              填入 {{ estimatedBodyFat.toFixed(1) }}%
            </button>
          </div>
          <div v-if="estimatedBodyFat != null && form.bodyFatPct !== estimatedBodyFat" class="bodyfat-hint">
            💡 根据身高 {{ form.heightCm }}cm / 体重 {{ form.weightKg }}kg / {{ genderLabel(form.gender) }} / {{ age }}岁 估算
          </div>
          <div class="bodyfat-disclaimer">
            基于 BMI 法估算，仅供参考。准确值请使用体脂秤测量。
          </div>
        </div>
      </section>

      <!-- 操作按钮 -->
      <section class="actions">
        <button type="submit" class="btn-primary" :disabled="saving || uploading" :class="{ 'is-loading': saving }">
          <span v-if="!saving">保存资料</span>
          <span v-else>保存中…</span>
        </button>
        <button type="button" class="btn-plain" :disabled="saving || uploading" @click="onReset">恢复当前</button>
      </section>

      <div class="foot-space"></div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile, updateProfile, uploadAvatar } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const avatarInputRef = ref(null)

const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)

/** 当前登录账号的完整资料（UserProfileVO 根） */
const rootData = ref(null)
/** 表单编辑对象（对 profile 的浅拷贝；头像字段也在里面） */
const form = reactive({
  nickname: '',
  avatarUrl: '',
  gender: 0,           // 0=未知 1=男 2=女
  birthday: '',        // YYYY-MM-DD
  heightCm: null,
  weightKg: null,
  bodyFatPct: null,
  fitnessLevel: null,  // 1-4
  theme: null,         // 1-3 (后端保留字段)
  bio: ''
})
/** 初始快照，用于「恢复当前」按钮 */
let initialSnapshot = null
// 头像加载失败标记(旧头像或上传后 URL 如果加载失败,回退到字母占位)
const avatarBroken = ref(false)

const today = new Date().toISOString().slice(0, 10)

/** —— 纯工具函数（单点归一，避免散落） —— */
function genderLabel(g) {
  const v = Number(g)
  if (Number.isNaN(v) || v === 0) return '未知'
  return v === 1 ? '男' : v === 2 ? '女' : '未知'
}

/** 根据生日计算周岁 */
function calcAge(birthday) {
  if (!birthday) return null
  const m = /^(\d{4})-(\d{1,2})-(\d{1,2})/.exec(String(birthday))
  if (!m) return null
  const y = +m[1], mo = +m[2], d = +m[3]
  const now = new Date()
  let age = now.getFullYear() - y
  if (now.getMonth() + 1 < mo || (now.getMonth() + 1 === mo && now.getDate() < d)) age--
  return age >= 0 ? age : null
}
const age = computed(() => calcAge(form.birthday))

/** BMI 值 */
const bmi = computed(() => {
  if (!form.heightCm || !form.weightKg) return null
  const m = form.heightCm / 100
  const v = form.weightKg / (m * m)
  return Number.isFinite(v) ? v : null
})

/** 估算体脂率（BMI 法，男女不同公式） */
const estimatedBodyFat = computed(() => {
  if (bmi.value == null || age.value == null || !form.gender || form.gender === 0) return null
  // 成人：男性 1.2*BMI + 0.23*age - 16.2 ; 女性 1.2*BMI + 0.23*age - 5.4
  const base = 1.2 * bmi.value + 0.23 * age.value
  const r = form.gender === 1 ? base - 16.2 : base - 5.4
  const clipped = Math.min(60, Math.max(3, r))
  return Number.isFinite(clipped) ? +clipped.toFixed(1) : null
})
const bodyFatPlaceholder = computed(() =>
  estimatedBodyFat.value != null ? `推荐 ≈ ${estimatedBodyFat.value}%` : '例如 18.0'
)

/** 头像字母 */
const letterAvatar = computed(() => {
  const s = form.nickname || rootData.value?.username || 'F'
  return s.trim().charAt(0).toUpperCase()
})

/** —— 初始化 / 快照 —— */
function applySnapshot(snap) {
  if (!snap) return
  Object.assign(form, snap)
}
function snapshotForm() {
  return {
    nickname: form.nickname,
    avatarUrl: form.avatarUrl,
    gender: form.gender,
    birthday: form.birthday,
    heightCm: form.heightCm,
    weightKg: form.weightKg,
    bodyFatPct: form.bodyFatPct,
    fitnessLevel: form.fitnessLevel,
    theme: form.theme,
    bio: form.bio
  }
}

async function load() {
  loading.value = true
  avatarBroken.value = false
  try {
    // 复用 Layout 已缓存的 profile，缺失再请求
    const root = userStore.profile || await getProfile()
    if (!userStore.profile) userStore.profile = root
    rootData.value = root
    const p = root.profile || {}
    const snap = {
      nickname: p.nickname || root.username || '',
      avatarUrl: p.avatarUrl || '',
      gender: p.gender != null ? Number(p.gender) : 0,
      birthday: p.birthday ? String(p.birthday).slice(0, 10) : '',
      heightCm: p.heightCm != null ? Number(p.heightCm) : null,
      weightKg: p.weightKg != null ? Number(p.weightKg) : null,
      bodyFatPct: p.bodyFatPct != null ? Number(p.bodyFatPct) : null,
      fitnessLevel: p.fitnessLevel != null ? Number(p.fitnessLevel) : null,
      theme: p.theme != null ? Number(p.theme) : null,
      bio: p.bio || ''
    }
    initialSnapshot = snap
    applySnapshot(snap)
  } finally {
    loading.value = false
  }
}

/** —— 交互处理 —— */
function setGender(v) {
  form.gender = v
}
function applyEstimatedBodyFat() {
  if (estimatedBodyFat.value != null) form.bodyFatPct = estimatedBodyFat.value
}
function onAvatarError(e) {
  const src = e?.target?.src
  console.warn('[ProfileEdit.avatar] 头像加载失败,回退字母占位. src=', src)
  avatarBroken.value = true
}
function onAvatarChange(e) {
  const f = e.target.files && e.target.files[0]
  if (!f) return
  if (f.size > 5 * 1024 * 1024) {
    alert('头像文件过大，请选择小于 5MB 的图片')
    e.target.value = ''
    return
  }
  uploading.value = true
  avatarBroken.value = false // 新上传的先清除"加载失败"标记
  uploadAvatar(f)
    .then((r) => {
      // 返回 {avatarUrl:string}，也兼容直接返回字符串的情况
      const url = (r && r.avatarUrl) || r
      if (typeof url === 'string') form.avatarUrl = url
    })
    .catch((err) => alert('头像上传失败：' + (err?.message || err)))
    .finally(() => {
      uploading.value = false
      e.target.value = ''
    })
}

/** 基础字段合法性（后端还会二次校验，前端仅防误操作） */
function validateForm() {
  if (form.nickname && form.nickname.length > 32) return '昵称长度不能超过 32'
  if (form.bio && form.bio.length > 200) return '简介长度不能超过 200'
  if (form.heightCm != null && (form.heightCm < 100 || form.heightCm > 250)) return '身高应在 100-250 cm 之间'
  if (form.weightKg != null && (form.weightKg < 30 || form.weightKg > 200)) return '体重应在 30-200 kg 之间'
  if (form.bodyFatPct != null && (form.bodyFatPct < 3 || form.bodyFatPct > 60)) return '体脂率应在 3-60% 之间'
  if (form.fitnessLevel != null && ![1, 2, 3, 4].includes(form.fitnessLevel)) return '健身等级非法'
  return null
}

async function onSave() {
  const errMsg = validateForm()
  if (errMsg) {
    alert(errMsg)
    return
  }
  saving.value = true
  try {
    // 仅序列化用户 profile 字段（对齐后端 UpdateProfileReq）
    const payload = {
      nickname: form.nickname || null,
      avatarUrl: form.avatarUrl || null,
      gender: form.gender,
      birthday: form.birthday || null,
      heightCm: form.heightCm != null ? Number(form.heightCm) : null,
      weightKg: form.weightKg != null ? Number(form.weightKg) : null,
      bodyFatPct: form.bodyFatPct != null ? Number(form.bodyFatPct) : null,
      fitnessLevel: form.fitnessLevel != null ? Number(form.fitnessLevel) : null,
      theme: form.theme != null ? Number(form.theme) : null,
      bio: form.bio || null
    }
    await updateProfile(payload)
    // 保存成功 → 刷新缓存 + 回到 profile 页
    await userStore.loadMe()
    router.back()
  } catch (err) {
    alert('保存失败：' + (err?.message || err))
  } finally {
    saving.value = false
  }
}

function onReset() {
  if (initialSnapshot) applySnapshot(initialSnapshot)
}

function onCancel() {
  if (saving.value) return
  // 若有未保存改动，提示确认（避免误触返回丢数据）
  const dirty = initialSnapshot && JSON.stringify(snapshotForm()) !== JSON.stringify(initialSnapshot)
  if (dirty && !confirm('有未保存的改动，确定返回吗？')) return
  router.back()
}

onMounted(load)
</script>

<style scoped>
.edit-page {
  min-height: 100vh;
  background: #f6f7fb;
  padding-bottom: 24px;
}

/* ====== 顶部栏 ====== */
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  height: 52px;
  padding: 0 8px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: saturate(180%) blur(14px);
  border-bottom: 1px solid #eef2f7;
}
.topbar-btn {
  min-width: 44px;
  height: 36px;
  padding: 0 12px;
  border-radius: 10px;
  border: none;
  background: transparent;
  color: #0f172a;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
}
.topbar-btn:active { background: #f1f5f9; }
.topbar-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.topbar-btn.icon-only svg { width: 20px; height: 20px; }
.topbar-btn.save {
  margin-left: auto;
  background: #7c5cff;
  color: white;
}
.topbar-btn.save:active { background: #6b4bff; }
.topbar-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 0 4px;
}

/* ====== 通用 ====== */
.card {
  background: white;
  border-radius: 16px;
  padding: 18px 16px 20px 16px;
  margin: 12px 16px 0 16px;
  box-shadow: 0 4px 14px -4px rgba(30, 27, 75, 0.06);
}
.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 14px 0;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.section-title--hint .section-hint {
  font-size: 11px;
  font-weight: 500;
  color: #94a3b8;
}
.loading {
  text-align: center;
  padding: 80px 0;
  color: #94a3b8;
  font-size: 14px;
}

/* ====== 头像区 ====== */
.hero-card {
  margin-top: 16px;
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
}
.hero-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.hero-avatar {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  cursor: pointer;
  overflow: hidden;
  box-shadow: 0 10px 24px -8px rgba(0, 0, 0, 0.25);
  user-select: none;
}
.hero-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hero-avatar-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.55);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s ease;
}
.hero-avatar:hover .hero-avatar-mask,
.hero-avatar:active .hero-avatar-mask {
  opacity: 1;
}
.hero-avatar-mask svg {
  width: 22px;
  height: 22px;
}
.hero-avatar.is-loading {
  opacity: 0.7;
  cursor: progress;
}
.hero-hint {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

/* ====== 表单字段 ====== */
.field {
  margin-bottom: 18px;
}
.field:last-child { margin-bottom: 0; }
.field-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}
.field-input {
  width: 100%;
  box-sizing: border-box;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 12px;
  font-size: 14px;
  color: #0f172a;
  outline: none;
  transition: border-color 0.15s ease, background 0.15s ease, box-shadow 0.15s ease;
  -webkit-appearance: none;
  appearance: none;
}
.field-input:focus {
  border-color: #7c5cff;
  background: white;
  box-shadow: 0 0 0 3px rgba(124, 92, 255, 0.12);
}
.field-textarea {
  height: auto;
  padding: 10px 14px;
  resize: vertical;
  line-height: 1.5;
  min-height: 88px;
}
.field-select {
  padding-right: 36px;
  background-image: url("data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'><polyline points='6 9 12 15 18 9'/></svg>");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px 16px;
}
.field-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}
.field-foot--right { justify-content: flex-end; }
.field-hint { font-size: 11px; color: #94a3b8; }
.field-counter { font-size: 11px; color: #94a3b8; }

/* 两列身体数据网格 */
.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
@media (max-width: 340px) {
  .field-grid { grid-template-columns: 1fr; }
}
.number-wrap {
  position: relative;
}
.number-wrap .field-input {
  padding-right: 52px;
}
.number-suffix {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  font-weight: 600;
  color: #94a3b8;
  pointer-events: none;
}

/* 性别分段按钮 */
.seg-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.seg {
  height: 44px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.15s ease;
}
.seg:active { transform: scale(0.98); }
.seg.active {
  background: #7c5cff;
  border-color: #7c5cff;
  color: white;
  box-shadow: 0 6px 16px -6px rgba(124, 92, 255, 0.45);
}

/* 体脂率估算 */
.bodyfat-wrap {
  display: flex;
  gap: 8px;
  align-items: stretch;
}
.bodyfat-wrap .number-wrap { flex: 1; }
.btn-estimate {
  padding: 0 12px;
  height: 44px;
  border-radius: 12px;
  border: 1px solid #7c5cff;
  background: rgba(124, 92, 255, 0.08);
  color: #7c5cff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s ease;
}
.btn-estimate:active { background: rgba(124, 92, 255, 0.18); }
.bodyfat-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #7c5cff;
  font-weight: 500;
}
.bodyfat-disclaimer {
  margin-top: 6px;
  font-size: 11px;
  color: #94a3b8;
}

/* ====== 底部操作按钮 ====== */
.actions {
  padding: 16px 16px 0 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.btn-primary {
  height: 48px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #7c5cff 0%, #6366f1 100%);
  color: white;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 10px 20px -10px rgba(99, 102, 241, 0.55);
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}
.btn-primary:active { transform: translateY(1px); }
.btn-primary:disabled, .btn-primary.is-loading { opacity: 0.65; cursor: not-allowed; }
.btn-plain {
  height: 44px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: background 0.15s ease;
}
.btn-plain:active { background: #f8fafc; }
.btn-plain:disabled { opacity: 0.5; cursor: not-allowed; }

.foot-space { height: 24px; }
</style>
