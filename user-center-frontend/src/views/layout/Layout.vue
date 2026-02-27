<template>
                    <a-config-provider>
    <a-layout class="layout-container">
      <a-layout-sider width="200px" class="aside">
        <div class="logo">用户中心</div>
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="inline"
          :router="true"
        >
          <a-menu-item key="/home" @click="router.push('/home')">
            <template #icon>
              <HomeOutlined />
            </template>
            <span>首页</span>
          </a-menu-item>
          <a-menu-item v-if="userStore.isAdmin" key="/user" @click="router.push('/user')">
            <template #icon>
              <UserOutlined />
            </template>
            <span>用户管理</span>
          </a-menu-item>
        </a-menu>
      </a-layout-sider>

      <a-layout>
        <a-layout-header class="header">
          <div class="header-right">
            <a-dropdown>
              <div class="user-dropdown">
                <a-avatar :size="32" :src="userStore.userInfo?.avatarUrl">
                  {{ userStore.username?.charAt(0)?.toUpperCase() }}
                </a-avatar>
                <span class="username">{{ userStore.username }}</span>
                <DownOutlined />
              </div>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="profile" @click="router.push('/profile')">
                    <template #icon>
                      <UserOutlined />
                    </template>
                    个人中心
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout" @click="handleLogout">
                    <template #icon>
                      <LogoutOutlined />
                    </template>
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </a-layout-header>

        <a-layout-content class="main">
          <router-view v-slot="{ Component }">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </router-view>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </a-config-provider>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {DownOutlined, HomeOutlined, LogoutOutlined, UserOutlined} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const selectedKeys = ref(['/home'])

// 监听路由变化，更新菜单选中状态
watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
  { immediate: true }
)

// 初始化用户信息
onMounted(() => {
  userStore.initUserInfo()
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.aside {
  background: #ffffff;
  border-right: 1px solid #f0f0f0;
}

.logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: #000;
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 24px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-dropdown:hover {
  background-color: #f5f5f5;
}

.username {
  margin: 0 8px;
  color: #333;
}

.main {
  background-color: #f0f2f5;
  padding: 24px;
  min-height: calc(100vh - 64px);
}
</style>
