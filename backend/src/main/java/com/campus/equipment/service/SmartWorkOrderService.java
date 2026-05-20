package com.campus.equipment.service;

import java.util.List;
import java.util.Map;

public interface SmartWorkOrderService {
    
    /**
     * 智能分配工单
     */
    Map<String, Object> smartAssign(Long faultId);
    
    /**
     * 获取维护人员技能评分
     */
    List<Map<String, Object>> getMaintainerSkills();
    
    /**
     * 获取工单分析统计
     */
    Map<String, Object> getWorkOrderAnalysis();
    
    /**
     * 预测工单完成时间
     */
    Map<String, Object> predictCompletionTime(Long faultId);
    
    /**
     * 获取智能排班建议
     */
    List<Map<String, Object>> getSchedulingSuggestions();
}
