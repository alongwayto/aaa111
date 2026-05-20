<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="greeting">
          <h1 class="page-title">运营驾驶舱</h1>
          <p class="page-subtitle">设备资产、健康风险、告警工单与维护成本总览</p>
        </div>
        <div class="header-actions">
          <span class="update-time">
            <el-icon><Clock /></el-icon>
            更新于 {{ lastUpdated }}
          </span>
          <el-button type="primary" @click="loadDashboard">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div
        v-for="(card, index) in metricCards"
        :key="card.key"
        class="stat-card"
        :class="card.tone"
        :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <div class="stat-icon">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span class="value">{{ card.value }}</span>
            <span class="unit">{{ card.unit }}</span>
          </div>
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-sub">{{ card.sub }}</div>
        </div>
        <div class="stat-decoration"></div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><PieChart /></el-icon>
              健康分布
            </div>
            <el-tag size="small" type="success" effect="dark">
              {{ formatPercent(dashboard.averageHealthScore) }}
            </el-tag>
          </div>
          <div ref="healthChartRef" class="chart chart-md"></div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><TrendCharts /></el-icon>
              近 7 天故障趋势
            </div>
            <el-tag size="small" type="warning" effect="dark">
              本月 {{ overview.monthFaults || 0 }} 起
            </el-tag>
          </div>
          <div ref="faultChartRef" class="chart chart-md"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 列表区域 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :xl="8">
        <div class="list-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><List /></el-icon>
              待办建议
            </div>
            <el-badge :value="alerts.unhandled || 0" type="danger" />
          </div>
          <div class="list-content">
            <div v-for="(item, index) in dashboard.actionItems" :key="index" class="list-item">
              <div class="item-index" :style="{ background: getItemColor(index) }">
                {{ index + 1 }}
              </div>
              <div class="item-text">{{ item }}</div>
            </div>
            <el-empty v-if="!dashboard.actionItems?.length" description="暂无待办建议" :image-size="60" />
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :xl="8">
        <div class="list-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Warning /></el-icon>
              风险设备
            </div>
            <el-tag size="small">{{ dashboard.riskDevices?.length || 0 }} 台</el-tag>
          </div>
          <div class="list-content risk-list">
            <div v-for="device in dashboard.riskDevices" :key="device.id" class="risk-item">
              <div class="risk-info">
                <div class="risk-name">{{ device.deviceName || device.deviceCode }}</div>
                <div class="risk-advice">{{ device.maintenanceAdvice || '暂无维护建议' }}</div>
              </div>
              <div class="risk-score-box">
                <span class="risk-score" :style="{ color: healthColor(device.healthScore) }">
                  {{ device.healthScore ?? 0 }}
                </span>
                <el-tag :type="riskTagType(device.riskLevel)" size="small" effect="dark">
                  {{ device.riskLabel || '未知' }}
                </el-tag>
              </div>
            </div>
            <el-empty v-if="!dashboard.riskDevices?.length" description="暂无风险设备" :image-size="60" />
          </div>
        </div>
      </el-col>

      <el-col :xs="24" :xl="8">
        <div class="list-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Bell /></el-icon>
              最新告警
            </div>
            <el-tag size="small" type="info">未处理</el-tag>
          </div>
          <div class="list-content alert-list">
            <div v-for="alert in dashboard.recentAlerts" :key="alert.id" class="alert-item">
              <div class="alert-header">
                <el-tag :type="alertType(alert.alertLevel)" size="small" effect="dark">
                  {{ alertLevelText(alert.alertLevel) }}
                </el-tag>
                <span class="alert-time">{{ formatTime(alert.createTime) }}</span>
              </div>
              <div class="alert-device">{{ alert.deviceName || alert.deviceCode }}</div>
              <div class="alert-message">{{ alert.alertMsg }}</div>
            </div>
            <el-empty v-if="!dashboard.recentAlerts?.length" description="暂无未处理告警" :image-size="60" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 底部图表 -->
    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Tickets /></el-icon>
              工单处理
            </div>
            <el-tag size="small" type="success" effect="dark">
              {{ formatPercent(workOrders.completionRate) }}
            </el-tag>
          </div>
          <div ref="orderChartRef" class="chart chart-sm"></div>
        </div>
      </el-col>

      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <div class="card-header">
            <div class="card-title">
              <el-icon class="title-icon"><Money /></el-icon>
              近 6 月维护成本
            </div>
            <span class="cost-total">合计 <strong>{{ totalCost }}</strong> 元</span>
          </div>
          <div ref="costChartRef" class="chart chart-sm"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { getOperationsDashboard } from '@/api/statistics'
