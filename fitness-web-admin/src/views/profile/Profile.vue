<template>
  <div class="fp-page">
    <div class="fp-page__header">
      <div>
        <h1 class="fp-page__title">个人中心</h1>
        <p class="fp-page__desc">资料管理 · 账号安全 · 训练统计 · 健康概览 · 设置 · 关于</p>
      </div>
    </div>

    <div class="fp-card fp-tabs-card">
      <el-tabs v-model="activeTab" class="fp-tabs">
        <el-tab-pane label="基本资料" name="basic">
          <BasicTab v-if="loadedOnce.basic" />
        </el-tab-pane>
        <el-tab-pane label="账号安全" name="account">
          <AccountTab v-if="loadedOnce.account" />
        </el-tab-pane>
        <el-tab-pane label="训练统计" name="training">
          <TrainingTab v-if="loadedOnce.training" />
        </el-tab-pane>
        <el-tab-pane label="健康概览" name="health">
          <HealthTab v-if="loadedOnce.health" />
        </el-tab-pane>
        <el-tab-pane label="设置" name="settings">
          <SettingsTab v-if="loadedOnce.settings" />
        </el-tab-pane>
        <el-tab-pane label="关于" name="about">
          <AboutTab v-if="loadedOnce.about" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
/**
 * 个人中心容器 —— 6 Tab 切换
 * 懒加载：仅当 Tab 首次被切到时挂载子组件（loadedOnce 跟踪）
 * 各 Tab 子组件位于 views/profile/tabs/
 */
import { ref, reactive, watch } from 'vue'
import BasicTab from './tabs/BasicTab.vue'
import AccountTab from './tabs/AccountTab.vue'
import TrainingTab from './tabs/TrainingTab.vue'
import HealthTab from './tabs/HealthTab.vue'
import SettingsTab from './tabs/SettingsTab.vue'
import AboutTab from './tabs/AboutTab.vue'

const activeTab = ref('basic')
const loadedOnce = reactive({
  basic: true, account: false, training: false,
  health: false, settings: false, about: false
})

watch(activeTab, name => { loadedOnce[name] = true })
</script>

<style scoped>
.fp-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.fp-page__title { margin: 0; font-size: 22px; color: var(--text); }
.fp-page__desc  { margin: 4px 0 0; font-size: 13px; color: var(--text-soft); }

.fp-tabs-card { padding: 8px 20px 20px; }
.fp-tabs :deep(.el-tabs__header) { margin-bottom: 18px; }
.fp-tabs :deep(.el-tabs__nav-wrap::after) { background-color: var(--border); }
.fp-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  height: 42px;
  padding: 0 18px;
}
.fp-tabs :deep(.el-tabs__item.is-active) { color: var(--fit-brand); font-weight: 600; }
.fp-tabs :deep(.el-tabs__active-bar) {
  background-color: var(--fit-brand);
  height: 3px;
  border-radius: 2px;
}
</style>
