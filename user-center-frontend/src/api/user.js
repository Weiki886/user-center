import request from './request'

// 登录
export function login(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

// 注册
export function register(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

// 获取验证码图片
export function getCaptcha() {
  return request({
    url: '/captcha/generate',
    method: 'get'
  })
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

// 上传头像
export function uploadAvatar(userId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/file/avatar/${userId}`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 修改密码（用户修改自己的密码，需要旧密码）
export function updatePassword(id, oldPassword, newPassword) {
  return request({
    url: `/user/${id}/password`,
    method: 'put',
    params: {
      oldPassword,
      newPassword
    }
  })
}

// 重置密码（管理员重置用户密码）
export function resetPassword(id, newPassword) {
  return request({
    url: `/user/${id}/reset-password`,
    method: 'put',
    data: {
      newPassword
    }
  })
}

// 获取当前登录用户信息（用于验证 token 是否有效）
export function getCurrentUser() {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

// 批量删除用户（前端循环调用单个删除实现）
export function batchDeleteUsers(ids) {
  // 循环调用单个删除 API
  return Promise.all(ids.map(id => deleteUser(id)))
}

// 批量更新用户状态（前端循环调用单个更新实现）
export function batchUpdateStatus(ids, status) {
  // 循环调用单个更新 API
  return Promise.all(ids.map(id => updateUser(id, { userStatus: status })))
}
