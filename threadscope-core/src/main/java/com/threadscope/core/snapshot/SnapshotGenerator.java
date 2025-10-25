package com.threadscope.core.snapshot;

import com.threadscope.core.ThreadInfo;
import com.threadscope.core.configuration.ThreadScopeConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Snapshot generator following Single Responsibility Principle.
 * Responsible only for generating snapshots, not monitoring or scheduling.
 * Uses Strategy pattern for different output formats.
 */
public class SnapshotGenerator {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    
    private final ThreadScopeConfig config;
    private final ExecutorService executor;
    private final List<SnapshotStrategy> strategies;
    
    public SnapshotGenerator(ThreadScopeConfig config) {
        this.config = Objects.requireNonNull(config, "Configuration cannot be null");
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.strategies = initializeStrategies();
    }
    
    /**
     * Initializes snapshot strategies based on configuration.
     */
    private List<SnapshotStrategy> initializeStrategies() {
        return List.of(
            new HtmlSnapshotStrategy(),
            new JsonSnapshotStrategy()
        );
    }
    
    /**
     * Generates a snapshot asynchronously.
     */
    public CompletableFuture<Void> generateSnapshot(SnapshotData snapshotData) {
        return CompletableFuture.runAsync(() -> {
            try {
                createOutputDirectory();
                
                String timestamp = TIMESTAMP_FORMAT.format(snapshotData.timestamp());
                String baseFileName = String.format("threadscope-%s", timestamp);
                
                for (SnapshotStrategy strategy : strategies) {
                    if (shouldGenerateFormat(strategy.getFormat())) {
                        strategy.generate(snapshotData, baseFileName, config.snapshot().directory());
                    }
                }
                
            } catch (Exception e) {
                throw new SnapshotGenerationException("Failed to generate snapshot", e);
            }
        }, executor);
    }
    
    /**
     * Determines if a format should be generated.
     */
    private boolean shouldGenerateFormat(SnapshotFormat format) {
        ThreadScopeConfig.OutputFormat configFormat = config.snapshot().format();
        if (configFormat == ThreadScopeConfig.OutputFormat.HTML) {
            return format == SnapshotFormat.HTML;
        } else if (configFormat == ThreadScopeConfig.OutputFormat.JSON) {
            return format == SnapshotFormat.JSON;
        } else if (configFormat == ThreadScopeConfig.OutputFormat.BOTH) {
            return true;
        }
        return false;
    }
    
    /**
     * Creates the output directory if it doesn't exist.
     */
    private void createOutputDirectory() throws IOException {
        Path directory = Paths.get(config.snapshot().directory());
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }
    
    /**
     * Snapshot data class.
     */
    public static final class SnapshotData {
        private final Instant timestamp;
        private final List<ThreadInfo> threads;
        private final List<DeadlockInfo> deadlocks;
        private final int totalThreads;
        private final int activeThreads;

        public SnapshotData(Instant timestamp, List<ThreadInfo> threads, List<DeadlockInfo> deadlocks,
                          int totalThreads, int activeThreads) {
            this.timestamp = timestamp;
            this.threads = threads;
            this.deadlocks = deadlocks;
            this.totalThreads = totalThreads;
            this.activeThreads = activeThreads;
        }

        public Instant timestamp() { return timestamp; }
        public List<ThreadInfo> threads() { return threads; }
        public List<DeadlockInfo> deadlocks() { return deadlocks; }
        public int totalThreads() { return totalThreads; }
        public int activeThreads() { return activeThreads; }
    }
    
    /**
     * Deadlock information class.
     */
    public static final class DeadlockInfo {
        private final long cycleId;
        private final List<Long> threadIds;
        private final double confidence;

        public DeadlockInfo(long cycleId, List<Long> threadIds, double confidence) {
            this.cycleId = cycleId;
            this.threadIds = threadIds;
            this.confidence = confidence;
        }

        public long cycleId() { return cycleId; }
        public List<Long> threadIds() { return threadIds; }
        public double confidence() { return confidence; }
    }
    
    /**
     * Snapshot format enumeration.
     */
    public enum SnapshotFormat {
        HTML, JSON
    }
    
