package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.SmartHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smart/health")
@RequiredArgsConstructor
public class SmartHealthController {

    private final SmartHealthService smartHealthService;

    /**
     * 获取设备健康评分
     */
    @GetMapping("/score/{deviceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getDeviceHealthScore(@PathVariable String deviceCode) {
        return Result.success(smartHealthService.getDeviceHealthScore(deviceCode));
    }

    /**
     * 获取所有设备健康排名
     */
    @GetMapping("/ranking")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getDeviceHealthRanking() {
        return Result.success(smartHealthService.getDeviceHealthRanking());
    }

    /**
     * 获取健康趋势分析
     */
    @GetMapping("/trend/{deviceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getHealthTrend(@PathVariable String deviceCode, 
                                    @RequestParam(defaultValue = "30") int days) {
        return Result.success(smartHealthService.getHealthTrend(deviceCode, days));
    }

    /**
     * 获取设备寿命预测
     */
    @GetMapping("/lifespan/{deviceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> predictDeviceLifespan(@PathVariable String deviceCode) {
        return Result.success(smartHealthService.predictDeviceLifespan(deviceCode));
    }

    /**
     * 获取维护优化建议
     */
    @GetMapping("/optimization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getMaintenanceOptimization() {
        return Result.success(smartHealthService.getMaintenanceOptimization());
    }

    /**
     * 获取整体健康报告
     */
    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getOverallHealthReport() {
        return Result.success(smartHealthService.getOverallHealthReport());
    }

    /**
     * 获取高风险设备列表
     */
    @GetMapping("/high-risk")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getHighRiskDevices() {
        return Result.success(smartHealthService.getHighRiskDevices());
    }
}
