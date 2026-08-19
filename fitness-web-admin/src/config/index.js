/**
 * 全局配置
 * - USE_MOCK：看板/用户模块是否走前端 mock（开发期默认 true）
 *   切换方式：在项目根目录 .env 设 VITE_USE_MOCK=false 即可走真实后端
 */
export const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') !== 'false'
