package com.cobre.notifier.api.infrastructure.scheduler;

import com.cobre.notifier.api.application.port.input.RetryFailedNotificationsUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler que ejecuta periódicamente el proceso de reintento de notificaciones fallidas.
 * Utiliza exponential backoff configurado en RetryService.
 */
@Slf4j
@Component
public class RetryScheduler {

    private final RetryFailedNotificationsUseCase retryService;
    
    @Value("${app.notification.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public RetryScheduler(RetryFailedNotificationsUseCase retryService) {
        this.retryService = retryService;
    }

    /**
     * Ejecuta el proceso de reintento de notificaciones fallidas cada 60 segundos (configurable).
     * El delay entre ejecuciones se configura con app.notification.scheduler.fixed-delay-ms.
     */
    @Scheduled(fixedDelayString = "${app.notification.scheduler.fixed-delay-ms:60000}")
    public void scheduleRetryFailedNotifications() {
        if (!schedulerEnabled) {
            log.debug("Retry scheduler is disabled, skipping execution");
            return;
        }

        try {
            log.debug("Starting scheduled retry process for failed notifications");
            int processedCount = retryService.retryFailedNotifications();
            
            if (processedCount > 0) {
                log.info("Scheduled retry process completed. Processed {} notifications", processedCount);
            } else {
                log.debug("Scheduled retry process completed. No notifications to retry");
            }
        } catch (Exception e) {
            log.error("Error executing scheduled retry process", e);
            // No re-lanzar la excepción para que el scheduler continúe funcionando
        }
    }
}

