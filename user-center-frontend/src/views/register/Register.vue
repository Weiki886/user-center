<template>
  <div class="register-container">
    <a-card class="register-card">
      <h2>用户注册</h2>
      <a-form :model="form" :rules="rules" ref="formRef" layout="vertical">
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" placeholder="请输入用户名" size="large">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="form.userAccount" placeholder="请输入账号" size="large">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="form.userPassword" placeholder="请输入密码" size="large">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item label="确认密码">
          <a-input-password v-model:value="form.checkPassword" placeholder="请确认密码" size="large">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>
        <a-form-item label="验证码" name="captchaCode">
          <div class="captcha-container">
            <a-input
              v-model:value="form.captchaCode"
              placeholder="请输入验证码"
              size="large"
              class="captcha-input"
            >
              <template #prefix>
                <SafetyOutlined />
              </template>
            </a-input>
            <img
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-image"
              @click="refreshCaptcha"
              title="点击刷新验证码"
            />
            <div v-else class="captcha-loading" @click="refreshCaptcha">
              <LoadingOutlined />
            </div>
          </div>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleRegister" :loading="loading" size="large" block>
            注册
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="link" @click="$router.push('/login')">已有账号？去登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {message} from 'ant-design-vue'
import {LoadingOutlined, LockOutlined, SafetyOutlined, UserOutlined} from '@ant-design/icons-vue'
import {getCaptcha} from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  captchaId: '',
  captchaCode: ''
})

const captchaImage = ref('')

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20之间', trigger: 'blur' }
  ],
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度在4-20之间', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20之间', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 获取验证码
const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    form.captchaId = res.data.captchaId
    captchaImage.value = res.data.captchaImage
  } catch (error) {
    message.error('获取验证码失败')
    console.error('获取验证码失败:', error)
  }
}

const handleRegister = async () => {
  if (form.userPassword !== form.checkPassword) {
    message.error('两次密码不一致')
    return
  }
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.registerAction(
      form.username,
      form.userAccount,
      form.userPassword,
      form.checkPassword,
      form.captchaId,
      form.captchaCode
    )
    message.success('注册成功')
    router.push('/login')
  } catch (error) {
    message.error(error.message || '注册失败')
    // 如果注册失败，刷新验证码
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 页面加载时获取验证码
onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.register-card {
  width: 400px;
  text-align: center;
}

.register-card h2 {
  margin-bottom: 24px;
  color: #333;
}

/* 表单验证错误提示左对齐 */
:deep(.ant-form-item-explain) {
  text-align: left;
}

/* 验证码容器 */
.captcha-container {
  display: flex;
  gap: 12px;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: border-color 0.3s;
}

.captcha-image:hover {
  border-color: #1890ff;
}

.captcha-loading {
  width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  font-size: 20px;
  color: #999;
}

.captcha-loading:hover {
  color: #1890ff;
  background: #e6f7ff;
}
</style>
