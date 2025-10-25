# 🧵 ThreadScope

## 📖 Introduction

ThreadScope is an enterprise-grade Java library designed for comprehensive real-time thread monitoring, visualization, and deadlock detection in production applications. ThreadScope provides zero-code integration with minimal performance impact while delivering powerful insights into application thread behavior.

The library addresses critical production challenges including deadlock detection, async thread monitoring, performance analysis, and system health visualization. ThreadScope is designed to be framework-agnostic, supporting Spring Boot, Micronaut, Quarkus, Vert.x, and plain Java applications with equal ease.

## ✨ Key Features

### 🔍 Core Monitoring Capabilities
- **📊 Real-time Thread Analysis** - Live monitoring of thread states, CPU usage, and memory consumption
- **🚨 Advanced Deadlock Detection** - Confidence-based detection with cycle analysis and visualization
- **⚡ Async Thread Monitoring** - Automatic detection and categorization of CompletableFuture, Reactive Streams, and Thread Pool workers
- **📈 Performance Metrics** - CPU time tracking, memory usage analysis, and thread lifecycle monitoring

### 🎨 User Interface & Visualization
- **🖥️ Interactive Dashboard** - Modern web interface with real-time updates and responsive design
- **🔗 Visual Deadlock Analysis** - Graphical representation of deadlock cycles and thread dependencies
- **🎯 Thread State Visualization** - Color-coded thread states with detailed stack trace information
- **📊 Performance Charts** - Historical data visualization and trend analysis

### 🏢 Enterprise Features
- **⚡ Zero Performance Impact** - Read-only monitoring with <2% CPU overhead
- **🌐 Framework Agnostic** - Universal compatibility across Java frameworks
- **🔒 Type-Safe Configuration** - Immutable configuration with builder patterns
- **🚀 Production Ready** - Enterprise-grade reliability and performance

## 🎯 Use Cases

### 🏭 Production Monitoring
- **🚨 Deadlock Detection** - Identify and resolve deadlock conditions in production environments
- **📊 Performance Analysis** - Monitor thread performance and identify bottlenecks
- **💚 System Health Monitoring** - Track application health through thread metrics
- **🔍 Incident Investigation** - Analyze thread dumps during production incidents

### 🛠️ Development & Testing
- **🐛 Debugging Thread Issues** - Visualize thread interactions during development
- **⚡ Load Testing Analysis** - Monitor thread behavior under load
- **👥 Code Review Support** - Provide thread analysis data for code reviews
- **🚀 Performance Optimization** - Identify optimization opportunities

### 🏢 Enterprise Operations
- **📋 SLA Monitoring** - Ensure application performance meets SLA requirements
- **📈 Capacity Planning** - Analyze thread usage patterns for capacity planning
- **📄 Compliance Reporting** - Generate thread monitoring reports for compliance
- **🌍 Multi-Environment Monitoring** - Consistent monitoring across dev, staging, and production

## 📋 Prerequisites

### 💻 System Requirements
- **☕ Java Runtime**: Java 11 or higher (tested with Java 11, 17, 21)
- **🔧 JVM Compatibility**: OpenJDK, HotSpot, Eclipse Temurin, or compatible JVM
- **💾 Memory**: Minimum 512MB heap space (recommended 1GB+)
- **💿 Disk Space**: 100MB for snapshot storage (configurable)

### 🌐 Platform Support
- **🖥️ Operating Systems**: Linux, Windows, macOS
- **🐳 Container Platforms**: Docker, Kubernetes, Podman
- **☁️ Cloud Platforms**: AWS, Azure, GCP, DigitalOcean
- **🔄 CI/CD Systems**: Jenkins, GitHub Actions, GitLab CI

### 🏗️ Framework Compatibility
- **🍃 Spring Boot**: 2.3+ (2.3.x, 3.0.x, 3.1.x, 3.2.x)
- **⚡ Micronaut**: 3.0+ (3.0.x, 3.1.x, 3.2.x)
- **🚀 Quarkus**: 2.0+ (2.0.x, 2.1.x, 3.0.x)
- **🔄 Vert.x**: 4.0+ (4.0.x, 4.1.x, 4.2.x)
- **☕ Plain Java**: Any JVM application

## 🚀 How to Use

### 🍃 Spring Boot Integration

#### 1️⃣ Add Dependency

