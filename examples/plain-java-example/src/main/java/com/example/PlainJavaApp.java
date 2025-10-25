package com.example;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadScopeConfigLoader;
import com.threadscope.web.ThreadDashboardServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Plain Java application demonstrating ThreadScope usage without Spring.
 * This shows how to use ThreadScope with any Java application.
 */
public class PlainJavaApp {
    
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Starting Plain Java Application with ThreadScope...");
        
        // Initialize ThreadScope programmatically
        initializeThreadScope();
        
        // Run some work
        runAsyncWork();
        
        // Keep the application running
        try {
            Thread.sleep(30000); // Run for 30 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            shutdown();
        }
    }
    
    /**
     * Initialize ThreadScope programmatically.
     */
    private static void initializeThreadScope() {
        try {
            // Load configuration
            ThreadScopeConfig config = ThreadScopeConfigLoader.load();
            
            if (!config.isEnabled()) {
                System.out.println("ThreadScope is disabled in configuration");
                return;
            }
            
            // Initialize ThreadScope
            ThreadScopeBootstrap.initialize();
            
            // Start dashboard if enabled
            if (config.getDashboard().isEnabled()) {
                ThreadDashboardServer dashboardServer = new ThreadDashboardServer(config);
                dashboardServer.start();
                System.out.println("ThreadScope dashboard started on port " + config.getDashboard().getPort());
            }
            
            // Add shutdown hook
            ThreadScopeBootstrap.addShutdownHook();
            
            System.out.println("ThreadScope initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize ThreadScope: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run some async work to demonstrate thread monitoring.
     */
    private static void runAsyncWork() {
        System.out.println("Starting async work...");
        
        // Submit multiple async tasks
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Task " + taskId + " started on thread: " + Thread.currentThread().getName());
                    Thread.sleep(1000 + (taskId * 100)); // Simulate work
                    System.out.println("Task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Task " + taskId + " interrupted");
                }
            });
        }
        
        // Submit some blocking tasks
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    System.out.println("Blocking task " + taskId + " started");
                    Thread.sleep(5000); // Simulate blocking operation
                    System.out.println("Blocking task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
    
    /**
     * Shutdown the application gracefully.
     */
    private static void shutdown() {
        System.out.println("Shutting down application...");
        
        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Shutdown ThreadScope
        ThreadScopeBootstrap.shutdown();
        
        System.out.println("Application shutdown complete");
    }
}
