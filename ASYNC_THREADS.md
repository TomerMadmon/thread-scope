# ThreadScope Async Thread Support

ThreadScope provides comprehensive monitoring and analysis of async threads in Java applications. This document explains how ThreadScope handles various types of async operations.

## 🔄 Supported Async Patterns

### 1. **CompletableFuture**
```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")
    .thenApply(String::toUpperCase);
```

**ThreadScope captures:**
- ✅ Future execution threads
- ✅ Completion handlers
- ✅ Exception handling threads
- ✅ Chaining operations

### 2. **Reactive Streams (Project Reactor)**
```java
Flux.interval(Duration.ofMillis(100))
    .map(i -> "Item " + i)
    .filter(s -> s.length() > 5)
    .subscribe(System.out::println);
```

**ThreadScope captures:**
- ✅ Reactor threads
- ✅ Stream processing threads
- ✅ Backpressure handling
- ✅ Subscription lifecycle

### 3. **Thread Pools**
```java
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> {
    // Async work
});
```

**ThreadScope captures:**
- ✅ Pool worker threads
- ✅ Task submission threads
- ✅ Thread lifecycle states
- ✅ Pool utilization

### 4. **Scheduled Tasks**
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
scheduler.scheduleAtFixedRate(() -> {
    // Periodic task
}, 0, 1, TimeUnit.SECONDS);
```

**ThreadScope captures:**
- ✅ Scheduled execution threads
- ✅ Timer threads
- ✅ Periodic task threads
- ✅ Delay handling

### 5. **Web Server Threads**
```java
@RestController
public class AsyncController {
    @GetMapping("/async")
    public CompletableFuture<String> asyncEndpoint() {
        return CompletableFuture.supplyAsync(() -> "Async response");
    }
}
```

**ThreadScope captures:**
- ✅ HTTP request threads
- ✅ Async response threads
- ✅ Connection pool threads
- ✅ I/O threads

## 📊 Async Thread Detection

ThreadScope automatically detects async threads using multiple strategies:

### **1. Thread Name Patterns**
- `CompletableFuture-*`
- `pool-*-thread-*`
- `ForkJoinPool-*`
- `Reactor-*`
- `RxJava-*`

### **2. Stack Trace Analysis**
- CompletableFuture operations
- ExecutorService usage
- Reactive stream operators
- Async I/O operations

### **3. Class Name Detection**
- `java.util.concurrent.CompletableFuture`
- `java.util.concurrent.ExecutorService`
- `reactor.core.publisher.Flux`
- `io.reactivex.Observable`

## 🎯 Async Thread Categories

ThreadScope categorizes async threads into types:

| Type | Description | Examples |
|------|-------------|----------|
| **COMPLETABLE_FUTURE** | CompletableFuture operations | `CompletableFuture-1` |
| **REACTIVE_STREAMS** | Reactive programming | `Reactor-1`, `RxJava-1` |
| **THREAD_POOL** | Executor service workers | `pool-1-thread-1` |
| **WEB_SERVER** | HTTP server threads | `Tomcat-1`, `Jetty-1` |
| **SCHEDULED_TASK** | Scheduled operations | `Scheduled-1` |
| **OTHER_ASYNC** | Other async patterns | Custom async threads |

## 📈 Monitoring Features

### **Real-time Metrics**
- Total async threads
- Runnable async threads
- Blocked async threads
- Waiting async threads
- Timed waiting async threads

### **Thread State Analysis**
```json
{
  "totalAsyncThreads": 15,
  "runnableAsyncThreads": 8,
  "blockedAsyncThreads": 2,
  "waitingAsyncThreads": 3,
  "timedWaitingAsyncThreads": 2
}
```

### **Stack Trace Capture**
- Full async execution chains
- CompletableFuture transformations
- Reactive stream operators
- Exception propagation paths

## 🔍 Dashboard Features

### **Async Thread View**
- Visual thread state indicators
- Async thread type badges
- Real-time state updates
- CPU time tracking

### **Deadlock Detection**
- Async thread deadlocks
- CompletableFuture chains
- Reactive stream deadlocks
- Thread pool deadlocks

### **Performance Analysis**
- Async thread utilization
- Blocking operation detection
- Thread pool efficiency
- Resource contention

## 🚀 Usage Examples

### **Spring Boot Async**
```java
@EnableThreadScope
@SpringBootApplication
public class AsyncApp {
    
