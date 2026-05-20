package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.SmartNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smart/notification")
@RequiredArgsConstructor
public class SmartNotificationController {

    private final SmartNotificationService smartNotificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(smartNotificationService.getNotifications(userId, page, size));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getUnreadCount(@RequestParam(required = false) Long userId) {
        return Result.success(smartNotificationService.getUnreadCount(userId));
    }

    /**
     * 标记通知已读
     */
    @PostMapping("/read/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> markAsRead(@PathVariable Long id) {
        boolean success = smartNotificationService.markAsRead(id);
        return success ? Result.success() : Result.fail("通知不存在");
    }

    /**
     * 标记所有通知已读
     */
    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<?> markAllAsRead(@RequestParam(required = false) Long userId) {
        smartNotificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteNotification(@PathVariable Long id) {
        boolean success = smartNotificationService.deleteNotification(id);
        return success ? Result.success() : Result.fail("通知不存在");
    }

    /**
     * 获取通知统计
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getNotificationStats(@RequestParam(required = false) Long userId) {
        return Result.success(smartNotificationService.getNotificationStats(userId));
    }

    /**
     * 订阅通知类型
     */
    @PostMapping("/subscribe")
    @PreAuthorize("isAuthenticated()")
    public Result<?> subscribeNotification(
            @RequestParam(required = false) Long userId,
            @RequestParam String type) {
        boolean success = smartNotificationService.subscribeNotification(userId, type);
        return success ? Result.success() : Result.fail("订阅失败");
    }

    /**
     * 取消订阅
     */
    @PostMapping("/unsubscribe")
    @PreAuthorize("isAuthenticated()")
    public Result<?> unsubscribeNotification(
            @RequestParam(required = false) Long userId,
            @RequestParam String type) {
        boolean success = smartNotificationService.unsubscribeNotification(userId, type);
        return success ? Result.success() : Result.fail("取消订阅失败");
    }

    /**
     * 获取订阅列表
     */
    @GetMapping("/subscriptions")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getUserSubscriptions(@RequestParam(required = false) Long userId) {
        return Result.success(smartNotificationService.getUserSubscriptions(userId));
    }

    /**
     * 触发智能通知生成（管理员调用）
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> generateNotifications() {
        smartNotificationService.generateIntelligentNotifications();
        return Result.success(Map.of("message", "智能通知生成完成"));
    }
}
