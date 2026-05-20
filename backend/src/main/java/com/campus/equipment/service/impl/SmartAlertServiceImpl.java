package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.DeviceAlert;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.SysUser;
import com.campus.equipment.mapper.DeviceAlertMapper;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.FaultReportMapper;
import com.campus.equipment.mapper.SysUserMapper;
import com.campus.equipment.service.SmartAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartAlertServiceImpl implements SmartAlertService {

    private final DeviceInfoMapper deviceMapper;
    private final DeviceAlertMapper alertMapper;
    private final FaultReportMapper faultMapper;
    private final SysUserMapper userMapper;

    // 预警规则配置
    private static final Map<String, Map<String, Object>> ALERT_RULES = new LinkedHashMap<>();
    
    static {
        // 健康度预警规则
        Map<String, Object> healthRule = new HashMap<>();
        healthRule.put("name", "健康度预警");
        healthRule.put("metric", "healthScore");
        healthRule.put("thresholds", Map.of("critical", 30, "warning", 50, "info", 70));
        healthRule.put("description", "设备健康度过低时触发预警");
        ALERT_RULES.put("health", healthRule);
        
        // CPU 预警规则
        Map<String, Object> cpuRule = new HashMap<>();
        cpuRule.put("name", "CPU负载预警");
        cpuRule.put("metric", "cpuUsage");
        cpuRule.put("thresholds", Map.of("critical", 90, "warning", 80, "info", 70));
        cpuRule.put("description", "CPU使用率过高时触发预警");
        ALERT_RULES.put("cpu", cpuRule);
        
        // 内存预警规则
        Map<String, Object> memoryRule = new HashMap<>();
        memoryRule.put("name", "内存使用预警");
        memoryRule.put("metric", "memoryUsage");
        memoryRule.put("thresholds", Map.of("critical", 90, "warning", 80, "info", 70));
        memoryRule.put("description", "内存使用率过高时触发预警");
        ALERT_RULES.put("memory", memoryRule);
        
        // 温度预警规则
        Map<String, Object> tempRule = new HashMap<>();
        tempRule.put("name", "温度预警");
        tempRule.put("metric", "temperature");
        tempRule.put("thresholds", Map.of("critical", 85, "warning", 75, "info", 65));
        tempRule.put("description", "设备温度过高时触发预警");
        ALERT_RULES.put("temperature", tempRule);
        
        // 离线预警规则
        Map<String, Object> offlineRule = new HashMap<>();
        offlineRule.put("name", "设备离线预警");
        offlineRule.put("metric", "onlineStatus");
        offlineRule.put("thresholds", Map.of("critical", 0, "warning", 0, "info", 0));
        offlineRule.put("description", "设备离线超过30分钟触发预警");
        ALERT_RULES.put("offline", offlineRule);
        
        // 故障频率预警
        Map<String, Object> faultFreqRule = new HashMap<>();
        faultFreqRule.put("name", "故障频率预警");
        faultFreqRule.put("metric", "faultFrequency");
        faultFreqRule.put("thresholds", Map.of("critical", 5, "warning", 3, "info", 2));
        faultFreqRule.put("description", "同一设备30天内故障次数过多触发预警");
        ALERT_RULES.put("faultFrequency", faultFreqRule);
        
        // 维保到期预警
        Map<String, Object> maintenanceRule = new HashMap<>();
        maintenanceRule.put("name", "维保到期预警");
        maintenanceRule.put("metric", "nextMaintenanceDate");
        maintenanceRule.put("thresholds", Map.of("critical", 3, "warning", 7, "info", 14));
        maintenanceRule.put("description", "距离下次维护日期的天数");
        ALERT_RULES.put("maintenance", maintenanceRule);
        
        // 能耗异常预警
        Map<String, Object> energyRule = new HashMap<>();
        energyRule.put("name", "能耗异常预警");
        energyRule.put("metric", "powerConsumption");
        energyRule.put("thresholds", Map.of("critical", 1.5, "warning", 1.3, "info", 1.2));
        energyRule.put("description", "能耗超过历史平均值的比例");
        ALERT_RULES.put("energy", energyRule);
    }

    @Override
    public List<Map<String, Object>> analyzeAndGenerateAlerts() {
        log.info("Starting smart alert analysis...");
        List<Map<String, Object>> newAlerts = new ArrayList<>();
        
        // 获取所有设备
        List<DeviceInfo> devices = deviceMapper.selectList(null);
        
        for (DeviceInfo device : devices) {
            // 1. 健康度检查
            checkHealthScore(device, newAlerts);
            
            // 2. 性能指标检查
            checkPerformanceMetrics(device, newAlerts);
            
            // 3. 在线状态检查
            checkOnlineStatus(device, newAlerts);
            
            // 4. 故障频率检查
            checkFaultFrequency(device, newAlerts);
            
            // 5. 维保到期检查
            checkMaintenanceDue(device, newAlerts);
            
            // 6. 能耗异常检查
            checkEnergyAnomaly(device, newAlerts);
        }
        
        // 保存新预警
        for (Map<String, Object> alert : newAlerts) {
            DeviceAlert entity = new DeviceAlert();
            entity.setDeviceCode((String) alert.get("deviceCode"));
            entity.setAlertType((String) alert.get("alertType"));
            entity.setAlertLevel((Integer) alert.get("alertLevel"));
            entity.setAlertTitle((String) alert.get("alertTitle"));
            entity.setAlertContent((String) alert.get("alertContent"));
            entity.setMetricValue(new BigDecimal(alert.get("metricValue").toString()));
            entity.setThresholdValue(new BigDecimal(alert.get("thresholdValue").toString()));
            entity.setStatus(0); // 未处理
            entity.setCreateTime(new Date());
            
            // 检查是否已存在相同未处理预警
            LambdaQueryWrapper<DeviceAlert> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DeviceAlert::getDeviceCode, entity.getDeviceCode())
                   .eq(DeviceAlert::getAlertType, entity.getAlertType())
                   .eq(DeviceAlert::getStatus, 0);
            
            if (alertMapper.selectCount(wrapper) == 0) {
                alertMapper.insert(entity);
            }
        }
        
        log.info("Smart alert analysis completed, generated {} new alerts", newAlerts.size());
        return newAlerts;
    }

    private void checkHealthScore(DeviceInfo device, List<Map<String, Object>> alerts) {
        Integer healthScore = device.getHealthScore();
        if (healthScore == null) return;
        
        int level;
        if (healthScore < 30) level = 1;      // 紧急
        else if (healthScore < 50) level = 2;  // 高
        else if (healthScore < 70) level = 3;  // 中
        else return;                            // 低于阈值不预警
        
        Map<String, Object> alert = new HashMap<>();
        alert.put("deviceCode", device.getDeviceCode());
        alert.put("deviceName", device.getDeviceName());
        alert.put("alertType", "health");
        alert.put("alertLevel", level);
        alert.put("alertTitle", "健康度" + getLevelName(level) + "预警");
        alert.put("alertContent", String.format("设备 %s 当前健康度为 %d，已低于正常阈值。建议：%s",
            device.getDeviceName(), healthScore, getHealthAdvice(healthScore)));
        alert.put("metricValue", healthScore);
        alert.put("thresholdValue", level <= 2 ? 30 : 70);
        alert.put("suggestion", getHealthAdvice(healthScore));
        alerts.add(alert);
    }

    private void checkPerformanceMetrics(DeviceInfo device, List<Map<String, Object>> alerts) {
        // CPU 检查
        Integer cpuUsage = device.getCpuUsage();
        if (cpuUsage != null && cpuUsage > 80) {
            int level = cpuUsage > 90 ? 1 : 2;
            addPerformanceAlert(device, "cpu", "CPU负载", cpuUsage, "%", level, alerts);
        }
        
        // 内存检查
        Integer memoryUsage = device.getMemoryUsage();
        if (memoryUsage != null && memoryUsage > 80) {
            int level = memoryUsage > 90 ? 1 : 2;
            addPerformanceAlert(device, "memory", "内存使用", memoryUsage, "%", level, alerts);
        }
        
        // 温度检查
        Double temperature = device.getTemperature();
        if (temperature != null && temperature > 75) {
            int level = temperature > 85 ? 1 : 2;
            addPerformanceAlert(device, "temperature", "设备温度", temperature, "C", level, alerts);
        }
    }

    private void addPerformanceAlert(DeviceInfo device, String type, String name, 
            Number value, String unit, int level, List<Map<String, Object>> alerts) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("deviceCode", device.getDeviceCode());
        alert.put("deviceName", device.getDeviceName());
        alert.put("alertType", type);
        alert.put("alertLevel", level);
        alert.put("alertTitle", name + getLevelName(level) + "预警");
        alert.put("alertContent", String.format("设备 %s 的%s为 %.1f%s，已超过正常范围。建议：%s",
            device.getDeviceName(), name, value.doubleValue(), unit, getPerformanceAdvice(type, value)));
        alert.put("metricValue", value);
        alert.put("thresholdValue", level == 1 ? (type.equals("temperature") ? 85 : 90) : (type.equals("temperature") ? 75 : 80));
        alert.put("suggestion", getPerformanceAdvice(type, value));
        alerts.add(alert);
    }

    private void checkOnlineStatus(DeviceInfo device, List<Map<String, Object>> alerts) {
        if (device.getOnlineStatus() != null && device.getOnlineStatus() == 0) {
            // 检查离线时长
            Date lastUpdate = device.getUpdateTime();
            if (lastUpdate != null) {
                long offlineMinutes = ChronoUnit.MINUTES.between(
                    lastUpdate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                    LocalDate.now().atStartOfDay()
                );
                
                if (offlineMinutes > 30) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("deviceCode", device.getDeviceCode());
                    alert.put("deviceName", device.getDeviceName());
                    alert.put("alertType", "offline");
                    alert.put("alertLevel", offlineMinutes > 60 ? 2 : 3);
                    alert.put("alertTitle", "设备离线告警");
                    alert.put("alertContent", String.format("设备 %s 已离线超过 %.0f 分钟，请检查网络连接或设备电源状态。",
                        device.getDeviceName(), (double) offlineMinutes));
                    alert.put("metricValue", offlineMinutes);
                    alert.put("thresholdValue", 30);
                    alert.put("suggestion", "检查设备电源和网络连接，或联系维护人员现场检查");
                    alerts.add(alert);
                }
            }
        }
    }

    private void checkFaultFrequency(DeviceInfo device, List<Map<String, Object>> alerts) {
        // 统计30天内故障次数
        Date thirtyDaysAgo = Date.from(LocalDate.now().minusDays(30).atStartOfDay()
            .atZone(ZoneId.systemDefault()).toInstant());
        
        LambdaQueryWrapper<FaultReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FaultReport::getDeviceCode, device.getDeviceCode())
               .ge(FaultReport::getReportTime, thirtyDaysAgo);
        long faultCount = faultMapper.selectCount(wrapper);
        
        if (faultCount >= 3) {
            int level = faultCount >= 5 ? 1 : 2;
            Map<String, Object> alert = new HashMap<>();
            alert.put("deviceCode", device.getDeviceCode());
            alert.put("deviceName", device.getDeviceName());
            alert.put("alertType", "faultFrequency");
            alert.put("alertLevel", level);
            alert.put("alertTitle", "故障频繁预警");
            alert.put("alertContent", String.format("设备 %s 在30天内已发生 %d 次故障，可能存在系统性隐患，建议进行全面检修。",
                device.getDeviceName(), faultCount));
            alert.put("metricValue", faultCount);
            alert.put("thresholdValue", 3);
            alert.put("suggestion", "建议安排全面检修，或考虑更换设备");
            alerts.add(alert);
        }
    }

    private void checkMaintenanceDue(DeviceInfo device, List<Map<String, Object>> alerts) {
        Date nextMaintenance = device.getNextMaintenanceDate();
        if (nextMaintenance == null) return;
        
        long daysUntilMaintenance = ChronoUnit.DAYS.between(LocalDate.now(), 
            nextMaintenance.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        
        if (daysUntilMaintenance <= 14) {
            int level = daysUntilMaintenance <= 3 ? 1 : (daysUntilMaintenance <= 7 ? 2 : 3);
            Map<String, Object> alert = new HashMap<>();
            alert.put("deviceCode", device.getDeviceCode());
            alert.put("deviceName", device.getDeviceName());
            alert.put("alertType", "maintenance");
            alert.put("alertLevel", level);
            alert.put("alertTitle", "维保到期提醒");
            alert.put("alertContent", String.format("设备 %s 距离下次维护还有 %d 天，请及时安排维护工作。",
                device.getDeviceName(), daysUntilMaintenance));
            alert.put("metricValue", daysUntilMaintenance);
            alert.put("thresholdValue", 14);
            alert.put("suggestion", "请在维保到期前完成维护工作");
            alerts.add(alert);
        }
    }

    private void checkEnergyAnomaly(DeviceInfo device, List<Map<String, Object>> alerts) {
        Double powerConsumption = device.getPowerConsumption();
        if (powerConsumption == null) return;
        
        // 计算历史平均值（简化版，实际应从数据库计算）
        Double avgPower = device.getAvgPowerConsumption();
        if (avgPower == null || avgPower == 0) return;
        
        double ratio = powerConsumption / avgPower;
        if (ratio > 1.3) {
            int level = ratio > 1.5 ? 1 : 2;
            Map<String, Object> alert = new HashMap<>();
            alert.put("deviceCode", device.getDeviceCode());
            alert.put("deviceName", device.getDeviceName());
            alert.put("alertType", "energy");
            alert.put("alertLevel", level);
            alert.put("alertTitle", "能耗异常预警");
            alert.put("alertContent", String.format("设备 %s 当前能耗 %.1fkWh，较历史均值高出 %.0f%%，可能存在异常。",
                device.getDeviceName(), powerConsumption, (ratio - 1) * 100));
            alert.put("metricValue", ratio);
            alert.put("thresholdValue", 1.3);
            alert.put("suggestion", "检查设备是否存在异常负载或故障");
            alerts.add(alert);
        }
    }

    @Override
    public List<Map<String, Object>> getAlerts(Map<String, Object> params) {
        LambdaQueryWrapper<DeviceAlert> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            if (params.get("deviceCode") != null) {
                wrapper.eq(DeviceAlert::getDeviceCode, params.get("deviceCode"));
            }
            if (params.get("alertLevel") != null) {
                wrapper.eq(DeviceAlert::getAlertLevel, params.get("alertLevel"));
            }
            if (params.get("status") != null) {
                wrapper.eq(DeviceAlert::getStatus, params.get("status"));
            }
            if (params.get("alertType") != null) {
                wrapper.eq(DeviceAlert::getAlertType, params.get("alertType"));
            }
        }
        
        wrapper.orderByDesc(DeviceAlert::getCreateTime);
        
        List<DeviceAlert> alerts = alertMapper.selectList(wrapper);
        return alerts.stream().map(this::convertAlertToMap).collect(Collectors.toList());
    }

    @Override
    public void handleAlert(Long alertId, String action) {
        DeviceAlert alert = alertMapper.selectById(alertId);
        if (alert == null) return;
        
        switch (action) {
            case "handle":
                alert.setStatus(1); // 已处理
                break;
            case "ignore":
                alert.setStatus(2); // 已忽略
                break;
            case "create_workorder":
                alert.setStatus(1);
                // 创建工单的逻辑由调用方处理
                break;
        }
        alertMapper.updateById(alert);
    }

    @Override
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总预警数
        long total = alertMapper.selectCount(null);
        stats.put("total", total);
        
        // 待处理
        long pending = alertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 0));
        stats.put("pending", pending);
        
        // 已处理
        long handled = alertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 1));
        stats.put("handled", handled);
        
        // 按级别统计
        Map<Integer, Long> byLevel = new HashMap<>();
        for (int i = 1; i <= 4; i++) {
            byLevel.put(i, alertMapper.selectCount(
                new LambdaQueryWrapper<DeviceAlert>()
                    .eq(DeviceAlert::getAlertLevel, i)
                    .eq(DeviceAlert::getStatus, 0)));
        }
        stats.put("byLevel", byLevel);
        
        // 按类型统计
        List<Map<String, Object>> byType = new ArrayList<>();
        for (String type : Arrays.asList("health", "cpu", "memory", "temperature", "offline", "faultFrequency", "maintenance", "energy")) {
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("type", type);
            typeStat.put("name", getAlertTypeName(type));
            typeStat.put("count", alertMapper.selectCount(
                new LambdaQueryWrapper<DeviceAlert>()
                    .eq(DeviceAlert::getAlertType, type)
                    .eq(DeviceAlert::getStatus, 0)));
            byType.add(typeStat);
        }
        stats.put("byType", byType);
        
        // 智能建议
        List<String> suggestions = generateSmartSuggestions();
        stats.put("suggestions", suggestions);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getAlertRules() {
        List<Map<String, Object>> rules = new ArrayList<>();
        ALERT_RULES.forEach((key, rule) -> {
            Map<String, Object> ruleCopy = new HashMap<>(rule);
            ruleCopy.put("id", key);
            rules.add(ruleCopy);
        });
        return rules;
    }

    @Override
    public void updateAlertRule(Long ruleId, Map<String, Object> rule) {
        if (ALERT_RULES.containsKey(ruleId.toString())) {
            Map<String, Object> existingRule = ALERT_RULES.get(ruleId.toString());
            if (rule.containsKey("thresholds")) {
                existingRule.put("thresholds", rule.get("thresholds"));
            }
            if (rule.containsKey("enabled")) {
                existingRule.put("enabled", rule.get("enabled"));
            }
        }
    }

    private Map<String, Object> convertAlertToMap(DeviceAlert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", alert.getId());
        map.put("deviceCode", alert.getDeviceCode());
        map.put("alertType", alert.getAlertType());
        map.put("alertTypeName", getAlertTypeName(alert.getAlertType()));
        map.put("alertLevel", alert.getAlertLevel());
        map.put("alertLevelName", getLevelName(alert.getAlertLevel()));
        map.put("alertTitle", alert.getAlertTitle());
        map.put("alertContent", alert.getAlertContent());
        map.put("metricValue", alert.getMetricValue());
        map.put("thresholdValue", alert.getThresholdValue());
        map.put("status", alert.getStatus());
        map.put("statusName", alert.getStatus() == 0 ? "待处理" : (alert.getStatus() == 1 ? "已处理" : "已忽略"));
        map.put("createTime", alert.getCreateTime());
        map.put("handleTime", alert.getHandleTime());
        return map;
    }

    private String getLevelName(int level) {
        return switch (level) {
            case 1 -> "紧急";
            case 2 -> "高";
            case 3 -> "中";
            case 4 -> "低";
            default -> "未知";
        };
    }

    private String getAlertTypeName(String type) {
        Map<String, String> names = Map.of(
            "health", "健康度",
            "cpu", "CPU负载",
            "memory", "内存使用",
            "temperature", "设备温度",
            "offline", "设备离线",
            "faultFrequency", "故障频率",
            "maintenance", "维保到期",
            "energy", "能耗异常"
        );
        return names.getOrDefault(type, type);
    }

    private String getHealthAdvice(int score) {
        if (score < 20) return "建议立即停机检修";
        if (score < 30) return "建议24小时内安排检修";
        if (score < 50) return "建议本周内安排维护";
        return "建议关注设备状态";
    }

    private String getPerformanceAdvice(String type, Number value) {
        return switch (type) {
            case "cpu" -> "检查是否有异常进程运行，考虑升级硬件或优化负载";
            case "memory" -> "清理内存缓存，检查内存泄漏问题";
            case "temperature" -> "检查散热系统，清理灰尘，确保通风良好";
            default -> "检查设备运行状态";
        };
    }

    private List<String> generateSmartSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        // 基于预警统计生成建议
        long pending = alertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 0));
        
        if (pending > 10) {
            suggestions.add("当前待处理预警较多，建议安排专项巡检");
        }
        
        // 检查高优先级预警
        long criticalCount = alertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getAlertLevel, 1)
                .eq(DeviceAlert::getStatus, 0));
        
        if (criticalCount > 0) {
            suggestions.add(String.format("发现 %d 个紧急预警，需要立即处理", criticalCount));
        }
        
        // 设备离线检查
        long offlineCount = alertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getAlertType, "offline")
                .eq(DeviceAlert::getStatus, 0));
        
        if (offlineCount > 0) {
            suggestions.add(String.format("有 %d 台设备离线，建议检查网络和电源", offlineCount));
        }
        
        // 维保到期提醒
        long maintenanceCount = alertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getAlertType, "maintenance")
                .eq(DeviceAlert::getStatus, 0));
        
        if (maintenanceCount > 0) {
            suggestions.add(String.format("有 %d 台设备即将到期维护，请提前安排", maintenanceCount));
        }
        
        if (suggestions.isEmpty()) {
            suggestions.add("系统运行正常，继续保持良好状态");
        }
        
        return suggestions;
    }
}
