<template>
  <div v-loading="loading" class="fp-tab">
    <el-form :model="form" label-width="100px" class="fp-form">
      <el-form-item label="头像">
        <div class="fp-avatar-row">
          <div class="fp-avatar">
            <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="avatar" />
            <span v-else>{{ letterAvatar }}</span>
          </div>
          <el-upload
            :show-file-list="false"
            :http-request="onAvatarUpload"
            accept="image/*"
          >
            <el-button :loading="uploading" plain>
              <el-icon><Upload /></el-icon><span>更换头像</span>
            </el-button>
          </el-upload>
          <span class="fp-hint">支持 JPG/PNG，建议 256×256</span>
        </div>
      </el-form-item>

      <el-form-item label="昵称">
        <el-input v-model="form.nickname" maxlength="32" show-word-limit />
      </el-form-item>

      <el-form-item label="性别">
        <el-radio-group v-model="form.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
          <el-radio :value="0">未知</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="生日">
        <el-date-picker v-model="form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
      </el-form-item>

      <el-form-item label="身高 (cm)">
        <el-input-number v-model="form.heightCm" :min="100" :max="250" :precision="1" />
      </el-form-item>

      <el-form-item label="体重 (kg)">
        <el-input-number v-model="form.weightKg" :min="30" :max="200" :precision="1" disabled />
        <span class="fp-hint">由身体数据自动同步</span>
      </el-form-item>

      <el-form-item label="体脂率 (%)">
        <el-input-number v-model="form.bodyFatPct" :min="3" :max="60" :precision="1" disabled />
        <span class="fp-hint">由身体数据自动同步</span>
      </el-form-item>

      <el-form-item label="健身等级">
        <el-select v-model="form.fitnessLevel" placeholder="选择等级">
          <el-option :value="1" label="入门" />
          <el-option :value="2" label="进阶" />
          <el-option :value="3" label="达人" />
          <el-option :value="4" label="专业" />
        </el-select>
      </el-form-item>

      <el-form-item label="简介">
        <el-input v-model="form.bio" type="textarea" :rows="3" maxlength="200" show-word-limit />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSaveProfile">保存资料</el-button>
        <el-button @click="load">重置</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">训练目标</el-divider>

    <el-form :model="form.goal" label-width="100px" class="fp-form">
      <el-form-item label="目标类型">
        <el-select v-model="form.goal.goalType">
          <el-option :value="1" label="减脂" />
          <el-option :value="2" label="增肌" />
          <el-option :value="3" label="塑形" />
          <el-option :value="4" label="维持健康" />
          <el-option :value="5" label="力量举" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标体重"><el-input-number v-model="form.goal.targetWeight" :min="30" :max="200" :precision="1" /></el-form-item>
      <el-form-item label="目标体脂"><el-input-number v-model="form.goal.targetBodyFat" :min="3" :max="60" :precision="1" /></el-form-item>
      <el-form-item label="每周训练"><el-input-number v-model="form.goal.weeklyWorkouts" :min="1" :max="14" /></el-form-item>
      <el-form-item label="每日热量"><el-input-number v-model="form.goal.dailyCalories" :min="1000" :max="5000" :step="50" /></el-form-item>
      <el-form-item label="每日饮水"><el-input-number v-model="form.goal.dailyWaterMl" :min="500" :max="5000" :step="100" /></el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker v-model="form.goal.startDate" type="date" value-format="YYYY-MM-DD" disabled />
      </el-form-item>
      <el-form-item label="目标日期">
        <el-date-picker v-model="form.goal.targetDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="savingGoal" @click="onSaveGoal">保存目标</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
/**
 * 基本资料 Tab
 * - 头像上传（el-upload custom http-request → uploadAvatar）
 * - 资料表单（昵称/性别/生日/身高/简介/健身等级）
 * - 体重/体脂只读（缓存自 body_metric，由健康模块写入）
 * - 训练目标表单（goal 子对象，独立保存）
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfile, updateMyProfile, updateMyGoal, uploadAvatar } from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const savingGoal = ref(false)
const uploading = ref(false)

const form = reactive({
  nickname: '', avatarUrl: '', gender: 0,
  birthday: '', heightCm: 170, weightKg: 0, bodyFatPct: 0,
  fitnessLevel: 1, bio: '',
  goal: {
    goalType: 2, targetWeight: 80, targetBodyFat: 15,
    weeklyWorkouts: 4, dailyCalories: 2200, dailyWaterMl: 2000,
    startDate: '', targetDate: ''
  }
})

const letterAvatar = computed(() => (form.nickname || 'F').charAt(0).toUpperCase())

async function load() {
  loading.value = true
  try {
    const data = await getMyProfile()
    Object.assign(form, data)
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
    ElMessage.success('头像上传成功，记得保存资料')
  } catch (e) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

async function onSaveProfile() {
  saving.value = true
  try {
    const payload = {
      nickname: form.nickname,
      avatarUrl: form.avatarUrl,
      gender: form.gender,
      birthday: form.birthday,
      heightCm: form.heightCm,
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

async function onSaveGoal() {
  savingGoal.value = true
  try {
    await updateMyGoal(form.goal)
    ElMessage.success('目标已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingGoal.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.fp-tab { padding: 4px 0; }
.fp-form { max-width: 640px; }
.fp-avatar-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.fp-avatar {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7c5cff, #22d3ee);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; font-weight: 700;
  overflow: hidden;
  border: 2px solid var(--border);
}
.fp-avatar img { width: 100%; height: 100%; object-fit: cover; }
.fp-hint { color: var(--text-muted); font-size: 12px; }
</style>
