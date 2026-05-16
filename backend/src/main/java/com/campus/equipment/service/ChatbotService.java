package com.campus.equipment.service;

import java.util.Map;

public interface ChatbotService {
    Map<String, Object> query(Long userId, String query);

    Map<String, Object> troubleshooting(Long deviceId);

    Map<String, Object> deviceBooking(String requirement);
}
