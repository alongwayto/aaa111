package com.campus.equipment.service;

import java.util.Map;

public interface DataAnalysisService {
    Map<String, Object> getDashboardData();

    Map<String, Object> getUsageTrend();

    Map<String, Object> getFailurePatterns();

    Map<String, Object> generateReport(String reportType);
}