import { startRealtimePoll, stopRealtimePoll, refreshAll } from '@/composables/useRealtime'

const loading = ref(false)
const lastUpdated = ref('--')
const isRealtimeActive = ref(false)
const healthChartRef = ref()
const faultChartRef = ref()
const orderChartRef = ref()
const costChartRef = ref()

const dashboard = reactive(createEmptyDashboard())
let healthChart
let faultChart
let orderChart
let costChart
let realtimeTimer = null

const overview = computed(() => dashboard.overview || {})
const alerts = computed(() => dashboard.alerts || {})
const workOrders = computed(() => dashboard.workOrders || {})

const metricCards = computed(() => [
  {
    key: 'total',
    label: '设备总数',
    value: overview.value.totalDevices || 0,
    unit: '台',
    sub: `在线 ${overview.value.onlineDevices || 0} 台`,
    icon: 'Monitor',
    tone: 'blue',
  },
  {
    key: 'online',
    label: '在线率',
    value: formatNumber(dashboard.onlineRate),
    unit: '%',
    sub: `故障 ${overview.value.faultDevices || 0} 台`,
    icon: 'Connection',
    tone: 'green',
  },
  {
    key: 'health',
    label: '平均健康度',
    value: formatNumber(dashboard.averageHealthScore),
    unit: '分',
    sub: healthStateText.value,
    icon: 'DataAnalysis',
    tone: 'teal',
  },
  {
    key: 'alerts',
    label: '未处理告警',
    value: alerts.value.unhandled || 0,
    unit: '条',
    sub: `紧急 ${alerts.value.critical || 0} 条`,
    icon: 'Bell',
    tone: 'red',
  },
  {
    key: 'orders',
    label: '待处理工单',
    value: workOrders.value.pending || 0,
    unit: '单',
    sub: `处理中 ${workOrders.value.processing || 0} 单`,
    icon: 'Tickets',
    tone: 'purple',
  },
  {
    key: 'cost',
    label: '维护成本',
    value: totalCost.value,
    unit: '元',
    sub: '近 6 月累计',
    icon: 'Money',
    tone: 'orange',
  },
])

const healthStateText = computed(() => {
  const score = Number(dashboard.averageHealthScore || 0)
  if (score >= 85) return '整体健康'
  if (score >= 70) return '需要关注'
  if (score >= 55) return '建议巡检'
  return '优先处理'
})

const totalCost = computed(() => {
  const values = dashboard.costMonthly?.costs || []
  const total = values.reduce((sum, item) => sum + Number(item || 0), 0)
  return formatMoney(total)
})

const getItemColor = (index) => {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#6f5fbd']
  return colors[index % colors.length]
}

onMounted(async () => {
  await nextTick()
  initCharts()
  await loadDashboard()
  window.addEventListener('resize', resizeCharts)
  
  // 启动实时数据轮询（每30秒自动更新）
  startRealtime()
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  stopRealtime()
  ;[healthChart, faultChart, orderChart, costChart].forEach(chart => chart?.dispose())
})

function startRealtime() {
  isRealtimeActive.value = true
  realtimeTimer = setInterval(async () => {
    await loadDashboard()
  }, 30000) // 每30秒自动刷新
  console.log('仪表盘实时更新已启动')
}

