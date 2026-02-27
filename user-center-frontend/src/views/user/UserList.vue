<template>
  <div class="user-list-container">
    <a-card>
      <template #title>
        <div class="card-header">
          <span>用户管理</span>
          <a-button type="primary" @click="handleAdd">新增用户</a-button>
        </div>
      </template>

      <a-form layout="inline" :model="queryParams" class="search-form">
        <a-form-item label="用户名">
          <a-input v-model:value="queryParams.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :dataSource="tableData"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <a-table-column title="头像" dataIndex="avatarUrl" width="80">
          <template #default="{ record }">
            <a-avatar v-if="record.avatarUrl" :size="40" :src="record.avatarUrl" />
            <a-avatar v-else :size="40">{{ record.username?.charAt(0)?.toUpperCase() }}</a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="ID" dataIndex="id" width="80" />
        <a-table-column title="用户名" dataIndex="username" width="120" />
        <a-table-column title="账号" dataIndex="userAccount" width="120" />
        <a-table-column title="性别" dataIndex="gender" width="80">
          <template #default="{ record }">
            {{ record.gender === 1 ? '男' : record.gender === 2 ? '女' : '未知' }}
          </template>
        </a-table-column>
        <a-table-column title="电话" dataIndex="phone" width="150" />
        <a-table-column title="邮箱" dataIndex="email" width="200" />
        <a-table-column title="角色" dataIndex="userRole" width="100">
          <template #default="{ record }">
            <a-tag :color="record.userRole === 'admin' ? 'red' : 'green'">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column title="创建时间" dataIndex="createTime" width="180" />
        <a-table-column title="操作" width="280">
          <template #default="{ record }">
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" @click="handleResetPassword(record)">重置密码</a-button>
            <a-popconfirm
              title="确定要删除该用户吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record)"
            >
              <a-button type="link" danger>删除</a-button>
            </a-popconfirm>
          </template>
        </a-table-column>
      </a-table>

      <div class="pagination-container">
        <a-pagination
          v-model:current="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-size-options="['10', '20', '50', '100']"
          :total="total"
          show-size-changer
          show-total
          @change="handleSearch"
          @showSizeChange="handleSearch"
        />
      </div>
    </a-card>

    <UserDialog
      v-model:visible="dialogVisible"
      :user-data="currentUser"
      @success="handleSearch"
    />

    <!-- 重置密码弹窗 -->
    <a-modal
      v-model:open="resetPasswordVisible"
      title="重置密码"
      @ok="handleResetPasswordSubmit"
      :confirm-loading="resetPasswordLoading"
      @cancel="resetPasswordVisible = false"
    >
      <a-form layout="vertical">
        <a-form-item label="新密码">
          <a-input-password v-model:value="resetPasswordForm.newPassword" placeholder="请输入新密码" />
        </a-form-item>
        <a-form-item label="确认新密码">
          <a-input-password v-model:value="resetPasswordForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {deleteUser, getUserList, resetPassword} from '@/api/user'
import {message} from 'ant-design-vue'
import UserDialog from './components/UserDialog.vue'
import {useUserStore} from '@/stores/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentUser = ref(null)
const userStore = useUserStore()

// 重置密码相关
const resetPasswordVisible = ref(false)
const resetPasswordLoading = ref(false)
const resetPasswordForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

const queryParams = reactive({
  page: 1,
  size: 10,
  username: ''
})

// 重置密码弹窗
const handleResetPassword = (record) => {
  resetPasswordForm.userId = record.id
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordVisible.value = true
}

// 确认重置密码
const handleResetPasswordSubmit = async () => {
  if (!resetPasswordForm.newPassword) {
    message.error('请输入新密码')
    return
  }
  if (resetPasswordForm.newPassword.length < 6) {
    message.error('密码长度不能少于6位')
    return
  }
  if (resetPasswordForm.newPassword !== resetPasswordForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  
  resetPasswordLoading.value = true
  try {
    await resetPassword(resetPasswordForm.userId, resetPasswordForm.newPassword)
    message.success('密码重置成功')
    resetPasswordVisible.value = false
  } catch (error) {
    message.error(error.message || '密码重置失败')
  } finally {
    resetPasswordLoading.value = false
  }
}

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.records.length > 0 ? res.data.total : 0

    // 检查当前登录用户是否在列表中，如果是则同步更新 store
    if (userStore.userInfo) {
      const currentUserInList = tableData.value.find(u => u.id === userStore.userInfo.id)
      if (currentUserInList) {
        // 检查是否有变化
        const hasChanges =
          currentUserInList.avatarUrl !== userStore.userInfo.avatarUrl ||
          currentUserInList.username !== userStore.userInfo.username ||
          currentUserInList.gender !== userStore.userInfo.gender ||
          currentUserInList.phone !== userStore.userInfo.phone ||
          currentUserInList.email !== userStore.userInfo.email

        if (hasChanges) {
          userStore.userInfo = { ...currentUserInList }
          localStorage.setItem('userInfo', JSON.stringify(currentUserInList))
        }
      }
    }
  } catch (error) {
    message.error(error.message || '获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.page = 1
  handleSearch()
}

const handleAdd = () => {
  currentUser.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentUser.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await deleteUser(row.id)
    message.success('删除成功')
    handleSearch()
  } catch (error) {
    message.error(error.message || '删除失败')
  }
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
