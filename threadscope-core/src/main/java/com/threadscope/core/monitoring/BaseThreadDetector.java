package com.threadscope.core.monitoring;

/**
 * Base implementation for thread detectors with common functionality.
 * Eliminates code duplication across detector implementations.
 */
public abstract class BaseThreadDetector<T> extends ThreadDetector.AbstractThreadDetector<T> {
    
    protected BaseThreadDetector(String name, boolean enabled, double confidenceThreshold) {
        super(name, enabled, confidenceThreshold);
    }
    
    /**
     * Creates detection metadata with common fields.
     */
    protected ThreadDetector.DetectionMetadata createMetadata(String description) {
        return new ThreadDetector.DetectionMetadata(
            getName(),
            System.currentTimeMillis(),
            "1.0.0",
            description
        );
    }
}