```xml
<dependency>
    <groupId>com.threadscope</groupId>
    <artifactId>threadscope-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2️⃣ Enable ThreadScope

```java
import com.threadscope.annotations.EnableThreadScope;

@EnableThreadScope
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 🔧 Non-Spring Applications

#### 🤖 Java Agent (Recommended)

```bash
java -javaagent:threadscope-agent.jar -jar myapp.jar
```

#### 💻 Programmatic Initialization

```java
import com.threadscope.core.ThreadScopeFactory;

// Initialize with default configuration
ThreadScopeService service = ThreadScopeFactory.createDefault();
service.start().join();

// Or with custom configuration
ThreadScopeService service = ThreadScopeFactory.create(builder -> builder
    .enabled(true)
    .dashboard(dashboard -> dashboard.port(9091))
    .snapshot(snapshot -> snapshot.interval(Duration.ofSeconds(5)))
);
```

### 🏗️ Framework Integration

#### ⚡ Micronaut
```java
@Singleton
public class MicronautApp {
    @PostConstruct
    public void init() {
        ThreadScopeBootstrap.initialize();
    }
}
```

#### 🚀 Quarkus
```java
@Singleton
public class QuarkusApp {
    void onStart(@Observes StartupEvent ev) {
        ThreadScopeBootstrap.initialize();
    }
}
```

#### 🔄 Vert.x
```java
public class VertxApp extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        ThreadScopeBootstrap.initialize();
        // ... rest of your code
    }
}
```

## 📊 How to Visualize and Check Results

### 🖥️ Web Dashboard

ThreadScope provides a comprehensive web dashboard accessible at `http://localhost:9090` (configurable port).

#### 🎯 Dashboard Features
- **📊 Real-time Thread Overview** - Live thread count, states, and performance metrics
- **🔍 Thread Explorer** - Detailed thread information with stack traces and CPU usage
- **🚨 Deadlock Detection** - Visual representation of deadlock cycles with confidence scores
- **⚡ Async Thread Analysis** - Categorization and monitoring of async thread patterns
- **📈 Performance Charts** - Historical data visualization and trend analysis
- **🔍 Advanced Filtering** - Filter threads by state, type, or name patterns

#### 🌐 Accessing the Dashboard
1. **🔗 Default URL**: `http://localhost:9090`
2. **⚙️ Custom Port**: Configure via `threadscope.dashboard.port`
3. **🚀 Auto-open**: Set `threadscope.dashboard.autoOpenBrowser=true`

### 📸 Snapshot Generation

ThreadScope automatically generates periodic snapshots with comprehensive thread information.

#### 🎨 HTML Reports
- **✨ Professional Styling** - Clean, responsive design with color-coded thread states
- **📋 Detailed Information** - Thread IDs, names, states, CPU time, and stack traces
- **🔗 Deadlock Visualization** - Clear representation of deadlock cycles
- **📊 Performance Metrics** - CPU usage, memory consumption, and thread statistics

#### 📄 JSON Data Export
- **🤖 Machine-readable Format** - Structured data for analysis and integration
- **📋 Complete Thread Information** - All thread details in JSON format
- **🚨 Deadlock Data** - Deadlock information with confidence scores
- **📝 Metadata** - Timestamps, system information, and configuration details

### 🔌 API Endpoints

The dashboard exposes REST endpoints for programmatic access:

#### 🎯 Core Endpoints
- `GET /api/threads` - List all threads with filtering options
- `GET /api/threads/{id}` - Get specific thread details
- `GET /api/threads/async` - Get async threads only
- `GET /api/threads/state/{state}` - Filter by thread state

#### 📊 Monitoring Endpoints
- `GET /api/deadlocks` - Get deadlock information with confidence scores
- `GET /api/async-stats` - Async thread statistics and categorization
- `GET /api/status` - System status and health metrics
- `GET /api/config` - Current configuration (read-only)

#### 🛠️ Utility Endpoints
- `GET /health` - Health check endpoint
- `GET /metrics` - Performance metrics
- `POST /api/snapshot` - Trigger manual snapshot generation

## ⚙️ Configuration

ThreadScope supports multiple configuration methods with clear precedence:

1. **🔧 System Properties** (`-Dthreadscope.*`) - Highest priority
2. **🌍 Environment Variables** (`THREADSCOPE_*`)
3. **📄 Configuration Files** (`threadscope.yaml`, `threadscope.properties`)
4. **💻 Programmatic Configuration** (Builder pattern)

