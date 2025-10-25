package com.threadscope;

import com.threadscope.annotations.EnableThreadScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bootstrap class that initializes ThreadScope when the @EnableThreadScope annotation is detected.
 */
public class ThreadScopeBootstrap {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadScopeBootstrap.class);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private static ThreadMonitor threadMonitor;
    private static ThreadScopeConfig config;

    /**
     * Initializes ThreadScope if not already initialized.
     */
    public static synchronized void initialize() {
        if (initialized.get()) {
            logger.debug("ThreadScope already initialized");
            return;
        }
        
        try {
            logger.info("Initializing ThreadScope...");
            
            // Load configuration
            config = ThreadScopeConfigLoader.load();
            
            if (!config.isEnabled()) {
                logger.info("ThreadScope is disabled in configuration");
                return;
            }
            
            // Create and start thread monitor
            threadMonitor = new ThreadMonitor(config);
            threadMonitor.start();
            
            initialized.set(true);
            logger.info("ThreadScope initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize ThreadScope", e);
            throw new RuntimeException("ThreadScope initialization failed", e);
        }
    }

    /**
     * Shuts down ThreadScope.
     */
    public static synchronized void shutdown() {
        if (!initialized.get()) {
            return;
        }
        
        try {
            logger.info("Shutting down ThreadScope...");
            
            if (threadMonitor != null) {
                threadMonitor.stop();
            }
            
            initialized.set(false);
            logger.info("ThreadScope shutdown complete");
            
        } catch (Exception e) {
            logger.error("Error during ThreadScope shutdown", e);
        }
    }

    /**
     * Checks if ThreadScope is initialized.
     */
    public static boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Gets the current configuration.
     */
    public static ThreadScopeConfig getConfig() {
        return config;
    }

    /**
     * Gets the thread monitor instance.
     */
    public static ThreadMonitor getThreadMonitor() {
        return threadMonitor;
    }

    /**
     * Scans for @EnableThreadScope annotation in the current classpath.
     */
    public static void scanForAnnotation() {
        try {
            // This is a simplified approach - in a real implementation,
            // you might want to use more sophisticated classpath scanning
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            
            for (StackTraceElement element : stackTrace) {
                try {
                    Class<?> clazz = Class.forName(element.getClassName());
                    if (clazz.isAnnotationPresent(EnableThreadScope.class)) {
                        logger.info("Found @EnableThreadScope annotation on class: {}", clazz.getName());
                        initialize();
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    // Ignore - class not found
                }
            }
        } catch (Exception e) {
            logger.debug("Error scanning for @EnableThreadScope annotation", e);
        }
    }

    /**
     * Adds a shutdown hook to ensure proper cleanup.
     */
    public static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered");
            shutdown();
        }));
    }
}
