package com.campus.equipment.service;

import com.campus.equipment.entity.AlertRecord;
import com.campus.equipment.entity.DeviceMetrics;

import java.util.List;
import java.util.Map;

public interface AnomalyDetectionService {
    List<DeviceMetrics> getAnomalousDevices();

    List<DeviceMetrics> getHighRiskDevices();

    List<AlertRecord> getUnhandledAlerts();

    Map<String, Object> predictFailure(Long deviceId);

    int runRealtimeDetection();
}
