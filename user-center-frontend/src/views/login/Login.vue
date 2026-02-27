<template>
  <div class="login-container">
    <a-card class="login-card">
      <h2>用户登录</h2>
      <a-form :model="form" :rules="rules" ref="formRef" layout="vertical">
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
        <a-form-item>
          <a-button type="primary" @click="handleLogin" :loading="loading" size="large" block>
            登录
          </a-button>
        </a-form-item>
        <a-form-item>
          <a-button type="link" @click="$router.push('/register')">还没有账号？去注册</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {message} from 'ant-design-vue'
import {LockOutlined, UserOutlined} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  userAccount: '',
  userPassword: ''
})

const rules = {
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, max: 20, message: '账号长度在4-20之间', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20之间', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.loginAction(form.userAccount, form.userPassword)
    message.success('登录成功')
    router.push('/')
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时检查是否有密码修改成功消息
onMounted(() => {
  if (route.query.from === 'passwordChanged') {
    message.success('密码修改成功，请重新登录')
    // 清除URL参数，避免刷新时重复显示
    router.replace({ path: '/login' })
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.login-card {
  width: 400px;
  text-align: center;
}

.login-card h2 {
  margin-bottom: 24px;
  color: #333;
}

/* 表单验证错误提示左对齐 */
:deep(.ant-form-item-explain) {
  text-align: left;
}
</style>
