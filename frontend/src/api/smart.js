import request from '@/utils/request'

// 智能仪表盘 API
export function getSmartDashboardData() {
  return request({
    url: '/api/smart/dashboard/data',
    method: 'get'
  })
}

export function getSmartInsights() {
  return request({
    url: '/api/smart/dashboard/insights',
    method: 'get'
  })
}

export function getTodoItems() {
  return request({
    url: '/api/smart/dashboard/todos',
    method: 'get'
  })
}

export function getSystemHealth() {
  return request({
    url: '/api/smart/dashboard/health',
    method: 'get'
  })
}

export function getRealtimeMonitor() {
  return request({
    url: '/api/smart/dashboard/realtime',
    method: 'get'
  })
}

export function getTodayOverview() {
  return request({
    url: '/api/smart/dashboard/today',
    method: 'get'
  })
}

export function getQuickActions() {
  return request({
    url: '/api/smart/dashboard/actions',
    method: 'get'
  })
}

// 智能预警 API
export function getSmartAlerts(params) {
  return request({
    url: '/api/smart/alert/list',
    method: 'get',
    params
  })
}

export function createSmartAlert(data) {
  return request({
    url: '/api/smart/alert/create',
    method: 'post',
    data
  })
}

export function updateAlertStatus(id, status) {
  return request({
    url: `/api/smart/alert/status/${id}`,
    method: 'put',
    data: { status }
  })
}

export function getAlertStatistics() {
  return request({
    url: '/api/smart/alert/statistics',
    method: 'get'
  })
}

// 智能工单 API
export function smartAssignWorkOrder(faultId) {
  return request({
    url: `/api/smart/workorder/assign/${faultId}`,
    method: 'post'
  })
}

export function getMaintainerSkills() {
  return request({
    url: '/api/smart/workorder/skills',
    method: 'get'
  })
}

export function getWorkOrderAnalysis() {
  return request({
    url: '/api/smart/workorder/analysis',
    method: 'get'
  })
}

export function predictCompletionTime(faultId) {
  return request({
    url: `/api/smart/workorder/predict/${faultId}`,
    method: 'get'
  })
}

export function getSchedulingSuggestions() {
  return request({
    url: '/api/smart/workorder/scheduling',
    method: 'get'
  })
}

// 智能健康 API
export function getDeviceHealthScore(deviceCode) {
  return request({
    url: `/api/smart/health/score/${deviceCode}`,
    method: 'get'
  })
}

export function getDeviceHealthRanking() {
  return request({
    url: '/api/smart/health/ranking',
    method: 'get'
  })
}

export function getHealthTrend(deviceCode, days) {
  return request({
    url: `/api/smart/health/trend/${deviceCode}`,
    method: 'get',
    params: { days }
  })
}

export function predictDeviceLifespan(deviceCode) {
  return request({
    url: `/api/smart/health/lifespan/${deviceCode}`,
    method: 'get'
  })
}

export function getMaintenanceOptimization() {
  return request({
    url: '/api/smart/health/optimization',
    method: 'get'
  })
}

export function getOverallHealthReport() {
  return request({
    url: '/api/smart/health/report',
    method: 'get'
  })
}

export function getHighRiskDevices() {
  return request({
    url: '/api/smart/health/high-risk',
    method: 'get'
  })
}

// 智能通知 API
export function getNotifications(params) {
  return request({
    url: '/api/smart/notification/list',
    method: 'get',
    params
  })
}

export function getUnreadCount(userId) {
  return request({
    url: '/api/smart/notification/unread',
    method: 'get',
    params: { userId }
  })
}

export function markNotificationRead(id) {
  return request({
    url: `/api/smart/notification/read/${id}`,
    method: 'post'
  })
}

export function markAllNotificationsRead(userId) {
  return request({
    url: '/api/smart/notification/read-all',
    method: 'post',
    params: { userId }
  })
}

export function deleteNotification(id) {
  return request({
    url: `/api/smart/notification/${id}`,
    method: 'delete'
  })
}

export function getNotificationStats(userId) {
  return request({
    url: '/api/smart/notification/stats',
    method: 'get',
    params: { userId }
  })
}

export function subscribeNotification(userId, type) {
  return request({
    url: '/api/smart/notification/subscribe',
    method: 'post',
    params: { userId, type }
  })
}

export function unsubscribeNotification(userId, type) {
  return request({
    url: '/api/smart/notification/unsubscribe',
    method: 'post',
    params: { userId, type }
  })
}

export function generateNotifications() {
  return request({
    url: '/api/smart/notification/generate',
    method: 'post'
  })
}
