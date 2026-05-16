package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/chatbot")
@Tag(name = "AI聊天机器人")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/query")
    @Operation(summary = "处理用户查询")
    public Result<Map<String, Object>> query(@RequestBody QueryReq req) {
        return Result.success(chatbotService.query(req.getUserId(), req.getQuery()));
    }

    @GetMapping("/troubleshooting/{deviceId}")
    @Operation(summary = "故障诊断")
    public Result<Map<String, Object>> troubleshooting(@PathVariable Long deviceId) {
        return Result.success(chatbotService.troubleshooting(deviceId));
    }

    @GetMapping("/device-booking")
    @Operation(summary = "设备预订建议")
    public Result<Map<String, Object>> booking(@RequestParam String requirement) {
        return Result.success(chatbotService.deviceBooking(requirement));
    }

    @Data
    public static class QueryReq {
        private Long userId;
        private String query;
    }
}
