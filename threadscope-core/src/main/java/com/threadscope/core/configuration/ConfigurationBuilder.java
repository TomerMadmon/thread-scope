package com.threadscope.core.configuration;

import java.time.Duration;
import java.util.function.Function;

/**
 * Builder pattern for ThreadScope configuration.
 * Provides fluent API for configuration creation following Builder pattern.
 */
public final class ConfigurationBuilder {
    
    private boolean enabled = true;
    private ThreadScopeConfig.SnapshotConfig snapshot = ThreadScopeConfig.SnapshotConfig.defaults();
    private ThreadScopeConfig.DashboardConfig dashboard = ThreadScopeConfig.DashboardConfig.defaults();
    private ThreadScopeConfig.AlertsConfig alerts = ThreadScopeConfig.AlertsConfig.defaults();
    private ThreadScopeConfig.LoggingConfig logging = ThreadScopeConfig.LoggingConfig.defaults();
    private ThreadScopeConfig.AdvancedConfig advanced = ThreadScopeConfig.AdvancedConfig.defaults();
    
    private ConfigurationBuilder() {}
    
    /**
     * Creates a new configuration builder.
     */
    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }
    
    /**
     * Creates a new configuration builder with default configuration.
     */
    public static ConfigurationBuilder defaults() {
        return new ConfigurationBuilder();
    }
    
    /**
     * Sets the enabled state.
     */
    public ConfigurationBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    /**
     * Configures snapshot settings.
     */
    public ConfigurationBuilder snapshot(Function<SnapshotConfigBuilder, SnapshotConfigBuilder> configurer) {
        SnapshotConfigBuilder builder = new SnapshotConfigBuilder(snapshot);
        this.snapshot = configurer.apply(builder).build();
        return this;
    }
    
    /**
     * Configures dashboard settings.
     */
    public ConfigurationBuilder dashboard(Function<DashboardConfigBuilder, DashboardConfigBuilder> configurer) {
        DashboardConfigBuilder builder = new DashboardConfigBuilder(dashboard);
        this.dashboard = configurer.apply(builder).build();
        return this;
    }
    
    /**
     * Configures alerts settings.
     */
    public ConfigurationBuilder alerts(Function<AlertsConfigBuilder, AlertsConfigBuilder> configurer) {
        AlertsConfigBuilder builder = new AlertsConfigBuilder(alerts);
        this.alerts = configurer.apply(builder).build();
        return this;
    }
    
    /**
     * Configures logging settings.
     */
    public ConfigurationBuilder logging(Function<LoggingConfigBuilder, LoggingConfigBuilder> configurer) {
        LoggingConfigBuilder builder = new LoggingConfigBuilder(logging);
        this.logging = configurer.apply(builder).build();
        return this;
    }
    
    /**
     * Configures advanced settings.
     */
    public ConfigurationBuilder advanced(Function<AdvancedConfigBuilder, AdvancedConfigBuilder> configurer) {
        AdvancedConfigBuilder builder = new AdvancedConfigBuilder(advanced);
        this.advanced = configurer.apply(builder).build();
        return this;
    }
    
    /**
     * Builds the final configuration.
     */
    public ThreadScopeConfig build() {
        return new ThreadScopeConfig(enabled, snapshot, dashboard, alerts, logging, advanced);
    }
    
    /**
     * Snapshot configuration builder.
     */
    public static class SnapshotConfigBuilder {
        private Duration interval;
        private ThreadScopeConfig.OutputFormat format;
        private String directory;
        private boolean enabled;
        
        SnapshotConfigBuilder(ThreadScopeConfig.SnapshotConfig defaults) {
            this.interval = defaults.interval();
            this.format = defaults.format();
            this.directory = defaults.directory();
            this.enabled = defaults.enabled();
        }
        
        public SnapshotConfigBuilder interval(Duration interval) {
            this.interval = interval;
            return this;
        }
        
        public SnapshotConfigBuilder format(ThreadScopeConfig.OutputFormat format) {
            this.format = format;
            return this;
        }
        
        public SnapshotConfigBuilder directory(String directory) {
            this.directory = directory;
            return this;
        }
        
        public SnapshotConfigBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        ThreadScopeConfig.SnapshotConfig build() {
            return new ThreadScopeConfig.SnapshotConfig(interval, format, directory, enabled);
        }
    }
    
    /**
     * Dashboard configuration builder.
     */
    public static class DashboardConfigBuilder {
        private boolean enabled;
        private int port;
        private boolean autoOpenBrowser;
        private String host;
        
        DashboardConfigBuilder(ThreadScopeConfig.DashboardConfig defaults) {
            this.enabled = defaults.enabled();
            this.port = defaults.port();
            this.autoOpenBrowser = defaults.autoOpenBrowser();
            this.host = defaults.host();
        }
        
        public DashboardConfigBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public DashboardConfigBuilder port(int port) {
            this.port = port;
            return this;
        }
        
        public DashboardConfigBuilder autoOpenBrowser(boolean autoOpenBrowser) {
            this.autoOpenBrowser = autoOpenBrowser;
            return this;
        }
        
        public DashboardConfigBuilder host(String host) {
            this.host = host;
            return this;
        }
        
        ThreadScopeConfig.DashboardConfig build() {
            return new ThreadScopeConfig.DashboardConfig(enabled, port, autoOpenBrowser, host);
        }
    }
    
    /**
     * Alerts configuration builder.
     */
    public static class AlertsConfigBuilder {
        private boolean deadlockDetection;
        private Duration hungThreadThreshold;
        private boolean emailNotifications;
        private String emailRecipients;
        
        AlertsConfigBuilder(ThreadScopeConfig.AlertsConfig defaults) {
            this.deadlockDetection = defaults.deadlockDetection();
            this.hungThreadThreshold = defaults.hungThreadThreshold();
            this.emailNotifications = defaults.emailNotifications();
            this.emailRecipients = defaults.emailRecipients();
        }
        
        public AlertsConfigBuilder deadlockDetection(boolean deadlockDetection) {
            this.deadlockDetection = deadlockDetection;
            return this;
        }
        
        public AlertsConfigBuilder hungThreadThreshold(Duration threshold) {
            this.hungThreadThreshold = threshold;
            return this;
        }
        
        public AlertsConfigBuilder emailNotifications(boolean emailNotifications) {
            this.emailNotifications = emailNotifications;
            return this;
        }
        
        public AlertsConfigBuilder emailRecipients(String emailRecipients) {
            this.emailRecipients = emailRecipients;
            return this;
        }
        
        ThreadScopeConfig.AlertsConfig build() {
            return new ThreadScopeConfig.AlertsConfig(deadlockDetection, hungThreadThreshold, emailNotifications, emailRecipients);
        }
    }
    
    /**
     * Logging configuration builder.
     */
    public static class LoggingConfigBuilder {
        private ThreadScopeConfig.LogLevel level;
        private ThreadScopeConfig.LogOutput output;
        private String logFile;
        
        LoggingConfigBuilder(ThreadScopeConfig.LoggingConfig defaults) {
            this.level = defaults.level();
            this.output = defaults.output();
            this.logFile = defaults.logFile();
        }
        
        public LoggingConfigBuilder level(ThreadScopeConfig.LogLevel level) {
            this.level = level;
            return this;
        }
        
        public LoggingConfigBuilder output(ThreadScopeConfig.LogOutput output) {
            this.output = output;
            return this;
        }
        
        public LoggingConfigBuilder logFile(String logFile) {
            this.logFile = logFile;
            return this;
        }
        
        ThreadScopeConfig.LoggingConfig build() {
            return new ThreadScopeConfig.LoggingConfig(level, output, logFile);
        }
    }
    
    /**
     * Advanced configuration builder.
     */
    public static class AdvancedConfigBuilder {
        private boolean includeSystemThreads;
        private int maxStackTraceDepth;
        private int maxThreadsToMonitor;
        private boolean enableAsyncDetection;
        
        AdvancedConfigBuilder(ThreadScopeConfig.AdvancedConfig defaults) {
            this.includeSystemThreads = defaults.includeSystemThreads();
            this.maxStackTraceDepth = defaults.maxStackTraceDepth();
            this.maxThreadsToMonitor = defaults.maxThreadsToMonitor();
            this.enableAsyncDetection = defaults.enableAsyncDetection();
        }
        
        public AdvancedConfigBuilder includeSystemThreads(boolean includeSystemThreads) {
            this.includeSystemThreads = includeSystemThreads;
            return this;
        }
        
        public AdvancedConfigBuilder maxStackTraceDepth(int maxStackTraceDepth) {
            this.maxStackTraceDepth = maxStackTraceDepth;
            return this;
        }
        
        public AdvancedConfigBuilder maxThreadsToMonitor(int maxThreadsToMonitor) {
            this.maxThreadsToMonitor = maxThreadsToMonitor;
            return this;
        }
        
        public AdvancedConfigBuilder enableAsyncDetection(boolean enableAsyncDetection) {
            this.enableAsyncDetection = enableAsyncDetection;
            return this;
        }
        
        ThreadScopeConfig.AdvancedConfig build() {
            return new ThreadScopeConfig.AdvancedConfig(includeSystemThreads, maxStackTraceDepth, maxThreadsToMonitor, enableAsyncDetection);
        }
    }
}
