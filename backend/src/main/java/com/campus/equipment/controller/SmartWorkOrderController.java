package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.SmartWorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smart/workorder")
@RequiredArgsConstructor
public class SmartWorkOrderController {

    private final SmartWorkOrderService smartWorkOrderService;

    /**
     * 智能分配工单
     */
    @PostMapping("/assign/{faultId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> smartAssign(@PathVariable Long faultId) {
        return Result.success(smartWorkOrderService.smartAssign(faultId));
    }

    /**
     * 获取维护人员技能评分
     */
    @GetMapping("/skills")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getMaintainerSkills() {
        return Result.success(smartWorkOrderService.getMaintainerSkills());
    }

    /**
     * 获取工单分析统计
     */
    @GetMapping("/analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getWorkOrderAnalysis() {
        return Result.success(smartWorkOrderService.getWorkOrderAnalysis());
    }

    /**
     * 预测工单完成时间
     */
    @GetMapping("/predict/{faultId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> predictCompletionTime(@PathVariable Long faultId) {
        return Result.success(smartWorkOrderService.predictCompletionTime(faultId));
    }

    /**
     * 获取智能排班建议
     */
    @GetMapping("/scheduling")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<?> getSchedulingSuggestions() {
        return Result.success(smartWorkOrderService.getSchedulingSuggestions());
    }
}
