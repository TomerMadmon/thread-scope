package com.threadscope.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for ThreadScope.
 * Maps to threadscope.* properties in application.yml/properties.
 */
@ConfigurationProperties(prefix = "threadscope")
public class ThreadScopeProperties {
    
    private boolean enabled = true;
    private SnapshotProperties snapshot = new SnapshotProperties();
    private DashboardProperties dashboard = new DashboardProperties();
    private AlertsProperties alerts = new AlertsProperties();
    private LoggingProperties logging = new LoggingProperties();
    private AdvancedProperties advanced = new AdvancedProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SnapshotProperties getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(SnapshotProperties snapshot) {
        this.snapshot = snapshot;
    }

    public DashboardProperties getDashboard() {
        return dashboard;
    }

    public void setDashboard(DashboardProperties dashboard) {
        this.dashboard = dashboard;
    }

    public AlertsProperties getAlerts() {
        return alerts;
    }

    public void setAlerts(AlertsProperties alerts) {
        this.alerts = alerts;
    }

    public LoggingProperties getLogging() {
        return logging;
    }

    public void setLogging(LoggingProperties logging) {
        this.logging = logging;
    }

    public AdvancedProperties getAdvanced() {
        return advanced;
    }

    public void setAdvanced(AdvancedProperties advanced) {
        this.advanced = advanced;
    }

    public static class SnapshotProperties {
        private String interval = "10s";
        private OutputProperties output = new OutputProperties();

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public OutputProperties getOutput() {
            return output;
        }

        public void setOutput(OutputProperties output) {
            this.output = output;
        }

        public static class OutputProperties {
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

    public static class DashboardProperties {
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

    public static class AlertsProperties {
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

    public static class LoggingProperties {
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

    public static class AdvancedProperties {
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
