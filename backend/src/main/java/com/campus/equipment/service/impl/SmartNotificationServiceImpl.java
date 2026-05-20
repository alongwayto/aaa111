package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.*;
import com.campus.equipment.mapper.*;
import com.campus.equipment.service.SmartHealthService;
import com.campus.equipment.service.SmartNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartNotificationServiceImpl implements SmartNotificationService {

    private final SmartHealthService smartHealthService;
    private final DeviceInfoMapper deviceInfoMapper;
    private final DeviceAlertMapper deviceAlertMapper;
    private final FaultReportMapper faultReportMapper;
    private final FaultWorkOrderMapper workOrderMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    // 模拟通知存储（实际应使用数据库）
    private static final Map<Long, List<Map<String, Object>>> notificationStore = new ConcurrentHashMap<>();
    private static final Map<Long, Set<String>> userSubscriptions = new ConcurrentHashMap<>();

    @Override
    public List<Map<String, Object>> getNotifications(Long userId, int page, int size) {
        List<Map<String, Object>> notifications = notificationStore.getOrDefault(userId, new ArrayList<>());
        
        // 按时间倒序
        notifications.sort((a, b) -> {
            LocalDateTime timeA = (LocalDateTime) a.getOrDefault("createTime", LocalDateTime.now().minusDays(1));
            LocalDateTime timeB = (LocalDateTime) b.getOrDefault("createTime", LocalDateTime.now());
            return timeB.compareTo(timeA);
        });
        
        // 分页
        int start = page * size;
        int end = Math.min(start + size, notifications.size());
        
        if (start >= notifications.size()) {
            return new ArrayList<>();
        }
        
        return notifications.subList(start, end);
    }

    @Override
    public Map<String, Object> getUnreadCount(Long userId) {
        List<Map<String, Object>> notifications = notificationStore.getOrDefault(userId, new ArrayList<>());
        
        long total = notifications.size();
        long unread = notifications.stream()
            .filter(n -> !Boolean.TRUE.equals(n.get("read")))
            .count();
        
        Map<String, Long> byType = new HashMap<>();
        for (Map<String, Object> n : notifications) {
            String type = (String) n.getOrDefault("type", "other");
            byType.merge(type, 1L, Long::sum);
        }
        
        return Map.of(
            "total", total,
            "unread", unread,
            "byType", byType
        );
    }

    @Override
    public boolean markAsRead(Long notificationId) {
        for (List<Map<String, Object>> notifications : notificationStore.values()) {
            for (Map<String, Object> n : notifications) {
                if (notificationId.equals(n.get("id"))) {
                    n.put("read", true);
                    n.put("readTime", LocalDateTime.now());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        List<Map<String, Object>> notifications = notificationStore.get(userId);
        if (notifications == null) return true;
        
        LocalDateTime now = LocalDateTime.now();
        notifications.forEach(n -> {
            n.put("read", true);
            n.put("readTime", now);
        });
        return true;
    }

    @Override
    public boolean deleteNotification(Long notificationId) {
        for (Map.Entry<Long, List<Map<String, Object>>> entry : notificationStore.entrySet()) {
            boolean removed = entry.getValue().removeIf(n -> notificationId.equals(n.get("id")));
            if (removed) return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getNotificationStats(Long userId) {
        List<Map<String, Object>> notifications = notificationStore.getOrDefault(userId, new ArrayList<>());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notifications.size());
        
        // 按类型统计
        Map<String, Long> byType = new HashMap<>();
        // 按级别统计
        Map<String, Long> byLevel = new HashMap<>();
        // 按状态统计
        Map<String, Long> byStatus = new HashMap<>();
        
        for (Map<String, Object> n : notifications) {
            String type = (String) n.getOrDefault("type", "other");
            String level = (String) n.getOrDefault("level", "info");
            boolean read = Boolean.TRUE.equals(n.get("read"));
            
            byType.merge(type, 1L, Long::sum);
            byLevel.merge(level, 1L, Long::sum);
            byStatus.merge(read ? "read" : "unread", 1L, Long::sum);
        }
        
        stats.put("byType", byType);
        stats.put("byLevel", byLevel);
        stats.put("byStatus", byStatus);
        
        // 计算各类型占比
        Map<String, String> typePercentages = new HashMap<>();
        int total = notifications.size();
        if (total > 0) {
            for (Map.Entry<String, Long> entry : byType.entrySet()) {
                double percentage = entry.getValue() * 100.0 / total;
                typePercentages.put(entry.getKey(), String.format("%.1f%%", percentage));
            }
        }
        stats.put("typePercentages", typePercentages);
        
        return stats;
    }

    @Override
    public boolean subscribeNotification(Long userId, String type) {
        userSubscriptions.computeIfAbsent(userId, k -> new HashSet<>()).add(type);
        return true;
    }

    @Override
    public boolean unsubscribeNotification(Long userId, String type) {
        Set<String> subs = userSubscriptions.get(userId);
        if (subs != null) {
            subs.remove(type);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getUserSubscriptions(Long userId) {
        return new ArrayList<>(userSubscriptions.getOrDefault(userId, new HashSet<>()));
    }

    @Override
    public void generateIntelligentNotifications() {
        log.info("开始生成智能通知...");
        
        // 获取所有管理员和维护人员
        List<SysUser> adminsAndMaintains = getAdminsAndMaintains();
        
        for (SysUser user : adminsAndMaintains) {
            generateHealthAlerts(user.getId());
            generateWorkOrderNotifications(user.getId());
            generateMaintenanceReminders(user.getId());
        }
        
        log.info("智能通知生成完成");
    }

    private List<SysUser> getAdminsAndMaintains() {
        // 获取所有角色为管理员或维护员的用户
        List<SysUserRole> allRoles = userRoleMapper.selectList(null);
        Set<Long> adminAndMaintainIds = allRoles.stream()
            .filter(r -> r.getRoleId() == 1 || r.getRoleId() == 2) // 1=管理员, 2=维护员
            .map(SysUserRole::getUserId)
            .collect(Collectors.toSet());
        
        if (adminAndMaintainIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, adminAndMaintainIds)
                .eq(SysUser::getStatus, 1)
        );
    }

    private void generateHealthAlerts(Long userId) {
        // 获取高风险设备
        List<Map<String, Object>> highRiskDevices = smartHealthService.getHighRiskDevices();
        
        for (Map<String, Object> device : highRiskDevices) {
            String riskLevel = (String) device.get("riskLevel");
            String level = "info";
            
            switch (riskLevel) {
                case "critical" -> level = "critical";
                case "high" -> level = "warning";
                default -> level = "info";
            }
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", generateNotificationId());
            notification.put("type", "health_alert");
            notification.put("level", level);
            notification.put("title", "设备健康预警");
            notification.put("content", String.format("设备「%s」健康评分仅为 %.1f 分，存在%s风险",
                device.get("deviceName"),
                device.get("totalScore"),
                riskLevel.equals("critical") ? "高" : "中"));
            notification.put("deviceCode", device.get("deviceCode"));
            notification.put("deviceName", device.get("deviceName"));
            notification.put("score", device.get("totalScore"));
            notification.put("riskLevel", riskLevel);
            notification.put("read", false);
            notification.put("createTime", LocalDateTime.now());
            
            addNotification(userId, notification);
        }
    }

    private void generateWorkOrderNotifications(Long userId) {
        // 获取待处理的工单
        long pendingCount = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, 0)
        );
        
        if (pendingCount > 0) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", generateNotificationId());
            notification.put("type", "workorder");
            notification.put("level", pendingCount > 10 ? "warning" : "info");
            notification.put("title", "工单待处理提醒");
            notification.put("content", String.format("当前有 %d 个故障工单等待处理，请及时分配", pendingCount));
            notification.put("pendingCount", pendingCount);
            notification.put("read", false);
            notification.put("createTime", LocalDateTime.now());
            
            addNotification(userId, notification);
        }
        
        // 获取超时的工单
        long overdueCount = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, 1)
                .lt(FaultWorkOrder::getUpdateTime, 
                    new Date(System.currentTimeMillis() - 48 * 60 * 60 * 1000)) // 超过48小时
        );
        
        if (overdueCount > 0) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", generateNotificationId());
            notification.put("type", "workorder_overdue");
            notification.put("level", "critical");
            notification.put("title", "工单处理超时");
            notification.put("content", String.format("有 %d 个工单已处理超过48小时未完成，请关注", overdueCount));
            notification.put("overdueCount", overdueCount);
            notification.put("read", false);
            notification.put("createTime", LocalDateTime.now());
            
            addNotification(userId, notification);
        }
    }

    private void generateMaintenanceReminders(Long userId) {
        // 获取需要维护的设备
        List<DeviceInfo> allDevices = deviceInfoMapper.selectList(null);
        List<Map<String, Object>> reminders = new ArrayList<>();
        
        for (DeviceInfo device : allDevices) {
            // 检查最后维护时间
            if (device.getLastMaintainTime() != null) {
                long daysSinceMaintenance = java.time.Duration.between(
                    device.getLastMaintainTime(), LocalDateTime.now()).toDays();
                
                if (daysSinceMaintenance > 30) { // 超过30天未维护
                    reminders.add(Map.of(
                        "device", device,
                        "days", daysSinceMaintenance
                    ));
                }
            }
        }
        
        if (!reminders.isEmpty()) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", generateNotificationId());
            notification.put("type", "maintenance_reminder");
            notification.put("level", "info");
            notification.put("title", "设备维护提醒");
            notification.put("content", String.format("有 %d 台设备超过30天未进行维护，建议安排巡检", reminders.size()));
            notification.put("deviceCount", reminders.size());
            notification.put("devices", reminders.stream()
                .limit(5)
                .map(r -> Map.of(
                    "deviceCode", ((DeviceInfo) r.get("device")).getDeviceCode(),
                    "deviceName", ((DeviceInfo) r.get("device")).getDeviceName(),
                    "days", r.get("days")
                ))
                .collect(Collectors.toList()));
            notification.put("read", false);
            notification.put("createTime", LocalDateTime.now());
            
            addNotification(userId, notification);
        }
    }

    private void addNotification(Long userId, Map<String, Object> notification) {
        notificationStore.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, notification);
        
        // 保持最多100条通知
        List<Map<String, Object>> notifications = notificationStore.get(userId);
        if (notifications.size() > 100) {
            notifications.subList(100, notifications.size()).clear();
        }
    }

    private Long generateNotificationId() {
        return System.currentTimeMillis() + new Random().nextInt(1000);
    }
}
