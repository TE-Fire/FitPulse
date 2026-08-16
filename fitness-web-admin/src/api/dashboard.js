import request from '@/utils/request'

export function summary() { return request.get('/api/v1/admin/dashboard/summary') }
export function trainingVolume() { return request.get('/api/v1/admin/dashboard/training-volume') }
export function muscleDistribution() { return request.get('/api/v1/admin/dashboard/muscle-distribution') }
export function weightProgress() { return request.get('/api/v1/admin/dashboard/weight-progress') }
export function calorieGap() { return request.get('/api/v1/admin/dashboard/calorie-gap') }
