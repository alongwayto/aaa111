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
@TableName("inventory_management")
public class InventoryManagement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String itemCode;
    private String itemName;
    private Integer currentStock;
    private Integer minStock;
    private Integer predictedDemand;
    private BigDecimal unitCost;
    private String supplier;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
