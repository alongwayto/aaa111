import request from '@/utils/request'

export const getAnomalousDevices = () => request.get('/anomaly-detection/anomalous-devices')
export const getHighRiskDevices = () => request.get('/anomaly-detection/high-risk-devices')
export const getMaintenancePlans = () => request.get('/maintenance-recommendation/recommended-plans')
export const getAnalysisDashboard = () => request.get('/data-analysis/dashboard')
export const queryChatbot = (message) => request.post('/chatbot/query', { query: message })
export const getLowStockItems = () => request.get('/inventory/low-stock')
export const getEnergyAnalysis = () => request.get('/energy/analysis')
