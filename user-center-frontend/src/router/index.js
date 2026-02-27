import {createRouter, createWebHistory} from 'vue-router'
import {message} from 'ant-design-vue'
import {useUserStore} from '@/stores/user'
import {getCurrentUser} from '@/api/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/Home.vue')
      },
      {
        path: 'user',
        name: 'User',
        component: () => import(/* webpackPrefetch: true */ '@/views/user/UserList.vue'),
        meta: { requiresAuth: true, roles: ['admin'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 每次路由跳转时，如果有 token，都验证其是否有效
  // 这样在页面刷新或后端重启后都能检测到登录过期
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const res = await getCurrentUser()
      // 验证成功，更新用户信息
      userStore.userInfo = res.data
      userStore.token = token
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    } catch (error) {
      // token 无效或已过期，清除登录信息
      // 响应拦截器已经处理了 401 提示和跳转，这里不需要重复提示
      userStore.token = ''
      userStore.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      next('/login')
      return
    }
  } else {
    // 没有 token，初始化用户信息
    userStore.initUserInfo()
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }

    // 检查角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
      const userRole = userStore.userRole
      if (!to.meta.roles.includes(userRole)) {
        // 无权限，跳转到首页
        message.warning('您没有权限访问该页面')
        next('/home')
        return
      }
    }
  }

  // 已登录用户访问登录页，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
    next('/home')
    return
  }

  next()
})

export default router

