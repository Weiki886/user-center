import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 【重要】在成功回调中也检测 401 错误
    // 因为后端返回 HTTP 200 + code 401，不会触发错误回调
    if (res.code === 401 || res.message === '请先登录') {
      handleUnauthorized()
      return Promise.reject(new Error(res.message || '登录已过期'))
    }

    if (res.code === 0) {
      return res
    } else {
      return Promise.reject(res)
    }
  },
  (error) => {
    // 重要：错误可能来自两个地方：
    // 1. HTTP 错误（如 401、500）- error.response 存在
    // 2. 业务错误（code !== 0）- error.response 可能不存在，但 error 本身就是后端返回的数据

    // 优先使用 error.response，如果不存在则使用 error 本身
    const res = error.response?.data || error
    const status = error.response?.status

    // 处理 401 未授权错误（HTTP 状态码或业务 code）
    if (status === 401 || res.code === 401 || res.message === '请先登录') {
      handleUnauthorized()
      return Promise.reject(new Error(res.message || '登录已过期'))
    }

    // 403: 禁止访问
    if (status === 403 || res.code === 403) {
      message.error('您没有权限访问该资源')
      return Promise.reject(error)
    }

    // 404: 资源不存在
    if (status === 404 || res.code === 404) {
      message.error('请求的资源不存在')
      return Promise.reject(error)
    }

    // 500: 服务器错误
    if (status >= 500 || res.code === 500) {
      message.error('服务器繁忙，请稍后重试')
      return Promise.reject(error)
    }

    // 其他业务错误，直接返回错误信息
    if (res.message) {
      return Promise.reject(new Error(res.message))
    }

    // 网络错误或其他未知错误
    if (!error.response) {
      message.error('网络连接失败，请检查网络')
      return Promise.reject(new Error('网络连接失败'))
    }

    return Promise.reject(error)
  }
)

// 处理未授权（登录过期）
function handleUnauthorized() {
  // 获取 userStore 实例
  const userStore = useUserStore()

  // 清理本地存储的登录信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')

  // 同步清理 Pinia 状态（重要！否则 Layout 不会更新）
  userStore.token = ''
  userStore.userInfo = null

  // 提示用户
  message.warning('登录已过期，请重新登录')

  // 跳转到登录页
  router.push('/login')
}

export default request