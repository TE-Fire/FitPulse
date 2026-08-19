<template>
  <div class="page profile-page">
    <header class="page-header">
      <h1 class="page-title">个人中心</h1>
    </header>

    <div v-if="loading" class="loading">加载中…</div>

    <template v-else-if="data">
      <!-- 用户卡片 -->
      <section class="card user-card">
        <div class="user-row">
          <div class="avatar">
            <span v-if="!profile?.avatarUrl">{{ (profile?.nickname || data.username || 'F').charAt(0).toUpperCase() }}</span>
            <img v-else :src="profile.avatarUrl" alt="avatar" />
          </div>
          <div class="user-info">
            <p class="nickname">{{ profile?.nickname || data.username }}</p>
            <p class="username">@{{ data.username }}</p>
          </div>
          <button class="edit-btn" @click="editProfile">编辑</button>
        </div>
      </section>

      <!-- 目标卡:后端 goal 模块尚未开发,占位提示 -->
      <section class="card goal-card">
        <div class="card-head">
          <span class="card-label">我的目标</span>
          <span class="goal-type-tag">开发中</span>
        </div>
        <p class="no-goal">目标设置模块尚未上线,后续完善后可自定义训练/体重/热量/饮水目标</p>
      </section>

      <!-- 资料卡 -->
      <section class="card info-card">
        <div class="card-head">
          <span class="card-label">基本资料</span>
        </div>
        <div class="info-list">
          <div class="info-row">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ data.email || '-' }}</span>
          </div>
          <div class="info-row" v-if="data.phone">
            <span class="info-label">手机</span>
            <span class="info-value">{{ data.phone }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">性别</span>
            <span class="info-value">{{ genderText(profile?.gender) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">身高</span>
            <span class="info-value">{{ profile?.heightCm ? profile.heightCm + ' cm' : '-' }}</span>
          </div>
          <div class="info-row" v-if="profile?.weightKg != null">
            <span class="info-label">体重</span>
            <span class="info-value">{{ profile.weightKg }} kg</span>
          </div>
          <div class="info-row" v-if="profile?.bodyFatPct != null">
            <span class="info-label">体脂率</span>
            <span class="info-value">{{ profile.bodyFatPct }}%</span>
          </div>
          <div class="info-row">
            <span class="info-label">生日</span>
            <span class="info-value">{{ profile?.birthday || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ formatDate(data.createdAt) }}</span>
          </div>
        </div>
      </section>

      <!-- 功能入口 -->
      <section class="card action-card">
        <div class="action-list">
          <div class="action-row" @click="goAction('records')">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <path d="M14 2v6h6" />
              <path d="M8 13h2" />
              <path d="M8 17h2" />
              <path d="M14 13h2" />
              <path d="M14 17h2" />
            </svg>
            <span class="action-label">训练记录</span>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </div>
          <div class="action-row" @click="goAction('body')">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2v6a6 6 0 0 0 12 0V2" />
              <path d="M6 22V10" />
              <path d="M18 22V10" />
            </svg>
            <span class="action-label">身体数据</span>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </div>
          <div class="action-row" @click="goAction('meals')">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 13.87A4 4 0 0 1 7.41 6a5.11 5.11 0 0 1 1.05-1.54 12.06 12.06 0 0 0-2.43-2.66A1 1 0 0 0 4 3.07a16 16 0 0 0 6 1.94A16 16 0 0 0 16 2.93a1 1 0 0 0-1.94-.2A12.07 12.07 0 0 0 11.6 5.4 5.11 5.11 0 0 1 12.65 6.94 4 4 0 0 1 14 13.88V16a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2Z" />
              <path d="M3 22h12" />
            </svg>
            <span class="action-label">饮食记录</span>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </div>
          <div class="action-row" @click="goAction('account')">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
            <span class="action-label">账号与安全</span>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m9 18 6-6-6-6" />
            </svg>
          </div>
          <div class="action-row last" @click="logout">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" x2="9" y1="12" y2="12" />
            </svg>
            <span class="action-label danger">退出登录</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const data = ref(null) // UserProfileVO 根:{userId,username,email,phone,status,lastLoginAt,createdAt,profile:{...}}

// 嵌套的 profile 对象:{nickname,avatarUrl,gender,birthday,heightCm,weightKg,bodyFatPct,fitnessLevel,theme,bio}
const profile = computed(() => data.value?.profile || null)

function genderText(g) {
  return g === 'MALE' ? '男' : g === 'FEMALE' ? '女' : '未设置'
}
function formatDate(str) {
  if (!str) return '-'
  // 后端返回的时间字符串含毫秒/时区,截取日期部分(yyyy-MM-dd)
  return String(str).slice(0, 10)
}

function editProfile() {
  alert('编辑资料功能原型暂未实现,后续将对接 PUT /user/profile')
}

function goAction(key) {
  const labelMap = { records: '训练记录', body: '身体数据', meals: '饮食记录', account: '账号与安全' }
  alert(`「${labelMap[key] || key}」子页面原型暂未实现,这是 BottomNav 外的子入口`)
}

async function logout() {
  await userStore.logout()
  router.replace('/login')
}

async function load() {
  loading.value = true
  try {
    // 优先复用 Layout onMounted 时 store 已拉到的 profile,避免重复请求
    if (userStore.profile) {
      data.value = userStore.profile
    } else {
      data.value = await getProfile()
      userStore.profile = data.value
    }
  } finally {
    loading.value = false
  }
}

watch(
  () => userStore.profile,
  (v) => { if (v && !data.value) data.value = v }
)

onMounted(load)
</script>

<style scoped>
.profile-page {
  padding: 0 16px 80px 16px;
  background: #f6f7fb;
  min-height: 100vh;
}
.page-header {
  padding: 24px 4px 20px 4px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

/* 通用卡片 */
.card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 4px 14px -4px rgba(30, 27, 75, 0.06);
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.card-label {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

/* 用户卡 */
.user-card {
  background: linear-gradient(135deg, #7c5cff 0%, #22d3ee 100%);
  color: white;
  border: none;
}
.user-row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  flex-shrink: 0;
}
.avatar img {
  width: 100%;
  height: 100%;
  border-radius: 999px;
  object-fit: cover;
}
.user-info {
  flex: 1;
}
.nickname {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px 0;
}
.username {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}
.edit-btn {
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #7c5cff;
  background: white;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.15s ease;
}
.edit-btn:active {
  transform: scale(0.95);
}

/* 目标卡 */
.goal-type-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(124, 92, 255, 0.1);
  color: #7c5cff;
}
.no-goal {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
  padding: 12px 0;
  margin: 0;
}

/* 资料卡 */
.info-list {
  display: flex;
  flex-direction: column;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}
.info-row:last-child {
  border-bottom: none;
}
.info-label {
  font-size: 13px;
  color: #64748b;
}
.info-value {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
}

/* 功能入口 */
.action-list {
  display: flex;
  flex-direction: column;
}
.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.15s ease;
}
.action-row.last {
  border-bottom: none;
}
.action-row:active {
  background: #f8fafc;
}
.action-icon {
  width: 18px;
  height: 18px;
  color: #7c5cff;
  flex-shrink: 0;
}
.action-label {
  flex: 1;
  font-size: 14px;
  color: #1e293b;
}
.action-label.danger {
  color: #ef4444;
}
.action-arrow {
  width: 16px;
  height: 16px;
  color: #cbd5e1;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #94a3b8;
  font-size: 14px;
}
</style>
