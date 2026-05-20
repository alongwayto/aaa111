import request from '@/utils/request'

export const getDevicePage = (params) => request.get('/device', { params })
export const getDeviceById = (id) => request.get(`/device/${id}`)
export const createDevice = (data) => request.post('/device', data)
export const updateDevice = (id, data) => request.put(`/device/${id}`, data)
export const deleteDevice = (id) => request.delete(`/device/${id}`)
export const batchDeleteDevices = (ids) => request.delete('/device/batch', { data: ids })
export const importDevices = (formData) => request.post('/device/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const downloadImportTemplate = () => request.get('/device/import-template', { responseType: 'blob' })
export const exportDevices = (params) => request.get('/device/export', { params, responseType: 'blob' })
export const updateOnlineStatus = (id, onlineStatus) => request.put(`/device/${id}/online-status`, null, { params: { onlineStatus } })

// 分类
export const getCategories = () => request.get('/device/categories')
export const createCategory = (data) => request.post('/device/categories', data)
export const updateCategory = (id, data) => request.put(`/device/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/device/categories/${id}`)

// 位置
export const getLocations = () => request.get('/device/locations')
export const createLocation = (data) => request.post('/device/locations', data)
export const updateLocation = (id, data) => request.put(`/device/locations/${id}`, data)
export const deleteLocation = (id) => request.delete(`/device/locations/${id}`)

// ============ 实时数据接口 ============

/**
 * 获取实时设备概览
 */
export const getRealtimeOverview = () => request.get('/device/realtime/overview')

/**
 * 获取实时设备列表
 */
export const getRealtimeDevices = () => request.get('/device/realtime/devices')

/**
 * 获取设备实时性能数据
 */
export const getDevicePerformance = (deviceCode) => request.get(`/device/realtime/performance/${deviceCode}`)

/**
 * 获取设备历史性能趋势
 */
export const getDeviceTrend = (deviceCode, hours = 24) => 
  request.get('/device/realtime/trend', { params: { deviceCode, hours } })

/**
 * 订阅设备实时指标
 */
export const subscribeDevice = (deviceCodes) => 
  request.post('/device/realtime/subscribe', { deviceCodes })

/**
 * 取消订阅设备
 */
export const unsubscribeDevice = (deviceCodes) => 
  request.delete('/device/realtime/subscribe', { data: { deviceCodes } })