    /**
     * Snapshot strategy interface.
     */
    public interface SnapshotStrategy {
        SnapshotFormat getFormat();
        void generate(SnapshotData data, String baseFileName, String directory) throws IOException;
    }
    
    /**
     * HTML snapshot strategy implementation.
     */
    public static class HtmlSnapshotStrategy implements SnapshotStrategy {
        
        @Override
        public SnapshotFormat getFormat() {
            return SnapshotFormat.HTML;
        }
        
        @Override
        public void generate(SnapshotData data, String baseFileName, String directory) throws IOException {
            String fileName = baseFileName + ".html";
            Path filePath = Paths.get(directory, fileName);
            
            String html = generateHtmlContent(data);
            Files.write(filePath, html.getBytes());
        }
        
        private String generateHtmlContent(SnapshotData data) {
            return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>ThreadScope Snapshot - " + data.timestamp() + "</title>\n" +
                "    <style>" + getCssStyles() + "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"header\">\n" +
                "        <h1>ThreadScope Snapshot</h1>\n" +
                "        <p>Generated: " + data.timestamp() + "</p>\n" +
                "        <p>Total Threads: " + data.totalThreads() + " | Active Threads: " + data.activeThreads() + "</p>\n" +
                "    </div>\n" +
                "    " + generateDeadlocksSection(data.deadlocks()) + "\n" +
                "    <div class=\"threads\">\n" +
                "        <h2>Threads (" + data.threads().size() + ")</h2>\n" +
                "        " + generateThreadsSection(data.threads()) + "\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        }
        
        private String generateDeadlocksSection(List<DeadlockInfo> deadlocks) {
            if (deadlocks.isEmpty()) {
                return "";
            }
            
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"deadlocks\">");
            html.append("<h2>⚠️ Deadlocks Detected</h2>");
            
            for (DeadlockInfo deadlock : deadlocks) {
                html.append("<div class=\"deadlock\">");
                html.append("<h3>Deadlock Cycle ").append(deadlock.cycleId()).append("</h3>");
                html.append("<p>Threads involved: ").append(deadlock.threadIds()).append("</p>");
                html.append("<p>Confidence: ").append(String.format("%.2f", deadlock.confidence())).append("</p>");
                html.append("</div>");
            }
            
            html.append("</div>");
            return html.toString();
        }
        
        private String generateThreadsSection(List<ThreadInfo> threads) {
            return threads.stream()
                .map(this::generateThreadHtml)
                .collect(Collectors.joining());
        }
        
        private String generateThreadHtml(ThreadInfo thread) {
            String lockInfo = "";
            if (thread.lockName() != null) {
                lockInfo += "<p><strong>Waiting for lock:</strong> " + escapeHtml(thread.lockName()) + "</p>";
            }
            if (thread.lockOwnerName() != null) {
                lockInfo += "<p><strong>Lock owner:</strong> " + escapeHtml(thread.lockOwnerName()) + "</p>";
            }
            String cpuInfo = "";
            if (thread.cpuTime() > 0) {
                cpuInfo = "<p><strong>CPU Time:</strong> " + (thread.cpuTime() / 1_000_000) + " ms</p>";
            }
            
            return "<div class=\"thread\">\n" +
                "    <h3>" + escapeHtml(thread.name()) + "</h3>\n" +
                "    <div class=\"thread-info\">\n" +
                "        <p><strong>ID:</strong> " + thread.id() + "</p>\n" +
                "        <p><strong>State:</strong> <span class=\"state-" + thread.state().name().toLowerCase() + "\">" + thread.state().name() + "</span></p>\n" +
                "        <p><strong>Priority:</strong> " + thread.priority() + "</p>\n" +
                "        <p><strong>Daemon:</strong> " + thread.isDaemon() + "</p>\n" +
                "        " + lockInfo + "\n" +
                "        " + cpuInfo + "\n" +
                "    </div>\n" +
                "    " + generateStackTraceHtml(thread.stackTrace()) + "\n" +
                "</div>";
        }
        
