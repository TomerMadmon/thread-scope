package com.threadscope;

import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

/**
 * Detects and categorizes async threads for enhanced monitoring.
 */
public class AsyncThreadDetector {
    
    private static final Pattern ASYNC_PATTERNS = Pattern.compile(
        "(?i)(async|completable|future|reactive|reactor|rx|stream|pool|executor|scheduled|timer|worker)"
    );
    
    private static final Set<String> ASYNC_THREAD_NAMES = Set.of(
        "ForkJoinPool", "CompletableFuture", "Reactor", "RxJava", "AsyncHttpClient",
        "Netty", "Tomcat", "Jetty", "Undertow", "Reactive", "WebFlux"
    );

    /**
     * Determines if a thread is an async thread.
     */
    public static boolean isAsyncThread(ThreadInfo threadInfo) {
        String threadName = threadInfo.getName();
        
        // Check for known async thread patterns
        if (ASYNC_PATTERNS.matcher(threadName).find()) {
            return true;
        }
        
        // Check for specific async thread names
        for (String asyncName : ASYNC_THREAD_NAMES) {
            if (threadName.contains(asyncName)) {
                return true;
            }
        }
        
        // Check stack trace for async operations
        List<StackTraceElement> stackTrace = threadInfo.getStackTrace();
        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (isAsyncClass(className)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Determines if a thread is an async thread (management ThreadInfo).
     */
    public static boolean isAsyncThread(java.lang.management.ThreadInfo threadInfo) {
        String threadName = threadInfo.getThreadName();
        
        // Check for known async thread patterns
        if (ASYNC_PATTERNS.matcher(threadName).find()) {
            return true;
        }
        
        // Check for specific async thread names
        for (String asyncName : ASYNC_THREAD_NAMES) {
            if (threadName.contains(asyncName)) {
                return true;
            }
        }
        
        // Check stack trace for async operations
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (isAsyncClass(className)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Checks if a class is related to async operations.
     */
    private static boolean isAsyncClass(String className) {
        return className.contains("CompletableFuture") ||
               className.contains("Future") ||
               className.contains("Executor") ||
               className.contains("ThreadPool") ||
               className.contains("Scheduled") ||
               className.contains("Reactor") ||
               className.contains("Flux") ||
               className.contains("Mono") ||
               className.contains("Observable") ||
               className.contains("Single") ||
               className.contains("Maybe") ||
               className.contains("WebFlux") ||
               className.contains("Reactive") ||
               className.contains("Async") ||
               className.contains("NonBlocking");
    }

    /**
     * Categorizes async threads by type.
     */
    public static AsyncThreadType categorizeAsyncThread(java.lang.management.ThreadInfo threadInfo) {
        String threadName = threadInfo.getThreadName();
        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        
        // CompletableFuture threads
        if (threadName.contains("CompletableFuture") || 
            (stackTrace != null && containsClass(stackTrace, "CompletableFuture"))) {
            return AsyncThreadType.COMPLETABLE_FUTURE;
        }
        
        // Reactive streams
        if (threadName.contains("Reactor") || 
            threadName.contains("RxJava") ||
            (stackTrace != null && (containsClass(stackTrace, "Flux") || 
                                   containsClass(stackTrace, "Mono") ||
                                   containsClass(stackTrace, "Observable")))) {
            return AsyncThreadType.REACTIVE_STREAMS;
        }
        
        // Thread pool workers
        if (threadName.contains("pool") || 
            threadName.contains("worker") ||
            threadName.contains("ForkJoinPool")) {
            return AsyncThreadType.THREAD_POOL;
        }
        
        // Web server threads
        if (threadName.contains("Tomcat") || 
            threadName.contains("Jetty") ||
            threadName.contains("Netty") ||
            threadName.contains("Undertow")) {
            return AsyncThreadType.WEB_SERVER;
        }
        
        // Scheduled tasks
        if (threadName.contains("Scheduled") || 
            threadName.contains("Timer") ||
            (stackTrace != null && containsClass(stackTrace, "ScheduledExecutorService"))) {
            return AsyncThreadType.SCHEDULED_TASK;
        }
        
        return AsyncThreadType.OTHER_ASYNC;
    }

    /**
     * Checks if stack trace contains a specific class.
     */
    private static boolean containsClass(StackTraceElement[] stackTrace, String className) {
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets async thread statistics.
     */
    public static AsyncThreadStats getAsyncThreadStats(List<ThreadInfo> threads) {
        AsyncThreadStats stats = new AsyncThreadStats();
        
        for (ThreadInfo thread : threads) {
            if (isAsyncThread(thread)) {
                stats.totalAsyncThreads++;
                
                switch (thread.getState()) {
                    case "RUNNABLE":
                        stats.runnableAsyncThreads++;
                        break;
                    case "BLOCKED":
                        stats.blockedAsyncThreads++;
                        break;
                    case "WAITING":
                        stats.waitingAsyncThreads++;
                        break;
                    case "TIMED_WAITING":
                        stats.timedWaitingAsyncThreads++;
                        break;
                }
            }
        }
        
        return stats;
    }

    /**
     * Async thread types.
     */
    public enum AsyncThreadType {
        COMPLETABLE_FUTURE,
        REACTIVE_STREAMS,
        THREAD_POOL,
        WEB_SERVER,
        SCHEDULED_TASK,
        OTHER_ASYNC
    }

    /**
     * Statistics for async threads.
     */
    public static class AsyncThreadStats {
        private int totalAsyncThreads = 0;
        private int runnableAsyncThreads = 0;
        private int blockedAsyncThreads = 0;
        private int waitingAsyncThreads = 0;
        private int timedWaitingAsyncThreads = 0;

        public int getTotalAsyncThreads() {
            return totalAsyncThreads;
        }

        public int getRunnableAsyncThreads() {
            return runnableAsyncThreads;
        }

        public int getBlockedAsyncThreads() {
            return blockedAsyncThreads;
        }

        public int getWaitingAsyncThreads() {
            return waitingAsyncThreads;
        }

        public int getTimedWaitingAsyncThreads() {
            return timedWaitingAsyncThreads;
        }
    }
}
