<template>
  <div class="smart-dashboard">
    <el-row :gutter="20">
      <!-- 左侧：智能洞察 -->
      <el-col :span="16">
        <!-- 系统健康评分 -->
        <el-card class="health-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">系统健康评分</span>
              <el-tag :type="getHealthTagType(systemHealth.status)">
                {{ systemHealth.statusDesc }}
              </el-tag>
            </div>
          </template>
          <div class="health-content">
            <div class="health-score">
              <el-progress type="circle" :percentage="systemHealth.score || 0" 
                :color="getHealthColor(systemHealth.status)"
                :width="120" />
            </div>
            <div class="health-stats">
              <div class="stat-item">
                <span class="label">在线率</span>
                <span class="value">{{ systemHealth.onlineRate || 0 }}%</span>
              </div>
              <div class="stat-item">
                <span class="label">本周故障</span>
                <span class="value danger">{{ systemHealth.weekFaults || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="label">待处理预警</span>
                <span class="value warning">{{ systemHealth.pendingAlerts || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="label">设备总数</span>
                <span class="value">{{ systemHealth.totalDevices || 0 }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 智能洞察列表 -->
        <el-card class="insights-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">智能洞察</span>
              <el-button type="primary" link @click="refreshInsights">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
            </div>
          </template>
          <div class="insights-list" v-loading="loading">
            <div v-for="insight in insights" :key="insight.category" 
              class="insight-item" :class="'insight-' + insight.type">
              <div class="insight-icon">
                <el-icon :size="24">
                  <component :is="getInsightIcon(insight.category)" />
                </el-icon>
              </div>
              <div class="insight-content">
                <div class="insight-title">{{ insight.title }}</div>
                <div class="insight-text">{{ insight.insight }}</div>
              </div>
              <el-tag :type="getInsightTagType(insight.type)" size="small">
                {{ getInsightTypeText(insight.type) }}
              </el-tag>
            </div>
            <el-empty v-if="!loading && insights.length === 0" description="暂无洞察数据" />
          </div>
        </el-card>

        <!-- 实时监控趋势 -->
        <el-card class="monitor-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">实时监控</span>
              <span class="update-time">更新于 {{ realtime.updateTime }}</span>
            </div>
          </template>
          <div class="monitor-content">
            <div class="monitor-summary">
              <div class="summary-item">
                <span class="number success">{{ realtime.onlineDevices || 0 }}</span>
                <span class="text">在线设备</span>
              </div>
              <div class="summary-item">
                <span class="number">{{ realtime.totalDevices || 0 }}</span>
                <span class="text">设备总数</span>
              </div>
              <div class="summary-item">
                <span class="number warning">{{ (realtime.totalDevices || 0) - (realtime.onlineDevices || 0) }}</span>
                <span class="text">离线设备</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：待办和快捷操作 -->
      <el-col :span="8">
        <!-- 待办事项 -->
        <el-card class="todo-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">待办事项</span>
              <el-badge :value="todoItems.length" :hidden="todoItems.length === 0">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="todo in todoItems" :key="todo.id" 
              class="todo-item" :class="'priority-' + todo.priority">
              <div class="todo-priority">
                <el-tag :type="getPriorityType(todo.priority)" size="small">
                  {{ getPriorityText(todo.priority) }}
                </el-tag>
              </div>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-text">{{ todo.content }}</div>
              </div>
              <div class="todo-count">{{ todo.count }}</div>
            </div>
            <el-empty v-if="todoItems.length === 0" description="暂无待办" />
          </div>
        </el-card>

        <!-- 今日概览 -->
        <el-card class="today-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">今日概览</span>
              <span class="date">{{ today.date }}</span>
            </div>
          </template>
          <div class="today-stats">
            <div class="today-stat">
              <el-icon class="icon"><Plus /></el-icon>
              <span class="value">{{ today.newDevices || 0 }}</span>
              <span class="label">新增设备</span>
            </div>
            <div class="today-stat">
              <el-icon class="icon warning"><Warning /></el-icon>
              <span class="value warning">{{ today.newFaults || 0 }}</span>
              <span class="label">新增故障</span>
            </div>
            <div class="today-stat">
              <el-icon class="icon"><Document /></el-icon>
              <span class="value">{{ today.newWorkOrders || 0 }}</span>
              <span class="label">新增工单</span>
            </div>
            <div class="today-stat">
              <el-icon class="icon success"><CircleCheck /></el-icon>
              <span class="value success">{{ today.completedWorkOrders || 0 }}</span>
              <span class="label">已完成</span>
            </div>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="actions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="title">快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <div v-for="action in quickActions" :key="action.id" 
              class="action-item" @click="handleAction(action)">
              <el-icon :size="24" class="action-icon">
                <component :is="action.icon" />
              </el-icon>
              <span class="action-title">{{ action.title }}</span>
            </div>
          </div>
        </el-card>

        <!-- AI 智能助手入口 -->
        <el-card class="ai-card" shadow="hover" @click="$router.push('/ai')">
          <div class="ai-content">
            <el-icon :size="48" class="ai-icon"><ChatDotRound /></el-icon>
            <div class="ai-text">
              <div class="ai-title">AI 智能助手</div>
              <div class="ai-desc">智能诊断、故障分析、维护建议</div>
            </div>
            <el-icon class="arrow"><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSmartDashboardData, getSmartInsights, getTodoItems, getSystemHealth, getRealtimeMonitor, getTodayOverview, getQuickActions } from '@/api/smart'
import { Refresh, Bell, Plus, Warning, Document, CircleCheck, ChatDotRound, ArrowRight, TrendCharts, WarningFilled, Money, DataLine } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const insights = ref([])
const todoItems = ref([])
const systemHealth = ref({})
const realtime = ref({})
const today = ref({})
const quickActions = ref([])

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

const loadDashboardData = async () => {
  loading.value = true
  try {
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
  } catch (error) {
    console.error('加载智能仪表盘数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const refreshInsights = async () => {
  try {
    const res = await getSmartInsights()
    if (res.code === 200) {
      insights.value = res.data || []
      ElMessage.success('洞察已刷新')
    }
  } catch (error) {
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
})
</script>

<style scoped>
.smart-dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 16px;
  font-weight: 600;
}

.health-card {
  margin-bottom: 20px;
}

.health-content {
  display: flex;
  align-items: center;
  gap: 40px;
}

.health-score {
  flex-shrink: 0;
}

.health-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  flex: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-item .value {
  font-size: 20px;
  font-weight: 600;
}

.stat-item .value.danger {
  color: #F56C6C;
}

.stat-item .value.warning {
  color: #E6A23C;
}

.insights-card {
  margin-bottom: 20px;
}

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
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.insight-item:hover {
  transform: translateX(4px);
}

.insight-item.insight-critical {
  background: #fef0f0;
  border-left: 4px solid #F56C6C;
}

.insight-item.insight-warning {
  background: #fdf6ec;
  border-left: 4px solid #E6A23C;
}

.insight-item.insight-positive {
  background: #f0f9eb;
  border-left: 4px solid #67C23A;
}

.insight-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
}

.insight-content {
  flex: 1;
}

.insight-title {
  font-weight: 600;
  margin-bottom: 4px;
}

.insight-text {
  font-size: 13px;
  color: #606266;
}

.monitor-card {
  margin-bottom: 20px;
}

.update-time {
  font-size: 12px;
  color: #909399;
}

.monitor-summary {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.summary-item .number {
  display: block;
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
}

.summary-item .number.success {
  color: #67C23A;
}

.summary-item .number.warning {
  color: #E6A23C;
}

.summary-item .text {
  font-size: 14px;
  color: #909399;
}

.todo-card {
  margin-bottom: 20px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
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
  font-weight: 600;
  margin-bottom: 4px;
}

.todo-text {
  font-size: 12px;
  color: #606266;
}

.todo-count {
  font-size: 20px;
  font-weight: 600;
  color: #409EFF;
}

.today-card {
  margin-bottom: 20px;
}

.date {
  font-size: 12px;
  color: #909399;
}

.today-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.today-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.today-stat .icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.today-stat .icon.success {
  color: #67C23A;
}

.today-stat .icon.warning {
  color: #E6A23C;
}

.today-stat .value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.today-stat .value.success {
  color: #67C23A;
}

.today-stat .value.warning {
  color: #E6A23C;
}

.today-stat .label {
  font-size: 12px;
  color: #909399;
}

.actions-card {
  margin-bottom: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 8px;
}

.action-title {
  font-size: 12px;
  text-align: center;
}

.ai-card {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px dashed #409EFF;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9eb 100%);
}

.ai-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.ai-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ai-icon {
  color: #409EFF;
}

.ai-text {
  flex: 1;
}

.ai-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.ai-desc {
  font-size: 12px;
  color: #606266;
}

.arrow {
  color: #409EFF;
}
</style>
