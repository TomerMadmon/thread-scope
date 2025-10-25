package com.threadscope.core.monitoring;

import com.threadscope.core.ThreadInfo;
import com.threadscope.core.ThreadInfo.AsyncThreadType;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Async thread detector implementation using Strategy pattern.
 * Detects and categorizes async threads with confidence scoring.
 */
public class AsyncThreadDetector extends BaseThreadDetector<AsyncDetectionResult> {
    
    private static final Pattern ASYNC_PATTERNS = Pattern.compile(
        "(?i)(async|completable|future|reactive|reactor|rx|stream|pool|executor|scheduled|timer|worker)"
    );
    
    private static final List<String> ASYNC_THREAD_NAMES = List.of(
        "ForkJoinPool", "CompletableFuture", "Reactor", "RxJava", "AsyncHttpClient",
        "Netty", "Tomcat", "Jetty", "Undertow", "Reactive", "WebFlux"
    );
    
    private static final List<String> ASYNC_CLASSES = List.of(
        "CompletableFuture", "Future", "Executor", "ThreadPool", "Scheduled",
        "Reactor", "Flux", "Mono", "Observable", "Single", "Maybe", "WebFlux",
        "Reactive", "Async", "NonBlocking"
    );
    
    public AsyncThreadDetector() {
        super("AsyncThreadDetector", true, 0.5);
    }
    
    public AsyncThreadDetector(boolean enabled, double confidenceThreshold) {
        super("AsyncThreadDetector", enabled, confidenceThreshold);
    }
    
    @Override
    public AsyncDetectionResult detect(ThreadInfo threadInfo) {
        if (!isEnabled()) {
            return new AsyncDetectionResult(false, AsyncThreadType.UNKNOWN, 0.0, getMetadata());
        }
        
        double confidence = calculateConfidence(threadInfo);
        boolean isAsync = confidence >= confidenceThreshold;
        AsyncThreadType type = isAsync ? categorizeAsyncThread(threadInfo) : AsyncThreadType.UNKNOWN;
        
        return new AsyncDetectionResult(isAsync, type, confidence, getMetadata());
    }
    
    @Override
    public List<AsyncDetectionResult> detectAll(List<ThreadInfo> threadInfos) {
        return threadInfos.stream()
            .map(this::detect)
            .toList();
    }
    
    /**
     * Calculates confidence score for async thread detection.
     */
    private double calculateConfidence(ThreadInfo threadInfo) {
        double confidence = 0.0;
        
        // Thread name pattern matching (40% weight)
        if (ASYNC_PATTERNS.matcher(threadInfo.name()).find()) {
            confidence += 0.4;
        }
        
        // Known async thread names (30% weight)
        if (ASYNC_THREAD_NAMES.stream().anyMatch(name -> threadInfo.name().contains(name))) {
            confidence += 0.3;
        }
        
        // Stack trace analysis (30% weight)
        if (threadInfo.stackTrace() != null && !threadInfo.stackTrace().isEmpty()) {
            long asyncClassMatches = threadInfo.stackTrace().stream()
                .mapToLong(element -> ASYNC_CLASSES.stream()
                    .mapToLong(asyncClass -> element.getClassName().contains(asyncClass) ? 1 : 0)
                    .sum())
                .sum();
            
            if (asyncClassMatches > 0) {
                confidence += Math.min(0.3, asyncClassMatches * 0.1);
            }
        }
        
        return Math.min(1.0, confidence);
    }
    
    /**
     * Categorizes async thread by type.
     */
    private AsyncThreadType categorizeAsyncThread(ThreadInfo threadInfo) {
        String threadName = threadInfo.name();
        
        // CompletableFuture detection
        if (threadName.contains("CompletableFuture") || 
            containsAsyncClass(threadInfo, "CompletableFuture")) {
            return AsyncThreadType.COMPLETABLE_FUTURE;
        }
        
        // Reactive streams detection
        if (threadName.contains("Reactor") || 
            threadName.contains("RxJava") ||
            containsAsyncClass(threadInfo, "Flux") ||
            containsAsyncClass(threadInfo, "Mono") ||
            containsAsyncClass(threadInfo, "Observable")) {
            return AsyncThreadType.REACTIVE_STREAMS;
        }
        
        // Thread pool detection
        if (threadName.contains("pool") || 
            threadName.contains("worker")) {
            return AsyncThreadType.THREAD_POOL;
        }
        
        // Web server detection
        if (threadName.contains("Tomcat") || 
            threadName.contains("Jetty") ||
            threadName.contains("Netty") ||
            threadName.contains("Undertow")) {
            return AsyncThreadType.WEB_SERVER;
        }
        
        // Scheduled task detection
        if (threadName.contains("Scheduled") || 
            threadName.contains("Timer") ||
            containsAsyncClass(threadInfo, "ScheduledExecutorService")) {
            return AsyncThreadType.SCHEDULED_TASK;
        }
        
        return AsyncThreadType.OTHER_ASYNC;
    }
    
    /**
     * Checks if stack trace contains async class.
     */
    private boolean containsAsyncClass(ThreadInfo threadInfo, String className) {
        return threadInfo.stackTrace() != null &&
               threadInfo.stackTrace().stream()
                   .anyMatch(element -> element.getClassName().contains(className));
    }
    
    /**
     * Gets detection metadata.
     */
    private DetectionMetadata getMetadata() {
        return createMetadata("Detects and categorizes async threads with confidence scoring");
    }
    
}
