package com.campus.equipment.task;

import com.campus.equipment.service.BackupService;
import com.campus.equipment.service.AnomalyDetectionService;
import com.campus.equipment.service.DataAnalysisService;
import com.campus.equipment.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final BackupService backupService;
    private final MonitorService monitorService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final DataAnalysisService dataAnalysisService;

    @Scheduled(fixedRate = 15000, initialDelay = 5000)
    public void simulateDeviceStatus() {
        try {
            monitorService.simulateRealtimeStatus();
        } catch (Exception e) {
            log.warn("Realtime status simulation failed", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoBackup() {
        log.info("Start automatic database backup");
        try {
            backupService.backup(2, "system");
            log.info("Automatic database backup completed");
        } catch (Exception e) {
            log.error("Automatic database backup failed", e);
        }
    }

    @Scheduled(cron = "0 30 3 * * ?")
    public void cleanOldStatusRecords() {
        log.info("Historical status cleanup task executed");
    }

    @Scheduled(cron = "${smart.anomaly.cron:0 */10 * * * ?}")
    public void runAnomalyDetection() {
        try {
            int processed = anomalyDetectionService.runRealtimeDetection();
            log.info("Anomaly detection completed, processed={} devices", processed);
        } catch (Exception e) {
            log.error("Anomaly detection task failed", e);
        }
    }

    @Scheduled(cron = "${smart.report.cron:0 0 6 * * ?}")
    public void generateDailyReport() {
        try {
            dataAnalysisService.generateReport("daily");
            log.info("Daily analysis report generated");
        } catch (Exception e) {
            log.error("Report generation task failed", e);
        }
    }
}
