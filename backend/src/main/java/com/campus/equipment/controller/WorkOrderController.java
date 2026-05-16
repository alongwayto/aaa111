package com.campus.equipment.controller;

import com.campus.equipment.service.WorkOrderService;
import com.campus.equipment.entity.WorkOrder;
import com.campus.equipment.dto.WorkOrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 工单管理 Controller
 */
@RestController
@RequestMapping("/v1/work-order")
@Tag(name = "工单管理", description = "工单相关接口")
public class WorkOrderController {

    @Autowired
    private WorkOrderService workOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建工单")
    public ResponseEntity<Map<String, Object>> createWorkOrder(@RequestBody WorkOrderDto workOrderDto) {
        Long workOrderId = workOrderService.createWorkOrder(workOrderDto);
        return ResponseEntity.ok(Map.of("code", 200, "workOrderId", workOrderId, "message", "工单已创建"));
    }

    @GetMapping("/pending")
    @Operation(summary = "获取待处理工单")
    public ResponseEntity<List<WorkOrder>> getPendingOrders() {
        return ResponseEntity.ok(workOrderService.getPendingOrders());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户工单")
    public ResponseEntity<List<WorkOrder>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(workOrderService.getUserOrders(userId));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取工单统计")
    public ResponseEntity<Map<String, Object>> getWorkOrderStatistics() {
        return ResponseEntity.ok(workOrderService.getWorkOrderStatistics());
    }

    @PutMapping("/{workOrderId}/status")
    @Operation(summary = "更新工单状态")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long workOrderId,
            @RequestParam Integer status,
            @RequestParam String remark) {
        workOrderService.updateOrderStatus(workOrderId, status, remark);
        return ResponseEntity.ok(Map.of("code", 200, "message", "工单状态已更新"));
    }

    @GetMapping("/{workOrderId}/completion-time")
    @Operation(summary = "预测完成时间")
    public ResponseEntity<Map<String, Object>> predictCompletionTime(@PathVariable Long workOrderId) {
        String estimatedTime = workOrderService.predictCompletionTime(workOrderId);
        return ResponseEntity.ok(Map.of("estimatedCompletionTime", estimatedTime));
    }
}
