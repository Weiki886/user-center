<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <template #title>
        <div class="card-header">个人中心</div>
      </template>
      <template #extra>
        <a-button type="link" @click="showPasswordModal">
          <LockOutlined /> 修改密码
        </a-button>
      </template>

      <a-form :model="form" :rules="rules" ref="formRef" layout="vertical">
        <!-- 头像上传 -->
        <a-form-item label="头像">
          <div class="avatar-wrapper">
            <a-upload
              list-type="picture-card"
              :show-upload-list="false"
              :before-upload="beforeUpload"
              :custom-request="customRequest"
              class="avatar-uploader"
            >
              <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar-img" />
              <div v-else class="avatar-placeholder">
                <plus-outlined />
              </div>
            </a-upload>
          </div>
          <div class="avatar-tip">支持 jpg、png 格式，大小不超过 10MB</div>
        </a-form-item>

        <a-form-item label="用户名" name="username">
          <a-input v-model:value="form.username" placeholder="请输入用户名" />
        </a-form-item>

        <a-form-item label="账号">
          <a-input v-model:value="form.userAccount" disabled />
        </a-form-item>

        <a-form-item label="性别" name="gender">
          <a-radio-group v-model:value="form.gender">
            <a-radio :value="0">未知</a-radio>
            <a-radio :value="1">男</a-radio>
            <a-radio :value="2">女</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="电话" name="phone">
          <a-input v-model:value="form.phone" placeholder="请输入电话号码" />
        </a-form-item>

        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="form.email" placeholder="请输入邮箱地址" />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" :loading="loading" @click="handleSubmit" size="large">
              保存修改
            </a-button>
            <a-button @click="resetForm" size="large">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="passwordModalVisible"
      title="修改密码"
      @ok="handlePasswordSubmit"
      :confirm-loading="passwordLoading"
      @cancel="passwordModalVisible = false"
    >
      <a-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" layout="vertical">
        <a-form-item label="旧密码" name="oldPassword">
          <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入旧密码" />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认新密码" name="checkPassword">
          <a-input-password v-model:value="passwordForm.checkPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {LockOutlined, PlusOutlined} from '@ant-design/icons-vue'
import {updatePassword, updateUser, uploadAvatar} from '@/api/user'
import {useUserStore} from '@/stores/user'
import {message} from 'ant-design-vue'

const router = useRouter()

const formRef = ref(null)
const loading = ref(false)
const userStore = useUserStore()

// 密码修改相关
const passwordModalVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  checkPassword: ''
})

const form = ref({
  id: '',
  username: '',
  userAccount: '',
  gender: 0,
  phone: '',
  email: '',
  avatarUrl: ''
})

// 保存原始数据用于重置
const originalForm = ref({})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20之间', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  checkPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        if (value !== passwordForm.value.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
}

// 显示修改密码弹窗
const showPasswordModal = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    checkPassword: ''
  }
  passwordModalVisible.value = true
}

// 提交修改密码
const handlePasswordSubmit = async () => {
  await passwordFormRef.value.validate()
  passwordLoading.value = true
  try {
    await updatePassword(form.value.id, passwordForm.value.oldPassword, passwordForm.value.newPassword)
    
    // 先隐藏弹窗
    passwordModalVisible.value = false
    
    // 清除本地存储的用户信息
    userStore.logout()
    
    // 跳转到登录页面，并传递成功消息
    router.push({
      path: '/login',
      query: { from: 'passwordChanged' }
    })
  } catch (error) {
    message.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

// 初始化用户信息
onMounted(() => {
  // 先从 localStorage 恢复用户信息到 store
  userStore.initUserInfo()
  
  // 从 store 获取用户信息
  if (userStore.userInfo) {
    form.value = { ...userStore.userInfo }
    originalForm.value = { ...userStore.userInfo }
  }
})

// 处理头像上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    message.error('头像大小不能超过 10MB!')
    return false
  }
  
  // 先显示预览图
    const reader = new FileReader()
    reader.onload = (e) => {
      form.value.avatarUrl = e.target.result
    }
  reader.readAsDataURL(file)
  
  return false // 阻止自动上传，使用自定义上传
}

// 自定义上传请求
const customRequest = async (options) => {
  const { file, onSuccess, onError } = options
  
  try {
    // 调用头像上传接口
    const userId = form.value.id
    const result = await uploadAvatar(userId, file)
    
    // 上传成功后更新表单中的头像URL
    if (result.data) {
      form.value.avatarUrl = result.data
    }
    
    onSuccess(result)
    message.success('头像上传成功')
  } catch (error) {
    onError(error)
    message.error(error.message || '头像上传失败')
  }
}

// 重置表单
const resetForm = () => {
  form.value = { ...originalForm.value }
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await updateUser(form.value.id, {
      username: form.value.username,
      gender: form.value.gender,
      phone: form.value.phone,
      email: form.value.email,
      avatarUrl: form.value.avatarUrl
    })

    // 更新 store 中的用户信息
    const updatedUserInfo = {
      ...userStore.userInfo,
      username: form.value.username,
      gender: form.value.gender,
      phone: form.value.phone,
      email: form.value.email,
      avatarUrl: form.value.avatarUrl
    }
    userStore.userInfo = updatedUserInfo
    localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))

    originalForm.value = { ...form.value }
    message.success('保存成功')
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.avatar-wrapper {
  display: inline-block;
}

.avatar-uploader {
  width: 120px !important;
  height: 120px !important;
  line-height: 120px !important;
}

.avatar-uploader :deep(.ant-upload) {
  width: 120px !important;
  height: 120px !important;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #8c939d;
}

.avatar-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
