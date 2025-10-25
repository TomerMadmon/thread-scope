package com.threadscope;

import com.threadscope.detectors.DeadlockDetector;
import com.threadscope.AsyncThreadDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Main thread monitoring service that collects thread information and generates snapshots.
 */
public class ThreadMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadMonitor.class);
    
    private final ThreadScopeConfig config;
    private final ThreadMXBean threadMXBean;
    private final DeadlockDetector deadlockDetector;
    private final ScheduledExecutorService scheduler;
    private final SnapshotGenerator snapshotGenerator;
    
    private volatile boolean running = false;

    public ThreadMonitor(ThreadScopeConfig config) {
        this.config = config;
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.deadlockDetector = new DeadlockDetector();
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ThreadScope-Monitor");
            t.setDaemon(true);
            return t;
        });
        this.snapshotGenerator = new SnapshotGenerator(config);
    }

    /**
     * Starts the thread monitoring service.
     */
    public void start() {
        if (running) {
            logger.warn("ThreadMonitor is already running");
            return;
        }
        
        if (!config.isEnabled()) {
            logger.info("ThreadScope is disabled");
            return;
        }
        
        logger.info("Starting ThreadScope monitoring");
        running = true;
        
        // Create output directory if it doesn't exist
        createOutputDirectory();
        
        // Schedule periodic snapshots
        Duration interval = config.getSnapshot().getInterval();
        scheduler.scheduleAtFixedRate(this::captureSnapshot, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
        
        // Schedule deadlock detection
        if (config.getAlerts().isDeadlockDetection()) {
            scheduler.scheduleAtFixedRate(this::checkForDeadlocks, 5, 5, TimeUnit.SECONDS);
        }
        
        logger.info("ThreadScope monitoring started with {} interval", interval);
    }

    /**
     * Stops the thread monitoring service.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        logger.info("Stopping ThreadScope monitoring");
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("ThreadScope monitoring stopped");
    }

    /**
     * Captures a snapshot of all threads.
     */
    public void captureSnapshot() {
        try {
            List<ThreadInfo> threads = getAllThreads();
            List<DeadlockDetector.DeadlockInfo> deadlocks = deadlockDetector.detectDeadlocks();
            
            Snapshot snapshot = new Snapshot();
            snapshot.setTimestamp(Instant.now());
            snapshot.setThreads(threads);
            snapshot.setDeadlocks(deadlocks);
            snapshot.setTotalThreads(threads.size());
            snapshot.setActiveThreads((int) threads.stream().filter(t -> "RUNNABLE".equals(t.getState())).count());
            
            // Generate snapshot files
            snapshotGenerator.generateSnapshot(snapshot);
            
            logger.debug("Captured snapshot with {} threads", threads.size());
            
        } catch (Exception e) {
            logger.error("Error capturing snapshot", e);
        }
    }

    /**
     * Gets information about all threads.
     */
    public List<ThreadInfo> getAllThreads() {
        long[] threadIds = threadMXBean.getAllThreadIds();
        java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, config.getAdvanced().getMaxStackTraceDepth());
        
        List<ThreadInfo> threads = new ArrayList<>();
        
        for (java.lang.management.ThreadInfo threadInfo : threadInfos) {
            if (threadInfo == null) {
                continue;
            }
            
            // Filter system threads if configured
            if (!config.getAdvanced().isIncludeSystemThreads() && isSystemThread(threadInfo)) {
                continue;
            }
            
            ThreadInfo info = convertToThreadInfo(threadInfo);
            
            // Detect and categorize async threads
            if (AsyncThreadDetector.isAsyncThread(threadInfo)) {
                info.setAsyncThread(true);
                info.setAsyncThreadType(AsyncThreadDetector.categorizeAsyncThread(threadInfo).name());
            }
            
            threads.add(info);
        }
        
        return threads;
    }

    /**
     * Checks for deadlocks and logs alerts.
     */
    private void checkForDeadlocks() {
        try {
            List<DeadlockDetector.DeadlockInfo> deadlocks = deadlockDetector.detectDeadlocks();
            if (!deadlocks.isEmpty()) {
                logger.error("DEADLOCK DETECTED: {} deadlock cycles found", deadlocks.size());
                for (DeadlockDetector.DeadlockInfo deadlock : deadlocks) {
                    logger.error("Deadlock cycle {}: threads {}", deadlock.getCycleId(), deadlock.getThreadIds());
                }
            }
        } catch (Exception e) {
            logger.error("Error checking for deadlocks", e);
        }
    }

    /**
     * Converts java.lang.management.ThreadInfo to our ThreadInfo.
     */
    private ThreadInfo convertToThreadInfo(java.lang.management.ThreadInfo threadInfo) {
        ThreadInfo info = new ThreadInfo();
        info.setId(threadInfo.getThreadId());
        info.setName(threadInfo.getThreadName());
        info.setState(threadInfo.getThreadState().toString());
        info.setDaemon(threadInfo.isDaemon());
        info.setPriority(threadInfo.getPriority());
        info.setInNative(threadInfo.isInNative());
        info.setSuspended(threadInfo.isSuspended());
        
        if (threadInfo.getLockName() != null) {
            info.setLockName(threadInfo.getLockName());
        }
        info.setLockOwnerId(threadInfo.getLockOwnerId());
        if (threadInfo.getLockOwnerName() != null) {
            info.setLockOwnerName(threadInfo.getLockOwnerName());
        }
        
        // Get CPU time if supported
        if (threadMXBean.isThreadCpuTimeSupported()) {
            try {
                info.setCpuTime(threadMXBean.getThreadCpuTime(threadInfo.getThreadId()));
                info.setUserTime(threadMXBean.getThreadUserTime(threadInfo.getThreadId()));
            } catch (Exception e) {
                // CPU time not available for this thread
            }
        }
        
        // Convert stack trace
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        if (stackTrace != null) {
            info.setStackTrace(Arrays.asList(stackTrace));
        }
        
        // Convert locked monitors
        if (threadInfo.getLockedMonitors() != null) {
            List<ThreadInfo.MonitorInfo> monitors = Arrays.stream(threadInfo.getLockedMonitors())
                    .map(monitor -> {
                        ThreadInfo.MonitorInfo monitorInfo = new ThreadInfo.MonitorInfo();
                        monitorInfo.setClassName(monitor.getClassName());
                        monitorInfo.setIdentityHashCode(monitor.getIdentityHashCode());
                        monitorInfo.setStackDepth(monitor.getLockedStackDepth());
                        monitorInfo.setStackFrame(monitor.getLockedStackFrame());
                        return monitorInfo;
                    })
                    .collect(Collectors.toList());
            info.setLockedMonitors(monitors);
        }
        
        // Convert locked synchronizers
        if (threadInfo.getLockedSynchronizers() != null) {
            List<ThreadInfo.LockInfo> locks = Arrays.stream(threadInfo.getLockedSynchronizers())
                    .map(lock -> {
                        ThreadInfo.LockInfo lockInfo = new ThreadInfo.LockInfo();
                        lockInfo.setClassName(lock.getClassName());
                        lockInfo.setIdentityHashCode(lock.getIdentityHashCode());
                        return lockInfo;
                    })
                    .collect(Collectors.toList());
            info.setLockedSynchronizers(locks);
        }
        
        return info;
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
     * Creates the output directory if it doesn't exist.
     */
    private void createOutputDirectory() {
        try {
            String directory = config.getSnapshot().getOutput().getDirectory();
            Path path = Paths.get(directory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created output directory: {}", directory);
            }
        } catch (IOException e) {
            logger.error("Failed to create output directory", e);
        }
    }

    /**
     * Represents a snapshot of thread state.
     */
    public static class Snapshot {
        private Instant timestamp;
        private List<ThreadInfo> threads;
        private List<DeadlockDetector.DeadlockInfo> deadlocks;
        private int totalThreads;
        private int activeThreads;

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public List<ThreadInfo> getThreads() {
            return threads;
        }

        public void setThreads(List<ThreadInfo> threads) {
            this.threads = threads;
        }

        public List<DeadlockDetector.DeadlockInfo> getDeadlocks() {
            return deadlocks;
        }

        public void setDeadlocks(List<DeadlockDetector.DeadlockInfo> deadlocks) {
            this.deadlocks = deadlocks;
        }

        public int getTotalThreads() {
            return totalThreads;
        }

        public void setTotalThreads(int totalThreads) {
            this.totalThreads = totalThreads;
        }

        public int getActiveThreads() {
            return activeThreads;
        }

        public void setActiveThreads(int activeThreads) {
            this.activeThreads = activeThreads;
        }
    }
}
