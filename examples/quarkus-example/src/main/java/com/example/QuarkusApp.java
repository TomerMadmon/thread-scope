package com.example;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadScopeConfigLoader;
import com.threadscope.web.ThreadDashboardServer;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Quarkus application demonstrating ThreadScope integration.
 * Shows how to use ThreadScope with Quarkus framework.
 */
@ApplicationScoped
public class QuarkusApp {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private ThreadDashboardServer dashboardServer;

    /**
     * Initialize ThreadScope on Quarkus startup.
     */
    void onStart(@Observes StartupEvent ev) {
        System.out.println("Starting Quarkus application with ThreadScope...");
        initializeThreadScope();
        startAsyncWork();
    }

    /**
     * Shutdown ThreadScope on Quarkus shutdown.
     */
    void onStop(@Observes ShutdownEvent ev) {
        System.out.println("Shutting down Quarkus application...");
        
        if (dashboardServer != null) {
            dashboardServer.stop();
        }
        
        ThreadScopeBootstrap.shutdown();
        executor.shutdown();
        
        System.out.println("Quarkus application shutdown complete");
    }

    /**
     * Initialize ThreadScope for Quarkus.
     */
    private void initializeThreadScope() {
        try {
            ThreadScopeConfig config = ThreadScopeConfigLoader.load();
            
            if (!config.isEnabled()) {
                System.out.println("ThreadScope is disabled");
                return;
            }
            
            ThreadScopeBootstrap.initialize();
            
            if (config.getDashboard().isEnabled()) {
                dashboardServer = new ThreadDashboardServer(config);
                dashboardServer.start();
                System.out.println("ThreadScope dashboard started on port " + config.getDashboard().getPort());
            }
            
            ThreadScopeBootstrap.addShutdownHook();
            System.out.println("ThreadScope initialized for Quarkus");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize ThreadScope: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Start some async work to demonstrate monitoring.
     */
    private void startAsyncWork() {
        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Quarkus task " + taskId + " running on: " + Thread.currentThread().getName());
                    Thread.sleep(2000);
                    System.out.println("Quarkus task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
