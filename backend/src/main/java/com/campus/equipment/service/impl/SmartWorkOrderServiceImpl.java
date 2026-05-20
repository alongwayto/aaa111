package com.campus.equipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.equipment.entity.FaultReport;
import com.campus.equipment.entity.FaultWorkOrder;
import com.campus.equipment.entity.SysUser;
import com.campus.equipment.entity.SysUserRole;
import com.campus.equipment.mapper.FaultReportMapper;
import com.campus.equipment.mapper.FaultWorkOrderMapper;
import com.campus.equipment.mapper.SysUserMapper;
import com.campus.equipment.mapper.SysUserRoleMapper;
import com.campus.equipment.service.SmartWorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartWorkOrderServiceImpl implements SmartWorkOrderService {

    private final FaultReportMapper faultReportMapper;
    private final FaultWorkOrderMapper workOrderMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public Map<String, Object> smartAssign(Long faultId) {
        FaultReport fault = faultReportMapper.selectById(faultId);
        if (fault == null) {
            return Map.of("success", false, "message", "故障记录不存在");
        }

        // 获取所有维护人员
        List<SysUser> maintainers = getMaintainers();
        if (maintainers.isEmpty()) {
            return Map.of("success", false, "message", "没有可用的维护人员");
        }

        // 计算每个维护人员的匹配度
        List<Map<String, Object>> scores = new ArrayList<>();
        for (SysUser maintainer : maintainers) {
            double score = calculateMatchScore(fault, maintainer);
            Map<String, Object> maintainerScore = new HashMap<>();
            maintainerScore.put("userId", maintainer.getId());
            maintainerScore.put("username", maintainer.getUsername());
            maintainerScore.put("realName", maintainer.getRealName());
            maintainerScore.put("phone", maintainer.getPhone());
            maintainerScore.put("score", score);
            maintainerScore.put("currentWorkload", getCurrentWorkload(maintainer.getId()));
            maintainerScore.put("expertise", getExpertise(maintainer.getId(), fault.getFaultType()));
            maintainerScore.put("successRate", getSuccessRate(maintainer.getId()));
            maintainerScore.put("avgCompletionTime", getAvgCompletionTime(maintainer.getId()));
            scores.add(maintainerScore);
        }

        // 按匹配度排序
        scores.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));

        // 构建推荐结果
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("faultId", faultId);
        result.put("faultInfo", Map.of(
            "faultType", fault.getFaultType(),
            "faultLevel", fault.getFaultLevel(),
            "deviceCode", fault.getDeviceCode(),
            "description", fault.getFaultDescription()
        ));
        result.put("recommendations", scores);
        
        // 推荐最佳人选
        if (!scores.isEmpty()) {
            Map<String, Object> best = scores.get(0);
            result.put("bestCandidate", best);
            result.put("reason", generateRecommendReason(fault, (SysUser) maintainers.stream()
                .filter(m -> m.getId().equals(best.get("userId"))).findFirst().orElse(null)));
        }

        return result;
    }

    private List<SysUser> getMaintainers() {
        // 获取所有角色为维护员的用户
        List<Long> roleIds = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, 2L) // 2=维护员
        ).stream().map(SysUserRole::getUserId).collect(Collectors.toList());
        
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getId, roleIds)
                .eq(SysUser::getStatus, 1)
        );
    }

    private double calculateMatchScore(FaultReport fault, SysUser maintainer) {
        double score = 100.0;
        
        // 1. 技能匹配度 (权重 40%)
        String expertise = getExpertise(maintainer.getId(), fault.getFaultType());
        double skillScore = switch (expertise) {
            case "expert" -> 40.0;
            case "proficient" -> 30.0;
            case "familiar" -> 20.0;
            default -> 10.0;
        };
        score += skillScore - 20; // 基准20分
        
        // 2. 当前工作量 (权重 30%)
        int workload = getCurrentWorkload(maintainer.getId());
        if (workload == 0) score += 30;
        else if (workload <= 2) score += 20;
        else if (workload <= 5) score += 10;
        else score -= workload * 5;
        
        // 3. 历史成功率 (权重 20%)
        double successRate = getSuccessRate(maintainer.getId());
        score += successRate * 0.2;
        
        // 4. 距离/位置匹配 (权重 10%) - 简化处理
        score += 10.0; // 实际应考虑地理位置
        
        // 5. 故障级别调整
        if (fault.getFaultLevel() != null) {
            if (fault.getFaultLevel() == 4) { // 紧急
                // 紧急情况优先选择经验丰富的
                score += expertise.equals("expert") ? 20 : (expertise.equals("proficient") ? 10 : 0);
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    private String getExpertise(Long userId, String faultType) {
        // 基于历史工单统计该用户处理某类故障的经验
        LambdaQueryWrapper<FaultWorkOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FaultWorkOrder::getAssigneeId, userId);
        
        List<FaultWorkOrder> orders = workOrderMapper.selectList(wrapper);
        if (orders.isEmpty()) return "none";
        
        // 统计成功完成的数量
        long successCount = orders.stream()
            .filter(o -> o.getStatus() == 3 && o.getHandleResult() != null && o.getHandleResult() == 1)
            .count();
        
        double rate = successCount / orders.size();
        int experienceCount = orders.size();
        
        if (experienceCount >= 10 && rate >= 0.8) return "expert";
        if (experienceCount >= 5 && rate >= 0.6) return "proficient";
        if (experienceCount >= 2) return "familiar";
        return "none";
    }

    private int getCurrentWorkload(Long userId) {
        // 获取该用户待处理的工单数量
        return workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getAssigneeId, userId)
                .in(FaultWorkOrder::getStatus, 0, 1)
        ).intValue();
    }

    private double getSuccessRate(Long userId) {
        List<FaultWorkOrder> orders = workOrderMapper.selectList(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getAssigneeId, userId)
                .eq(FaultWorkOrder::getStatus, 3)
        );
        
        if (orders.isEmpty()) return 0.5; // 默认50%
        
        long successCount = orders.stream()
            .filter(o -> o.getHandleResult() != null && o.getHandleResult() == 1)
            .count();
        
        return (double) successCount / orders.size();
    }

    private double getAvgCompletionTime(Long userId) {
        List<FaultWorkOrder> completedOrders = workOrderMapper.selectList(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getAssigneeId, userId)
                .eq(FaultWorkOrder::getStatus, 3)
        );
        
        if (completedOrders.isEmpty()) return 0;
        
        return completedOrders.stream()
            .filter(o -> o.getActualStart() != null && o.getActualEnd() != null)
            .mapToLong(o -> o.getActualEnd().getTime() - o.getActualStart().getTime())
            .average()
            .orElse(0) / (1000 * 60 * 60); // 转换为小时
    }

    private String generateRecommendReason(FaultReport fault, SysUser maintainer) {
        if (maintainer == null) return "系统推荐";
        
        StringBuilder reason = new StringBuilder();
        reason.append("推荐 ").append(maintainer.getRealName());
        
        String expertise = getExpertise(maintainer.getId(), fault.getFaultType());
        switch (expertise) {
            case "expert" -> reason.append("，因其对").append(fault.getFaultType()).append("类型故障有丰富经验");
            case "proficient" -> reason.append("，其擅长处理").append(fault.getFaultType()).append("类故障");
            default -> reason.append("，当前工作量较少可及时处理");
        }
        
        int workload = getCurrentWorkload(maintainer.getId());
        if (workload < 3) {
            reason.append("，当前待办较少可快速响应");
        }
        
        return reason.toString();
    }

    @Override
    public List<Map<String, Object>> getMaintainerSkills() {
        List<SysUser> maintainers = getMaintainers();
        List<Map<String, Object>> skills = new ArrayList<>();
        
        for (SysUser maintainer : maintainers) {
            Map<String, Object> skill = new HashMap<>();
            skill.put("userId", maintainer.getId());
            skill.put("username", maintainer.getUsername());
            skill.put("realName", maintainer.getRealName());
            skill.put("phone", maintainer.getPhone());
            skill.put("currentWorkload", getCurrentWorkload(maintainer.getId()));
            skill.put("successRate", getSuccessRate(maintainer.getId()));
            skill.put("avgCompletionTime", getAvgCompletionTime(maintainer.getId()));
            skill.put("totalOrders", workOrderMapper.selectCount(
                new LambdaQueryWrapper<FaultWorkOrder>()
                    .eq(FaultWorkOrder::getAssigneeId, maintainer.getId())
            ).intValue());
            skills.add(skill);
        }
        
        return skills;
    }

    @Override
    public Map<String, Object> getWorkOrderAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // 总工单数
        long totalOrders = workOrderMapper.selectCount(null);
        analysis.put("totalOrders", totalOrders);
        
        // 待处理
        long pendingOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>().eq(FaultWorkOrder::getStatus, 0));
        analysis.put("pendingOrders", pendingOrders);
        
        // 处理中
        long processingOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>().eq(FaultWorkOrder::getStatus, 1));
        analysis.put("processingOrders", processingOrders);
        
        // 已完成
        long completedOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>().eq(FaultWorkOrder::getStatus, 3));
        analysis.put("completedOrders", completedOrders);
        
        // 完成率
        double completionRate = totalOrders > 0 ? (double) completedOrders / totalOrders * 100 : 0;
        analysis.put("completionRate", completionRate);
        
        // 平均处理时间
        List<FaultWorkOrder> completed = workOrderMapper.selectList(
            new LambdaQueryWrapper<FaultWorkOrder>().eq(FaultWorkOrder::getStatus, 3));
        double avgTime = completed.stream()
            .filter(o -> o.getActualStart() != null && o.getActualEnd() != null)
            .mapToLong(o -> o.getActualEnd().getTime() - o.getActualStart().getTime())
            .average()
            .orElse(0) / (1000 * 60 * 60);
        analysis.put("avgCompletionTime", avgTime);
        
        // 工单类型分布
        Map<String, Long> typeDistribution = new HashMap<>();
        completed.forEach(o -> {
            String type = o.getFaultType() != null ? o.getFaultType() : "其他";
            typeDistribution.merge(type, 1L, Long::sum);
        });
        analysis.put("typeDistribution", typeDistribution);
        
        // 性能排名
        List<Map<String, Object>> rankings = getMaintainerSkills().stream()
            .sorted((a, b) -> Double.compare((double) b.get("successRate"), (double) a.get("successRate")))
            .limit(5)
            .collect(Collectors.toList());
        analysis.put("topPerformers", rankings);
        
        return analysis;
    }

    @Override
    public Map<String, Object> predictCompletionTime(Long faultId) {
        FaultReport fault = faultReportMapper.selectById(faultId);
        if (fault == null) {
            return Map.of("success", false, "message", "故障记录不存在");
        }
        
        // 基于历史数据预测
        List<FaultWorkOrder> similarOrders = workOrderMapper.selectList(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getFaultType, fault.getFaultType())
                .eq(FaultWorkOrder::getStatus, 3)
        );
        
        Map<String, Object> prediction = new HashMap<>();
        prediction.put("faultId", faultId);
        prediction.put("faultType", fault.getFaultType());
        prediction.put("faultLevel", fault.getFaultLevel());
        
        if (similarOrders.isEmpty()) {
            // 无历史数据，使用默认值
            prediction.put("predictedHours", fault.getFaultLevel() == 4 ? 2 : (fault.getFaultLevel() == 3 ? 4 : 8));
            prediction.put("confidence", 0.5);
            prediction.put("reason", "基于故障级别估算");
        } else {
            // 计算平均处理时间
            double avgHours = similarOrders.stream()
                .filter(o -> o.getActualStart() != null && o.getActualEnd() != null)
                .mapToLong(o -> o.getActualEnd().getTime() - o.getActualStart().getTime())
                .average()
                .orElse(8 * 60 * 60 * 1000) / (1000 * 60 * 60);
            
            // 根据故障级别调整
            double levelFactor = switch (fault.getFaultLevel() != null ? fault.getFaultLevel() : 1) {
                case 4 -> 0.5;  // 紧急，加快
                case 3 -> 0.8;  // 高
                case 2 -> 1.0;  // 中
                default -> 1.2; // 低，可稍慢
            };
            
            prediction.put("predictedHours", avgHours * levelFactor);
            prediction.put("avgHistoricalHours", avgHours);
            prediction.put("confidence", Math.min(0.95, 0.5 + similarOrders.size() * 0.05));
            prediction.put("similarCasesCount", similarOrders.size());
        }
        
        return prediction;
    }

    @Override
    public List<Map<String, Object>> getSchedulingSuggestions() {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        // 检查工作量不均衡
        List<SysUser> maintainers = getMaintainers();
        if (maintainers.size() < 2) return suggestions;
        
        List<Integer> workloads = maintainers.stream()
            .map(m -> getCurrentWorkload(m.getId()))
            .sorted()
            .collect(Collectors.toList());
        
        int min = workloads.get(0);
        int max = workloads.get(workloads.size() - 1);
        
        if (max - min > 3) {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("type", "workload_imbalance");
            suggestion.put("level", "warning");
            suggestion.put("title", "工作量不均衡");
            suggestion.put("content", String.format("维护人员工作量差异较大，建议将部分工单从 '%s' 转移至其他人员",
                maintainers.stream()
                    .filter(m -> getCurrentWorkload(m.getId()) == max)
                    .findFirst()
                    .map(SysUser::getRealName)
                    .orElse("高负载人员")));
            suggestion.put("action", "rebalance");
            suggestions.add(suggestion);
        }
        
        // 检查长期未处理工单
        long overdueOrders = workOrderMapper.selectCount(
            new LambdaQueryWrapper<FaultWorkOrder>()
                .eq(FaultWorkOrder::getStatus, 0)
                .lt(FaultWorkOrder::getCreateTime, 
                    new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)) // 超过24小时
        );
        
        if (overdueOrders > 0) {
            Map<String, Object> suggestion = new HashMap<>();
            suggestion.put("type", "overdue_orders");
            suggestion.put("level", "critical");
            suggestion.put("title", "工单积压");
            suggestion.put("content", String.format("有 %d 个工单已超过24小时未分配，请及时处理", overdueOrders));
            suggestion.put("action", "urgent_assign");
            suggestions.add(suggestion);
        }
        
        // 检查成功率低的维护人员
        for (SysUser maintainer : maintainers) {
            double successRate = getSuccessRate(maintainer.getId());
            int totalOrders = workOrderMapper.selectCount(
                new LambdaQueryWrapper<FaultWorkOrder>()
                    .eq(FaultWorkOrder::getAssigneeId, maintainer.getId())
            ).intValue();
            
            if (totalOrders >= 5 && successRate < 0.6) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "low_success_rate");
                suggestion.put("level", "info");
                suggestion.put("title", "成功率待提升");
                suggestion.put("content", String.format("'%s' 的工单完成成功率仅 %.0f%%，建议安排培训或协助",
                    maintainer.getRealName(), successRate * 100));
                suggestion.put("userId", maintainer.getId());
                suggestion.put("action", "training");
                suggestions.add(suggestion);
            }
        }
        
        return suggestions;
    }
}
