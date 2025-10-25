package com.threadscope.core.monitoring;

import com.threadscope.core.ThreadInfo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Deadlock detector implementation using Strategy pattern.
 * Detects deadlocks with confidence scoring and cycle analysis.
 */
public class DeadlockDetector extends BaseThreadDetector<DeadlockDetectionResult> {
    
    private final ThreadMXBean threadMXBean;
    
    public DeadlockDetector() {
        super("DeadlockDetector", true, 0.8);
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }
    
    public DeadlockDetector(boolean enabled, double confidenceThreshold) {
        super("DeadlockDetector", enabled, confidenceThreshold);
        this.threadMXBean = ManagementFactory.getThreadMXBean();
    }
    
    @Override
    public DeadlockDetectionResult detect(ThreadInfo threadInfo) {
        // Individual thread deadlock detection is not meaningful
        // Deadlocks are detected at the system level
        return new DeadlockDetectionResult(false, 0.0, List.of(), getMetadata());
    }
    
    @Override
    public List<DeadlockDetectionResult> detectAll(List<ThreadInfo> threadInfos) {
        if (!isEnabled()) {
            return List.of();
        }
        
        try {
            List<DeadlockDetectionResult> results = new ArrayList<>();
            
            // Check for deadlocked threads
            long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadlockedThreadIds != null && deadlockedThreadIds.length > 0) {
                results.addAll(processDeadlockCycles(deadlockedThreadIds));
            }
            
            // Check for monitor deadlocks if supported
            if (threadMXBean.isObjectMonitorUsageSupported()) {
                long[] monitorDeadlockedThreadIds = threadMXBean.findMonitorDeadlockedThreads();
                if (monitorDeadlockedThreadIds != null && monitorDeadlockedThreadIds.length > 0) {
                    results.addAll(processDeadlockCycles(monitorDeadlockedThreadIds));
                }
            }
            
            return results;
            
        } catch (Exception e) {
            // Return empty result on error
            return List.of();
        }
    }
    
    /**
     * Processes deadlock cycles and returns detection results.
     */
    private List<DeadlockDetectionResult> processDeadlockCycles(long[] deadlockedThreadIds) {
        List<DeadlockDetectionResult> results = new ArrayList<>();
        List<DeadlockCycle> cycles = analyzeDeadlockCycles(deadlockedThreadIds);
        
        for (DeadlockCycle cycle : cycles) {
            double confidence = calculateCycleConfidence(cycle);
            if (isValidConfidence(confidence)) {
                results.add(new DeadlockDetectionResult(
                    true,
                    confidence,
                    cycle.threadIds(),
                    getMetadata()
                ));
            }
        }
        
        return results;
    }
    
    /**
     * Analyzes deadlock cycles from thread IDs.
     */
    private List<DeadlockCycle> analyzeDeadlockCycles(long[] deadlockedThreadIds) {
        java.lang.management.ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreadIds);
        Map<Long, Set<Long>> cycles = new HashMap<>();
        Set<Long> processed = new HashSet<>();
        
        for (java.lang.management.ThreadInfo threadInfo : threadInfos) {
            if (threadInfo == null || processed.contains(threadInfo.getThreadId())) {
                continue;
            }
            
            Set<Long> cycle = new HashSet<>();
            findCycle(threadInfo, threadInfos, cycle, processed);
            
            if (cycle.size() > 1) {
                cycles.put(cycle.iterator().next(), cycle);
            }
        }
        
        return cycles.entrySet().stream()
            .map(entry -> new DeadlockCycle(entry.getKey(), new ArrayList<>(entry.getValue())))
            .toList();
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
            long lockOwnerId = threadInfo.getLockOwnerId();
            
            if (lockOwnerId != -1) {
                // Find the thread that owns this lock
                for (java.lang.management.ThreadInfo otherThread : allThreads) {
                    if (otherThread != null && otherThread.getThreadId() == lockOwnerId) {
                        findCycle(otherThread, allThreads, cycle, processed);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Calculates confidence score for deadlock cycle.
     */
    private double calculateCycleConfidence(DeadlockCycle cycle) {
        int cycleSize = cycle.threadIds().size();
        
        // Higher confidence for larger cycles
        if (cycleSize >= 4) return 1.0;
        if (cycleSize == 3) return 0.9;
        if (cycleSize == 2) return 0.8;
        
        return 0.5;
    }
    
    /**
     * Gets detection metadata.
     */
    private DetectionMetadata getMetadata() {
        return createMetadata("Detects deadlock cycles with confidence scoring");
    }
    
    /**
     * Deadlock cycle class.
     */
    public static final class DeadlockCycle {
        private final long cycleId;
        private final List<Long> threadIds;

        public DeadlockCycle(long cycleId, List<Long> threadIds) {
            this.cycleId = cycleId;
            this.threadIds = threadIds;
        }

        public long cycleId() { return cycleId; }
        public List<Long> threadIds() { return threadIds; }
    }
}
