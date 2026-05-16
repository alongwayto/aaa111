package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_metrics")
public class DeviceMetrics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private BigDecimal cpuUsage;
    private BigDecimal memoryUsage;
    private BigDecimal diskUsage;
    private BigDecimal temperature;
    private BigDecimal anomalyScore;
    private Integer riskLevel;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
