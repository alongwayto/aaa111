package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("analysis_report")
public class AnalysisReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportName;
    private String reportType;
    private String reportContent;
    private String fileUrl;
    private String generatedBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
