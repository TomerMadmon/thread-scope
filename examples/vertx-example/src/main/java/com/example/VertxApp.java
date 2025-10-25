package com.example;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadScopeConfigLoader;
import com.threadscope.web.ThreadDashboardServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Vert.x application demonstrating ThreadScope integration.
 * Shows how to use ThreadScope with Vert.x framework.
 */
public class VertxApp extends AbstractVerticle {
    
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private ThreadDashboardServer dashboardServer;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new VertxApp());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        System.out.println("Starting Vert.x application with ThreadScope...");
        
        try {
            initializeThreadScope();
            startAsyncWork();
            startPromise.complete();
        } catch (Exception e) {
            startPromise.fail(e);
        }
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        System.out.println("Shutting down Vert.x application...");
        
        if (dashboardServer != null) {
            dashboardServer.stop();
        }
        
        ThreadScopeBootstrap.shutdown();
        executor.shutdown();
        
        System.out.println("Vert.x application shutdown complete");
        stopPromise.complete();
    }

    /**
     * Initialize ThreadScope for Vert.x.
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
            System.out.println("ThreadScope initialized for Vert.x");
            
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
                    System.out.println("Vert.x task " + taskId + " running on: " + Thread.currentThread().getName());
                    Thread.sleep(2000);
                    System.out.println("Vert.x task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }
}
