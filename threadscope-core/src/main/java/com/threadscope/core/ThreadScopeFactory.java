package com.threadscope.core;

import com.threadscope.core.configuration.ThreadScopeConfig;
import com.threadscope.core.configuration.ConfigurationBuilder;

import java.util.function.Function;

/**
 * Factory for creating ThreadScope services following Factory pattern.
 * Provides different creation strategies and configuration options.
 */
public final class ThreadScopeFactory {
    
    private ThreadScopeFactory() {
        // Utility class
    }
    
    /**
     * Creates a ThreadScope service with default configuration.
     */
    public static ThreadScopeService createDefault() {
        return create(ThreadScopeConfig.defaults());
    }
    
    /**
     * Creates a ThreadScope service with custom configuration.
     */
    public static ThreadScopeService create(ThreadScopeConfig config) {
        return new ThreadScopeService(config);
    }
    
    /**
     * Creates a ThreadScope service using builder pattern.
     */
    public static ThreadScopeService create(Function<ConfigurationBuilder, ConfigurationBuilder> configurer) {
        ThreadScopeConfig config = configurer.apply(ConfigurationBuilder.builder()).build();
        return create(config);
    }
    
    /**
     * Creates a ThreadScope service for development.
     */
    public static ThreadScopeService createForDevelopment() {
        return create(builder -> builder
            .enabled(true)
            .dashboard(dashboard -> dashboard
                .enabled(true)
                .port(9090)
                .autoOpenBrowser(true)
            )
            .snapshot(snapshot -> snapshot
                .interval(java.time.Duration.ofSeconds(5))
                .format(ThreadScopeConfig.OutputFormat.HTML)
            )
            .alerts(alerts -> alerts
                .deadlockDetection(true)
                .hungThreadThreshold(java.time.Duration.ofSeconds(10))
            )
        );
    }
    
    /**
     * Creates a ThreadScope service for production.
     */
    public static ThreadScopeService createForProduction() {
        return create(builder -> builder
            .enabled(true)
            .dashboard(dashboard -> dashboard
                .enabled(false) // Disable dashboard in production
            )
            .snapshot(snapshot -> snapshot
                .interval(java.time.Duration.ofMinutes(5))
                .format(ThreadScopeConfig.OutputFormat.JSON)
                .directory("/var/log/threadscope")
            )
            .alerts(alerts -> alerts
                .deadlockDetection(true)
                .hungThreadThreshold(java.time.Duration.ofMinutes(2))
                .emailNotifications(true)
            )
            .logging(logging -> logging
                .level(ThreadScopeConfig.LogLevel.WARN)
                .output(ThreadScopeConfig.LogOutput.FILE)
                .logFile("/var/log/threadscope/threadscope.log")
            )
        );
    }
    
    /**
     * Creates a ThreadScope service for testing.
     */
    public static ThreadScopeService createForTesting() {
        return create(builder -> builder
            .enabled(true)
            .dashboard(dashboard -> dashboard
                .enabled(false)
            )
            .snapshot(snapshot -> snapshot
                .interval(java.time.Duration.ofSeconds(1))
                .format(ThreadScopeConfig.OutputFormat.JSON)
                .directory("./test-thread-dumps")
            )
            .alerts(alerts -> alerts
                .deadlockDetection(true)
                .hungThreadThreshold(java.time.Duration.ofSeconds(5))
            )
            .advanced(advanced -> advanced
                .maxThreadsToMonitor(100)
                .enableAsyncDetection(true)
            )
        );
    }
    
    /**
     * Creates a ThreadScope service with minimal configuration.
     */
    public static ThreadScopeService createMinimal() {
        return create(builder -> builder
            .enabled(true)
            .dashboard(dashboard -> dashboard.enabled(false))
            .snapshot(snapshot -> snapshot.enabled(false))
            .alerts(alerts -> alerts.deadlockDetection(false))
        );
    }
    
    /**
     * Creates a ThreadScope service with custom configuration file.
     */
    public static ThreadScopeService createFromFile(String configFile) {
        // Implementation would load configuration from file
        // For now, return default configuration
        return createDefault();
    }
    
    /**
     * Creates a ThreadScope service with environment variables.
     */
    public static ThreadScopeService createFromEnvironment() {
        return create(builder -> {
            // Load from environment variables
            String enabled = System.getenv("THREADSCOPE_ENABLED");
            String dashboardPort = System.getenv("THREADSCOPE_DASHBOARD_PORT");
            String snapshotInterval = System.getenv("THREADSCOPE_SNAPSHOT_INTERVAL");
            
            if (enabled != null) {
                builder.enabled(Boolean.parseBoolean(enabled));
            }
            
            if (dashboardPort != null) {
                builder.dashboard(dashboard -> dashboard.port(Integer.parseInt(dashboardPort)));
            }
            
            if (snapshotInterval != null) {
                builder.snapshot(snapshot -> snapshot.interval(java.time.Duration.parse("PT" + snapshotInterval)));
            }
            
            return builder;
        });
    }
}
