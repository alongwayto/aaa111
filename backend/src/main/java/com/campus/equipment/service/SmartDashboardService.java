package com.campus.equipment.service;

import java.util.List;
import java.util.Map;

public interface SmartDashboardService {
    
    /**
     * 获取智能仪表盘数据
     */
    Map<String, Object> getDashboardData();
    
    /**
     * 获取智能洞察列表
     */
    List<Map<String, Object>> getInsights();
    
    /**
     * 获取待办事项
     */
    List<Map<String, Object>> getTodoItems();
    
    /**
     * 获取系统健康概览
     */
    Map<String, Object> getSystemHealthOverview();
    
    /**
     * 获取实时监控数据
     */
    Map<String, Object> getRealtimeMonitor();
    
    /**
     * 获取今日概览
     */
    Map<String, Object> getTodayOverview();
    
    /**
     * 获取快速操作建议
     */
    List<Map<String, Object>> getQuickActions();
}
