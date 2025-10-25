package com.threadscope.core.configuration;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable configuration using Builder pattern and modern Java features.
 * Follows SOLID principles with proper encapsulation and immutability.
 */
public final class ThreadScopeConfig {
    
    private final boolean enabled;
    private final SnapshotConfig snapshot;
    private final DashboardConfig dashboard;
    private final AlertsConfig alerts;
    private final LoggingConfig logging;
    private final AdvancedConfig advanced;

    public ThreadScopeConfig(boolean enabled, SnapshotConfig snapshot, DashboardConfig dashboard,
                           AlertsConfig alerts, LoggingConfig logging, AdvancedConfig advanced) {
        this.enabled = enabled;
        this.snapshot = snapshot;
        this.dashboard = dashboard;
        this.alerts = alerts;
        this.logging = logging;
        this.advanced = advanced;
    }
    
    // Getters
    public boolean enabled() { return enabled; }
    public SnapshotConfig snapshot() { return snapshot; }
    public DashboardConfig dashboard() { return dashboard; }
    public AlertsConfig alerts() { return alerts; }
    public LoggingConfig logging() { return logging; }
    public AdvancedConfig advanced() { return advanced; }
    
    /**
     * Default configuration factory.
     */
    public static ThreadScopeConfig defaults() {
        return new ThreadScopeConfig(
            true,
            SnapshotConfig.defaults(),
            DashboardConfig.defaults(),
            AlertsConfig.defaults(),
            LoggingConfig.defaults(),
            AdvancedConfig.defaults()
        );
    }
    
    /**
     * Creates a new configuration with updated enabled state.
     */
    public ThreadScopeConfig withEnabled(boolean enabled) {
        return new ThreadScopeConfig(enabled, snapshot, dashboard, alerts, logging, advanced);
    }
    
    /**
     * Creates a new configuration with updated snapshot config.
     */
    public ThreadScopeConfig withSnapshot(SnapshotConfig snapshot) {
        return new ThreadScopeConfig(enabled, snapshot, dashboard, alerts, logging, advanced);
    }
    
