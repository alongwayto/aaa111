<template>
  <div class="smart-dashboard" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="header-icon">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="header-text">
            <h1>智能分析中心</h1>
            <p>基于 AI 的设备健康分析、故障预测与智能决策</p>
          </div>
        </div>
        <div class="header-actions">
          <div class="realtime-indicator">
            <span class="pulse-dot"></span>
            <span class="update-text">自动更新中 · {{ lastUpdateTime }}</span>
          </div>
          <el-button type="primary" @click="refreshInsights">
            <el-icon><Refresh /></el-icon>
            刷新洞察
          </el-button>
        </div>
      </div>
    </div>

    <el-row :gutter="20" class="content-row">
      <!-- 左侧主区域 -->
      <el-col :xs="24" :lg="16">
        <!-- 系统健康评分卡片 -->
        <div class="health-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><DataAnalysis /></el-icon>
              系统健康评分
            </div>
            <el-tag :type="getHealthTagType(systemHealth.status)" effect="dark" size="large">
              {{ systemHealth.statusDesc }}
            </el-tag>
          </div>
          <div class="health-content">
            <div class="health-score-wrapper">
              <el-progress
                type="circle"
                :percentage="systemHealth.score || 0"
                :color="getHealthColor(systemHealth.status)"
                :width="140"
                :stroke-width="12"
              >
                <template #default>
                  <div class="score-inner">
                    <span class="score-value">{{ systemHealth.score || 0 }}</span>
                    <span class="score-label">健康分</span>
                  </div>
                </template>
              </el-progress>
            </div>
            <div class="health-stats">
              <div class="stat-grid">
                <div class="stat-item">
                  <div class="stat-icon green"><el-icon><Connection /></el-icon></div>
                  <div class="stat-info">
                    <span class="stat-value">{{ systemHealth.onlineRate || 0 }}%</span>
                    <span class="stat-label">在线率</span>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
                  <div class="stat-info">
                    <span class="stat-value">{{ systemHealth.weekFaults || 0 }}</span>
                    <span class="stat-label">本周故障</span>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon orange"><el-icon><Bell /></el-icon></div>
                  <div class="stat-info">
                    <span class="stat-value">{{ systemHealth.pendingAlerts || 0 }}</span>
                    <span class="stat-label">待处理预警</span>
                  </div>
                </div>
                <div class="stat-item">
                  <div class="stat-icon blue"><el-icon><Monitor /></el-icon></div>
                  <div class="stat-info">
                    <span class="stat-value">{{ systemHealth.totalDevices || 0 }}</span>
                    <span class="stat-label">设备总数</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 智能洞察列表 -->
        <div class="insights-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><MagicStick /></el-icon>
              智能洞察
              <span class="badge">{{ insights.length }} 条</span>
            </div>
          </div>
          <div class="insights-list">
            <div
              v-for="(insight, index) in insights"
              :key="insight.category"
              class="insight-item"
              :class="'insight-' + insight.type"
              :style="{ animationDelay: `${index * 0.1}s` }"
            >
              <div class="insight-icon">
                <el-icon :size="22">
                  <component :is="getInsightIcon(insight.category)" />
                </el-icon>
              </div>
              <div class="insight-content">
                <div class="insight-title">{{ insight.title }}</div>
                <div class="insight-text">{{ insight.insight }}</div>
              </div>
              <el-tag :type="getInsightTagType(insight.type)" size="small" effect="dark">
                {{ getInsightTypeText(insight.type) }}
              </el-tag>
            </div>
            <el-empty v-if="!loading && insights.length === 0" description="暂无洞察数据，系统将持续学习分析" :image-size="80" />
          </div>
        </div>

        <!-- 实时监控 -->
        <div class="monitor-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Odometer /></el-icon>
              实时监控
            </div>
            <span class="update-time">
              <span class="pulse-dot"></span>
              更新于 {{ realtime.updateTime }}
            </span>
          </div>
          <div class="monitor-content">
            <div class="monitor-summary">
              <div class="summary-item online">
                <div class="summary-circle">
                  <el-icon :size="28"><CircleCheck /></el-icon>
                </div>
                <span class="number">{{ realtime.onlineDevices || 0 }}</span>
                <span class="text">在线设备</span>
              </div>
              <div class="summary-divider">
                <span>/</span>
              </div>
              <div class="summary-item">
                <div class="summary-circle total">
                  <el-icon :size="28"><Monitor /></el-icon>
                </div>
                <span class="number">{{ realtime.totalDevices || 0 }}</span>
                <span class="text">设备总数</span>
              </div>
              <div class="summary-divider">
                <span>/</span>
              </div>
              <div class="summary-item offline">
                <div class="summary-circle warning">
                  <el-icon :size="28"><CircleClose /></el-icon>
                </div>
                <span class="number">{{ (realtime.totalDevices || 0) - (realtime.onlineDevices || 0) }}</span>
                <span class="text">离线设备</span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧边栏 -->
      <el-col :xs="24" :lg="8">
        <!-- 待办事项 -->
        <div class="sidebar-card todo-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Bell /></el-icon>
              待办事项
            </div>
            <el-badge :value="todoItems.length" type="danger" />
          </div>
          <div class="todo-list">
            <div
              v-for="todo in todoItems"
              :key="todo.id"
              class="todo-item"
              :class="'priority-' + todo.priority"
            >
              <div class="todo-priority">
                <el-tag :type="getPriorityType(todo.priority)" size="small" effect="dark">
                  {{ getPriorityText(todo.priority) }}
                </el-tag>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-text">{{ todo.content }}</div>
              </div>
              <div class="todo-count">{{ todo.count }}</div>
            </div>
            <el-empty v-if="todoItems.length === 0" description="暂无待办" :image-size="60" />
          </div>
        </div>

        <!-- 今日概览 -->
        <div class="sidebar-card today-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Calendar /></el-icon>
              今日概览
            </div>
            <span class="date-badge">{{ today.date }}</span>
          </div>
          <div class="today-stats">
            <div class="today-stat">
              <div class="stat-icon"><el-icon><Plus /></el-icon></div>
              <div class="stat-value">{{ today.newDevices || 0 }}</div>
              <div class="stat-label">新增设备</div>
            </div>
            <div class="today-stat warning">
              <div class="stat-icon"><el-icon><Warning /></el-icon></div>
              <div class="stat-value">{{ today.newFaults || 0 }}</div>
              <div class="stat-label">新增故障</div>
            </div>
            <div class="today-stat">
              <div class="stat-icon"><el-icon><Document /></el-icon></div>
              <div class="stat-value">{{ today.newWorkOrders || 0 }}</div>
              <div class="stat-label">新增工单</div>
            </div>
            <div class="today-stat success">
              <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
              <div class="stat-value">{{ today.completedWorkOrders || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="sidebar-card actions-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Operation /></el-icon>
              快捷操作
            </div>
          </div>
          <div class="quick-actions">
            <div
              v-for="action in quickActions"
              :key="action.id"
              class="action-item"
              @click="handleAction(action)"
            >
              <div class="action-icon-wrapper">
                <el-icon :size="20">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <span class="action-title">{{ action.title }}</span>
            </div>
          </div>
        </div>

        <!-- AI 智能助手入口 -->
        <div class="ai-entry-card" @click="$router.push('/ai')">
          <div class="ai-bg-effect"></div>
          <div class="ai-content">
            <div class="ai-icon-wrapper">
              <el-icon :size="36"><ChatDotRound /></el-icon>
            </div>
            <div class="ai-text">
              <div class="ai-title">AI 智能助手</div>
              <div class="ai-desc">智能诊断、故障分析、维护建议</div>
            </div>
            <el-icon class="ai-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSmartDashboardData, getSmartInsights, getTodoItems } from '@/api/smart'
import {
  Refresh, Bell, Plus, Warning, Document, CircleCheck, CircleClose,
  ChatDotRound, ArrowRight, TrendCharts, WarningFilled, Money,
  DataLine, Cpu, DataAnalysis, Connection, Monitor, MagicStick,
  Odometer, Calendar, Operation, Grid
} from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const insights = ref([])
const todoItems = ref([])
const systemHealth = ref({})
const realtime = ref({})
const today = ref({})
const quickActions = ref([])
const isRealtimeActive = ref(false)
const lastUpdateTime = ref('--')
let realtimeTimer = null

const getHealthTagType = (status) => {
  const map = { excellent: 'success', good: 'success', fair: 'warning', warning: 'warning', critical: 'danger' }
  return map[status] || 'info'
}

const getHealthColor = (status) => {
  const map = { excellent: '#67C23A', good: '#85CE61', fair: '#E6A23C', warning: '#F56C6C', critical: '#F56C6C' }
  return map[status] || '#909399'
}

const getInsightIcon = (category) => {
  const map = { health: 'TrendCharts', fault: 'WarningFilled', maintenance: 'Tools', cost: 'Money', distribution: 'DataLine' }
  return map[category] || 'InfoFilled'
}

const getInsightTagType = (type) => {
  const map = { positive: 'success', info: 'info', warning: 'warning', critical: 'danger' }
  return map[type] || 'info'
}

const getInsightTypeText = (type) => {
  const map = { positive: '良好', info: '提示', warning: '注意', critical: '紧急' }
  return map[type] || type
}

const getPriorityType = (priority) => {
  const map = { critical: 'danger', high: 'warning', medium: 'info', low: 'info' }
  return map[priority] || 'info'
}

const getPriorityText = (priority) => {
  const map = { critical: '紧急', high: '高', medium: '中', low: '低' }
  return map[priority] || priority
}

const loadDashboardData = async (silent = false) => {
  try {
    if (!silent) loading.value = true
    const [dashboardRes, insightsRes, todosRes] = await Promise.all([
      getSmartDashboardData(),
      getSmartInsights(),
      getTodoItems()
    ])

    if (dashboardRes.code === 200) {
      const data = dashboardRes.data
      systemHealth.value = data.systemHealth || {}
      realtime.value = data.realtimeMonitor || {}
      today.value = data.todayOverview || {}
      quickActions.value = data.quickActions || []
    }

    if (insightsRes.code === 200) {
      insights.value = insightsRes.data || []
    }

    if (todosRes.code === 200) {
      todoItems.value = todosRes.data || []
    }
    
    lastUpdateTime.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('加载智能仪表盘数据失败:', error)
  } finally {
    if (!silent) loading.value = false
  }
}

const refreshInsights = async () => {
  try {
    const res = await getSmartInsights()
    if (res.code === 200) {
      insights.value = res.data || []
      ElMessage.success('洞察已刷新')
    }
  } catch {
    ElMessage.error('刷新失败')
  }
}

const handleAction = (action) => {
  if (action.path) {
    router.push(action.path)
  }
}

onMounted(() => {
  loadDashboardData()
  startRealtime()
})

onUnmounted(() => {
  stopRealtime()
})

function startRealtime() {
  isRealtimeActive.value = true
  realtimeTimer = setInterval(async () => {
    await loadDashboardData(true)
  }, 30000) // 每30秒自动更新
  console.log('智能仪表盘实时更新已启动')
}

function stopRealtime() {
  if (realtimeTimer) {
    clearInterval(realtimeTimer)
    realtimeTimer = null
  }
  isRealtimeActive.value = false
  console.log('智能仪表盘实时更新已停止')
}
</script>

<style scoped>
.smart-dashboard {
  padding: 0;
  background: linear-gradient(180deg, #f0f4f8 0%, #e8eef5 100%);
  min-height: calc(100vh - 112px);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 28px 24px;
  margin: -24px -24px 24px -24px;
  border-radius: 0 0 24px 24px;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
}

.header-text h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: white;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.realtime-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: #67C23A;
  border-radius: 50%;
  animation: pulse-animation 1.5s ease-in-out infinite;
}

@keyframes pulse-animation {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.update-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

/* 通用卡片样式 */
.health-card, .insights-card, .monitor-card, .sidebar-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.title-icon {
  font-size: 20px;
  color: #667eea;
}

.badge {
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: normal;
}

/* 健康评分卡片 */
.health-content {
  display: flex;
  align-items: center;
  gap: 48px;
}

.health-score-wrapper {
  flex-shrink: 0;
}

.score-inner {
  text-align: center;
}

.score-value {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.score-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  flex: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: transform 0.3s;
}

.stat-item:hover {
  transform: translateX(4px);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-icon.green { background: linear-gradient(135deg, #67c23a, #52c41a); }
.stat-icon.red { background: linear-gradient(135deg, #f56c6c, #f56c6c); }
.stat-icon.orange { background: linear-gradient(135deg, #e6a23c, #f39c12); }
.stat-icon.blue { background: linear-gradient(135deg, #409eff, #1890ff); }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

/* 洞察列表 */
.insights-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.insight-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid transparent;
  transition: all 0.3s;
  animation: slideIn 0.5s ease forwards;
  opacity: 0;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}

.insight-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.insight-item.insight-critical {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.insight-item.insight-warning {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.insight-item.insight-positive {
  background: #f0f9eb;
  border-left-color: #67c23a;
}

.insight-icon {
  width: 44px;
  height: 44px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.insight-content {
  flex: 1;
  min-width: 0;
}

.insight-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.insight-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

/* 实时监控 */
.update-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: #67c23a;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.monitor-summary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 20px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.summary-circle {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #67c23a, #52c41a);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.summary-circle.total {
  background: linear-gradient(135deg, #409eff, #1890ff);
}

.summary-circle.warning {
  background: linear-gradient(135deg, #e6a23c, #f39c12);
}

.summary-item .number {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.summary-item .text {
  font-size: 14px;
  color: #909399;
}

.summary-divider {
  font-size: 32px;
  color: #dcdfe6;
}

/* 待办事项 */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 12px;
  transition: transform 0.3s;
}

.todo-item:hover {
  transform: translateX(4px);
}

.todo-item.priority-critical {
  background: #fef0f0;
}

.todo-item.priority-high {
  background: #fdf6ec;
}

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.todo-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.todo-count {
  font-size: 24px;
  font-weight: 700;
  color: #667eea;
}

/* 今日概览 */
.date-badge {
  background: #f0f2f5;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  color: #909399;
}

.today-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.today-stat {
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  text-align: center;
}

.today-stat .stat-icon {
  width: 40px;
  height: 40px;
  margin: 0 auto 8px;
  background: linear-gradient(135deg, #409eff, #1890ff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.today-stat.warning .stat-icon {
  background: linear-gradient(135deg, #e6a23c, #f39c12);
}

.today-stat.success .stat-icon {
  background: linear-gradient(135deg, #67c23a, #52c41a);
}

.today-stat .stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.today-stat .stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.action-icon-wrapper {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.action-item:hover .action-icon-wrapper {
  background: rgba(255, 255, 255, 0.3);
}

.action-title {
  font-size: 12px;
  color: #606266;
  text-align: center;
}

.action-item:hover .action-title {
  color: white;
}

/* AI 入口卡片 */
.ai-entry-card {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s;
}

.ai-entry-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.ai-bg-effect {
  position: absolute;
  right: -30px;
  bottom: -30px;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.ai-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  color: white;
}

.ai-icon-wrapper {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-text {
  flex: 1;
}

.ai-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
}

.ai-desc {
  font-size: 13px;
  opacity: 0.85;
}

.ai-arrow {
  font-size: 24px;
  opacity: 0.8;
}

/* 响应式 */
@media (max-width: 992px) {
  .health-content {
    flex-direction: column;
    gap: 24px;
  }
  
  .stat-grid {
    width: 100%;
  }
  
  .monitor-summary {
    flex-direction: column;
    gap: 16px;
  }
  
  .summary-divider {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-header {
    margin: -24px -16px 16px -16px;
    padding: 20px 16px;
    border-radius: 0 0 16px 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>
