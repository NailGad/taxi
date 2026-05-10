package com.taxi.notification.worker;

import com.taxi.notification.model.NotificationTask;
import com.taxi.notification.repository.NotificationRepository;
import com.taxi.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Value("${notification.worker.pool-size:4}")
    private int poolSize;

    @Value("${notification.worker.poll-interval-ms:5000}")
    private long pollIntervalMs;

    @Value("${notification.worker.max-retries:3}")
    private int maxRetries;

    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void startWorkers() {
        log.info("Starting Notification Worker pool with {} threads", poolSize);
        executorService = Executors.newFixedThreadPool(poolSize);
        running.set(true);

        for (int i = 0; i < poolSize; i++) {
            final int workerId = i;
            executorService.submit(() -> runWorker(workerId));
        }
    }

    private void runWorker(int workerId) {
        log.info("Worker {} started", workerId);

        while (running.get()) {
            try {
                NotificationTask task = notificationRepository.findNextPendingTask(maxRetries)
                        .orElse(null);

                if (task != null) {
                    log.info("Worker {} picked up task {}", workerId, task.getId());

                    notificationService.sendNotification(task);

                    log.debug("Worker {} finished task {}", workerId, task.getId());
                } else {
                    Thread.sleep(pollIntervalMs);
                }
            } catch (InterruptedException e) {
                log.warn("Worker {} interrupted", workerId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Worker {} error: {}", workerId, e.getMessage(), e);
                try {
                    Thread.sleep(pollIntervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Worker {} stopped", workerId);
    }

    @PreDestroy
    public void gracefulShutdown() {
        log.info("Starting graceful shutdown of Notification Worker pool");
        running.set(false);

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.warn("Worker pool didn't terminate gracefully, forcing shutdown");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("Notification Worker pool shutdown complete");
    }
}