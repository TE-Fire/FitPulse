// User 模块 API(对齐 docs/接口文档.md 八、User)
import { mockUserMe } from '@/mock'
import { mockCall } from '@/utils/request'

// 我的资料 GET /user/me
export function getMe() {
  return mockCall(mockUserMe)
}
