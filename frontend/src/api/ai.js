import request from '@/utils/request'

export const aiChat = (messages, systemPrompt) => request.post('/ai/chat', { messages, systemPrompt })
export const aiDiagnose = (data) => request.post('/ai/diagnose', data)
export const aiMaintenanceAdvice = (data) => request.post('/ai/maintenance-advice', data)
