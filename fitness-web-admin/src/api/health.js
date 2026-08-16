import request from '@/utils/request'

export function listBodyMetrics(params) { return request.get('/api/v1/health/body-metrics', { params }) }
export function addBodyMetric(data) { return request.post('/api/v1/health/body-metrics', data) }
export function updateBodyMetric(id, data) { return request.put(/api/v1/health/body-metrics/, data) }
export function listFoods(params) { return request.get('/api/v1/health/foods', { params }) }
export function addFood(data) { return request.post('/api/v1/health/foods', data) }
export function updateFood(id, data) { return request.put(/api/v1/health/foods/, data) }
export function deleteFood(id) { return request.delete(/api/v1/health/foods/) }
