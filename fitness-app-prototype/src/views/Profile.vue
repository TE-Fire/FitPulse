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
            <span v-if="!data.avatarUrl">{{ (data.nickname || data.username || 'F').charAt(0).toUpperCase() }}</span>
            <img v-else :src="data.avatarUrl" alt="avatar" />
          </div>
          <div class="user-info">
            <p class="nickname">{{ data.nickname || data.username }}</p>
            <p class="username">@{{ data.username }}</p>
          </div>
          <button class="edit-btn">编辑</button>
        </div>
      </section>

      <!-- 目标卡 -->
      <section class="card goal-card">
        <div class="card-head">
          <span class="card-label">我的目标</span>
          <span class="goal-type-tag">{{ data.goal?.goalTypeText || '未设置' }}</span>
        </div>
        <div v-if="data.goal" class="goal-grid">
          <div class="goal-item">
            <p class="goal-label">目标体重</p>
            <p class="goal-value">{{ data.goal.targetWeightKg }}<span class="goal-unit">kg</span></p>
          </div>
          <div class="goal-item">
            <p class="goal-label">每周训练</p>
            <p class="goal-value">{{ data.goal.weeklyWorkoutDays }}<span class="goal-unit">天</span></p>
          </div>
          <div class="goal-item">
            <p class="goal-label">每日热量</p>
            <p class="goal-value">{{ data.goal.kcalTarget }}<span class="goal-unit">kcal</span></p>
          </div>
          <div class="goal-item">
            <p class="goal-label">每日饮水</p>
            <p class="goal-value">{{ data.goal.waterGoalMl }}<span class="goal-unit">ml</span></p>
          </div>
        </div>
        <p v-else class="no-goal">尚未设置训练目标</p>
      </section>

      <!-- 资料卡 -->
      <section class="card info-card">
        <div class="card-head">
          <span class="card-label">基本资料</span>
        </div>
        <div class="info-list">
          <div class="info-row">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ data.email }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">性别</span>
            <span class="info-value">{{ genderText(data.gender) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">身高</span>
            <span class="info-value">{{ data.heightCm ? data.heightCm + ' cm' : '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">生日</span>
            <span class="info-value">{{ data.birthday || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">注册时间</span>
            <span class="info-value">{{ data.registeredAt }}</span>
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
          <div class="action-row" @click="goAction('goal')">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10" />
              <circle cx="12" cy="12" r="6" />
              <circle cx="12" cy="12" r="2" />
            </svg>
            <span class="action-label">我的目标</span>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMe } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const data = ref(null)

function genderText(g) {
  return g === 1 ? '男' : g === 2 ? '女' : '未设置'
}

function goAction(key) {
  // 原型阶段:子页面未实现,以提示替代
  alert(`「${key}」子页面原型暂未实现,这是 BottomNav 外的子入口`)
}

function logout() {
  userStore.logout()
  router.replace('/login')
}

onMounted(async () => {
  try {
    data.value = await getMe()
  } finally {
    loading.value = false
  }
})
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
.goal-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.goal-item {
  background: #f8fafc;
  padding: 10px 12px;
  border-radius: 10px;
}
.goal-label {
  font-size: 11px;
  color: #64748b;
  margin: 0 0 4px 0;
}
.goal-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}
.goal-unit {
  font-size: 11px;
  font-weight: 500;
  color: #94a3b8;
  margin-left: 2px;
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
