package com.campus.equipment.dto;

import lombok.Data;

@Data
public class WorkOrderDto {
    private Long deviceId;
    private Long userId;
    private Long assigneeId;
    private String title;
    private String description;
    private String orderType;
    private Integer priority;
}
