package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.SmartAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Smart Alert")
@RestController
@RequestMapping("/smart-alert")
@RequiredArgsConstructor
public class SmartAlertController {

    private final SmartAlertService smartAlertService;

    @Operation(summary = "智能分析并生成预警")
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<List<Map<String, Object>>> analyze() {
        List<Map<String, Object>> alerts = smartAlertService.analyzeAndGenerateAlerts();
        return Result.success(alerts);
    }

    @Operation(summary = "获取预警列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) Integer alertLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String alertType) {
        Map<String, Object> params = Map.of(
            "deviceCode", deviceCode != null ? deviceCode : "",
            "alertLevel", alertLevel != null ? alertLevel : 0,
            "status", status != null ? status : -1,
            "alertType", alertType != null ? alertType : ""
        );
        List<Map<String, Object>> alerts = smartAlertService.getAlerts(params);
        return Result.success(alerts);
    }

    @Operation(summary = "获取预警统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> statistics() {
        Map<String, Object> stats = smartAlertService.getAlertStatistics();
        return Result.success(stats);
    }

    @Operation(summary = "处理预警")
    @PostMapping("/handle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<Void> handle(@PathVariable Long id, @RequestParam String action) {
        smartAlertService.handleAlert(id, action);
        return Result.success(null);
    }

    @Operation(summary = "获取预警规则")
    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> rules() {
        List<Map<String, Object>> rules = smartAlertService.getAlertRules();
        return Result.success(rules);
    }

    @Operation(summary = "更新预警规则")
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> rule) {
        smartAlertService.updateAlertRule(id, rule);
        return Result.success(null);
    }
}
