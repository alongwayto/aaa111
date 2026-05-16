package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.DataAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/data-analysis")
@Tag(name = "数据分析")
@RequiredArgsConstructor
public class DataAnalysisController {

    private final DataAnalysisService dataAnalysisService;

    @GetMapping("/dashboard")
    @Operation(summary = "仪表板数据")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(dataAnalysisService.getDashboardData());
    }

    @GetMapping("/usage-trend")
    @Operation(summary = "使用趋势")
    public Result<Map<String, Object>> usageTrend() {
        return Result.success(dataAnalysisService.getUsageTrend());
    }

    @GetMapping("/failure-patterns")
    @Operation(summary = "故障模式")
    public Result<Map<String, Object>> failurePatterns() {
        return Result.success(dataAnalysisService.getFailurePatterns());
    }

    @PostMapping("/generate-report")
    @Operation(summary = "生成分析报告")
    public Result<Map<String, Object>> generateReport(@RequestParam(required = false) String reportType) {
        return Result.success(dataAnalysisService.generateReport(reportType));
    }
}
