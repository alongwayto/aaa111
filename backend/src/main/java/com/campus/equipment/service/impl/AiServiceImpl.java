package com.campus.equipment.service.impl;

import com.campus.equipment.entity.DeviceInfo;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.MaintenanceCost;
import com.campus.equipment.mapper.DeviceInfoMapper;
import com.campus.equipment.mapper.FaultReportMapper;
import com.campus.equipment.mapper.MaintenanceCostMapper;
import com.campus.equipment.service.AiService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final DeviceInfoMapper deviceMapper;
    private final FaultReportMapper faultMapper;
    private final MaintenanceCostMapper costMapper;

    @Value("${ai.api.url:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String aiApiUrl;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.model:doubao-pro)}")
    private String aiModel;

    private static final String SYSTEM_PROMPT = """"
        你是一个专业的校园设备管理AI助手，名字叫"智校园"。
        
        你的主要职责：
        1. 回答关于校园设备管理系统的各种问题
        2. 提供设备故障诊断和维护建议
        3. 分析设备运行数据和趋势
        4. 帮助用户理解设备状态和健康度
        
        回复要求：
        - 使用中文回答
        - 专业但易于理解
        - 适当使用emoji增加友好度
        - 对于需要具体数据的查询，引导用户提供设备ID或时间范围
        - 如遇系统问题，提供清晰的解决步骤
        
        如果用户询问的内容超出设备管理范围，礼貌地将对话引导回设备管理话题。
        """;

    @Override
    public String chat(List<Map<String, String>> messages, String systemPrompt) {
        try {
            String finalSystemPrompt = systemPrompt.isEmpty() ? SYSTEM_PROMPT : systemPrompt;
            
            // 构建消息列表
            List<Map<String, Object>> chatMessages = new ArrayList<>();
            chatMessages.add(Map.of("role", "system", "content", finalSystemPrompt));
            
            for (Map<String, String> msg : messages) {
                chatMessages.add(Map.of(
                    "role", msg.getOrDefault("role", "user"),
                    "content", msg.getOrDefault("content", "")
                ));
            }

            // 如果没有用户消息，添加一个默认消息
            boolean hasUserMessage = chatMessages.stream()
                .anyMatch(m -> "user".equals(m.get("role")));
            if (!hasUserMessage) {
                return "请输入您的问题。";
            }

            // 调用 AI API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel);
            requestBody.put("messages", chatMessages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + aiApiKey);

            String response = HttpUtil.createPost(aiApiUrl)
                .headerMap(headers, false)
                .body(JSONUtil.toJsonStr(requestBody))
                .timeout(60000)
                .execute()
                .body();

            return parseAiResponse(response);
        } catch (Exception e) {
            log.error("AI chat error", e);
            return getFallbackResponse();
        }
    }

    @Override
    public String diagnoseDevice(String deviceCode, String symptom) {
        try {
            // 获取设备信息
            DeviceInfo device = deviceMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeviceInfo>()
                    .eq(DeviceInfo::getDeviceCode, deviceCode)
            );

            StringBuilder context = new StringBuilder();
            context.append("设备诊断请求\n");
            context.append("设备编号: ").append(deviceCode).append("\n");
            context.append("故障现象: ").append(symptom).append("\n");
            
            if (device != null) {
                context.append("设备名称: ").append(device.getDeviceName()).append("\n");
                context.append("设备类型: ").append(device.getCategoryName()).append("\n");
                context.append("安装位置: ").append(device.getLocationName()).append("\n");
                context.append("当前状态: ").append(device.getStatus()).append("\n");
                context.append("健康评分: ").append(device.getHealthScore()).append("\n");
            }

            // 获取近期故障记录
            List<FaultReport> recentFaults = faultMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FaultReport>()
                    .eq(FaultReport::getDeviceCode, deviceCode)
                    .orderByDesc(FaultReport::getReportTime)
                    .last("LIMIT 5")
            );

            if (!recentFaults.isEmpty()) {
                context.append("\n近期故障记录:\n");
                for (FaultReport fault : recentFaults) {
                    context.append("- ").append(fault.getReportTime())
                           .append(": ").append(fault.getFaultDescription())
                           .append(" (状态: ").append(fault.getStatus()).append(")\n");
                }
            }

            String prompt = """
                请根据以下信息进行设备故障诊断：
                
                """ + context.toString() + """
                
                请提供：
                1. 可能的原因分析
                2. 建议的排查步骤
                3. 初步处理建议
                4. 是否需要专业人员介入
                """;

            return chat(List.of(Map.of("role", "user", "content", prompt)), "");
        } catch (Exception e) {
            log.error("Diagnose device error", e);
            return "诊断服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public String generateMaintenanceAdvice(Long deviceId, String location) {
        try {
            StringBuilder context = new StringBuilder();
            context.append("维护建议生成请求\n");
            
            if (deviceId != null) {
                DeviceInfo device = deviceMapper.selectById(deviceId);
                if (device != null) {
                    context.append("设备ID: ").append(device.getId()).append("\n");
                    context.append("设备名称: ").append(device.getDeviceName()).append("\n");
                    context.append("设备类型: ").append(device.getCategoryName()).append("\n");
                    context.append("当前状态: ").append(device.getStatus()).append("\n");
                    context.append("健康评分: ").append(device.getHealthScore()).append("\n");
                    context.append("上次维护: ").append(device.getLastMaintenanceDate()).append("\n");
                    context.append("下次维护: ").append(device.getNextMaintenanceDate()).append("\n");
                }
            } else if (location != null && !location.isEmpty()) {
                context.append("位置: ").append(location).append("\n");
                List<DeviceInfo> devices = deviceMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DeviceInfo>()
                        .like(DeviceInfo::getLocationName, location)
                );
                context.append("该位置共有 ").append(devices.size()).append(" 台设备\n");
            }

            // 获取维护成本数据
            List<MaintenanceCost> costs = costMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MaintenanceCost>()
                    .orderByDesc(MaintenanceCost::getCostDate)
                    .last("LIMIT 10")
            );

            if (!costs.isEmpty()) {
                context.append("\n近期维护成本:\n");
                for (MaintenanceCost cost : costs) {
                    context.append("- ").append(cost.getCostDate())
                           .append(": ¥").append(cost.getCostAmount())
                           .append(" (").append(cost.getCostType()).append(")\n");
                }
            }

            String prompt = """
                基于以下信息生成设备维护建议：
                
                """ + context.toString() + """
                
                请提供：
                1. 当前维护状态评估
                2. 维护优先级建议
                3. 具体维护项目清单
                4. 维护周期建议
                5. 成本优化建议
                """;

            return chat(List.of(Map.of("role", "user", "content", prompt)), "");
        } catch (Exception e) {
            log.error("Generate maintenance advice error", e);
            return "维护建议生成失败，请稍后再试。";
        }
    }

    @Override
    public String predictFailures(Long deviceId) {
        try {
            StringBuilder context = new StringBuilder();
            context.append("故障预测分析请求\n");
            
            DeviceInfo device = null;
            if (deviceId != null) {
                device = deviceMapper.selectById(deviceId);
            }
            
            if (device != null) {
                context.append("设备名称: ").append(device.getDeviceName()).append("\n");
                context.append("设备类型: ").append(device.getCategoryName()).append("\n");
                context.append("健康评分: ").append(device.getHealthScore()).append("\n");
                context.append("使用年限: ").append(device.getPurchaseDate() != null ? 
                    calculateYears(device.getPurchaseDate()) + " 年" : "未知").append("\n");
            }

            // 获取故障历史
            List<FaultReport> allFaults = faultMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FaultReport>()
                    .orderByDesc(FaultReport::getReportTime)
                    .last("LIMIT 20")
            );

            if (!allFaults.isEmpty()) {
                context.append("\n近期故障统计:\n");
                context.append("总故障数: ").append(allFaults.size()).append("\n");
                
                // 按类型统计
                Map<String, Long> faultTypeCount = new HashMap<>();
                for (FaultReport fault : allFaults) {
                    String type = fault.getFaultType() != null ? fault.getFaultType() : "其他";
                    faultTypeCount.merge(type, 1L, Long::sum);
                }
                context.append("故障类型分布:\n");
                faultTypeCount.forEach((type, count) -> 
                    context.append("  - ").append(type).append(": ").append(count).append(" 次\n"));
            }

            String prompt = """
                基于以下数据进行故障预测分析：
                
                """ + context.toString() + """
                
                请提供：
                1. 风险等级评估 (高/中/低)
                2. 可能发生的故障类型预测
                3. 预计发生时间窗口
                4. 预防措施建议
                5. 紧急程度和处理优先级
                """;

            return chat(List.of(Map.of("role", "user", "content", prompt)), "");
        } catch (Exception e) {
            log.error("Predict failures error", e);
            return "故障预测服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public String generateReport(String reportType, String timeRange) {
        try {
            StringBuilder context = new StringBuilder();
            context.append("报告生成请求\n");
            context.append("报告类型: ").append(reportType).append("\n");
            context.append("时间范围: ").append(timeRange).append("\n");

            // 获取统计数据
            long totalDevices = deviceMapper.selectCount(null);
            context.append("\n设备总数: ").append(totalDevices).append(" 台\n");

            List<FaultReport> faults = faultMapper.selectList(null);
            context.append("故障记录总数: ").append(faults.size()).append(" 条\n");

            List<MaintenanceCost> costs = costMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MaintenanceCost>()
                    .orderByDesc(MaintenanceCost::getCostDate)
                    .last("LIMIT 10")
            );
            
            if (!costs.isEmpty()) {
                double totalCost = costs.stream()
                    .mapToDouble(c -> c.getCostAmount() != null ? c.getCostAmount().doubleValue() : 0)
                    .sum();
                context.append("维护成本总计: ¥").append(String.format("%.2f", totalCost)).append("\n");
            }

            String prompt = """
                请生成一份 """ + reportType + """ 报告：
                
                """ + context.toString() + """
                
                报告要求：
                1. 结构清晰，使用markdown格式
                2. 包含数据摘要和分析
                3. 提供问题发现和改进建议
                4. 使用表格展示关键数据
                """;

            return chat(List.of(Map.of("role", "user", "content", prompt)), "");
        } catch (Exception e) {
            log.error("Generate report error", e);
            return "报告生成服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public String analyzeImage(Object file, String question) {
        // 图片分析功能需要额外的视觉模型支持
        // 这里提供一个基础实现
        return "图片分析功能正在开发中，敬请期待。\n\n" +
               "目前支持的功能：\n" +
               "- 设备拍照上传（用于设备识别）\n" +
               "- 故障现场照片分析\n" +
               "- 二维码/条码扫描识别";
    }

    @Override
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "智能校园助手");
        status.put("status", "running");
        status.put("version", "1.0.0");
        status.put("capabilities", List.of(
            "设备诊断",
            "维护建议",
            "故障预测",
            "报告生成",
            "智能问答"
        ));
        status.put("model", aiModel);
        status.put("uptime", System.currentTimeMillis());
        return status;
    }

    private String parseAiResponse(String response) {
        try {
            JSONObject json = JSONUtil.parseObj(response);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                if (message != null) {
                    return message.getStr("content");
                }
            }
            return "AI 服务返回格式异常";
        } catch (Exception e) {
            log.error("Parse AI response error", e);
            return "AI 服务响应解析失败";
        }
    }

    private String getFallbackResponse() {
        return """
            抱歉，AI 服务暂时不可用。这可能是由于：
            - 网络连接问题
            - AI 服务正在维护
            - 请求超时
            
            建议您：
            1. 检查网络连接
            2. 稍后再试
            3. 如问题持续，请联系系统管理员
            
            同时您可以尝试使用系统内置的设备诊断功能。
            """;
    }

    private double calculateYears(Date purchaseDate) {
        if (purchaseDate == null) return 0;
        long diff = System.currentTimeMillis() - purchaseDate.getTime();
        return diff / (365.25 * 24 * 60 * 60 * 1000.0);
    }
}
