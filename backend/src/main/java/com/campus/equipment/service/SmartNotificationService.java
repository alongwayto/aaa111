package com.campus.equipment.service;

import java.util.List;
import java.util.Map;

public interface SmartNotificationService {
    
    /**
     * 获取智能通知列表
     */
    List<Map<String, Object>> getNotifications(Long userId, int page, int size);
    
    /**
     * 获取未读通知数量
     */
    Map<String, Object> getUnreadCount(Long userId);
    
    /**
     * 标记通知已读
     */
    boolean markAsRead(Long notificationId);
    
    /**
     * 标记所有通知已读
     */
    boolean markAllAsRead(Long userId);
    
    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId);
    
    /**
     * 获取通知统计
     */
    Map<String, Object> getNotificationStats(Long userId);
    
    /**
     * 订阅通知
     */
    boolean subscribeNotification(Long userId, String type);
    
    /**
     * 取消订阅
     */
    boolean unsubscribeNotification(Long userId, String type);
    
    /**
     * 获取用户订阅列表
     */
    List<String> getUserSubscriptions(Long userId);
    
    /**
     * 智能生成通知（由系统调用）
     */
    void generateIntelligentNotifications();
}
