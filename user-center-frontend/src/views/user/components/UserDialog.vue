<template>
  <a-modal
    v-model:open="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="600px"
    :closable="false"
    @cancel="handleClose"
  >
    <a-form :model="form" :rules="rules" ref="formRef" layout="vertical">
      <a-form-item label="头像">
        <a-upload
          list-type="picture-card"
          :show-upload-list="false"
          :before-upload="beforeUpload"
          :custom-request="customRequest"
        >
          <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar" />
          <div v-else class="avatar-uploader">
            <plus-outlined />
          </div>
        </a-upload>
      </a-form-item>
      <a-form-item label="用户名" name="username">
        <a-input v-model:value="form.username" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="账号" name="userAccount">
        <a-input v-model:value="form.userAccount" placeholder="请输入账号" :disabled="isEdit" />
      </a-form-item>
      <a-form-item v-if="!isEdit" label="密码" name="userPassword">
        <a-input-password v-model:value="form.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item v-if="!isEdit" label="确认密码" name="checkPassword">
        <a-input-password v-model:value="form.checkPassword" placeholder="请确认密码" />
      </a-form-item>
      <a-form-item label="性别" name="gender">
        <a-radio-group v-model:value="form.gender">
          <a-radio :value="0">未知</a-radio>
          <a-radio :value="1">男</a-radio>
          <a-radio :value="2">女</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="电话" name="phone">
        <a-input v-model:value="form.phone" placeholder="请输入电话" />
      </a-form-item>
      <a-form-item label="邮箱" name="email">
        <a-input v-model:value="form.email" placeholder="请输入邮箱" />
      </a-form-item>
    </a-form>

    <template #footer>
      <a-button @click="handleClose">取消</a-button>
      <a-button type="primary" :loading="loading" @click="handleSubmit">确定</a-button>
    </template>
  </a-modal>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {PlusOutlined} from '@ant-design/icons-vue'
import {register, updateUser, uploadAvatar} from '@/api/user'
import {message} from 'ant-design-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  userData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

// 创建本地 ref 来控制 dialog 显示
const visible = ref(false)

// 监听 props.visible 的变化，同步到本地
watch(() => props.visible, (val) => {
  visible.value = val
})

// 监听本地变化并通知父组件
watch(visible, (val) => {
  emit('update:visible', val)
})

const formRef = ref(null)
const loading = ref(false)
const avatarUploading = ref(false)

const form = ref({
  username: '',
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  gender: 0,
  phone: '',
  email: '',
  avatarUrl: ''
})

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
  checkPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.value.userPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const isEdit = computed(() => !!props.userData?.id)

// 监听 visible 和 userData 变化
watch(() => [visible.value, props.userData], ([val, userData]) => {
  if (val) {
    if (userData && userData.id) {
      form.value = { ...userData }
    } else {
      form.value = {
        username: '',
        userAccount: '',
        userPassword: '',
        checkPassword: '',
        gender: 0,
        phone: '',
        email: '',
        avatarUrl: ''
      }
    }
  }
}, { immediate: true })

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
  
  return false // 阻止自动上传
}

// 自定义上传请求
const customRequest = async (options) => {
  const { file, onSuccess, onError } = options
  
  // 如果是编辑模式，使用当前用户ID；新增模式需要先保存用户
  const userId = props.userData?.id
  
  if (!userId) {
    // 新增模式：Base64 预览已经在 beforeUpload 中处理了
    // 提示用户保存后再重新上传真正的图片
    onSuccess({ data: form.value.avatarUrl })
    return
  }

  try {
    avatarUploading.value = true
    // 调用头像上传接口
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
  } finally {
    avatarUploading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  setTimeout(() => {
    formRef.value?.resetFields()
    form.value = {
      username: '',
      userAccount: '',
      userPassword: '',
      checkPassword: '',
      gender: 0,
      phone: '',
      email: '',
      avatarUrl: ''
    }
  }, 100)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateUser(props.userData.id, form.value)
      message.success('更新成功')
    } else {
      await register({
        username: form.value.username,
        userAccount: form.value.userAccount,
        userPassword: form.value.userPassword,
        checkPassword: form.value.checkPassword,
        gender: form.value.gender,
        phone: form.value.phone,
        email: form.value.email,
        avatarUrl: form.value.avatarUrl
      })
      message.success('新增成功')
    }
    emit('success')
    handleClose()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #8c939d;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar {
  width: 100px;
  height: 100px;
  object-fit: cover;
}
</style>
