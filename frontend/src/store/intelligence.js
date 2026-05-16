import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAnalysisDashboard } from '@/api/intelligence'

export const useIntelligenceStore = defineStore('intelligence', () => {
  const dashboard = ref({})

  async function fetchDashboard() {
    const res = await getAnalysisDashboard()
    dashboard.value = res.data || {}
    return dashboard.value
  }

  return {
    dashboard,
    fetchDashboard,
  }
})