        private String generateStackTraceHtml(List<StackTraceElement> stackTrace) {
            if (stackTrace == null || stackTrace.isEmpty()) {
                return "";
            }
            
            String stackTraceText = stackTrace.stream()
                .map(StackTraceElement::toString)
                .map(this::escapeHtml)
                .collect(Collectors.joining("\n"));
            
            return "<div class=\"stack-trace\">\n" +
                "    <h4>Stack Trace:</h4>\n" +
                "    <pre>" + stackTraceText + "</pre>\n" +
                "</div>";
        }
        
        private String getCssStyles() {
            return "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }\n" +
                ".header { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 20px; }\n" +
                ".deadlocks { background: #fff3cd; border: 1px solid #ffeaa7; padding: 20px; border-radius: 8px; margin-bottom: 20px; }\n" +
                ".threads { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n" +
                ".thread { border: 1px solid #e0e0e0; border-radius: 4px; margin: 15px 0; padding: 15px; background: #fafafa; }\n" +
                ".state-RUNNABLE { color: #28a745; font-weight: bold; }\n" +
                ".state-BLOCKED { color: #dc3545; font-weight: bold; }\n" +
                ".state-WAITING { color: #ffc107; font-weight: bold; }\n" +
                ".stack-trace { margin-top: 10px; background: #f8f9fa; padding: 10px; border-radius: 4px; font-family: 'Courier New', monospace; font-size: 0.8em; }";
        }
        
        private String escapeHtml(String text) {
            if (text == null) return "";
            return text.replace("&", "&amp;")
                      .replace("<", "&lt;")
                      .replace(">", "&gt;")
                      .replace("\"", "&quot;")
                      .replace("'", "&#39;");
        }
    }
    
    /**
     * JSON snapshot strategy implementation.
     */
    public static class JsonSnapshotStrategy implements SnapshotStrategy {
        
        @Override
        public SnapshotFormat getFormat() {
            return SnapshotFormat.JSON;
        }
        
        @Override
        public void generate(SnapshotData data, String baseFileName, String directory) throws IOException {
            String fileName = baseFileName + ".json";
            Path filePath = Paths.get(directory, fileName);
            
            // Use Jackson ObjectMapper for JSON generation
            // Implementation would use ObjectMapper.writeValue(filePath.toFile(), data)
            // For brevity, using simple JSON string generation
            String json = generateJsonContent(data);
            Files.write(filePath, json.getBytes());
        }
        
        private String generateJsonContent(SnapshotData data) {
            return "{\n" +
                "  \"timestamp\": \"" + data.timestamp() + "\",\n" +
                "  \"totalThreads\": " + data.totalThreads() + ",\n" +
                "  \"activeThreads\": " + data.activeThreads() + ",\n" +
                "  \"deadlocks\": " + generateDeadlocksJson(data.deadlocks()) + ",\n" +
                "  \"threads\": " + generateThreadsJson(data.threads()) + "\n" +
                "}";
        }
        
        private String generateDeadlocksJson(List<DeadlockInfo> deadlocks) {
            return deadlocks.stream()
                .map(deadlock -> "{\n" +
                    "  \"cycleId\": " + deadlock.cycleId() + ",\n" +
                    "  \"threadIds\": " + deadlock.threadIds() + ",\n" +
                    "  \"confidence\": " + String.format("%.2f", deadlock.confidence()) + "\n" +
                    "}")
                .collect(Collectors.joining(", ", "[", "]"));
        }
        
        private String generateThreadsJson(List<ThreadInfo> threads) {
            return threads.stream()
                .map(thread -> "{\n" +
                    "  \"id\": " + thread.id() + ",\n" +
                    "  \"name\": \"" + thread.name() + "\",\n" +
                    "  \"state\": \"" + thread.state().name() + "\",\n" +
                    "  \"priority\": " + thread.priority() + ",\n" +
                    "  \"daemon\": " + thread.isDaemon() + ",\n" +
                    "  \"asyncThread\": " + thread.isAsyncThread() + ",\n" +
                    "  \"asyncType\": \"" + thread.asyncThreadType().name() + "\"\n" +
                    "}")
                .collect(Collectors.joining(", ", "[", "]"));
        }
    }
    
    /**
     * Custom exception for snapshot generation errors.
     */
    public static class SnapshotGenerationException extends RuntimeException {
        public SnapshotGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
