package com.campus.equipment.controller;

import com.campus.equipment.common.Result;
import com.campus.equipment.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "AI Assistant")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 聊天对话")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
            String systemPrompt = (String) request.getOrDefault("systemPrompt", "");
            
            String response = aiService.chat(messages, systemPrompt);
            return Result.success(response);
        } catch (Exception e) {
            log.error("AI chat error", e);
            return Result.fail("AI 服务调用失败: " + e.getMessage());
        }
    }

    @Operation(summary = "设备智能诊断")
    @PostMapping("/diagnose")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<String> diagnose(@RequestBody Map<String, Object> request) {
        try {
            String deviceCode = (String) request.get("deviceCode");
            String symptom = (String) request.get("symptom");
            
            String diagnosis = aiService.diagnoseDevice(deviceCode, symptom);
            return Result.success(diagnosis);
        } catch (Exception e) {
            log.error("AI diagnose error", e);
            return Result.fail("诊断失败: " + e.getMessage());
        }
    }

    @Operation(summary = "维护建议生成")
    @PostMapping("/maintenance-advice")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<String> maintenanceAdvice(@RequestBody Map<String, Object> request) {
        try {
            Long deviceId = request.get("deviceId") != null 
                ? Long.valueOf(request.get("deviceId").toString()) : null;
            String location = (String) request.get("location");
            
            String advice = aiService.generateMaintenanceAdvice(deviceId, location);
            return Result.success(advice);
        } catch (Exception e) {
            log.error("AI maintenance advice error", e);
            return Result.fail("建议生成失败: " + e.getMessage());
        }
    }

    @Operation(summary = "故障预测")
    @PostMapping("/predictive-maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<String> predictiveMaintenance(@RequestBody Map<String, Object> request) {
        try {
            Long deviceId = request.get("deviceId") != null 
                ? Long.valueOf(request.get("deviceId").toString()) : null;
            
            String prediction = aiService.predictFailures(deviceId);
            return Result.success(prediction);
        } catch (Exception e) {
            log.error("AI predictive maintenance error", e);
            return Result.fail("预测失败: " + e.getMessage());
        }
    }

    @Operation(summary = "生成分析报告")
    @PostMapping("/generate-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<String> generateReport(@RequestBody Map<String, Object> request) {
        try {
            String reportType = (String) request.get("reportType");
            String timeRange = (String) request.get("timeRange");
            
            String report = aiService.generateReport(reportType, timeRange);
            return Result.success(report);
        } catch (Exception e) {
            log.error("AI generate report error", e);
            return Result.fail("报告生成失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取 AI 服务状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        try {
            Map<String, Object> status = aiService.getServiceStatus();
            return Result.success(status);
        } catch (Exception e) {
            log.error("Get AI status error", e);
            return Result.fail("获取状态失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传图片进行视觉分析")
    @PostMapping("/analyze-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAINTAINER')")
    public Result<String> analyzeImage(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "question", defaultValue = "请描述这张图片的内容") String question) {
        try {
            String analysis = aiService.analyzeImage(file, question);
            return Result.success(analysis);
        } catch (Exception e) {
            log.error("AI image analysis error", e);
            return Result.fail("图片分析失败: " + e.getMessage());
        }
    }
}
