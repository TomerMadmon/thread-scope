package com.threadscope.core.monitoring;

import java.util.List;

/**
 * Deadlock detection result class.
 */
public final class DeadlockDetectionResult implements ThreadDetector.DetectionResult {
    private final boolean hasDeadlock;
    private final double confidence;
    private final List<Long> involvedThreadIds;
    private final ThreadDetector.DetectionMetadata metadata;

    public DeadlockDetectionResult(boolean hasDeadlock, double confidence, List<Long> involvedThreadIds, ThreadDetector.DetectionMetadata metadata) {
        this.hasDeadlock = hasDeadlock;
        this.confidence = confidence;
        this.involvedThreadIds = involvedThreadIds;
        this.metadata = metadata;
    }

    public boolean hasDeadlock() { return hasDeadlock; }
    public List<Long> involvedThreadIds() { return involvedThreadIds; }
    
    @Override
    public double getConfidence() {
        return confidence;
    }
    
    @Override
    public ThreadDetector.DetectionMetadata getMetadata() {
        return metadata;
    }
}
