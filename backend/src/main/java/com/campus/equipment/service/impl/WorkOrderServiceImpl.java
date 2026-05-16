package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.dto.WorkOrderDto;
import com.campus.equipment.entity.WorkOrder;
import com.campus.equipment.mapper.WorkOrderMapper;
import com.campus.equipment.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderMapper workOrderMapper;

    @Override
    public Long createWorkOrder(WorkOrderDto workOrderDto) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setDeviceId(workOrderDto.getDeviceId());
        workOrder.setUserId(workOrderDto.getUserId());
        workOrder.setAssigneeId(workOrderDto.getAssigneeId());
        workOrder.setTitle(workOrderDto.getTitle());
        workOrder.setDescription(workOrderDto.getDescription());
        workOrder.setOrderType(workOrderDto.getOrderType());
        workOrder.setPriority(workOrderDto.getPriority() == null ? 2 : workOrderDto.getPriority());
        workOrder.setStatus(0);
        workOrderMapper.insert(workOrder);
        return workOrder.getId();
    }

    @Override
    public List<WorkOrder> getPendingOrders() {
        return workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getStatus, 0)
                .orderByDesc(WorkOrder::getCreateTime));
    }

    @Override
    public List<WorkOrder> getUserOrders(Long userId) {
        return workOrderMapper.selectList(new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getUserId, userId)
                .orderByDesc(WorkOrder::getCreateTime));
    }

    @Override
    public Map<String, Object> getWorkOrderStatistics() {
        long total = workOrderMapper.selectCount(new LambdaQueryWrapper<>());
        long pending = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 0));
        long processing = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 1));
        long completed = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 2));
        long closed = workOrderMapper.selectCount(new LambdaQueryWrapper<WorkOrder>().eq(WorkOrder::getStatus, 3));

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pending", pending);
        result.put("processing", processing);
        result.put("completed", completed);
        result.put("closed", closed);
        result.put("completionRate", total == 0 ? 0 : (completed * 100.0 / total));
        return result;
    }

    @Override
    public void updateOrderStatus(Long workOrderId, Integer status, String remark) {
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            return;
        }
        workOrder.setStatus(status);
        workOrder.setRemark(remark);
        workOrderMapper.updateById(workOrder);
    }

    @Override
    public String predictCompletionTime(Long workOrderId) {
        WorkOrder workOrder = workOrderMapper.selectById(workOrderId);
        if (workOrder == null) {
            return "未知";
        }

        int hours = 24;
        if (workOrder.getPriority() != null && workOrder.getPriority() >= 3) {
            hours = 4;
        } else if (workOrder.getPriority() != null && workOrder.getPriority() == 2) {
            hours = 12;
        }
        LocalDateTime estimate = LocalDateTime.now().plusHours(hours);
        return estimate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
