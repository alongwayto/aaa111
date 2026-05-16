package com.campus.equipment.service;

import com.campus.equipment.entity.MaintenancePlan;

import java.util.List;
import java.util.Map;

public interface MaintenanceRecommendationService {
    List<MaintenancePlan> getRecommendedPlans();

    Map<String, Object> getDeviceSuggestion(Long deviceId);

    Map<String, Object> getOptimalMaintenanceTime(Long deviceId);
}
