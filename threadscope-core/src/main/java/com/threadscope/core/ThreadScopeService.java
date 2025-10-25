package com.threadscope.core;

import com.threadscope.core.configuration.ThreadScopeConfig;
import com.threadscope.core.monitoring.ThreadMonitorService;
import com.threadscope.core.monitoring.ThreadMonitorService.MonitoringEvent;
import com.threadscope.core.snapshot.SnapshotGenerator;
import com.threadscope.core.snapshot.SnapshotGenerator.SnapshotData;

import java.util.List;
import java.util.Objects;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main ThreadScope service orchestrator following SOLID principles.
 * Coordinates between monitoring, snapshot generation, and scheduling.
 * Uses Dependency Injection and Observer pattern.
 */
public class ThreadScopeService {
    
    private final ThreadScopeConfig config;
    private final ThreadMonitorService monitorService;
    private final SnapshotGenerator snapshotGenerator;
    private final ScheduledExecutorService scheduler;
    
    private volatile boolean running = false;
    
    public ThreadScopeService(ThreadScopeConfig config) {
        this.config = Objects.requireNonNull(config, "Configuration cannot be null");
        this.monitorService = new ThreadMonitorService(config);
        this.snapshotGenerator = new SnapshotGenerator(config);
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ThreadScope-Scheduler");
            t.setDaemon(true);
            return t;
        });
        
        setupEventHandling();
    }
    
    /**
     * Sets up event handling between components.
     */
    private void setupEventHandling() {
        monitorService.addEventListener(this::handleMonitoringEvent);
    }
    
    /**
     * Starts the ThreadScope service.
     */
    public CompletableFuture<Void> start() {
        if (running) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                running = true;
                
                // Start monitoring service
                monitorService.start().join();
                
                // Schedule periodic snapshots
                if (config.snapshot().enabled()) {
                    scheduleSnapshots();
                }
                
                // Schedule deadlock detection
                if (config.alerts().deadlockDetection()) {
                    scheduleDeadlockDetection();
                }
                
            } catch (Exception e) {
                throw new ThreadScopeException("Failed to start ThreadScope service", e);
            }
        });
    }
    
    /**
     * Stops the ThreadScope service.
     */
    public CompletableFuture<Void> stop() {
        if (!running) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                running = false;
                
                // Stop scheduler
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                
                // Stop monitoring service
                monitorService.stop().join();
                
            } catch (Exception e) {
                throw new ThreadScopeException("Failed to stop ThreadScope service", e);
            }
        });
    }
    
    /**
     * Gets all threads.
     */
    public CompletableFuture<List<ThreadInfo>> getAllThreads() {
        return monitorService.getAllThreads();
    }
    
    /**
     * Gets threads by state.
     */
    public CompletableFuture<List<ThreadInfo>> getThreadsByState(ThreadInfo.ThreadState state) {
        return monitorService.getThreadsByState(state);
    }
    
    /**
     * Gets async threads.
     */
    public CompletableFuture<List<ThreadInfo>> getAsyncThreads() {
        return monitorService.getAsyncThreads();
    }
    
    /**
     * Generates a snapshot manually.
     */
    public CompletableFuture<Void> generateSnapshot() {
        return monitorService.getAllThreads()
            .thenCompose(threads -> {
                SnapshotData snapshotData = new SnapshotData(
                    Instant.now(),
                    threads,
                    List.of(), // Deadlocks would be detected separately
                    threads.size(),
                    (int) threads.stream()
                        .filter(thread -> thread.state() == ThreadInfo.ThreadState.RUNNABLE)
                        .count()
                );
                
                return snapshotGenerator.generateSnapshot(snapshotData);
            });
    }
    
    /**
     * Checks if the service is running.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gets the current configuration.
     */
    public ThreadScopeConfig getConfig() {
        return config;
    }
    
    /**
     * Schedules periodic snapshots.
     */
    private void scheduleSnapshots() {
        Duration interval = config.snapshot().interval();
        scheduler.scheduleAtFixedRate(
            () -> generateSnapshot().exceptionally(throwable -> {
                System.err.println("Error generating snapshot: " + throwable.getMessage());
                return null;
            }),
            0,
            interval.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Schedules deadlock detection.
     */
    private void scheduleDeadlockDetection() {
        scheduler.scheduleAtFixedRate(
            () -> {
                // Deadlock detection is handled by the monitor service
                // This is just a periodic trigger
            },
            5,
            5,
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Handles monitoring events.
     */
    private void handleMonitoringEvent(MonitoringEvent event) {
        if (event instanceof MonitoringEvent.ServiceStarted) {
            System.out.println("ThreadScope monitoring service started");
        } else if (event instanceof MonitoringEvent.ServiceStopped) {
            System.out.println("ThreadScope monitoring service stopped");
        } else if (event instanceof MonitoringEvent.ThreadsCollected) {
            MonitoringEvent.ThreadsCollected collected = (MonitoringEvent.ThreadsCollected) event;
            System.out.println("Collected " + collected.threads().size() + " threads");
        } else if (event instanceof MonitoringEvent.DeadlockDetected) {
            MonitoringEvent.DeadlockDetected detected = (MonitoringEvent.DeadlockDetected) event;
            System.err.println("DEADLOCK DETECTED: " + detected.threadIds());
        } else if (event instanceof MonitoringEvent.AsyncThreadDetected) {
            MonitoringEvent.AsyncThreadDetected detected = (MonitoringEvent.AsyncThreadDetected) event;
            System.out.println("Async thread detected: " + detected.thread().name());
        }
    }
    
    /**
     * Custom exception for ThreadScope errors.
     */
    public static class ThreadScopeException extends RuntimeException {
        public ThreadScopeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
