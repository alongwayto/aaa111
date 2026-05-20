package com.campus.equipment.service;

import java.util.List;
import java.util.Map;

public interface AiService {
    
    /**
     * 通用聊天对话
     */
    String chat(List<Map<String, String>> messages, String systemPrompt);
    
    /**
     * 设备智能诊断
     */
    String diagnoseDevice(String deviceCode, String symptom);
    
    /**
     * 生成维护建议
     */
    String generateMaintenanceAdvice(Long deviceId, String location);
    
    /**
     * 故障预测
     */
    String predictFailures(Long deviceId);
    
    /**
     * 生成分析报告
     */
    String generateReport(String reportType, String timeRange);
    
    /**
     * 图片分析
     */
    String analyzeImage(Object file, String question);
    
    /**
     * 获取服务状态
     */
    Map<String, Object> getServiceStatus();
}
