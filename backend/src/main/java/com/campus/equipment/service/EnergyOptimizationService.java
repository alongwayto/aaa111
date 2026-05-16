package com.campus.equipment.service;

import com.campus.equipment.entity.EnergyConsumption;

import java.util.List;
import java.util.Map;

public interface EnergyOptimizationService {
    Map<String, Object> getEnergyAnalysis();

    List<EnergyConsumption> getHighConsumingDevices();

    List<Map<String, Object>> getEnergyRanking();
}
