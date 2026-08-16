import request from '@/utils/request'

export function login(data) {
  return request.post('/api/v1/auth/login', data)
}
export function register(data) {
  return request.post('/api/v1/auth/register', data)
}
export function refreshToken(refreshToken) {
  return request.post('/api/v1/auth/refresh', { refreshToken })
}
export function logout() {
  return request.post('/api/v1/auth/logout')
}
export function getUserProfile() {
  return request.get('/api/v1/user/profile')
}
export function updateProfile(data) {
  return request.put('/api/v1/user/profile', data)
}
export function getUserGoal() {
  return request.get('/api/v1/user/goal')
}
export function updateGoal(data) {
  return request.put('/api/v1/user/goal', data)
}
export function changePassword(data) {
  return request.put('/api/v1/user/password', data)
}
