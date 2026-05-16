package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.entity.InventoryManagement;
import com.campus.equipment.service.InventoryManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/inventory")
@Tag(name = "库存管理")
@RequiredArgsConstructor
public class InventoryManagementController {

    private final InventoryManagementService inventoryManagementService;

    @GetMapping("/low-stock")
    @Operation(summary = "库存告警")
    public Result<List<InventoryManagement>> lowStock() {
        return Result.success(inventoryManagementService.getLowStockItems());
    }

    @GetMapping("/purchase-suggestion")
    @Operation(summary = "采购建议")
    public Result<List<InventoryManagement>> purchaseSuggestion() {
        return Result.success(inventoryManagementService.getPurchaseSuggestions());
    }

    @GetMapping("/predict-demand")
    @Operation(summary = "需求预测")
    public Result<Map<String, Object>> predictDemand() {
        return Result.success(inventoryManagementService.predictDemand());
    }
}
