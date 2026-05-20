package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.*;
import com.campus.equipment.mapper.*;
import com.campus.equipment.service.SmartDashboardService;
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
public class SmartDashboardServiceImpl implements SmartDashboardService {

    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceAlertMapper deviceAlertMapper;
    private final FaultReportMapper faultReportMapper;
    private final FaultWorkOrderMapper workOrderMapper;
    private final MaintenanceCostMapper maintenanceCostMapper;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        // 今日概览
        data.put("todayOverview", getTodayOverview());
        
        // 系统健康
        data.put("systemHealth", getSystemHealthOverview());
        
        // 实时监控
        data.put("realtimeMonitor", getRealtimeMonitor());
        
        // 智能洞察
        data.put("insights", getInsights());
        
        // 待办事项
        data.put("todoItems", getTodoItems());
        
        // 快速操作
        data.put("quickActions", getQuickActions());
        
        // 设备统计
        data.put("deviceStats", getDeviceStats());
        
        // 故障统计
        data.put("faultStats", getFaultStats());
        
        // 成本分析
        data.put("costAnalysis", getCostAnalysis());
        
        return data;
    }

    @Override
    public List<Map<String, Object>> getInsights() {
        List<Map<String, Object>> insights = new ArrayList<>();
        
        // 1. 设备健康洞察
        Map<String, Object> healthInsight = analyzeHealthInsight();
        if (healthInsight != null) {
            insights.add(healthInsight);
        }
        
        // 2. 故障趋势洞察
        Map<String, Object> faultInsight = analyzeFaultTrendInsight();
        if (faultInsight != null) {
            insights.add(faultInsight);
        }
        
        // 3. 维护效率洞察
        Map<String, Object> maintenanceInsight = analyzeMaintenanceInsight();
        if (maintenanceInsight != null) {
            insights.add(maintenanceInsight);
        }
        
        // 4. 成本优化洞察
        Map<String, Object> costInsight = analyzeCostInsight();
        if (costInsight != null) {
            insights.add(costInsight);
        }
        
        // 5. 设备分布洞察
        Map<String, Object> deviceInsight = analyzeDeviceDistributionInsight();
        if (deviceInsight != null) {
            insights.add(deviceInsight);
        }
        
        return insights;
    }

    private Map<String, Object> analyzeHealthInsight() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        if (devices.isEmpty()) return null;
        
        int onlineCount = (int) devices.stream().filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1).count();
        double onlineRate = devices.size() > 0 ? (double) onlineCount / devices.size() * 100 : 0;
        
        String insight;
        String type;
        
        if (onlineRate >= 95) {
            insight = "系统运行状态极佳，所有设备正常运行";
            type = "positive";
        } else if (onlineRate >= 85) {
            insight = "大部分设备运行正常，整体状态良好";
            type = "info";
        } else if (onlineRate >= 70) {
            insight = String.format("有 %.0f%% 的设备离线，请检查网络连接", 100 - onlineRate);
            type = "warning";
        } else {
            insight = String.format("离线设备较多（%.0f%%），建议立即排查", 100 - onlineRate);
            type = "critical";
        }
        
        return Map.of(
            "category", "health",
            "type", type,
            "title", "设备健康洞察",
            "insight", insight,
            "data", Map.of(
                "onlineCount", onlineCount,
                "totalCount", devices.size(),
                "onlineRate", Math.round(onlineRate * 10) / 10.0
            )
        );
    }

    private Map<String, Object> analyzeFaultTrendInsight() {
        // 最近7天的故障数据
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<FaultReport> recentFaults = faultReportMapper.selectList(
            new LambdaQueryWrapper<FaultReport>()
                .ge(FaultReport::getReportTime, weekAgo)
        );
        
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        List<FaultReport> monthFaults = faultReportMapper.selectList(
            new LambdaQueryWrapper<FaultReport>()
                .ge(FaultReport::getReportTime, monthAgo)
        );
        
        double weeklyAvg = monthFaults.size() / 4.0;
        String insight;
        String type;
        
        if (recentFaults.size() > weeklyAvg * 1.5) {
            insight = String.format("本周故障数激增（%d起），较平均水平高出 %.0f%%，建议加强巡检", 
                recentFaults.size(), (recentFaults.size() / weeklyAvg - 1) * 100);
            type = "critical";
        } else if (recentFaults.size() > weeklyAvg * 1.2) {
            insight = "故障频率略有上升，建议关注";
            type = "warning";
        } else {
            insight = String.format("故障率正常，本周共发生 %d 起故障", recentFaults.size());
            type = "positive";
        }
        
        return Map.of(
            "category", "fault",
            "type", type,
            "title", "故障趋势洞察",
            "insight", insight,
            "data", Map.of(
                "weeklyFaults", recentFaults.size(),
                "monthlyFaults", monthFaults.size(),
                "weeklyAvg", Math.round(weeklyAvg * 10) / 10.0
            )
        );
    }

    private Map<String, Object> analyzeMaintenanceInsight() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        int maintainedCount = (int) devices.stream()
            .filter(d -> d.getLastMaintainTime() != null && d.getLastMaintainTime().isAfter(monthAgo))
            .count();
        
        double maintenanceRate = devices.size() > 0 ? (double) maintainedCount / devices.size() * 100 : 0;
        
        String insight;
        String type;
        
        if (maintenanceRate >= 80) {
            insight = "维护工作执行良好，大部分设备得到及时维护";
            type = "positive";
        } else if (maintenanceRate >= 50) {
            insight = String.format("仅 %.0f%% 的设备得到月度维护，建议增加维护频次", maintenanceRate);
            type = "warning";
        } else {
            insight = "维护覆盖率不足，建议立即安排设备巡检";
            type = "critical";
        }
        
        return Map.of(
            "category", "maintenance",
            "type", type,
            "title", "维护效率洞察",
            "insight", insight,
            "data", Map.of(
                "maintainedCount", maintainedCount,
                "totalCount", devices.size(),
                "maintenanceRate", Math.round(maintenanceRate * 10) / 10.0
            )
        );
    }

    private Map<String, Object> analyzeCostInsight() {
        List<MaintenanceCost> costs = maintenanceCostMapper.selectList(
            new LambdaQueryWrapper<MaintenanceCost>()
                .ge(MaintenanceCost::getCostDate, LocalDate.now().minusMonths(1))
        );
        
        BigDecimal totalCost = costs.stream()
            .map(MaintenanceCost::getCostAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String insight;
        String type;
        
        if (costs.isEmpty()) {
            insight = "本月暂无维护成本记录";
            type = "info";
        } else if (totalCost.compareTo(new BigDecimal("10000")) > 0) {
            insight = String.format("本月维护成本较高，累计 %.2f 元，建议关注", totalCost);
            type = "warning";
        } else {
            insight = String.format("本月维护成本控制良好，共 %.2f 元", totalCost);
            type = "positive";
        }
        
        return Map.of(
            "category", "cost",
            "type", type,
            "title", "成本分析洞察",
            "insight", insight,
            "data", Map.of(
                "costCount", costs.size(),
                "totalCost", totalCost
            )
        );
    }

    private Map<String, Object> analyzeDeviceDistributionInsight() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        
        Map<Integer, Long> byStatus = devices.stream()
            .collect(Collectors.groupingBy(
                d -> d.getStatus() != null ? d.getStatus() : 0,
                Collectors.counting()
            ));
        
        // 计算维修中设备占比
        long repairingCount = byStatus.getOrDefault(2, 0L);
        double repairingRate = devices.size() > 0 ? (double) repairingCount / devices.size() * 100 : 0;
        
        String insight;
        String type;
        
        if (repairingRate > 20) {
            insight = String.format("%.0f%% 的设备正在维修，可能存在集中故障", repairingRate);
            type = "warning";
        } else if (repairingRate > 10) {
            insight = String.format("%.0f%% 的设备维修中，运行状态基本正常", repairingRate);
            type = "info";
        } else {
            insight = "设备运行状态良好，大部分设备正常工作";
            type = "positive";
        }
        
        return Map.of(
            "category", "distribution",
            "type", type,
            "title", "设备分布洞察",
            "insight", insight,
            "data", Map.of(
                "byStatus", byStatus,
                "repairingCount", repairingCount,
                "totalCount", devices.size()
            )
        );
    }

    @Override
    public List<Map<String, Object>> getTodoItems() {
        List<Map<String, Object>> todos = new ArrayList<>();
        
        // 1. 待处理故障
        long pendingFaults = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>().isNull(FaultReport::getStatus)
        );
        if (pendingFaults > 0) {
            todos.add(Map.of(
                "id", 1,
                "type", "fault",
                "priority", "high",
                "title", "待处理故障",
                "content", String.format("有 %d 个故障待确认", pendingFaults),
                "count", pendingFaults
            ));
        }
        
        // 2. 待分配工单
        long unassignedOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>().eq(FaultWorkOrder::getStatus, 0)
        );
        if (unassignedOrders > 0) {
            todos.add(Map.of(
                "id", 2,
                "type", "workorder",
                "priority", "high",
                "title", "待分配工单",
                "content", String.format("有 %d 个工单等待分配", unassignedOrders),
                "count", unassignedOrders
            ));
        }
        
        // 3. 处理中超时工单
        long overdueOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, 1)
                .lt(FaultWorkOrder::getUpdateTime, 
                    new Date(System.currentTimeMillis() - 48 * 60 * 60 * 1000))
        );
        if (overdueOrders > 0) {
            todos.add(Map.of(
                "id", 3,
                "type", "overdue",
                "priority", "critical",
                "title", "处理超时",
                "content", String.format("有 %d 个工单处理超过48小时", overdueOrders),
                "count", overdueOrders
            ));
        }
        
        // 4. 未处理预警
        long pendingAlerts = deviceAlertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 0)
        );
        if (pendingAlerts > 0) {
            todos.add(Map.of(
                "id", 4,
                "type", "alert",
                "priority", "medium",
                "title", "未处理预警",
                "content", String.format("有 %d 条设备预警未处理", pendingAlerts),
                "count", pendingAlerts
            ));
        }
        
        // 5. 需要维护的设备
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        long needsMaintenance = deviceInfoMapper.selectCount(
            new LambdaQueryWrapper<DeviceInfo>()
                .and(w -> w
                    .isNull(DeviceInfo::getLastMaintainTime)
                    .or()
                    .lt(DeviceInfo::getLastMaintainTime, monthAgo)
                )
        );
        if (needsMaintenance > 0) {
            todos.add(Map.of(
                "id", 5,
                "type", "maintenance",
                "priority", "low",
                "title", "待维护设备",
                "content", String.format("有 %d 台设备超过30天未维护", needsMaintenance),
                "count", needsMaintenance
            ));
        }
        
        // 按优先级排序
        todos.sort((a, b) -> {
            String p1 = (String) a.get("priority");
            String p2 = (String) b.get("priority");
            int order = Map.of("critical", 0, "high", 1, "medium", 2, "low", 3).getOrDefault(p1, 4)
                - Map.of("critical", 0, "high", 1, "medium", 2, "low", 3).getOrDefault(p2, 4);
            return order;
        });
        
        return todos;
    }

    @Override
    public Map<String, Object> getSystemHealthOverview() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        
        // 计算在线率
        int onlineCount = (int) devices.stream()
            .filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1)
            .count();
        double onlineRate = devices.size() > 0 ? (double) onlineCount / devices.size() * 100 : 0;
        
        // 计算故障率
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long weekFaults = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>().ge(FaultReport::getReportTime, weekAgo)
        );
        double faultRate = devices.size() > 0 ? (double) weekFaults / devices.size() * 10 : 0;
        
        // 计算预警率
        long pendingAlerts = deviceAlertMapper.selectCount(
            new LambdaQueryWrapper<DeviceAlert>().eq(DeviceAlert::getStatus, 0)
        );
        double alertRate = devices.size() > 0 ? (double) pendingAlerts / devices.size() * 100 : 0;
        
        // 综合健康评分
        double healthScore = 100 - (faultRate * 5) - (alertRate * 0.5);
        healthScore = Math.max(0, Math.min(100, healthScore));
        
        // 状态描述
        String status;
        String statusDesc;
        if (healthScore >= 90) {
            status = "excellent";
            statusDesc = "系统运行极佳";
        } else if (healthScore >= 80) {
            status = "good";
            statusDesc = "系统运行正常";
        } else if (healthScore >= 70) {
            status = "fair";
            statusDesc = "需要注意";
        } else if (healthScore >= 60) {
            status = "warning";
            statusDesc = "需要关注";
        } else {
            status = "critical";
            statusDesc = "需要立即处理";
        }
        
        return Map.of(
            "score", Math.round(healthScore * 10) / 10.0,
            "status", status,
            "statusDesc", statusDesc,
            "onlineRate", Math.round(onlineRate * 10) / 10.0,
            "weekFaults", weekFaults,
            "faultRate", Math.round(faultRate * 10) / 10.0,
            "pendingAlerts", pendingAlerts,
            "alertRate", Math.round(alertRate * 10) / 10.0,
            "totalDevices", devices.size()
        );
    }

    @Override
    public Map<String, Object> getRealtimeMonitor() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        
        // 按设备类型统计在线状态
        Map<String, Map<String, Object>> byType = new LinkedHashMap<>();
        
        for (DeviceInfo device : devices) {
            String type = device.getCategoryId() != null ? 
                String.valueOf(device.getCategoryId()) : "unknown";
            
            byType.computeIfAbsent(type, k -> Map.of(
                "total", 0,
                "online", 0,
                "offline", 0
            ));
            
            Map<String, Object> stats = new HashMap<>(byType.get(type));
            stats.put("total", (int) stats.get("total") + 1);
            if (device.getOnlineStatus() != null && device.getOnlineStatus() == 1) {
                stats.put("online", (int) stats.get("online") + 1);
            } else {
                stats.put("offline", (int) stats.get("offline") + 1);
            }
            byType.put(type, stats);
        }
        
        // 实时数据点
        List<Map<String, Object>> dataPoints = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("time", LocalDateTime.now().minusMinutes(10 - i)
                .format(DateTimeFormatter.ofPattern("HH:mm")));
            point.put("online", (int) devices.stream()
                .filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1)
                .count() + (int) (Math.random() * 5 - 2));
            dataPoints.add(point);
        }
        
        return Map.of(
            "totalDevices", devices.size(),
            "onlineDevices", devices.stream()
                .filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1)
                .count(),
            "byType", byType,
            "dataPoints", dataPoints,
            "updateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME)
        );
    }

    @Override
    public Map<String, Object> getTodayOverview() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        
        // 今日新增设备
        long todayDevices = deviceInfoMapper.selectCount(
            new LambdaQueryWrapper<DeviceInfo>()
                .ge(DeviceInfo::getCreateTime, startOfDay)
        );
        
        // 今日故障
        long todayFaults = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>()
                .ge(FaultReport::getReportTime, startOfDay)
        );
        
        // 今日工单
        long todayWorkOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .ge(FaultWorkOrder::getCreateTime, startOfDay)
        );
        
        // 今日完成工单
        long todayCompleted = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, 3)
                .ge(FaultWorkOrder::getUpdateTime, startOfDay)
        );
        
        // 今日成本
        List<MaintenanceCost> todayCosts = maintenanceCostMapper.selectList(
            new LambdaQueryWrapper<MaintenanceCost>()
                .ge(MaintenanceCost::getCostDate, LocalDate.now())
        );
        BigDecimal todayCost = todayCosts.stream()
            .map(MaintenanceCost::getCostAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
            "date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            "newDevices", todayDevices,
            "newFaults", todayFaults,
            "newWorkOrders", todayWorkOrders,
            "completedWorkOrders", todayCompleted,
            "maintenanceCost", todayCost
        );
    }

    @Override
    public List<Map<String, Object>> getQuickActions() {
        List<Map<String, Object>> actions = new ArrayList<>();
        
        actions.add(Map.of(
            "id", "add_device",
            "icon", "plus",
            "title", "添加设备",
            "description", "登记新设备信息",
            "path", "/device/add"
        ));
        
        actions.add(Map.of(
            "id", "report_fault",
            "icon", "warning",
            "title", "故障报修",
            "description", "提交设备故障报告",
            "path", "/fault/report"
        ));
        
        actions.add(Map.of(
            "id", "work_orders",
            "icon", "document",
            "title", "工单管理",
            "description", "查看和处理工单",
            "path", "/fault/workorder"
        ));
        
        actions.add(Map.of(
            "id", "maintenance",
            "icon", "tool",
            "title", "维护计划",
            "description", "制定设备维护计划",
            "path", "/maintenance/plan"
        ));
        
        actions.add(Map.of(
            "id", "ai_assistant",
            "icon", "chat",
            "title", "AI 助手",
            "description", "智能诊断和建议",
            "path", "/ai"
        ));
        
        actions.add(Map.of(
            "id", "smart_insights",
            "icon", "data-analysis",
            "title", "智能洞察",
            "description", "查看系统智能分析",
            "path", "/dashboard/smart"
        ));
        
        return actions;
    }

    private Map<String, Object> getDeviceStats() {
        List<DeviceInfo> devices = deviceInfoMapper.selectList(null);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", devices.size());
        stats.put("online", devices.stream()
            .filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1).count());
        stats.put("offline", devices.stream()
            .filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 0).count());
        stats.put("normal", devices.stream()
            .filter(d -> d.getStatus() != null && d.getStatus() == 1).count());
        stats.put("repairing", devices.stream()
            .filter(d -> d.getStatus() != null && d.getStatus() == 2).count());
        stats.put("scrapped", devices.stream()
            .filter(d -> d.getStatus() != null && d.getStatus() == 3).count());
        
        return stats;
    }

    private Map<String, Object> getFaultStats() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 本周故障
        long weekFaults = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>().ge(FaultReport::getReportTime, weekAgo)
        );
        
        // 本月故障
        long monthFaults = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>().ge(FaultReport::getReportTime, monthAgo)
        );
        
        // 待处理
        long pending = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>()
                .isNull(FaultReport::getStatus)
        );
        
        // 已解决
        long resolved = faultReportMapper.selectCount(
            new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getStatus, 3)
        );
        
        stats.put("week", weekFaults);
        stats.put("month", monthFaults);
        stats.put("pending", pending);
        stats.put("resolved", resolved);
        
        return stats;
    }

    private Map<String, Object> getCostAnalysis() {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        List<MaintenanceCost> costs = maintenanceCostMapper.selectList(
            new LambdaQueryWrapper<MaintenanceCost>()
                .ge(MaintenanceCost::getCostDate, LocalDate.now().minusDays(30))
        );
        
        BigDecimal total = costs.stream()
            .map(MaintenanceCost::getCostAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, BigDecimal> byType = new LinkedHashMap<>();
        for (MaintenanceCost cost : costs) {
            String type = cost.getCostType() != null ? cost.getCostType() : "other";
            byType.merge(type, cost.getCostAmount() != null ? cost.getCostAmount() : BigDecimal.ZERO, BigDecimal::add);
        }
        
        return Map.of(
            "total", total,
            "count", costs.size(),
            "avgPerItem", costs.isEmpty() ? BigDecimal.ZERO : 
                total.divide(BigDecimal.valueOf(costs.size()), 2, BigDecimal.ROUND_HALF_UP),
            "byType", byType
        );
    }
}
