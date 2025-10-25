package com.threadscope.detectors;

import com.threadscope.ThreadInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects deadlocks in the JVM using ThreadMXBean.
 */
public class DeadlockDetector {
    
    private static final Logger logger = LoggerFactory.getLogger(DeadlockDetector.class);
    private final ThreadMXBean threadMXBean;

    public DeadlockDetector() {
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }

    /**
     * Detects deadlocks and returns information about deadlocked threads.
     * @return List of deadlock information, empty if no deadlocks found
     */
    public List<DeadlockInfo> detectDeadlocks() {
        List<DeadlockInfo> deadlocks = new ArrayList<>();
        
        try {
            // Check for deadlocked threads
            long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
                logger.warn("Deadlock detected involving {} threads", deadlockedThreadIds.length);
                
                // Get detailed thread information
                java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreadIds);
                
                // Group threads by deadlock cycles
                Map<Long, Set<Long>> deadlockCycles = analyzeDeadlockCycles(threadInfos);
                
                for (Map.Entry<Long, Set<Long>> cycle : deadlockCycles.entrySet()) {
                    DeadlockInfo deadlockInfo = new DeadlockInfo();
                    deadlockInfo.setCycleId(cycle.getKey());
                    deadlockInfo.setThreadIds(new ArrayList<>(cycle.getValue()));
                    deadlockInfo.setTimestamp(System.currentTimeMillis());
                    
                    // Add thread details
                    List<ThreadInfo> threadDetails = Arrays.stream(threadInfos)
                            .filter(info -> cycle.getValue().contains(info.getThreadId()))
                            .map(this::convertToThreadInfo)
                            .collect(Collectors.toList());
                    
                    deadlockInfo.setThreads(threadDetails);
                    deadlocks.add(deadlockInfo);
                }
            }
            
            // Check for monitor deadlocks (if supported)
            if (threadMXBean.isObjectMonitorUsageSupported()) {
                long[] monitorDeadlockedThreadIds = threadMXBean.findMonitorDeadlockedThreads();
                if (monitorDeadlockedThreadIds != null && monitorDeadlockedThreadIds.length > 0) {
                    logger.warn("Monitor deadlock detected involving {} threads", monitorDeadlockedThreadIds.length);
                    // Process monitor deadlocks similarly
                }
            }
            
        } catch (Exception e) {
            logger.error("Error detecting deadlocks", e);
        }
        
        return deadlocks;
    }

    /**
     * Analyzes deadlock cycles from thread information.
     */
    private Map<Long, Set<Long>> analyzeDeadlockCycles(java.lang.management.ThreadInfo[] threadInfos) {
        Map<Long, Set<Long>> cycles = new HashMap<>();
        Set<Long> processed = new HashSet<>();
        
        for (java.lang.management.ThreadInfo threadInfo : threadInfos) {
            if (processed.contains(threadInfo.getThreadId())) {
                continue;
            }
            
            Set<Long> cycle = new HashSet<>();
            findCycle(threadInfo, threadInfos, cycle, processed);
            
            if (cycle.size() > 1) {
                cycles.put(cycle.iterator().next(), cycle);
            }
        }
        
        return cycles;
    }

    /**
     * Recursively finds deadlock cycles.
     */
    private void findCycle(java.lang.management.ThreadInfo threadInfo, 
                          java.lang.management.ThreadInfo[] allThreads, 
                          Set<Long> cycle, 
                          Set<Long> processed) {
        
        if (processed.contains(threadInfo.getThreadId())) {
            return;
        }
        
        if (cycle.contains(threadInfo.getThreadId())) {
            return; // Cycle detected
        }
        
        cycle.add(threadInfo.getThreadId());
        processed.add(threadInfo.getThreadId());
        
        // Check what this thread is waiting for
        if (threadInfo.getLockInfo() != null) {
            String lockName = threadInfo.getLockInfo().toString();
            long lockOwnerId = threadInfo.getLockOwnerId();
            
            if (lockOwnerId != -1) {
                // Find the thread that owns this lock
                for (java.lang.management.ThreadInfo otherThread : allThreads) {
                    if (otherThread.getThreadId() == lockOwnerId) {
                        findCycle(otherThread, allThreads, cycle, processed);
                        break;
                    }
                }
            }
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
     * Information about a detected deadlock.
     */
    public static class DeadlockInfo {
        private Long cycleId;
        private List<Long> threadIds;
        private List<ThreadInfo> threads;
        private long timestamp;

        public Long getCycleId() {
            return cycleId;
        }

        public void setCycleId(Long cycleId) {
            this.cycleId = cycleId;
        }

        public List<Long> getThreadIds() {
            return threadIds;
        }

        public void setThreadIds(List<Long> threadIds) {
            this.threadIds = threadIds;
        }

        public List<ThreadInfo> getThreads() {
            return threads;
        }

        public void setThreads(List<ThreadInfo> threads) {
            this.threads = threads;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
