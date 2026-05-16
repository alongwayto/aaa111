package com.campus.equipment.service.impl;

import com.campus.equipment.entity.ChatbotInteraction;
import com.campus.equipment.mapper.ChatbotInteractionMapper;
import com.campus.equipment.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final ChatbotInteractionMapper chatbotInteractionMapper;

    @Override
    public Map<String, Object> query(Long userId, String query) {
        String response = "已收到您的问题，建议先检查设备电源、网络连接和最近告警记录。";
        ChatbotInteraction interaction = new ChatbotInteraction();
        interaction.setUserId(userId);
        interaction.setUserMessage(query);
        interaction.setBotResponse(response);
        interaction.setIntent(detectIntent(query));
        interaction.setEntities("{}");
        chatbotInteractionMapper.insert(interaction);

        Map<String, Object> result = new HashMap<>();
        result.put("intent", interaction.getIntent());
        result.put("response", response);
        return result;
    }

    @Override
    public Map<String, Object> troubleshooting(Long deviceId) {
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("steps", new String[]{"检查供电", "检查网络", "查看最近告警", "执行重启"});
        return result;
    }

    @Override
    public Map<String, Object> deviceBooking(String requirement) {
        Map<String, Object> result = new HashMap<>();
        result.put("requirement", requirement);
        result.put("recommendation", "建议预订 A 栋 301 的备用设备，当前负载较低。");
        return result;
    }

    private String detectIntent(String query) {
        if (query == null) {
            return "unknown";
        }
        if (query.contains("故障") || query.contains("异常")) {
            return "troubleshooting";
        }
        if (query.contains("预订") || query.contains("预约")) {
            return "booking";
        }
        return "qa";
    }
}
