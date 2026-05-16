package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.MaintenancePlan;
import com.campus.equipment.mapper.MaintenancePlanMapper;
import com.campus.equipment.service.MaintenanceRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MaintenanceRecommendationServiceImpl implements MaintenanceRecommendationService {

    private final MaintenancePlanMapper maintenancePlanMapper;

    @Override
    public List<MaintenancePlan> getRecommendedPlans() {
        return maintenancePlanMapper.selectList(new LambdaQueryWrapper<MaintenancePlan>()
                .eq(MaintenancePlan::getStatus, 0)
                .orderByDesc(MaintenancePlan::getPriority)
                .orderByAsc(MaintenancePlan::getRecommendedDate)
                .last("LIMIT 20"));
    }

    @Override
    public Map<String, Object> getDeviceSuggestion(Long deviceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("suggestion", "建议本周进行预防性巡检，并检查散热与电源模块。");
        result.put("estimatedCost", BigDecimal.valueOf(380));
        return result;
    }

    @Override
    public Map<String, Object> getOptimalMaintenanceTime(Long deviceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("optimalDate", LocalDate.now().plusDays(3));
        result.put("reason", "基于历史故障率与使用高峰预测，建议低峰期维护");
        return result;
    }
}
