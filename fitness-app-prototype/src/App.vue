<template>
  <!-- App Shell:居中显示一个手机宽(390px)的容器,模拟手机屏 -->
  <div class="app-shell">
    <div class="phone-frame">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
// 根组件:仅承担 App Shell 容器职责
</script>

<style scoped>
.app-shell {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  background: #e9ebf0;
}
.phone-frame {
  width: 100%;
  max-width: 420px;
  min-height: 100vh;
  background: #f6f7fb;
  position: relative;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.04), 0 12px 40px -8px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}
@media (min-width: 768px) {
  .app-shell {
    align-items: center;
    padding: 0 16px;
  }
  .phone-frame {
    max-width: 390px;
    min-height: 92vh;
    margin: 4vh 0;
    border-radius: 28px;
  }
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