### 📋 Configuration Table

| Category | Property | Default | Description |
|----------|----------|---------|-------------|
| **🔧 Core** | `threadscope.enabled` | `true` | Enable/disable ThreadScope |
| **🔧 Core** | `threadscope.dashboard.enabled` | `true` | Enable web dashboard |
| **🔧 Core** | `threadscope.dashboard.port` | `9090` | Dashboard port |
| **🔧 Core** | `threadscope.dashboard.autoOpenBrowser` | `false` | Auto-open browser on startup |
| **🔧 Core** | `threadscope.dashboard.host` | `localhost` | Dashboard host binding |
| **📸 Snapshot** | `threadscope.snapshot.enabled` | `true` | Enable snapshot generation |
| **📸 Snapshot** | `threadscope.snapshot.interval` | `10s` | Snapshot generation interval |
| **📸 Snapshot** | `threadscope.snapshot.format` | `html` | Output format (html, json, both) |
| **📸 Snapshot** | `threadscope.snapshot.directory` | `./thread-dumps` | Output directory |
| **🚨 Alerts** | `threadscope.alerts.deadlockDetection` | `true` | Enable deadlock detection |
| **🚨 Alerts** | `threadscope.alerts.hungThreadThreshold` | `15s` | Hung thread detection threshold |
| **🚨 Alerts** | `threadscope.alerts.emailNotifications` | `false` | Enable email alerts |
| **🚨 Alerts** | `threadscope.alerts.emailRecipients` | `""` | Email recipients for alerts |
| **⚙️ Advanced** | `threadscope.advanced.includeSystemThreads` | `false` | Include JVM system threads |
| **⚙️ Advanced** | `threadscope.advanced.maxStackTraceDepth` | `20` | Maximum stack trace depth |
| **⚙️ Advanced** | `threadscope.advanced.maxThreadsToMonitor` | `1000` | Maximum threads to monitor |
| **⚙️ Advanced** | `threadscope.advanced.enableAsyncDetection` | `true` | Enable async thread detection |

### 📄 YAML Configuration Example

```yaml
threadscope:
  enabled: true
  snapshot:
    interval: 10s
    format: html
    directory: ./thread-dumps
    enabled: true
  dashboard:
    enabled: true
    port: 9090
    autoOpenBrowser: false
    host: localhost
  alerts:
    deadlockDetection: true
    hungThreadThreshold: 15s
    emailNotifications: false
    emailRecipients: ""
  logging:
    level: INFO
    output: console
    logFile: ""
  advanced:
    includeSystemThreads: false
    maxStackTraceDepth: 20
    maxThreadsToMonitor: 1000
    enableAsyncDetection: true
```

### 🌍 Environment Variables

```bash
export THREADSCOPE_ENABLED=true
export THREADSCOPE_DASHBOARD_PORT=9090
export THREADSCOPE_SNAPSHOT_INTERVAL=10s
export THREADSCOPE_ALERTS_DEADLOCK_DETECTION=true
```

## ⚠️ Limitations

### 🔧 Technical Limitations
- **☕ Java Version**: Requires Java 11 or higher (no support for Java 8)
- **🔧 JVM Compatibility**: Limited to OpenJDK, HotSpot, and Eclipse Temurin JVMs
- **🧵 Thread Count**: Maximum 1000 threads monitored by default (configurable)
- **📚 Stack Trace Depth**: Limited to 20 levels by default (configurable)
- **💾 Memory Usage**: Additional 10MB memory overhead for monitoring
- **⚡ CPU Overhead**: 2% CPU overhead under typical workloads

### 🚫 Functional Limitations
- **👁️ Read-Only Monitoring**: Cannot modify thread behavior or state
- **🚫 No Thread Creation**: Cannot create or manage application threads
- **📊 Limited Historical Data**: No long-term data retention (snapshots only)
- **🖥️ Single JVM**: Cannot monitor threads across multiple JVM instances
- **🌐 No Distributed Monitoring**: Limited to single application monitoring

### 🌍 Platform Limitations
- **🖥️ Operating System**: Limited to Linux, Windows, and macOS
- **🐳 Container Support**: Requires container runtime compatibility
- **☁️ Cloud Platforms**: Dependent on cloud provider JVM support
- **🌐 Network Requirements**: Dashboard requires network access for remote monitoring

