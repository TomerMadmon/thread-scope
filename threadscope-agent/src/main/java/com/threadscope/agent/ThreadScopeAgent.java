package com.threadscope.agent;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadScopeConfigLoader;
import com.threadscope.web.ThreadDashboardServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

/**
 * Java agent for initializing ThreadScope in non-Spring applications.
 * Can be used with -javaagent:threadscope-agent.jar
 */
public class ThreadScopeAgent {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadScopeAgent.class);
    
    private static ThreadDashboardServer dashboardServer;

    /**
     * Premain method called when the agent is loaded.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("ThreadScope agent starting...");
        initializeThreadScope();
    }

    /**
     * Agentmain method for dynamic attachment.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.info("ThreadScope agent attaching...");
        initializeThreadScope();
    }

    /**
     * Initializes ThreadScope.
     */
    private static void initializeThreadScope() {
        try {
            // Load configuration
            ThreadScopeConfig config = ThreadScopeConfigLoader.load();
            
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
            
            logger.info("ThreadScope agent initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize ThreadScope agent", e);
            throw new RuntimeException("ThreadScope agent initialization failed", e);
        }
    }

    /**
     * Shuts down the agent.
     */
    public static void shutdown() {
        try {
            logger.info("ThreadScope agent shutting down...");
            
            if (dashboardServer != null) {
                dashboardServer.stop();
            }
            
            ThreadScopeBootstrap.shutdown();
            
            logger.info("ThreadScope agent shutdown complete");
            
        } catch (Exception e) {
            logger.error("Error during ThreadScope agent shutdown", e);
        }
    }
}
