package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.SmartDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smart/dashboard")
@RequiredArgsConstructor
public class SmartDashboardController {

    private final SmartDashboardService smartDashboardService;

    /**
     * 获取智能仪表盘完整数据
     */
    @GetMapping("/data")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getDashboardData() {
        return Result.success(smartDashboardService.getDashboardData());
    }

    /**
     * 获取智能洞察列表
     */
    @GetMapping("/insights")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getInsights() {
        return Result.success(smartDashboardService.getInsights());
    }

    /**
     * 获取待办事项
     */
    @GetMapping("/todos")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getTodoItems() {
        return Result.success(smartDashboardService.getTodoItems());
    }

    /**
     * 获取系统健康概览
     */
    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getSystemHealthOverview() {
        return Result.success(smartDashboardService.getSystemHealthOverview());
    }

    /**
     * 获取实时监控数据
     */
    @GetMapping("/realtime")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getRealtimeMonitor() {
        return Result.success(smartDashboardService.getRealtimeMonitor());
    }

    /**
     * 获取今日概览
     */
    @GetMapping("/today")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getTodayOverview() {
        return Result.success(smartDashboardService.getTodayOverview());
    }

    /**
     * 获取快速操作建议
     */
    @GetMapping("/actions")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getQuickActions() {
        return Result.success(smartDashboardService.getQuickActions());
    }
}