function stopRealtime() {
  if (realtimeTimer) {
    clearInterval(realtimeTimer)
    realtimeTimer = null
  }
  isRealtimeActive.value = false
  console.log('仪表盘实时更新已停止')
}

async function loadDashboard(silent = false) {
  try {
    if (!silent) loading.value = true
    const res = await getOperationsDashboard()
    Object.assign(dashboard, createEmptyDashboard(), res.data || {})
    lastUpdated.value = formatFullTime(new Date())
    await nextTick()
    renderCharts()
  } finally {
    if (!silent) loading.value = false
  }
}

function initCharts() {
  healthChart = echarts.init(healthChartRef.value)
  faultChart = echarts.init(faultChartRef.value)
  orderChart = echarts.init(orderChartRef.value)
  costChart = echarts.init(costChartRef.value)
}

function renderCharts() {
  renderHealthChart()
  renderFaultChart()
  renderOrderChart()
  renderCostChart()
}

function renderHealthChart() {
  healthChart?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 台 ({d}%)' },
    legend: { bottom: 10, textStyle: { color: '#666' } },
    series: [
      {
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: dashboard.healthDistribution,
        color: ['#67c23a', '#409eff', '#e6a23c', '#f56c6c'],
      },
    ],
  })
}

function renderFaultChart() {
  const trend = dashboard.faultTrend || {}
  faultChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 28, right: 24, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trend.dates || [],
      axisLabel: { formatter: value => String(value).slice(5), color: '#666' },
      axisLine: { lineStyle: { color: '#e4e7ed' } },
    },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    series: [
      {
        name: '故障数',
        type: 'line',
        smooth: true,
        data: trend.counts || [],
        color: '#f56c6c',
        symbolSize: 8,
        lineStyle: { width: 3 },
        areaStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0)' }
          ])
        },
      },
    ],
  })
}

function renderOrderChart() {
  const orders = workOrders.value
  orderChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 24, right: 18, bottom: 32, left: 50 },
    xAxis: { type: 'category', data: ['待处理', '处理中', '已完成'], axisLabel: { color: '#666' } },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: '#666' } },
    series: [
      {
        name: '工单数',
        type: 'bar',
        barWidth: 40,
        data: [orders.pending || 0, orders.processing || 0, orders.finished || 0],
        itemStyle: {
          color: params => ['#f56c6c', '#409eff', '#67c23a'][params.dataIndex],
          borderRadius: [8, 8, 0, 0],
        },
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.2)' }
        },
      },
    ],
  })
}

function renderCostChart() {
  const cost = dashboard.costMonthly || {}
  costChart?.setOption({
    tooltip: {
      trigger: 'axis',
      valueFormatter: value => `${formatMoney(value)} 元`,
    },
    grid: { top: 24, right: 24, bottom: 40, left: 60 },
    xAxis: { type: 'category', data: cost.labels || [], axisLabel: { color: '#666' } },
    yAxis: { type: 'value', axisLabel: { color: '#666', formatter: v => `${v/1000}k` } },
    series: [
      {
        name: '维护成本',
        type: 'bar',
        barWidth: 32,
        data: cost.costs || [],
        itemStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#6f5fbd' },
            { offset: 1, color: '#a78bfa' }
          ]),
          borderRadius: [8, 8, 0, 0],
        },
      },
    ],
  })
}

function resizeCharts() {
  ;[healthChart, faultChart, orderChart, costChart].forEach(chart => chart?.resize())
}

function createEmptyDashboard() {
  return {
    overview: {},
    onlineRate: 0,
    averageHealthScore: 0,
    healthDistribution: [],
    riskDevices: [],
    recentAlerts: [],
    workOrders: {},
    alerts: {},
    actionItems: [],
    faultTrend: { dates: [], counts: [] },
    costMonthly: { labels: [], costs: [] },
  }
}

