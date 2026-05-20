package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.DeviceAlert;
import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.FaultWorkOrder;
import com.campus.equipment.mapper.DeviceAlertMapper;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.FaultReportMapper;
import com.campus.equipment.mapper.FaultWorkOrderMapper;
import com.campus.equipment.service.SmartHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartHealthServiceImpl implements SmartHealthService {

    private final DeviceInfoMapper deviceInfoMapper;
    private final FaultReportMapper faultReportMapper;
    private final FaultWorkOrderMapper workOrderMapper;
    private final DeviceAlertMapper deviceAlertMapper;

    @Override
    public Map<String, Object> getDeviceHealthScore(String deviceCode) {
        DeviceInfo device = deviceInfoMapper.selectOne(
            new LambdaQueryWrapper<DeviceInfo>().eq(DeviceInfo::getDeviceCode, deviceCode));
        
        if (device == null) {
            return Map.of("success", false, "message", "设备不存在");
        }
        
        Map<String, Object> score = calculateHealthScore(device);
        score.put("deviceCode", deviceCode);
        score.put("deviceName", device.getDeviceName());
        score.put("categoryId", device.getCategoryId());
        score.put("locationId", device.getLocationId());
        score.put("brand", device.getBrand());
        score.put("model", device.getModel());
        score.put("purchaseDate", device.getPurchaseDate());
        score.put("status", device.getStatus());
        score.put("onlineStatus", device.getOnlineStatus());
        
        return score;
    }

    @Override
    public List<Map<String, Object>> getDeviceHealthRanking() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        List<Map<String, Object>> rankings = new ArrayList<>();
        
        for (DeviceInfo device : devices) {
            Map<String, Object> score = calculateHealthScore(device);
            score.put("deviceCode", device.getDeviceCode());
            score.put("deviceName", device.getDeviceName());
            score.put("categoryId", device.getCategoryId());
            score.put("locationId", device.getLocationId());
            score.put("brand", device.getBrand());
            score.put("status", device.getStatus());
            score.put("onlineStatus", device.getOnlineStatus());
            rankings.add(score);
        }
        
        // 按健康评分降序排序
        rankings.sort((a, b) -> Double.compare(
            (double) b.get("totalScore"), (double) a.get("totalScore")));
        
        // 添加排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).put("rank", i + 1);
        }
        
        return rankings;
    }

    @Override
    public Map<String, Object> getHealthTrend(String deviceCode, int days) {
        DeviceInfo device = deviceInfoMapper.selectOne(
            new LambdaQueryWrapper<DeviceInfo>().eq(DeviceInfo::getDeviceCode, deviceCode));
        
        if (device == null) {
            return Map.of("success", false, "message", "设备不存在");
        }
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("deviceCode", deviceCode);
        trend.put("deviceName", device.getDeviceName());
        trend.put("days", days);
        
        // 模拟趋势数据（实际应从历史数据计算）
        List<Map<String, Object>> history = new ArrayList<>();
        double baseScore = 85.0;
        long now = System.currentTimeMillis();
        long dayMs = 24 * 60 * 60 * 1000L;
        
        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ISO_LOCAL_DATE));
            // 模拟逐渐下降的趋势，带有随机波动
            double variation = Math.random() * 5 - 2;
            point.put("score", Math.max(60, Math.min(100, baseScore + variation - (days - i) * 0.5)));
            history.add(point);
        }
        
        trend.put("history", history);
        
        // 计算趋势方向
        if (history.size() >= 2) {
            double recent = (double) history.get(history.size() - 1).get("score");
            double older = (double) history.get(0).get("score");
            String direction = recent > older + 2 ? "improving" : (recent < older - 2 ? "declining" : "stable");
            trend.put("direction", direction);
            trend.put("change", recent - older);
        }
        
        return trend;
    }

    @Override
    public Map<String, Object> predictDeviceLifespan(String deviceCode) {
        DeviceInfo device = deviceInfoMapper.selectOne(
            new LambdaQueryWrapper<DeviceInfo>().eq(DeviceInfo::getDeviceCode, deviceCode));
        
        if (device == null) {
            return Map.of("success", false, "message", "设备不存在");
        }
        
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("deviceCode", deviceCode);
        prediction.put("deviceName", device.getDeviceName());
        prediction.put("purchaseDate", device.getPurchaseDate());
        
        // 获取故障历史
        List<FaultReport> faults = faultReportMapper.selectList(
            new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getDeviceCode, deviceCode)
                .orderByDesc(FaultReport::getReportTime)
        );
        
        prediction.put("totalFaults", faults.size());
        
        // 获取健康评分
        Map<String, Object> healthScore = calculateHealthScore(device);
        double score = (double) healthScore.get("totalScore");
        
        // 基于健康评分和使用时间估算剩余寿命
        // 默认预期寿命10年
        int expectedLifespan = 10;
        int usedYears = 0;
        
        if (device.getPurchaseDate() != null) {
            LocalDate purchase = device.getPurchaseDate();
            usedYears = LocalDate.now().getYear() - purchase.getYear();
        }
        
        // 寿命因子：健康评分越低，剩余寿命越短
        double lifeFactor = score / 100.0;
        int remainingYears = (int) ((expectedLifespan - usedYears) * lifeFactor);
        remainingYears = Math.max(0, remainingYears);
        
        prediction.put("expectedLifespan", expectedLifespan);
        prediction.put("usedYears", usedYears);
        prediction.put("remainingYears", remainingYears);
        prediction.put("healthScore", score);
        prediction.put("confidence", 0.7 + faults.size() * 0.02); // 故障记录越多，预测越准确
        
        // 生成建议
        List<String> suggestions = new ArrayList<>();
        if (score < 70) {
            suggestions.add("设备健康状况较差，建议尽快进行全面检修");
        }
        if (remainingYears < 2) {
            suggestions.add("预计剩余寿命不足2年，建议列入更新计划");
        }
        if (faults.size() > 5) {
            suggestions.add("历史故障频繁，建议加强日常巡检");
        }
        prediction.put("suggestions", suggestions);
        
        return prediction;
    }

    @Override
    public Map<String, Object> getMaintenanceOptimization() {
        Map<String, Object> optimization = new HashMap<>();
        
        // 获取所有设备健康状况
        List<Map<String, Object>> healthRanking = getDeviceHealthRanking();
        
        // 按设备类型分组分析
        Map<Long, List<Map<String, Object>>> byCategory = healthRanking.stream()
            .collect(Collectors.groupingBy(d -> (Long) d.getOrDefault("categoryId", 0L)));
        
        Map<String, Object> categoryAnalysis = new HashMap<>();
        for (Map.Entry<Long, List<Map<String, Object>>> entry : byCategory.entrySet()) {
            Long categoryId = entry.getKey();
            List<Map<String, Object>> devices = entry.getValue();
            
            double avgScore = devices.stream()
                .mapToDouble(d -> (double) d.get("totalScore"))
                .average()
                .orElse(80);
            
            categoryAnalysis.put(String.valueOf(categoryId), Map.of(
                "deviceCount", devices.size(),
                "avgHealthScore", avgScore,
                "healthiest", devices.stream()
                    .max(Comparator.comparingDouble(d -> (double) d.get("totalScore")))
                    .map(d -> d.get("deviceName"))
                    .orElse("N/A"),
                "needsAttention", devices.stream()
                    .filter(d -> (double) d.get("totalScore") < 70)
                    .map(d -> Map.of(
                        "deviceCode", d.get("deviceCode"),
                        "deviceName", d.get("deviceName"),
                        "score", d.get("totalScore")))
                    .collect(Collectors.toList())
            ));
        }
        
        optimization.put("categoryAnalysis", categoryAnalysis);
        
        // 优化建议
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // 1. 维护资源分配建议
        long criticalCount = healthRanking.stream()
            .filter(d -> (double) d.get("totalScore") < 60)
            .count();
        
        if (criticalCount > 0) {
            suggestions.add(Map.of(
                "type", "resource_allocation",
                "priority", "high",
                "title", "重点维护资源配置",
                "content", String.format("当前有 %d 台设备健康评分低于60分，建议增加维护资源投入", criticalCount)
            ));
        }
        
        // 2. 预防性维护建议
        long warningCount = healthRanking.stream()
            .filter(d -> (double) d.get("totalScore") < 80)
            .count();
        
        if (warningCount > 0) {
            suggestions.add(Map.of(
                "type", "preventive_maintenance",
                "priority", "medium",
                "title", "预防性维护计划",
                "content", String.format("建议对 %d 台健康评分低于80分的设备安排预防性维护", warningCount)
            ));
        }
        
        // 3. 设备更新建议
        List<String> agingDevices = healthRanking.stream()
            .filter(d -> (double) d.get("totalScore") < 65)
            .map(d -> (String) d.get("deviceName"))
            .limit(5)
            .collect(Collectors.toList());
        
        if (!agingDevices.isEmpty()) {
            suggestions.add(Map.of(
                "type", "device_replacement",
                "priority", "low",
                "title", "设备更新建议",
                "content", "以下设备建议考虑更新换代：" + String.join("、", agingDevices)
            ));
        }
        
        optimization.put("suggestions", suggestions);
        optimization.put("totalDevices", healthRanking.size());
        optimization.put("avgHealthScore", healthRanking.stream()
            .mapToDouble(d -> (double) d.get("totalScore"))
            .average()
            .orElse(80));
        
        return optimization;
    }

    @Override
    public Map<String, Object> getOverallHealthReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<DeviceInfo> allDevices = deviceInfoMapper.selectList(null);
        List<Map<String, Object>> rankings = getDeviceHealthRanking();
        
        // 总体统计
        double avgScore = rankings.stream()
            .mapToDouble(d -> (double) d.get("totalScore"))
            .average()
            .orElse(80);
        
        report.put("totalDevices", allDevices.size());
        report.put("onlineDevices", allDevices.stream().filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1).count());
        report.put("offlineDevices", allDevices.stream().filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 0).count());
        report.put("avgHealthScore", avgScore);
        
        // 设备健康分布
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("excellent", rankings.stream().filter(d -> (double) d.get("totalScore") >= 90).count());
        distribution.put("good", rankings.stream().filter(d -> (double) d.get("totalScore") >= 80 && (double) d.get("totalScore") < 90).count());
        distribution.put("fair", rankings.stream().filter(d -> (double) d.get("totalScore") >= 70 && (double) d.get("totalScore") < 80).count());
        distribution.put("poor", rankings.stream().filter(d -> (double) d.get("totalScore") >= 60 && (double) d.get("totalScore") < 70).count());
        distribution.put("critical", rankings.stream().filter(d -> (double) d.get("totalScore") < 60).count());
        
        report.put("healthDistribution", distribution);
        
        // 健康等级描述
        String healthLevel;
        if (avgScore >= 90) healthLevel = "优秀 - 设备整体运行状态极佳";
        else if (avgScore >= 80) healthLevel = "良好 - 设备整体运行正常";
        else if (avgScore >= 70) healthLevel = "一般 - 部分设备需要关注";
        else if (avgScore >= 60) healthLevel = "较差 - 建议加强维护";
        else healthLevel = "危急 - 需立即处理";
        
        report.put("healthLevel", healthLevel);
        
        // 高风险设备
        report.put("highRiskDevices", rankings.stream()
            .filter(d -> (double) d.get("totalScore") < 70)
            .limit(10)
            .collect(Collectors.toList()));
        
        // TOP 5 健康设备
        report.put("topHealthyDevices", rankings.stream()
            .limit(5)
            .collect(Collectors.toList()));
        
        // 设备类型健康对比
        Map<Long, Double> categoryScores = rankings.stream()
            .collect(Collectors.groupingBy(
                d -> (Long) d.getOrDefault("categoryId", 0L),
                Collectors.averagingDouble(d -> (double) d.get("totalScore"))
            ));
        report.put("categoryHealthComparison", categoryScores);
        
        // 近期趋势（模拟）
        report.put("trend", Map.of(
            "direction", avgScore >= 80 ? "stable" : "declining",
            "change", avgScore >= 80 ? "+0.5" : "-2.3",
            "description", avgScore >= 80 ? "较上周基本持平" : "较上周有所下降"
        ));
        
        return report;
    }

    @Override
    public List<Map<String, Object>> getHighRiskDevices() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        List<Map<String, Object>> riskList = new ArrayList<>();
        
        for (DeviceInfo device : devices) {
            Map<String, Object> score = calculateHealthScore(device);
            double totalScore = (double) score.get("totalScore");
            
            // 健康评分低于70分视为高风险
            if (totalScore < 70) {
                score.put("deviceCode", device.getDeviceCode());
                score.put("deviceName", device.getDeviceName());
                score.put("categoryId", device.getCategoryId());
                score.put("locationId", device.getLocationId());
                score.put("brand", device.getBrand());
                
                // 风险等级
                String riskLevel;
                if (totalScore < 50) riskLevel = "critical";
                else if (totalScore < 60) riskLevel = "high";
                else riskLevel = "medium";
                
                score.put("riskLevel", riskLevel);
                riskList.add(score);
            }
        }
        
        // 按风险评分降序排序
        riskList.sort((a, b) -> Double.compare(
            (double) b.get("totalScore"), (double) a.get("totalScore")));
        
        return riskList;
    }

    private Map<String, Object> calculateHealthScore(DeviceInfo device) {
        Map<String, Object> score = new HashMap<>();
        
        // 1. 在线状态评分 (权重 20%)
        double onlineScore = 100;
        if (device.getOnlineStatus() != null && device.getOnlineStatus() == 0) {
            onlineScore = 50; // 离线设备
        }
        score.put("onlineScore", onlineScore);
        
        // 2. 故障频率评分 (权重 30%)
        long faultCount = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getDeviceCode, device.getDeviceCode())
                .ge(FaultReport::getReportTime, 
                    new Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000)) // 最近1年
        ).intValue();
        
        double faultScore = 100;
        if (faultCount > 10) faultScore = 30;
        else if (faultCount > 5) faultScore = 50;
        else if (faultCount > 2) faultScore = 70;
        score.put("faultScore", faultScore);
        score.put("faultCount", faultCount);
        
        // 3. 预警状态评分 (权重 25%)
        long alertCount = deviceAlertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>()
                .eq(DeviceAlert::getDeviceCode, device.getDeviceCode())
                .eq(DeviceAlert::getStatus, 0) // 未处理的预警
        ).intValue();
        
        double alertScore = 100;
        if (alertCount > 5) alertScore = 30;
        else if (alertCount > 2) alertScore = 50;
        else if (alertCount > 0) alertScore = 70;
        score.put("alertScore", alertScore);
        score.put("activeAlertCount", alertCount);
        
        // 4. 使用年限评分 (权重 25%)
        double ageScore = 100;
        if (device.getPurchaseDate() != null) {
            LocalDate purchase = device.getPurchaseDate();
            int years = LocalDate.now().getYear() - purchase.getYear();
            // 默认预期寿命10年
            double usageRatio = years / 10.0;
            
            if (usageRatio >= 0.9) ageScore = 30;
            else if (usageRatio >= 0.7) ageScore = 50;
            else if (usageRatio >= 0.5) ageScore = 70;
        }
        score.put("ageScore", ageScore);
        
        // 综合评分
        double totalScore = onlineScore * 0.20 + faultScore * 0.30 
            + alertScore * 0.25 + ageScore * 0.25;
        score.put("totalScore", Math.round(totalScore * 10) / 10.0);
        
        // 评分等级
        String level;
        if (totalScore >= 90) level = "excellent";
        else if (totalScore >= 80) level = "good";
        else if (totalScore >= 70) level = "fair";
        else if (totalScore >= 60) level = "poor";
        else level = "critical";
        score.put("level", level);
        
        // 问题诊断
        List<String> issues = new ArrayList<>();
        if (onlineScore < 80) issues.add("设备离线，请检查网络连接");
        if (faultScore < 80) issues.add("故障频率较高，需加强维护");
        if (alertScore < 80) issues.add("存在未处理的预警，请及时处理");
        if (ageScore < 80) issues.add("使用年限较长，关注设备更新");
        score.put("issues", issues);
        
        return score;
    }
}
