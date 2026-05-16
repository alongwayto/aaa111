package com.campus.equipment.service;

import com.campus.equipment.dto.WorkOrderDto;
import com.campus.equipment.entity.WorkOrder;

import java.util.List;
import java.util.Map;

public interface WorkOrderService {
    Long createWorkOrder(WorkOrderDto workOrderDto);

    List<WorkOrder> getPendingOrders();

    List<WorkOrder> getUserOrders(Long userId);

    Map<String, Object> getWorkOrderStatistics();

    void updateOrderStatus(Long workOrderId, Integer status, String remark);

    String predictCompletionTime(Long workOrderId);
}
