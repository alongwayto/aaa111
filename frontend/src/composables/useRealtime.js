/**
 * 实时数据管理 - 使用轮询机制实现数据的实时更新
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRealtimeOverview, getRealtimeDevices } from '@/api/device'
import { getActiveAlerts, getWorkOrderStats } from '@/api/fault'

// 全局实时数据状态
const realtimeState = reactive({
  overview: null,
  devices: [],
  alerts: [],
  workOrderStats: null,
  lastUpdate: null,
  isConnected: false,
  updateCount: 0
})

// 轮询定时器
let pollTimer = null
let alertTimer = null

// 实时数据回调
const callbacks = {
  overview: [],
  devices: [],
  alerts: [],
  workOrder: []
}

/**
 * 注册数据更新回调
 */
export function onRealtimeUpdate(type, callback) {
  if (callbacks[type]) {
    callbacks[type].push(callback)
  }
}

/**
 * 移除数据更新回调
 */
export function offRealtimeUpdate(type, callback) {
  if (callbacks[type]) {
    const index = callbacks[type].indexOf(callback)
    if (index > -1) {
      callbacks[type].splice(index, 1)
    }
  }
}

/**
 * 获取实时概览数据
 */
async function fetchOverview() {
  try {
    const res = await getRealtimeOverview()
    if (res.code === 200) {
      realtimeState.overview = res.data
      realtimeState.lastUpdate = new Date()
      callbacks.overview.forEach(cb => cb(res.data))
    }
  } catch (error) {
    console.error('获取实时概览失败:', error)
  }
}

/**
 * 获取实时设备列表
 */
async function fetchDevices() {
  try {
    const res = await getRealtimeDevices()
    if (res.code === 200) {
      realtimeState.devices = res.data || []
      callbacks.devices.forEach(cb => cb(realtimeState.devices))
    }
  } catch (error) {
    console.error('获取实时设备失败:', error)
  }
}

/**
 * 获取活跃告警
 */
async function fetchAlerts() {
  try {
    const res = await getActiveAlerts()
    if (res.code === 200) {
      const oldCount = realtimeState.alerts.filter(a => !a.read).length
      realtimeState.alerts = res.data || []
      
      // 如果有新的未读告警，发送通知
      const newCount = realtimeState.alerts.filter(a => !a.read).length
      if (newCount > oldCount && oldCount > 0) {
        const newAlerts = realtimeState.alerts.filter(a => !a.read).slice(0, newCount - oldCount)
        newAlerts.forEach(alert => {
          ElMessage.warning({
            message: `收到新告警: ${alert.alertName}`,
            duration: 5000,
            showClose: true
          })
        })
      }
      
      callbacks.alerts.forEach(cb => cb(realtimeState.alerts))
    }
  } catch (error) {
    console.error('获取实时告警失败:', error)
  }
}

/**
 * 获取工单统计
 */
async function fetchWorkOrderStats() {
  try {
    const res = await getWorkOrderStats()
    if (res.code === 200) {
      realtimeState.workOrderStats = res.data
      callbacks.workOrder.forEach(cb => cb(res.data))
    }
  } catch (error) {
    console.error('获取工单统计失败:', error)
  }
}

/**
 * 启动实时数据轮询
 * @param {Object} options 配置选项
 */
export function startRealtimePoll(options = {}) {
  const {
    overviewInterval = 30000,    // 概览数据刷新间隔（毫秒）
    devicesInterval = 10000,      // 设备数据刷新间隔
    alertsInterval = 15000,       // 告警数据刷新间隔
    workOrderInterval = 60000    // 工单统计刷新间隔
  } = options

  // 如果已经在运行，先停止
  stopRealtimePoll()

  realtimeState.isConnected = true
  console.log('实时数据轮询已启动')

  // 立即获取一次数据
  Promise.all([
    fetchOverview(),
    fetchDevices(),
    fetchAlerts(),
    fetchWorkOrderStats()
  ])

  // 设置定时器
  pollTimer = setInterval(() => {
    fetchOverview()
    fetchDevices()
  }, devicesInterval)

  alertTimer = setInterval(() => {
    fetchAlerts()
  }, alertsInterval)

  // 工单统计间隔较长
  setInterval(() => {
    fetchWorkOrderStats()
  }, workOrderInterval)
}

/**
 * 停止实时数据轮询
 */
export function stopRealtimePoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (alertTimer) {
    clearInterval(alertTimer)
    alertTimer = null
  }
  realtimeState.isConnected = false
  console.log('实时数据轮询已停止')
}

/**
 * 强制刷新所有数据
 */
export async function refreshAll() {
  await Promise.all([
    fetchOverview(),
    fetchDevices(),
    fetchAlerts(),
    fetchWorkOrderStats()
  ])
  realtimeState.updateCount++
}

/**
 * 获取实时状态
 */
export function useRealtime() {
  return {
    state: realtimeState,
    startRealtimePoll,
    stopRealtimePoll,
    refreshAll,
    onRealtimeUpdate,
    offRealtimeUpdate
  }
}

/**
 * 组合式函数：在组件中使用实时数据
 */
export function useRealtimeData(options = {}) {
  const { autoStart = true } = options
  
  const state = reactive({
    overview: null,
    devices: [],
    alerts: [],
    workOrderStats: null,
    isLoading: ref(false)
  })

  // 数据更新回调
  const onOverviewUpdate = (data) => { state.overview = data }
  const onDevicesUpdate = (data) => { state.devices = data }
  const onAlertsUpdate = (data) => { state.alerts = data }
  const onWorkOrderUpdate = (data) => { state.workOrderStats = data }

  onMounted(() => {
    if (autoStart) {
      startRealtimePoll(options)
    }
    
    // 注册回调
    onRealtimeUpdate('overview', onOverviewUpdate)
    onRealtimeUpdate('devices', onDevicesUpdate)
    onRealtimeUpdate('alerts', onAlertsUpdate)
    onRealtimeUpdate('workOrder', onWorkOrderUpdate)
  })

  onUnmounted(() => {
    // 移除回调
    offRealtimeUpdate('overview', onOverviewUpdate)
    offRealtimeUpdate('devices', onDevicesUpdate)
    offRealtimeUpdate('alerts', onAlertsUpdate)
    offRealtimeUpdate('workOrder', onWorkOrderUpdate)
  })

  return {
    ...state,
    refreshAll
  }
}

export default {
  useRealtime,
  useRealtimeData,
  startRealtimePoll,
  stopRealtimePoll,
  refreshAll
}