### 🔒 Security Considerations
- **🔌 Port Access**: Dashboard port must be accessible for monitoring
- **📊 Data Exposure**: Thread information may contain sensitive data
- **🛡️ Network Security**: Dashboard should be secured in production environments
- **💿 File System Access**: Requires write access for snapshot generation

## 🚀 Performance Impact

ThreadScope is designed for minimal performance impact in production environments:

### 📊 Benchmarks
- **⚡ CPU Overhead**: ≤ 2% under typical workloads
- **💾 Memory Usage**: < 10MB additional memory
- **🧵 Thread Impact**: No thread modification or blocking
- **💿 I/O Impact**: Minimal disk usage for snapshots

### 🎯 Optimization Features
- **⏳ Lazy Loading** - Only collect data when needed
- **🧠 Smart Sampling** - Adaptive monitoring intervals
- **🏗️ Efficient Data Structures** - Immutable objects with minimal allocation
- **🔄 Background Processing** - All monitoring runs in separate threads

## 🔧 Troubleshooting

### 🚨 Common Issues

#### 🚫 ThreadScope Not Starting
1. **✅ Check Annotation**: Verify `@EnableThreadScope` is present
2. **📄 Configuration**: Validate YAML/properties syntax
3. **📝 Logs**: Check application logs for initialization errors
4. **🔌 Port**: Ensure port 9090 is available (if dashboard enabled)
5. **📦 Dependencies**: Verify all required dependencies are included

#### 🖥️ Dashboard Not Accessible
1. **✅ Enable Dashboard**: Set `threadscope.dashboard.enabled=true`
2. **🔌 Port Availability**: Check if port is available
3. **⚙️ Custom Port**: Try `threadscope.dashboard.port=9091`
4. **🔥 Firewall**: Check firewall settings
5. **🌐 Host Binding**: Verify `threadscope.dashboard.host` setting

#### 📸 No Snapshots Generated
1. **⏰ Interval Setting**: Verify `threadscope.snapshot.interval` is configured
2. **📁 Directory Permissions**: Check output directory write permissions
3. **✅ Enable Snapshots**: Ensure `threadscope.snapshot.enabled=true`
4. **💿 Disk Space**: Check available disk space
5. **🧵 Thread Activity**: Verify application has active threads

#### ⚡ Performance Issues
1. **💾 Memory Usage**: Monitor heap usage with `-Xmx` settings
2. **📸 Snapshot Frequency**: Reduce `threadscope.snapshot.interval`
3. **🧵 Thread Limits**: Adjust `threadscope.advanced.maxThreadsToMonitor`
4. **📚 Stack Depth**: Reduce `threadscope.advanced.maxStackTraceDepth`


## 📚 Examples

### 🍃 Basic Spring Boot Application

```java
@EnableThreadScope
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### ⚙️ Custom Configuration

```java
@Configuration
public class ThreadScopeConfig {
    
    @Bean
    public ThreadScopeService threadScopeService() {
        return ThreadScopeFactory.create(builder -> builder
            .dashboard(dashboard -> dashboard.port(9091))
            .snapshot(snapshot -> snapshot.interval(Duration.ofSeconds(5)))
            .alerts(alerts -> alerts.deadlockDetection(true))
        );
    }
}
```

### ⚡ Async Thread Monitoring

```java
// ThreadScope automatically detects:
// - CompletableFuture threads
// - Reactive Streams (Reactor, RxJava)
// - Thread Pool workers
// - Web Server threads (Tomcat, Jetty, Netty)
// - Scheduled tasks
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### 🛠️ Development Setup

```bash
git clone https://github.com/threadscope/threadscope.git
cd threadscope
mvn clean install
```

### 🧪 Running Tests

```bash
mvn test
mvn integration-test
```

## 📞 Support

For support and questions:

- **📧 Email**: support@threadscope.com
- **🐛 Issues**: [GitHub Issues](https://github.com/threadscope/threadscope/issues)
- **📖 Documentation**: [docs.threadscope.com](https://docs.threadscope.com)
- **💬 Discussions**: [GitHub Discussions](https://github.com/threadscope/threadscope/discussions)

## 🙏 Acknowledgments

- 🏗️ Built with modern Java best practices
- 💡 Inspired by enterprise monitoring needs
- 👥 Community-driven development
- 📄 Open source and MIT licensed