    /**
     * Creates a new configuration with updated dashboard config.
     */
    public ThreadScopeConfig withDashboard(DashboardConfig dashboard) {
        return new ThreadScopeConfig(enabled, snapshot, dashboard, alerts, logging, advanced);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadScopeConfig that = (ThreadScopeConfig) o;
        return enabled == that.enabled &&
                Objects.equals(snapshot, that.snapshot) &&
                Objects.equals(dashboard, that.dashboard) &&
                Objects.equals(alerts, that.alerts) &&
                Objects.equals(logging, that.logging) &&
                Objects.equals(advanced, that.advanced);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enabled, snapshot, dashboard, alerts, logging, advanced);
    }
    
    @Override
    public String toString() {
        return "ThreadScopeConfig{" +
                "enabled=" + enabled +
                ", snapshot=" + snapshot +
                ", dashboard=" + dashboard +
                ", alerts=" + alerts +
                ", logging=" + logging +
                ", advanced=" + advanced +
                '}';
    }
    
    /**
     * Immutable snapshot configuration.
     */
    public static final class SnapshotConfig {
        private final Duration interval;
        private final OutputFormat format;
        private final String directory;
        private final boolean enabled;

        public SnapshotConfig(Duration interval, OutputFormat format, String directory, boolean enabled) {
            this.interval = interval;
            this.format = format;
            this.directory = directory;
            this.enabled = enabled;
        }
        
        // Getters
        public Duration interval() { return interval; }
        public OutputFormat format() { return format; }
        public String directory() { return directory; }
        public boolean enabled() { return enabled; }
        
        public static SnapshotConfig defaults() {
            return new SnapshotConfig(
                Duration.ofSeconds(10),
                OutputFormat.HTML,
                "./thread-dumps",
                true
            );
        }
        
        public SnapshotConfig withInterval(Duration interval) {
            return new SnapshotConfig(interval, format, directory, enabled);
        }
        
        public SnapshotConfig withFormat(OutputFormat format) {
            return new SnapshotConfig(interval, format, directory, enabled);
        }
        
        public SnapshotConfig withDirectory(String directory) {
            return new SnapshotConfig(interval, format, directory, enabled);
        }
        
        public SnapshotConfig withEnabled(boolean enabled) {
            return new SnapshotConfig(interval, format, directory, enabled);
        }
    }
    
    /**
     * Immutable dashboard configuration.
     */
    public static final class DashboardConfig {
        private final boolean enabled;
        private final int port;
        private final boolean autoOpenBrowser;
        private final String host;

        public DashboardConfig(boolean enabled, int port, boolean autoOpenBrowser, String host) {
            this.enabled = enabled;
            this.port = port;
            this.autoOpenBrowser = autoOpenBrowser;
            this.host = host;
        }
        
        // Getters
        public boolean enabled() { return enabled; }
        public int port() { return port; }
        public boolean autoOpenBrowser() { return autoOpenBrowser; }
        public String host() { return host; }
        
        public static DashboardConfig defaults() {
            return new DashboardConfig(true, 9090, false, "localhost");
        }
        
        public DashboardConfig withEnabled(boolean enabled) {
            return new DashboardConfig(enabled, port, autoOpenBrowser, host);
        }
        
        public DashboardConfig withPort(int port) {
            return new DashboardConfig(enabled, port, autoOpenBrowser, host);
        }
        
        public DashboardConfig withAutoOpenBrowser(boolean autoOpenBrowser) {
            return new DashboardConfig(enabled, port, autoOpenBrowser, host);
        }
        
        public DashboardConfig withHost(String host) {
            return new DashboardConfig(enabled, port, autoOpenBrowser, host);
        }
    }
    
    /**
     * Immutable alerts configuration.
     */
    public static final class AlertsConfig {
        private final boolean deadlockDetection;
        private final Duration hungThreadThreshold;
        private final boolean emailNotifications;
        private final String emailRecipients;

        public AlertsConfig(boolean deadlockDetection, Duration hungThreadThreshold, 
                           boolean emailNotifications, String emailRecipients) {
            this.deadlockDetection = deadlockDetection;
            this.hungThreadThreshold = hungThreadThreshold;
            this.emailNotifications = emailNotifications;
            this.emailRecipients = emailRecipients;
        }
        
        // Getters
        public boolean deadlockDetection() { return deadlockDetection; }
        public Duration hungThreadThreshold() { return hungThreadThreshold; }
        public boolean emailNotifications() { return emailNotifications; }
        public String emailRecipients() { return emailRecipients; }
        
        public static AlertsConfig defaults() {
            return new AlertsConfig(
                true,
                Duration.ofSeconds(15),
                false,
                ""
            );
        }
        
        public AlertsConfig withDeadlockDetection(boolean deadlockDetection) {
            return new AlertsConfig(deadlockDetection, hungThreadThreshold, emailNotifications, emailRecipients);
        }
        
        public AlertsConfig withHungThreadThreshold(Duration threshold) {
            return new AlertsConfig(deadlockDetection, threshold, emailNotifications, emailRecipients);
        }
    }
    
    /**
     * Immutable logging configuration.
     */
    public static final class LoggingConfig {
        private final LogLevel level;
        private final LogOutput output;
        private final String logFile;

        public LoggingConfig(LogLevel level, LogOutput output, String logFile) {
            this.level = level;
            this.output = output;
            this.logFile = logFile;
        }
        
        // Getters
        public LogLevel level() { return level; }
        public LogOutput output() { return output; }
        public String logFile() { return logFile; }
        
        public static LoggingConfig defaults() {
            return new LoggingConfig(LogLevel.INFO, LogOutput.CONSOLE, "");
        }
        
        public LoggingConfig withLevel(LogLevel level) {
            return new LoggingConfig(level, output, logFile);
        }
        
        public LoggingConfig withOutput(LogOutput output) {
            return new LoggingConfig(level, output, logFile);
        }
        
        public LoggingConfig withLogFile(String logFile) {
            return new LoggingConfig(level, output, logFile);
        }
    }
    
    /**
     * Immutable advanced configuration.
     */
    public static final class AdvancedConfig {
        private final boolean includeSystemThreads;
        private final int maxStackTraceDepth;
        private final int maxThreadsToMonitor;
        private final boolean enableAsyncDetection;

        public AdvancedConfig(boolean includeSystemThreads, int maxStackTraceDepth, 
                             int maxThreadsToMonitor, boolean enableAsyncDetection) {
            this.includeSystemThreads = includeSystemThreads;
            this.maxStackTraceDepth = maxStackTraceDepth;
            this.maxThreadsToMonitor = maxThreadsToMonitor;
            this.enableAsyncDetection = enableAsyncDetection;
        }
        
        // Getters
        public boolean includeSystemThreads() { return includeSystemThreads; }
        public int maxStackTraceDepth() { return maxStackTraceDepth; }
        public int maxThreadsToMonitor() { return maxThreadsToMonitor; }
        public boolean enableAsyncDetection() { return enableAsyncDetection; }
        
        public static AdvancedConfig defaults() {
            return new AdvancedConfig(false, 20, 1000, true);
        }
        
        public AdvancedConfig withIncludeSystemThreads(boolean includeSystemThreads) {
            return new AdvancedConfig(includeSystemThreads, maxStackTraceDepth, maxThreadsToMonitor, enableAsyncDetection);
        }
        
        public AdvancedConfig withMaxStackTraceDepth(int maxStackTraceDepth) {
            return new AdvancedConfig(includeSystemThreads, maxStackTraceDepth, maxThreadsToMonitor, enableAsyncDetection);
        }
        
        public AdvancedConfig withMaxThreadsToMonitor(int maxThreadsToMonitor) {
            return new AdvancedConfig(includeSystemThreads, maxStackTraceDepth, maxThreadsToMonitor, enableAsyncDetection);
        }
        
        public AdvancedConfig withEnableAsyncDetection(boolean enableAsyncDetection) {
            return new AdvancedConfig(includeSystemThreads, maxStackTraceDepth, maxThreadsToMonitor, enableAsyncDetection);
        }
    }
    
    /**
     * Output format enumeration.
     */
    public enum OutputFormat {
        HTML, JSON, BOTH
    }
    
    /**
     * Log level enumeration.
     */
    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * Log output enumeration.
     */
    public enum LogOutput {
        CONSOLE, FILE, BOTH
    }
}
