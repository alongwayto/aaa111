package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.AlertRecord;
import com.campus.equipment.entity.DeviceMetrics;
import com.campus.equipment.entity.DeviceStatusRecord;
import com.campus.equipment.mapper.AlertRecordMapper;
import com.campus.equipment.mapper.DeviceMetricsMapper;
import com.campus.equipment.mapper.DeviceStatusRecordMapper;
import com.campus.equipment.service.AnomalyDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private static final BigDecimal Z_SCORE_THRESHOLD = BigDecimal.valueOf(2.0);

    private final DeviceMetricsMapper deviceMetricsMapper;
    private final AlertRecordMapper alertRecordMapper;
    private final DeviceStatusRecordMapper statusRecordMapper;

    @Override
    public List<DeviceMetrics> getAnomalousDevices() {
        return deviceMetricsMapper.selectList(new LambdaQueryWrapper<DeviceMetrics>()
                .ge(DeviceMetrics::getAnomalyScore, Z_SCORE_THRESHOLD)
                .orderByDesc(DeviceMetrics::getAnomalyScore)
                .last("LIMIT 20"));
    }

    @Override
    public List<DeviceMetrics> getHighRiskDevices() {
        return deviceMetricsMapper.selectList(new LambdaQueryWrapper<DeviceMetrics>()
                .ge(DeviceMetrics::getRiskLevel, 3)
                .orderByDesc(DeviceMetrics::getRiskLevel)
                .orderByDesc(DeviceMetrics::getAnomalyScore)
                .last("LIMIT 20"));
    }

    @Override
    public List<AlertRecord> getUnhandledAlerts() {
        return alertRecordMapper.selectList(new LambdaQueryWrapper<AlertRecord>()
                .eq(AlertRecord::getStatus, 0)
                .orderByDesc(AlertRecord::getCreateTime)
                .last("LIMIT 50"));
    }

    @Override
    public Map<String, Object> predictFailure(Long deviceId) {
        List<DeviceMetrics> metrics = deviceMetricsMapper.selectList(new LambdaQueryWrapper<DeviceMetrics>()
                .eq(DeviceMetrics::getDeviceId, deviceId)
                .orderByDesc(DeviceMetrics::getCreateTime)
                .last("LIMIT 10"));
        BigDecimal avgScore = metrics.stream()
                .map(DeviceMetrics::getAnomalyScore)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!metrics.isEmpty()) {
            avgScore = avgScore.divide(BigDecimal.valueOf(metrics.size()), 2, RoundingMode.HALF_UP);
        }
        String risk = avgScore.compareTo(BigDecimal.valueOf(2.5)) >= 0 ? "high"
                : avgScore.compareTo(BigDecimal.valueOf(1.5)) >= 0 ? "medium" : "low";
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("averageAnomalyScore", avgScore);
        result.put("riskLevel", risk);
        result.put("recommendation", "high".equals(risk) ? "建议立即巡检并生成工单" : "建议持续观察运行指标");
        return result;
    }

    @Override
    public int runRealtimeDetection() {
        List<DeviceStatusRecord> records = statusRecordMapper.selectList(new LambdaQueryWrapper<DeviceStatusRecord>()
                .orderByDesc(DeviceStatusRecord::getRecordTime)
                .last("LIMIT 200"));
        Map<Long, List<DeviceStatusRecord>> grouped = records.stream()
                .filter(r -> r.getDeviceId() != null)
                .collect(Collectors.groupingBy(DeviceStatusRecord::getDeviceId));

        int count = 0;
        for (Map.Entry<Long, List<DeviceStatusRecord>> entry : grouped.entrySet()) {
            List<DeviceStatusRecord> deviceRecords = entry.getValue();
            if (deviceRecords.isEmpty()) {
                continue;
            }
            DeviceStatusRecord latest = deviceRecords.get(0);
            BigDecimal anomalyScore = calculateZScore(latest, deviceRecords);

            DeviceMetrics metrics = new DeviceMetrics();
            metrics.setDeviceId(latest.getDeviceId());
            metrics.setDeviceCode(latest.getDeviceCode());
            metrics.setCpuUsage(latest.getCpuUsage());
            metrics.setMemoryUsage(latest.getMemoryUsage());
            metrics.setDiskUsage(latest.getDiskUsage());
            metrics.setTemperature(latest.getTemperature());
            metrics.setAnomalyScore(anomalyScore);
            metrics.setRiskLevel(anomalyScore.compareTo(BigDecimal.valueOf(3)) >= 0 ? 3 : anomalyScore.compareTo(Z_SCORE_THRESHOLD) >= 0 ? 2 : 1);
            deviceMetricsMapper.insert(metrics);

            if (anomalyScore.compareTo(Z_SCORE_THRESHOLD) >= 0) {
                AlertRecord alert = new AlertRecord();
                alert.setDeviceId(latest.getDeviceId());
                alert.setAlertType("anomaly");
                alert.setAlertLevel(anomalyScore.compareTo(BigDecimal.valueOf(3)) >= 0 ? 3 : 2);
                alert.setAlertMessage("检测到异常波动，Z-Score=" + anomalyScore);
                alert.setStatus(0);
                alertRecordMapper.insert(alert);
            }
            count++;
        }
        return count;
    }

    private BigDecimal calculateZScore(DeviceStatusRecord latest, List<DeviceStatusRecord> records) {
        BigDecimal latestCpu = latest.getCpuUsage() == null ? BigDecimal.ZERO : latest.getCpuUsage();
        BigDecimal avg = records.stream()
                .map(r -> r.getCpuUsage() == null ? BigDecimal.ZERO : r.getCpuUsage())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = records.stream()
                .map(r -> {
                    BigDecimal value = r.getCpuUsage() == null ? BigDecimal.ZERO : r.getCpuUsage();
                    BigDecimal diff = value.subtract(avg);
                    return diff.multiply(diff);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);

        double std = Math.sqrt(variance.doubleValue());
        if (std <= 0.0001D) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(Math.abs(latestCpu.subtract(avg).doubleValue()) / std).setScale(2, RoundingMode.HALF_UP);
    }
}
