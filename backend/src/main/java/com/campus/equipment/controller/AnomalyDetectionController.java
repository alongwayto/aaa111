package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.entity.AlertRecord;
import com.campus.equipment.entity.DeviceMetrics;
import com.campus.equipment.service.AnomalyDetectionService;
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
@RequestMapping("/v1/anomaly-detection")
@Tag(name = "异常检测")
@RequiredArgsConstructor
public class AnomalyDetectionController {

    private final AnomalyDetectionService anomalyDetectionService;

    @GetMapping("/anomalous-devices")
    @Operation(summary = "获取异常设备")
    public Result<List<DeviceMetrics>> getAnomalousDevices() {
        return Result.success(anomalyDetectionService.getAnomalousDevices());
    }

    @GetMapping("/high-risk-devices")
    @Operation(summary = "获取高风险设备")
    public Result<List<DeviceMetrics>> getHighRiskDevices() {
        return Result.success(anomalyDetectionService.getHighRiskDevices());
    }

    @GetMapping("/unhandled-alerts")
    @Operation(summary = "获取未处理告警")
    public Result<List<AlertRecord>> getUnhandledAlerts() {
        return Result.success(anomalyDetectionService.getUnhandledAlerts());
    }

    @GetMapping("/predict-failure/{deviceId}")
    @Operation(summary = "预测设备故障")
    public Result<Map<String, Object>> predictFailure(@PathVariable Long deviceId) {
        return Result.success(anomalyDetectionService.predictFailure(deviceId));
    }
}
