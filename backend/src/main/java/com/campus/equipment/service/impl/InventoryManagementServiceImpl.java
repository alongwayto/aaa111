package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.InventoryManagement;
import com.campus.equipment.mapper.InventoryManagementMapper;
import com.campus.equipment.service.InventoryManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryManagementServiceImpl implements InventoryManagementService {

    private final InventoryManagementMapper inventoryMapper;

    @Override
    public List<InventoryManagement> getLowStockItems() {
        return inventoryMapper.selectList(new LambdaQueryWrapper<InventoryManagement>()
                .apply("current_stock <= min_stock")
                .orderByAsc(InventoryManagement::getCurrentStock)
                .last("LIMIT 30"));
    }

    @Override
    public List<InventoryManagement> getPurchaseSuggestions() {
        return inventoryMapper.selectList(new LambdaQueryWrapper<InventoryManagement>()
                .apply("predicted_demand > current_stock")
                .orderByDesc(InventoryManagement::getPredictedDemand)
                .last("LIMIT 30"));
    }

    @Override
    public Map<String, Object> predictDemand() {
        Long lowStock = inventoryMapper.selectCount(new LambdaQueryWrapper<InventoryManagement>()
                .apply("current_stock <= min_stock"));
        Map<String, Object> result = new HashMap<>();
        result.put("lowStockItemCount", lowStock);
        result.put("forecast", "建议本周补货关键配件，避免库存告急。");
        return result;
    }
}
