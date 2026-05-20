import request from '@/utils/request'

export const getFaultPage = (params) => request.get('/fault', { params })
export const getFaultById = (id) => request.get(`/fault/${id}`)
export const createFault = (data) => request.post('/fault', data)
export const assignWorkOrder = (data) => request.post('/fault/assign', data)
export const importHistoricalWorkOrder = (data) => request.post('/fault/workorder/import', data)
export const startHandle = (id) => request.post(`/fault/workorder/${id}/start`)
export const completeHandle = (data) => request.post('/fault/workorder/complete', data)
export const archiveFault = (id) => request.post(`/fault/${id}/archive`)
export const evaluateWorkOrder = (id, data) => request.post(`/fault/workorder/${id}/evaluate`, data)
export const getWorkOrderPage = (params) => request.get('/fault/workorders', { params })
export const getWorkOrderByFaultId = (faultId) => request.get(`/fault/${faultId}/workorder`)

// ============ 告警相关 ============

export const getAlertPage = (params) => request.get('/alert', { params })
export const getAlertById = (id) => request.get(`/alert/${id}`)
export const createAlert = (data) => request.post('/alert', data)
export const updateAlertStatus = (id, status) => request.put(`/alert/${id}/status`, null, { params: { status } })

/**
 * 获取活跃告警（实时更新用）
 */
export const getActiveAlerts = () => request.get('/alert/active')

/**
 * 获取告警统计
 */
export const getAlertStatistics = () => request.get('/alert/statistics')

/**
 * 确认告警
 */
export const confirmAlert = (id) => request.post(`/alert/${id}/confirm`)

/**
 * 处理告警
 */
export const handleAlert = (id, data) => request.post(`/alert/${id}/handle`, data)

// ============ 工单统计 ============

/**
 * 获取工单统计（实时更新用）
 */
export const getWorkOrderStats = () => request.get('/fault/workorder/stats')

/**
 * 获取待处理工单列表
 */
export const getPendingWorkOrders = () => request.get('/fault/workorders/pending')
