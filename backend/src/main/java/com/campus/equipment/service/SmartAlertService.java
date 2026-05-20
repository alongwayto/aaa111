package com.campus.equipment.service;

import java.util.List;
import java.util.Map;

public interface SmartAlertService {
    
    /**
     * 智能分析设备状态，生成预警
     */
    List<Map<String, Object>> analyzeAndGenerateAlerts();
    
    /**
     * 获取预警列表
     */
    List<Map<String, Object>> getAlerts(Map<String, Object> params);
    
    /**
     * 处理预警
     */
    void handleAlert(Long alertId, String action);
    
    /**
     * 获取预警统计
     */
    Map<String, Object> getAlertStatistics();
    
    /**
     * 智能配置预警规则
     */
    List<Map<String, Object>> getAlertRules();
    
    /**
     * 更新预警规则
     */
    void updateAlertRule(Long ruleId, Map<String, Object> rule);
}
