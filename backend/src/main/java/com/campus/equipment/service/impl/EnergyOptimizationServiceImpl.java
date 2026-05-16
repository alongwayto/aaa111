package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.EnergyConsumption;
import com.campus.equipment.mapper.EnergyConsumptionMapper;
import com.campus.equipment.service.EnergyOptimizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnergyOptimizationServiceImpl implements EnergyOptimizationService {

    private final EnergyConsumptionMapper energyConsumptionMapper;

    @Override
    public Map<String, Object> getEnergyAnalysis() {
        List<EnergyConsumption> latest = energyConsumptionMapper.selectList(new LambdaQueryWrapper<EnergyConsumption>()
                .orderByDesc(EnergyConsumption::getStatDate)
                .last("LIMIT 100"));
        BigDecimal total = latest.stream()
                .map(EnergyConsumption::getEnergyUsed)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new HashMap<>();
        result.put("records", latest.size());
        result.put("totalEnergyUsed", total);
        result.put("suggestion", "建议对高耗能设备执行分时调度与巡检。");
        return result;
    }

    @Override
    public List<EnergyConsumption> getHighConsumingDevices() {
        return energyConsumptionMapper.selectList(new LambdaQueryWrapper<EnergyConsumption>()
                .orderByDesc(EnergyConsumption::getEnergyUsed)
                .last("LIMIT 10"));
    }

    @Override
    public List<Map<String, Object>> getEnergyRanking() {
        return getHighConsumingDevices().stream().map(item -> {
            Map<String, Object> row = new HashMap<>();
            row.put("deviceId", item.getDeviceId());
            row.put("deviceName", item.getDeviceName());
            row.put("energyUsed", item.getEnergyUsed());
            return row;
        }).collect(Collectors.toList());
    }
}
