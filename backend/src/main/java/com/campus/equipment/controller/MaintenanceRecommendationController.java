package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.entity.MaintenancePlan;
import com.campus.equipment.service.MaintenanceRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/maintenance-recommendation")
@Tag(name = "维护推荐")
@RequiredArgsConstructor
public class MaintenanceRecommendationController {

    private final MaintenanceRecommendationService maintenanceRecommendationService;

    @GetMapping("/recommended-plans")
    @Operation(summary = "获取推荐维护计划")
    public Result<List<MaintenancePlan>> getRecommendedPlans() {
        return Result.success(maintenanceRecommendationService.getRecommendedPlans());
    }

    @GetMapping("/device-suggestion/{deviceId}")
    @Operation(summary = "获取设备维护建议")
    public Result<Map<String, Object>> getDeviceSuggestion(@PathVariable Long deviceId) {
        return Result.success(maintenanceRecommendationService.getDeviceSuggestion(deviceId));
    }

    @GetMapping("/optimal-time/{deviceId}")
    @Operation(summary = "预测最佳维护时间")
    public Result<Map<String, Object>> getOptimalTime(@PathVariable Long deviceId) {
        return Result.success(maintenanceRecommendationService.getOptimalMaintenanceTime(deviceId));
    }
}
