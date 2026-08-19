<template>
  <div v-loading="loading" class="fp-tab">
    <el-form :model="form" label-width="100px" class="fp-form">
      <el-form-item label="用户名">
        <el-input v-model="form.username" disabled>
          <template #append>
            <el-tooltip content="用户名注册后不可修改" placement="top">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-input v-model="form.createdAt" disabled />
      </el-form-item>
      <el-form-item label="最近登录">
        <el-input v-model="form.lastLoginAt" disabled />
      </el-form-item>
      <el-form-item label="账号状态">
        <el-tag :type="form.status === 1 ? 'success' : 'danger'" effect="light">
          {{ form.status === 1 ? '正常' : '已禁用' }}
        </el-tag>
      </el-form-item>
      <el-divider content-position="left">联系方式（可修改）</el-divider>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="example@qq.com" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="138****8888" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="onSaveAccount">保存账号</el-button>
        <el-button type="warning" plain @click="pwdDialog = true">
          <el-icon><Lock /></el-icon><span>修改密码</span>
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialog" title="修改密码" width="440px" :close-on-click-modal="false">
      <el-form :model="pwdForm" label-width="100px" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingPwd" @click="onSavePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 账号安全 Tab
 * - 用户名只读（注册后不可改）
 * - 邮箱/手机可改
 * - 修改密码弹窗（原密码 + 新密码 + 确认密码，前端校验）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfile, updateMyProfile, updateMyPassword } from '@/api/user'

const loading = ref(false)
const saving = ref(false)
const savingPwd = ref(false)
const pwdDialog = ref(false)
const pwdFormRef = ref(null)

const form = reactive({
  username: '', email: '', phone: '',
  status: 1, createdAt: '', lastLoginAt: ''
})

const pwdForm = reactive({
  oldPassword: '', newPassword: '', confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (value !== pwdForm.newPassword) cb(new Error('两次输入的密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

async function load() {
  loading.value = true
  try {
    const data = await getMyProfile()
    Object.assign(form, {
      username: data.username,
      email: data.email,
      phone: data.phone,
      status: data.status,
      createdAt: data.createdAt,
      lastLoginAt: data.lastLoginAt
    })
  } catch (e) {
    ElMessage.error('加载账号信息失败')
  } finally {
    loading.value = false
  }
}

async function onSaveAccount() {
  if (!form.email) {
    ElMessage.warning('邮箱不能为空')
    return
  }
  saving.value = true
  try {
    await updateMyProfile({ email: form.email, phone: form.phone })
    ElMessage.success('账号信息已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function onSavePwd() {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    savingPwd.value = true
    try {
      await updateMyPassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword,
        confirmPassword: pwdForm.confirmPassword
      })
      ElMessage.success('密码修改成功，下次请使用新密码登录')
      pwdDialog.value = false
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
    } catch (e) {
      ElMessage.error('密码修改失败')
    } finally {
      savingPwd.value = false
    }
  })
}

onMounted(load)
</script>

<style scoped>
.fp-tab { padding: 4px 0; }
.fp-form { max-width: 560px; }
</style>
