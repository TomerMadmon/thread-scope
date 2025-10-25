package com.threadscope.core.monitoring;

import com.threadscope.core.ThreadInfo;

/**
 * Async detection result class.
 */
public final class AsyncDetectionResult implements ThreadDetector.DetectionResult {
    private final boolean isAsync;
    private final ThreadInfo.AsyncThreadType type;
    private final double confidence;
    private final ThreadDetector.DetectionMetadata metadata;

    public AsyncDetectionResult(boolean isAsync, ThreadInfo.AsyncThreadType type, double confidence, ThreadDetector.DetectionMetadata metadata) {
        this.isAsync = isAsync;
        this.type = type;
        this.confidence = confidence;
        this.metadata = metadata;
    }

    public boolean isAsync() { return isAsync; }
    public ThreadInfo.AsyncThreadType type() { return type; }
    
    @Override
    public double getConfidence() {
        return confidence;
    }
    
    @Override
    public ThreadDetector.DetectionMetadata getMetadata() {
        return metadata;
    }
}
