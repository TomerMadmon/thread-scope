package com.example;

import com.threadscope.EnableThreadScope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Example demonstrating ThreadScope monitoring of async threads.
 * This shows how ThreadScope captures various types of async operations.
 */
@EnableThreadScope
@SpringBootApplication
@RestController
public class AsyncExample {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public static void main(String[] args) {
        SpringApplication.run(AsyncExample.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Async ThreadScope Example! Check the dashboard at http://localhost:9090";
    }

    /**
     * Demonstrates CompletableFuture async operations
     */
    @GetMapping("/completable-future")
    public String completableFuture() {
        CompletableFuture<String> future1 = CompletableFuture
            .supplyAsync(() -> {
                try {
                    Thread.sleep(1000);
                    return "Task 1 completed";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Task 1 interrupted";
                }
            }, executor);

        CompletableFuture<String> future2 = CompletableFuture
            .supplyAsync(() -> {
                try {
                    Thread.sleep(1500);
                    return "Task 2 completed";
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Task 2 interrupted";
                }
            }, executor);

        CompletableFuture<String> combined = future1
            .thenCombine(future2, (result1, result2) -> result1 + " + " + result2)
            .thenApply(result -> "Combined: " + result);

        // Don't wait for completion - let it run async
        combined.thenAccept(System.out::println);
        
        return "CompletableFuture operations started - check dashboard for async threads";
    }

    /**
     * Demonstrates reactive streams (Project Reactor)
     */
    @GetMapping("/reactive")
    public String reactive() {
        Flux.interval(Duration.ofMillis(100))
            .take(10)
            .map(i -> "Reactive item " + i)
            .doOnNext(System.out::println)
            .subscribe();

        return "Reactive stream started - check dashboard for async threads";
    }

    /**
     * Demonstrates scheduled async tasks
     */
    @GetMapping("/scheduled")
    public String scheduled() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Scheduled task running on: " + Thread.currentThread().getName());
        }, 0, 2, TimeUnit.SECONDS);

        return "Scheduled tasks started - check dashboard for async threads";
    }

    /**
     * Demonstrates async with blocking operations
     */
    @GetMapping("/blocking-async")
    public String blockingAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                // Simulate blocking I/O
                Thread.sleep(3000);
                System.out.println("Blocking async task completed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executor);

        return "Blocking async task started - check dashboard for BLOCKED threads";
    }

    /**
     * Demonstrates async with locks and potential deadlocks
     */
    @GetMapping("/async-locks")
    public String asyncLocks() {
        Object lock1 = new Object();
        Object lock2 = new Object();

        CompletableFuture.runAsync(() -> {
            synchronized (lock1) {
                try {
                    Thread.sleep(1000);
                    synchronized (lock2) {
                        System.out.println("Thread 1 acquired both locks");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, executor);

        CompletableFuture.runAsync(() -> {
            synchronized (lock2) {
                try {
                    Thread.sleep(1000);
                    synchronized (lock1) {
                        System.out.println("Thread 2 acquired both locks");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, executor);

        return "Async lock operations started - check dashboard for potential deadlocks";
    }

    /**
     * Demonstrates high-frequency async operations
     */
    @GetMapping("/high-frequency")
    public String highFrequency() {
        for (int i = 0; i < 50; i++) {
            final int taskId = i;
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(100 + (taskId * 10));
                    System.out.println("High-frequency task " + taskId + " completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, executor);
        }

        return "50 high-frequency async tasks started - check dashboard for thread activity";
    }

    /**
     * Demonstrates async with exceptions
     */
    @GetMapping("/async-exceptions")
    public String asyncExceptions() {
        CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Async exception occurred");
        }, executor)
        .exceptionally(throwable -> {
            System.out.println("Caught async exception: " + throwable.getMessage());
            return "Exception handled";
        })
        .thenAccept(System.out::println);

        return "Async exception handling started - check dashboard for error threads";
    }
}