function formatNumber(value) {
  const num = Number(value || 0)
  return Number.isInteger(num) ? num : num.toFixed(1)
}

function formatPercent(value) {
  return `${formatNumber(value)}%`
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 0 })
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(5, 16)
}

function formatFullTime(value) {
  const date = value instanceof Date ? value : new Date(value)
  const pad = num => String(num).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function healthColor(value) {
  const num = Number(value || 0)
  if (num >= 85) return '#67c23a'
  if (num >= 70) return '#409eff'
  if (num >= 55) return '#e6a23c'
  return '#f56c6c'
}

function riskTagType(level) {
  if (level === 'critical') return 'danger'
  if (level === 'warning') return 'warning'
  if (level === 'watch') return 'primary'
  return 'success'
}

function alertType(level) {
  if (level >= 4) return 'danger'
  if (level >= 3) return 'warning'
  if (level >= 2) return 'info'
  return 'success'
}

function alertLevelText(level) {
  if (level >= 4) return '紧急'
  if (level >= 3) return '高'
  if (level >= 2) return '中'
  return '低'
}
</script>

<style scoped>
.dashboard-container {
  padding: 0;
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8f0 100%);
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
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 16px;
}

.greeting .page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: white;
}

.greeting .page-subtitle {
  margin: 6px 0 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.update-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  animation: slideUp 0.5s ease forwards;
  opacity: 0;
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-card.blue .stat-icon { background: linear-gradient(135deg, #409eff, #1890ff); }
.stat-card.green .stat-icon { background: linear-gradient(135deg, #67c23a, #52c41a); }
.stat-card.teal .stat-icon { background: linear-gradient(135deg, #1abc9c, #16a085); }
.stat-card.red .stat-icon { background: linear-gradient(135deg, #f56c6c, #f56c6c); }
.stat-card.purple .stat-icon { background: linear-gradient(135deg, #6f5fbd, #8b7fd3); }
.stat-card.orange .stat-icon { background: linear-gradient(135deg, #e6a23c, #f39c12); }

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value .value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2a37;
  line-height: 1;
}

.stat-value .unit {
  font-size: 14px;
  color: #909399;
}

.stat-label {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.stat-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.stat-decoration {
  position: absolute;
  right: -20px;
  bottom: -20px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  opacity: 0.1;
}

.stat-card.blue .stat-decoration { background: #409eff; }
.stat-card.green .stat-decoration { background: #67c23a; }
.stat-card.red .stat-decoration { background: #f56c6c; }

/* 图表卡片 */
.content-row {
  margin-bottom: 20px;
}

.chart-card, .list-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: 100%;
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
  color: #409eff;
}

.cost-total {
  font-size: 14px;
  color: #909399;
}

.cost-total strong {
  color: #f56c6c;
  font-weight: 700;
}

/* 图表 */
.chart {
  width: 100%;
}

.chart-md {
  height: 280px;
}

.chart-sm {
  height: 240px;
}

/* 列表内容 */
.list-content {
  max-height: 320px;
  overflow-y: auto;
}

.list-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed #f0f2f5;
}

.list-item:last-child {
  border-bottom: none;
}

.item-index {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.item-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

/* 风险设备 */
.risk-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px dashed #f0f2f5;
}

.risk-item:last-child {
  border-bottom: none;
}

.risk-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.risk-advice {
  font-size: 12px;
  color: #909399;
}

.risk-score-box {
  text-align: right;
}

.risk-score {
  display: block;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}

/* 告警列表 */
.alert-item {
  padding: 14px 0;
  border-bottom: 1px dashed #f0f2f5;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.alert-time {
  font-size: 12px;
  color: #c0c4cc;
}

.alert-device {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.alert-message {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    margin: -24px -16px 16px -16px;
    padding: 20px 16px;
    border-radius: 0 0 16px 16px;
  }
  
  .greeting .page-title {
    font-size: 22px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .stat-value .value {
    font-size: 24px;
  }
}
</style>
