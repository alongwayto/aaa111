package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.AnalysisReport;
import com.campus.equipment.entity.EnergyConsumption;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.mapper.AnalysisReportMapper;
import com.campus.equipment.mapper.EnergyConsumptionMapper;
import com.campus.equipment.mapper.FaultReportMapper;
import com.campus.equipment.service.DataAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataAnalysisServiceImpl implements DataAnalysisService {

    private final FaultReportMapper faultReportMapper;
    private final EnergyConsumptionMapper energyConsumptionMapper;
    private final AnalysisReportMapper analysisReportMapper;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("usageTrend", getUsageTrend());
        result.put("failurePatterns", getFailurePatterns());
        result.put("energy", energyConsumptionMapper.selectList(new LambdaQueryWrapper<EnergyConsumption>()
                .orderByDesc(EnergyConsumption::getStatDate)
                .last("LIMIT 10")));
        return result;
    }

    @Override
    public Map<String, Object> getUsageTrend() {
        List<String> dates = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.toString());
            values.add((int) (60 + Math.random() * 40));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("values", values);
        return result;
    }

    @Override
    public Map<String, Object> getFailurePatterns() {
        List<FaultReport> faults = faultReportMapper.selectList(new LambdaQueryWrapper<FaultReport>()
                .eq(FaultReport::getDeleted, 0));
        Map<String, Long> grouped = faults.stream().collect(Collectors.groupingBy(
                f -> f.getFaultType() == null ? "其他" : f.getFaultType(),
                Collectors.counting()
        ));
        List<Map<String, Object>> top10 = grouped.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("top10", top10);
        return result;
    }

    @Override
    public Map<String, Object> generateReport(String reportType) {
        AnalysisReport report = new AnalysisReport();
        report.setReportType(reportType == null ? "dashboard" : reportType);
        report.setReportName("智能分析报告-" + LocalDate.now());
        report.setReportContent("系统自动生成报告摘要");
        report.setGeneratedBy("system");
        analysisReportMapper.insert(report);

        Map<String, Object> result = new HashMap<>();
        result.put("reportId", report.getId());
        result.put("reportName", report.getReportName());
        result.put("status", "generated");
        return result;
    }
}
