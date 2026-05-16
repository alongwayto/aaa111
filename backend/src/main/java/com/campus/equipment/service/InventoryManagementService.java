package com.campus.equipment.service;

import com.campus.equipment.entity.InventoryManagement;

import java.util.List;
import java.util.Map;

public interface InventoryManagementService {
    List<InventoryManagement> getLowStockItems();

    List<InventoryManagement> getPurchaseSuggestions();

    Map<String, Object> predictDemand();
}
