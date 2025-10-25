package com.threadscope;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Loads ThreadScope configuration from various sources.
 * Priority: Environment variables > System properties > YAML file > Properties file > Defaults
 */
public class ThreadScopeConfigLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadScopeConfigLoader.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper();

    public static ThreadScopeConfig load() {
        ThreadScopeConfig config = new ThreadScopeConfig();
        
        // Load from YAML file
        loadFromYaml(config);
        
        // Load from properties file
        loadFromProperties(config);
        
        // Override with system properties
        loadFromSystemProperties(config);
        
        // Override with environment variables
        loadFromEnvironment(config);
        
        return config;
    }

    private static void loadFromYaml(ThreadScopeConfig config) {
        try {
            File yamlFile = new File("threadscope.yaml");
            if (yamlFile.exists()) {
                ThreadScopeConfig yamlConfig = yamlMapper.readValue(yamlFile, ThreadScopeConfig.class);
                mergeConfig(config, yamlConfig);
                logger.info("Loaded configuration from threadscope.yaml");
            }
        } catch (IOException e) {
            logger.warn("Failed to load threadscope.yaml: {}", e.getMessage());
        }
    }

    private static void loadFromProperties(ThreadScopeConfig config) {
        try {
            File propsFile = new File("threadscope.properties");
            if (propsFile.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(propsFile)) {
                    props.load(fis);
                }
                loadFromProperties(config, props, "");
                logger.info("Loaded configuration from threadscope.properties");
            }
        } catch (IOException e) {
            logger.warn("Failed to load threadscope.properties: {}", e.getMessage());
        }
    }

    private static void loadFromSystemProperties(ThreadScopeConfig config) {
        Properties systemProps = System.getProperties();
        loadFromProperties(config, systemProps, "threadscope.");
    }

    private static void loadFromEnvironment(ThreadScopeConfig config) {
        // Check for THREADSCOPE_ENABLED
        String enabled = System.getenv("THREADSCOPE_ENABLED");
        if (enabled != null) {
            config.setEnabled(Boolean.parseBoolean(enabled));
        }

        // Check for other environment variables
        String dashboardEnabled = System.getenv("THREADSCOPE_DASHBOARD_ENABLED");
        if (dashboardEnabled != null) {
            config.getDashboard().setEnabled(Boolean.parseBoolean(dashboardEnabled));
        }

        String dashboardPort = System.getenv("THREADSCOPE_DASHBOARD_PORT");
        if (dashboardPort != null) {
            try {
                config.getDashboard().setPort(Integer.parseInt(dashboardPort));
            } catch (NumberFormatException e) {
                logger.warn("Invalid THREADSCOPE_DASHBOARD_PORT: {}", dashboardPort);
            }
        }

        String snapshotInterval = System.getenv("THREADSCOPE_SNAPSHOT_INTERVAL");
        if (snapshotInterval != null) {
            try {
                config.getSnapshot().setInterval(java.time.Duration.parse("PT" + snapshotInterval));
            } catch (Exception e) {
                logger.warn("Invalid THREADSCOPE_SNAPSHOT_INTERVAL: {}", snapshotInterval);
            }
        }
    }

    private static void loadFromProperties(ThreadScopeConfig config, Properties props, String prefix) {
        // Basic config
        setBooleanProperty(props, prefix + "enabled", config::setEnabled);
        
        // Dashboard config
        setBooleanProperty(props, prefix + "dashboard.enabled", config.getDashboard()::setEnabled);
        setIntProperty(props, prefix + "dashboard.port", config.getDashboard()::setPort);
        setBooleanProperty(props, prefix + "dashboard.autoOpenBrowser", config.getDashboard()::setAutoOpenBrowser);
        
        // Snapshot config
        setStringProperty(props, prefix + "snapshot.interval", value -> {
            try {
                config.getSnapshot().setInterval(java.time.Duration.parse("PT" + value));
            } catch (Exception e) {
                logger.warn("Invalid snapshot interval: {}", value);
            }
        });
        setStringProperty(props, prefix + "snapshot.output.format", config.getSnapshot().getOutput()::setFormat);
        setStringProperty(props, prefix + "snapshot.output.directory", config.getSnapshot().getOutput()::setDirectory);
        
        // Alerts config
        setBooleanProperty(props, prefix + "alerts.deadlockDetection", config.getAlerts()::setDeadlockDetection);
        setLongProperty(props, prefix + "alerts.hungThreadThresholdMs", config.getAlerts()::setHungThreadThresholdMs);
        
        // Logging config
        setStringProperty(props, prefix + "logging.level", config.getLogging()::setLevel);
        setStringProperty(props, prefix + "logging.output", config.getLogging()::setOutput);
        
        // Advanced config
        setBooleanProperty(props, prefix + "advanced.includeSystemThreads", config.getAdvanced()::setIncludeSystemThreads);
        setIntProperty(props, prefix + "advanced.maxStackTraceDepth", config.getAdvanced()::setMaxStackTraceDepth);
    }

    private static void setBooleanProperty(Properties props, String key, java.util.function.Consumer<Boolean> setter) {
        String value = props.getProperty(key);
        if (value != null) {
            setter.accept(Boolean.parseBoolean(value));
        }
    }

    private static void setIntProperty(Properties props, String key, java.util.function.Consumer<Integer> setter) {
        String value = props.getProperty(key);
        if (value != null) {
            try {
                setter.accept(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}", key, value);
            }
        }
    }

    private static void setLongProperty(Properties props, String key, java.util.function.Consumer<Long> setter) {
        String value = props.getProperty(key);
        if (value != null) {
            try {
                setter.accept(Long.parseLong(value));
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for {}: {}", key, value);
            }
        }
    }

    private static void setStringProperty(Properties props, String key, java.util.function.Consumer<String> setter) {
        String value = props.getProperty(key);
        if (value != null) {
            setter.accept(value);
        }
    }

    private static void mergeConfig(ThreadScopeConfig target, ThreadScopeConfig source) {
        if (source.isEnabled() != target.isEnabled()) {
            target.setEnabled(source.isEnabled());
        }
        
        // Merge snapshot config
        if (source.getSnapshot() != null) {
            if (source.getSnapshot().getInterval() != null) {
                target.getSnapshot().setInterval(source.getSnapshot().getInterval());
            }
            if (source.getSnapshot().getOutput() != null) {
                if (source.getSnapshot().getOutput().getFormat() != null) {
                    target.getSnapshot().getOutput().setFormat(source.getSnapshot().getOutput().getFormat());
                }
                if (source.getSnapshot().getOutput().getDirectory() != null) {
                    target.getSnapshot().getOutput().setDirectory(source.getSnapshot().getOutput().getDirectory());
                }
            }
        }
        
        // Merge dashboard config
        if (source.getDashboard() != null) {
            if (source.getDashboard().isEnabled() != target.getDashboard().isEnabled()) {
                target.getDashboard().setEnabled(source.getDashboard().isEnabled());
            }
            if (source.getDashboard().getPort() != target.getDashboard().getPort()) {
                target.getDashboard().setPort(source.getDashboard().getPort());
            }
            if (source.getDashboard().isAutoOpenBrowser() != target.getDashboard().isAutoOpenBrowser()) {
                target.getDashboard().setAutoOpenBrowser(source.getDashboard().isAutoOpenBrowser());
            }
        }
        
        // Merge alerts config
        if (source.getAlerts() != null) {
            if (source.getAlerts().isDeadlockDetection() != target.getAlerts().isDeadlockDetection()) {
                target.getAlerts().setDeadlockDetection(source.getAlerts().isDeadlockDetection());
            }
            if (source.getAlerts().getHungThreadThresholdMs() != target.getAlerts().getHungThreadThresholdMs()) {
                target.getAlerts().setHungThreadThresholdMs(source.getAlerts().getHungThreadThresholdMs());
            }
        }
        
        // Merge logging config
        if (source.getLogging() != null) {
            if (source.getLogging().getLevel() != null) {
                target.getLogging().setLevel(source.getLogging().getLevel());
            }
            if (source.getLogging().getOutput() != null) {
                target.getLogging().setOutput(source.getLogging().getOutput());
            }
        }
        
        // Merge advanced config
        if (source.getAdvanced() != null) {
            if (source.getAdvanced().isIncludeSystemThreads() != target.getAdvanced().isIncludeSystemThreads()) {
                target.getAdvanced().setIncludeSystemThreads(source.getAdvanced().isIncludeSystemThreads());
            }
            if (source.getAdvanced().getMaxStackTraceDepth() != target.getAdvanced().getMaxStackTraceDepth()) {
                target.getAdvanced().setMaxStackTraceDepth(source.getAdvanced().getMaxStackTraceDepth());
            }
        }
    }
}
