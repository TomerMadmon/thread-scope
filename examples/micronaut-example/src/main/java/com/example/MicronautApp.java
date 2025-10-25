package com.example;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadScopeConfigLoader;
import com.threadscope.web.ThreadDashboardServer;
import io.micronaut.runtime.Micronaut;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.context.event.ShutdownEvent;
import jakarta.inject.Singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Micronaut application demonstrating ThreadScope integration.
 * Shows how to use ThreadScope with Micronaut framework.
 */
@Singleton
public class MicronautApp implements ApplicationEventListener<StartupEvent> {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private ThreadDashboardServer dashboardServer;

    public static void main(String[] args) {
        Micronaut.run(MicronautApp.class, args);
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        System.out.println("Starting Micronaut application with ThreadScope...");
        initializeThreadScope();
        startAsyncWork();
    }

    /**
     * Initialize ThreadScope for Micronaut.
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
            System.out.println("ThreadScope initialized for Micronaut");
            
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
                    System.out.println("Micronaut task " + taskId + " running on: " + Thread.currentThread().getName());
                    Thread.sleep(2000);
                    System.out.println("Micronaut task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    /**
     * Handle application shutdown.
     */
    public void onApplicationEvent(ShutdownEvent event) {
        System.out.println("Shutting down Micronaut application...");
        
        if (dashboardServer != null) {
            dashboardServer.stop();
        }
        
        ThreadScopeBootstrap.shutdown();
        executor.shutdown();
        
        System.out.println("Micronaut application shutdown complete");
    }
}
