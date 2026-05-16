package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("energy_consumption")
public class EnergyConsumption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private String deviceName;
    private BigDecimal energyUsed;
    private BigDecimal cost;
    private LocalDate statDate;
    private Integer warningLevel;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
