package com.example;

import com.threadscope.EnableThreadScope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Example Spring Boot application demonstrating ThreadScope usage.
 */
@EnableThreadScope
@SpringBootApplication
@RestController
public class MyApplication {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Hello from ThreadScope example! Visit /dashboard to see thread monitoring.";
    }

    @GetMapping("/work")
    public String doWork() {
        // Simulate some work with multiple threads
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(2000);
                    System.out.println("Work completed by thread: " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        return "Work submitted to thread pool";
    }

    @GetMapping("/block")
    public String block() {
        // Simulate blocking operation
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Blocking operation completed";
    }
}
