import request from '@/utils/request'

// AI 聊天 - 支持多轮对话
export const aiChat = (messages, systemPrompt) => {
  return request.post('/ai/chat', { messages, systemPrompt })
}

// AI 设备诊断 - 分析设备故障原因
export const aiDiagnose = (data) => {
  return request.post('/ai/diagnose', data)
}

// AI 维护建议 - 生成设备维护计划
export const aiMaintenanceAdvice = (data) => {
  return request.post('/ai/maintenance-advice', data)
}

// AI 故障预测 - 基于历史数据预测故障
export const aiPredictiveMaintenance = (data) => {
  return request.post('/ai/predictive-maintenance', data)
}

// AI 生成报告 - 生成统计分析报告
export const aiGenerateReport = (data) => {
  return request.post('/ai/generate-report', data)
}

// 获取 AI 模型状态
export const getAiStatus = () => {
  return request.get('/ai/status')
}
