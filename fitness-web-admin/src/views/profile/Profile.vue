<template>
  <div class="fp-card fp-profile-placeholder">
    <el-icon :size="48" color="#7c5cff"><UserFilled /></el-icon>
    <h2>个人中心</h2>
    <p>本页为占位，下轮将拆为 6 Tab：</p>
    <ul>
      <li>基本资料</li>
      <li>账号安全</li>
      <li>训练统计</li>
      <li>健康概览</li>
      <li>设置</li>
      <li>关于</li>
    </ul>
  </div>
</template>

<script setup>
// 占位组件，下轮实现 6 Tab（task d7）
</script>

<style scoped>
.fp-profile-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 48px 32px;
  text-align: center;
}
.fp-profile-placeholder h2 { margin: 8px 0 4px; color: var(--text); }
.fp-profile-placeholder p { color: var(--text-soft); margin: 0; }
.fp-profile-placeholder ul {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
  display: grid;
  grid-template-columns: repeat(3, auto);
  gap: 6px 18px;
  color: var(--text-muted);
  font-size: 13px;
}
</style>
