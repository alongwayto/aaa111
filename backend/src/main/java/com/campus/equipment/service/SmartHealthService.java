package com.campus.equipment.service;

import java.util.List;
import java.util.Map;

public interface SmartHealthService {
    
    /**
     * 获取设备健康评分
     */
    Map<String, Object> getDeviceHealthScore(String deviceCode);
    
    /**
     * 获取所有设备健康排名
     */
    List<Map<String, Object>> getDeviceHealthRanking();
    
    /**
     * 获取健康趋势分析
     */
    Map<String, Object> getHealthTrend(String deviceCode, int days);
    
    /**
     * 获取设备寿命预测
     */
    Map<String, Object> predictDeviceLifespan(String deviceCode);
    
    /**
     * 获取维护优化建议
     */
    Map<String, Object> getMaintenanceOptimization();
    
    /**
     * 获取整体健康报告
     */
    Map<String, Object> getOverallHealthReport();
    
    /**
     * 获取高风险设备列表
     */
    List<Map<String, Object>> getHighRiskDevices();
}
