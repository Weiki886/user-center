import {defineStore} from 'pinia'
import {login, register} from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    isAdmin: (state) => state.userInfo?.userRole === 'admin',
    userRole: (state) => state.userInfo?.userRole || 'user'
  },
  
  actions: {
    async loginAction(userAccount, userPassword) {
      try {
        const res = await login({ userAccount, userPassword })
        this.token = res.data.token
        this.userInfo = res.data.user
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return true
      } catch (error) {
        throw error
      }
    },
    
    async registerAction(username, userAccount, userPassword, checkPassword, captchaId, captchaCode) {
      try {
        const res = await register({ username, userAccount, userPassword, checkPassword, captchaId, captchaCode })
        return res.data
      } catch (error) {
        throw error
      }
    },
    
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    initUserInfo() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          this.userInfo = JSON.parse(userInfoStr)
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
    }
  }
})

