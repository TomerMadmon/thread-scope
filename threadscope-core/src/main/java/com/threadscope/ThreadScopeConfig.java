package com.threadscope;

import java.time.Duration;

/**
 * Configuration class for ThreadScope.
 * Contains all configurable properties with sensible defaults.
 */
public class ThreadScopeConfig {
    
    private boolean enabled = true;
    private SnapshotConfig snapshot = new SnapshotConfig();
    private DashboardConfig dashboard = new DashboardConfig();
    private AlertsConfig alerts = new AlertsConfig();
    private LoggingConfig logging = new LoggingConfig();
    private AdvancedConfig advanced = new AdvancedConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SnapshotConfig getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(SnapshotConfig snapshot) {
        this.snapshot = snapshot;
    }

    public DashboardConfig getDashboard() {
        return dashboard;
    }

    public void setDashboard(DashboardConfig dashboard) {
        this.dashboard = dashboard;
    }

    public AlertsConfig getAlerts() {
        return alerts;
    }

    public void setAlerts(AlertsConfig alerts) {
        this.alerts = alerts;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }

    public AdvancedConfig getAdvanced() {
        return advanced;
    }

    public void setAdvanced(AdvancedConfig advanced) {
        this.advanced = advanced;
    }

    public static class SnapshotConfig {
        private Duration interval = Duration.ofSeconds(10);
        private OutputConfig output = new OutputConfig();

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }

        public OutputConfig getOutput() {
            return output;
        }

        public void setOutput(OutputConfig output) {
            this.output = output;
        }

        public static class OutputConfig {
            private String format = "html";
            private String directory = "./thread-dumps";

            public String getFormat() {
                return format;
            }

            public void setFormat(String format) {
                this.format = format;
            }

            public String getDirectory() {
                return directory;
            }

            public void setDirectory(String directory) {
                this.directory = directory;
            }
        }
    }

    public static class DashboardConfig {
        private boolean enabled = true;
        private int port = 9090;
        private boolean autoOpenBrowser = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isAutoOpenBrowser() {
            return autoOpenBrowser;
        }

        public void setAutoOpenBrowser(boolean autoOpenBrowser) {
            this.autoOpenBrowser = autoOpenBrowser;
        }
    }

    public static class AlertsConfig {
        private boolean deadlockDetection = true;
        private long hungThreadThresholdMs = 15000;

        public boolean isDeadlockDetection() {
            return deadlockDetection;
        }

        public void setDeadlockDetection(boolean deadlockDetection) {
            this.deadlockDetection = deadlockDetection;
        }

        public long getHungThreadThresholdMs() {
            return hungThreadThresholdMs;
        }

        public void setHungThreadThresholdMs(long hungThreadThresholdMs) {
            this.hungThreadThresholdMs = hungThreadThresholdMs;
        }
    }

    public static class LoggingConfig {
        private String level = "INFO";
        private String output = "console";

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }

    public static class AdvancedConfig {
        private boolean includeSystemThreads = false;
        private int maxStackTraceDepth = 20;

        public boolean isIncludeSystemThreads() {
            return includeSystemThreads;
        }

        public void setIncludeSystemThreads(boolean includeSystemThreads) {
            this.includeSystemThreads = includeSystemThreads;
        }

        public int getMaxStackTraceDepth() {
            return maxStackTraceDepth;
        }

        public void setMaxStackTraceDepth(int maxStackTraceDepth) {
            this.maxStackTraceDepth = maxStackTraceDepth;
        }
    }
}
