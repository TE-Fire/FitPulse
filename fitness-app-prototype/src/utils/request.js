// 原型阶段请求工具:调用 mock + 模拟延迟 + 解包 Result<T>.data
// 后续接真实后端时,只需把 mockCall 换成 axios 调用,api/*.js 无需改动
import { delay } from '@/mock'

export async function mockCall(fn, ...args) {
  await delay(300 + Math.random() * 400) // 模拟网络延迟
  const res = fn(...args)
  if (res.code !== 200) {
    throw new Error(res.message || '请求失败')
  }
  return res.data
}