    @Async
    public CompletableFuture<String> asyncMethod() {
        return CompletableFuture.supplyAsync(() -> {
            // Async work
            return "Result";
        });
    }
}
```

### **Reactive WebFlux**
```java
@EnableThreadScope
@SpringBootApplication
public class ReactiveApp {
    
    @GetMapping("/reactive")
    public Mono<String> reactiveEndpoint() {
        return Mono.fromCallable(() -> "Reactive result")
            .subscribeOn(Schedulers.parallel());
    }
}
```

### **Custom Async Operations**
```java
@EnableThreadScope
public class CustomAsyncApp {
    
    public void customAsync() {
        CompletableFuture.runAsync(() -> {
            // Custom async logic
        }, customExecutor);
    }
}
```

## ⚙️ Configuration

### **Async-Specific Settings**
```yaml
threadscope:
  advanced:
    maxStackTraceDepth: 30  # Deeper traces for async chains
    includeSystemThreads: false
  snapshot:
    interval: 3s  # More frequent for async monitoring
  alerts:
    hungThreadThresholdMs: 5000  # Shorter for async operations
```

### **Environment Variables**
```bash
export THREADSCOPE_ASYNC_MONITORING=true
export THREADSCOPE_ASYNC_DEPTH=30
export THREADSCOPE_ASYNC_INTERVAL=3s
```

## 🔧 API Endpoints

### **Async Thread Statistics**
```http
GET /api/async-stats
```

**Response:**
```json
{
  "totalAsyncThreads": 15,
  "runnableAsyncThreads": 8,
  "blockedAsyncThreads": 2,
  "waitingAsyncThreads": 3,
  "timedWaitingAsyncThreads": 2,
  "asyncThreads": [
    {
      "id": 123,
      "name": "CompletableFuture-1",
      "state": "RUNNABLE",
      "type": "COMPLETABLE_FUTURE",
      "cpuTime": 1500000
    }
  ]
}
```

### **Thread Details with Async Info**
```http
GET /api/threads
```

**Response includes:**
```json
{
  "threads": [
    {
      "id": 123,
      "name": "CompletableFuture-1",
      "state": "RUNNABLE",
      "isAsyncThread": true,
      "asyncThreadType": "COMPLETABLE_FUTURE",
      "stackTrace": [...]
    }
  ]
}
```

## 🎯 Best Practices

### **1. Async Thread Monitoring**
- Monitor async thread pools for efficiency
- Watch for thread starvation
- Track async operation completion rates

### **2. Deadlock Prevention**
- Monitor CompletableFuture chains
- Check reactive stream backpressure
- Analyze thread pool contention

### **3. Performance Optimization**
- Identify blocking async operations
- Monitor async thread CPU usage
- Track async operation latencies

### **4. Troubleshooting**
- Use stack traces to trace async flows
- Monitor thread state transitions
- Analyze async thread lifecycle

## 🔍 Troubleshooting Async Issues

### **Common Problems**

1. **Async Thread Starvation**
   - Symptoms: Async operations hang
   - Solution: Increase thread pool size
   - Monitor: Thread pool utilization

2. **CompletableFuture Deadlocks**
   - Symptoms: Future never completes
   - Solution: Check dependency chains
   - Monitor: Deadlock detection

3. **Reactive Stream Backpressure**
   - Symptoms: Memory issues, slow processing
   - Solution: Implement backpressure handling
   - Monitor: Stream thread states

4. **Thread Pool Exhaustion**
   - Symptoms: Task submission failures
   - Solution: Tune pool parameters
   - Monitor: Pool thread counts

## 📊 Dashboard Views

### **Async Thread Overview**
- Total async threads by type
- State distribution
- CPU utilization
- Memory usage

### **Thread Pool Analysis**
- Pool utilization rates
- Task queue depths
- Thread lifecycle states
- Performance metrics

### **Reactive Stream Monitoring**
- Stream processing rates
- Backpressure indicators
- Operator performance
- Error propagation

ThreadScope provides comprehensive async thread monitoring that helps you understand, optimize, and troubleshoot async operations in your Java applications.
