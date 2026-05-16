package com.campus.equipment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chatbot_interaction")
public class ChatbotInteraction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userMessage;
    private String botResponse;
    private String intent;
    private String entities;
    private Integer satisfactionScore;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
