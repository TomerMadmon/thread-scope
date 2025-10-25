package com.threadscope.boot;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadScopeConfigLoader;
import com.threadscope.annotations.EnableThreadScope;
import com.threadscope.web.ThreadDashboardServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Spring Boot auto-configuration for ThreadScope.
 * Automatically initializes ThreadScope when @EnableThreadScope is present.
 */
@Configuration
@ConditionalOnClass(EnableThreadScope.class)
@EnableConfigurationProperties(ThreadScopeProperties.class)
public class ThreadScopeAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadScopeAutoConfiguration.class);
    
    private ThreadScopeConfig config;
    private ThreadDashboardServer dashboardServer;

    @PostConstruct
    public void initialize() {
        try {
            logger.info("ThreadScope auto-configuration starting...");
            
            // Load configuration
            config = ThreadScopeConfigLoader.load();
            
            if (!config.isEnabled()) {
                logger.info("ThreadScope is disabled in configuration");
                return;
            }
            
            // Initialize ThreadScope
            ThreadScopeBootstrap.initialize();
            
            // Start dashboard if enabled
            if (config.getDashboard().isEnabled()) {
                dashboardServer = new ThreadDashboardServer(config);
                dashboardServer.start();
            }
            
            // Add shutdown hook
            ThreadScopeBootstrap.addShutdownHook();
            
            logger.info("ThreadScope auto-configuration completed");
            
        } catch (Exception e) {
            logger.error("Failed to initialize ThreadScope", e);
            throw new RuntimeException("ThreadScope initialization failed", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            logger.info("ThreadScope auto-configuration shutting down...");
            
            if (dashboardServer != null) {
                dashboardServer.stop();
            }
            
            ThreadScopeBootstrap.shutdown();
            
            logger.info("ThreadScope auto-configuration shutdown complete");
            
        } catch (Exception e) {
            logger.error("Error during ThreadScope shutdown", e);
        }
    }

    @Bean
    @ConditionalOnProperty(name = "threadscope.enabled", havingValue = "true", matchIfMissing = true)
    public ThreadScopeConfig threadScopeConfig() {
        return config;
    }
}
