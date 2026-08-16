import request from '@/utils/request'

export function listExercises(params) { return request.get('/api/v1/training/exercises', { params }) }
export function getExercise(id) { return request.get(/api/v1/training/exercises/) }
export function addExercise(data) { return request.post('/api/v1/training/exercises', data) }
export function updateExercise(id, data) { return request.put(/api/v1/training/exercises/, data) }
export function deleteExercise(id) { return request.delete(/api/v1/training/exercises/) }
export function listPlans(params) { return request.get('/api/v1/training/plans', { params }) }
export function addPlan(data) { return request.post('/api/v1/training/plans', data) }
export function updatePlan(id, data) { return request.put(/api/v1/training/plans/, data) }
export function listRecords(params) { return request.get('/api/v1/training/records', { params }) }
