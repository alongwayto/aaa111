package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.entity.EnergyConsumption;
import com.campus.equipment.service.EnergyOptimizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/energy")
@Tag(name = "能耗管理")
@RequiredArgsConstructor
public class EnergyOptimizationController {

    private final EnergyOptimizationService energyOptimizationService;

    @GetMapping("/analysis")
    @Operation(summary = "能耗分析")
    public Result<Map<String, Object>> analysis() {
        return Result.success(energyOptimizationService.getEnergyAnalysis());
    }

    @GetMapping("/high-consuming-devices")
    @Operation(summary = "高耗能设备")
    public Result<List<EnergyConsumption>> highConsumingDevices() {
        return Result.success(energyOptimizationService.getHighConsumingDevices());
    }

    @GetMapping("/ranking")
    @Operation(summary = "能耗排名")
    public Result<List<Map<String, Object>>> ranking() {
        return Result.success(energyOptimizationService.getEnergyRanking());
    }
}
