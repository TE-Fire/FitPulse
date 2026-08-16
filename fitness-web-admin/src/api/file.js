import request from '@/utils/request'

export function uploadFile(file, bizType = 'misc') {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('bizType', bizType)
  return request.post('/api/v1/file/upload', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
export function listFiles() { return request.get('/api/v1/file/list') }
export function deleteFile(objectName) { return request.delete(/api/v1/file/) }
