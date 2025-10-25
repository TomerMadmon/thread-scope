package com.threadscope.core.monitoring;

import com.threadscope.core.ThreadInfo;
import com.threadscope.core.configuration.ThreadScopeConfig;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Thread monitoring service following Single Responsibility Principle.
 * Responsible only for thread monitoring, not snapshot generation or scheduling.
 * Uses Observer pattern for event-driven monitoring.
 */
public class ThreadMonitorService {
    
    private final ThreadScopeConfig config;
    private final ThreadMXBean threadMXBean;
    private final ExecutorService executor;
    private final List<ThreadDetector<?>> detectors;
    private final List<Consumer<MonitoringEvent>> eventListeners;
    
    private volatile boolean running = false;
    
    public ThreadMonitorService(ThreadScopeConfig config) {
        this.config = Objects.requireNonNull(config, "Configuration cannot be null");
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.detectors = new ArrayList<>();
        this.eventListeners = new ArrayList<>();
        
        initializeDetectors();
    }
    
    /**
     * Initializes thread detectors based on configuration.
     */
    private void initializeDetectors() {
        if (config.alerts().deadlockDetection()) {
            detectors.add(new DeadlockDetector());
        }
        
        if (config.advanced().enableAsyncDetection()) {
            detectors.add(new AsyncThreadDetector());
        }
    }
    
    /**
     * Starts the monitoring service.
     */
    public CompletableFuture<Void> start() {
        if (running) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            running = true;
            notifyEventListeners(new MonitoringEvent.ServiceStarted());
        }, executor);
    }
    
    /**
     * Stops the monitoring service.
     */
    public CompletableFuture<Void> stop() {
        if (!running) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            running = false;
            notifyEventListeners(new MonitoringEvent.ServiceStopped());
        }, executor);
    }
    
    /**
     * Gets all threads with detection results.
     */
    public CompletableFuture<List<ThreadInfo>> getAllThreads() {
        return CompletableFuture.supplyAsync(() -> {
            if (!running) {
                return List.of();
            }
            
            long[] threadIds = threadMXBean.getAllThreadIds();
            java.lang.management.ThreadInfo[] threadInfos = 
                threadMXBean.getThreadInfo(threadIds, config.advanced().maxStackTraceDepth());
            
            List<ThreadInfo> threads = Arrays.stream(threadInfos)
                .filter(Objects::nonNull)
                .filter(this::shouldIncludeThread)
                .map(ThreadInfo::from)
                .map(this::applyDetections)
                .toList();
            
            notifyEventListeners(new MonitoringEvent.ThreadsCollected(threads));
            return threads;
        }, executor);
    }
    
    /**
     * Gets threads by state.
     */
    public CompletableFuture<List<ThreadInfo>> getThreadsByState(ThreadInfo.ThreadState state) {
        return getAllThreads().thenApply(threads -> 
            threads.stream()
                .filter(thread -> thread.state() == state)
                .toList()
        );
    }
    
    /**
     * Gets async threads only.
     */
    public CompletableFuture<List<ThreadInfo>> getAsyncThreads() {
        return getAllThreads().thenApply(threads ->
            threads.stream()
                .filter(ThreadInfo::isAsyncThread)
                .toList()
        );
    }
    
    /**
     * Adds a thread detector.
     */
    public void addDetector(ThreadDetector<?> detector) {
        detectors.add(detector);
    }
    
    /**
     * Removes a thread detector.
     */
    public void removeDetector(ThreadDetector<?> detector) {
        detectors.remove(detector);
    }
    
    /**
     * Adds an event listener.
     */
    public void addEventListener(Consumer<MonitoringEvent> listener) {
        eventListeners.add(listener);
    }
    
    /**
     * Removes an event listener.
     */
    public void removeEventListener(Consumer<MonitoringEvent> listener) {
        eventListeners.remove(listener);
    }
    
    /**
     * Checks if the service is running.
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Determines if a thread should be included in monitoring.
     */
    private boolean shouldIncludeThread(java.lang.management.ThreadInfo threadInfo) {
        if (!config.advanced().includeSystemThreads() && isSystemThread(threadInfo)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if a thread is a system thread.
     */
    private boolean isSystemThread(java.lang.management.ThreadInfo threadInfo) {
        String name = threadInfo.getThreadName();
        return name.startsWith("GC task thread") ||
               name.startsWith("G1 ") ||
               name.startsWith("GC worker") ||
               name.startsWith("VM Thread") ||
               name.startsWith("VM Periodic Task Thread") ||
               name.startsWith("Reference Handler") ||
               name.startsWith("Finalizer") ||
               name.startsWith("Signal Dispatcher") ||
               name.startsWith("Attach Listener") ||
               name.startsWith("Common-Cleaner") ||
               name.startsWith("process reaper");
    }
    
    /**
     * Applies all detectors to a thread.
     */
    private ThreadInfo applyDetections(ThreadInfo threadInfo) {
        ThreadInfo result = threadInfo;
        
        for (ThreadDetector<?> detector : detectors) {
            if (detector.isEnabled()) {
                if (detector instanceof AsyncThreadDetector) {
                    AsyncThreadDetector asyncDetector = (AsyncThreadDetector) detector;
                    AsyncDetectionResult detection = asyncDetector.detect(threadInfo);
                    if (detection.isAsync()) {
                        result = result.withAsyncInfo(true, detection.type());
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Notifies all event listeners.
     */
    private void notifyEventListeners(MonitoringEvent event) {
        eventListeners.forEach(listener -> {
            try {
                listener.accept(event);
            } catch (Exception e) {
                // Log error but don't propagate
                System.err.println("Error notifying event listener: " + e.getMessage());
            }
        });
    }
    
    /**
     * Monitoring event class hierarchy.
     */
    public static abstract class MonitoringEvent {
        
        public static class ServiceStarted extends MonitoringEvent {
            @Override
            public String toString() {
                return "ServiceStarted{}";
            }
        }
        
        public static class ServiceStopped extends MonitoringEvent {
            @Override
            public String toString() {
                return "ServiceStopped{}";
            }
        }
        
        public static class ThreadsCollected extends MonitoringEvent {
            private final List<ThreadInfo> threads;
            
            public ThreadsCollected(List<ThreadInfo> threads) {
                this.threads = threads;
            }
            
            public List<ThreadInfo> threads() {
                return threads;
            }
            
            @Override
            public String toString() {
                return "ThreadsCollected{threads=" + threads.size() + "}";
            }
        }
        
        public static class DeadlockDetected extends MonitoringEvent {
            private final List<Long> threadIds;
            
            public DeadlockDetected(List<Long> threadIds) {
                this.threadIds = threadIds;
            }
            
            public List<Long> threadIds() {
                return threadIds;
            }
            
            @Override
            public String toString() {
                return "DeadlockDetected{threadIds=" + threadIds + "}";
            }
        }
        
        public static class AsyncThreadDetected extends MonitoringEvent {
            private final ThreadInfo thread;
            
            public AsyncThreadDetected(ThreadInfo thread) {
                this.thread = thread;
            }
            
            public ThreadInfo thread() {
                return thread;
            }
            
            @Override
            public String toString() {
                return "AsyncThreadDetected{thread=" + thread + "}";
            }
        }
    }
}
