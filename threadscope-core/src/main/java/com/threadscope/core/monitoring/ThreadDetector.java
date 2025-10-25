package com.threadscope.core.monitoring;

import com.threadscope.core.ThreadInfo;

import java.util.List;
import java.util.function.Predicate;

/**
 * Generic thread detector interface following Strategy pattern.
 * Allows different detection strategies to be implemented.
 * 
 * @param <T> The type of detection result
 */
public interface ThreadDetector<T> {
    
    /**
     * Detects specific thread characteristics.
     * 
     * @param threadInfo The thread information to analyze
     * @return Detection result of type T
     */
    T detect(ThreadInfo threadInfo);
    
    /**
     * Detects characteristics for multiple threads.
     * 
     * @param threadInfos List of thread information to analyze
     * @return List of detection results
     */
    List<T> detectAll(List<ThreadInfo> threadInfos);
    
    /**
     * Gets the detector name for identification.
     * 
     * @return Detector name
     */
    String getName();
    
    /**
     * Checks if the detector is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    boolean isEnabled();
    
    /**
     * Generic detection result interface.
     */
    interface DetectionResult {
        /**
         * Gets the confidence level of the detection (0.0 to 1.0).
         */
        double getConfidence();
        
        /**
         * Gets the detection metadata.
         */
        DetectionMetadata getMetadata();
    }
    
    /**
     * Detection metadata container.
     */
    public static final class DetectionMetadata {
        private final String detectorName;
        private final long timestamp;
        private final String version;
        private final String description;

        public DetectionMetadata(String detectorName, long timestamp, String version, String description) {
            this.detectorName = detectorName;
            this.timestamp = timestamp;
            this.version = version;
            this.description = description;
        }

        public String getDetectorName() { return detectorName; }
        public long getTimestamp() { return timestamp; }
        public String getVersion() { return version; }
        public String getDescription() { return description; }
    }
    
    /**
     * Base implementation providing common functionality.
     */
    abstract class AbstractThreadDetector<T> implements ThreadDetector<T> {
        
        protected final String name;
        protected final boolean enabled;
        protected final double confidenceThreshold;
        
        protected AbstractThreadDetector(String name, boolean enabled, double confidenceThreshold) {
            this.name = name;
            this.enabled = enabled;
            this.confidenceThreshold = confidenceThreshold;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public boolean isEnabled() {
            return enabled;
        }
        
        /**
         * Filters threads based on a predicate.
         */
        protected List<ThreadInfo> filterThreads(List<ThreadInfo> threads, Predicate<ThreadInfo> predicate) {
            return threads.stream()
                .filter(predicate)
                .toList();
        }
        
        /**
         * Validates detection confidence.
         */
        protected boolean isValidConfidence(double confidence) {
            return confidence >= confidenceThreshold;
        }
    }
}
