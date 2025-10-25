package com.threadscope;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates thread snapshots in various formats (HTML, JSON).
 */
public class SnapshotGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SnapshotGenerator.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final ThreadScopeConfig config;

    public SnapshotGenerator(ThreadScopeConfig config) {
        this.config = config;
    }

    /**
     * Generates a snapshot in the configured format.
     */
    public void generateSnapshot(ThreadMonitor.Snapshot snapshot) {
        String timestamp = TIMESTAMP_FORMAT.format(snapshot.getTimestamp());
        String baseFileName = String.format("threadscope-%s", timestamp);
        
        try {
            if ("json".equalsIgnoreCase(config.getSnapshot().getOutput().getFormat()) || 
                "both".equalsIgnoreCase(config.getSnapshot().getOutput().getFormat())) {
                generateJsonSnapshot(snapshot, baseFileName);
            }
            
            if ("html".equalsIgnoreCase(config.getSnapshot().getOutput().getFormat()) || 
                "both".equalsIgnoreCase(config.getSnapshot().getOutput().getFormat())) {
                generateHtmlSnapshot(snapshot, baseFileName);
            }
            
        } catch (Exception e) {
            logger.error("Error generating snapshot", e);
        }
    }

    /**
     * Generates a JSON snapshot.
     */
    private void generateJsonSnapshot(ThreadMonitor.Snapshot snapshot, String baseFileName) throws IOException {
        String fileName = baseFileName + ".json";
        Path filePath = Paths.get(config.getSnapshot().getOutput().getDirectory(), fileName);
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), snapshot);
        logger.debug("Generated JSON snapshot: {}", filePath);
    }

    /**
     * Generates an HTML snapshot.
     */
    private void generateHtmlSnapshot(ThreadMonitor.Snapshot snapshot, String baseFileName) throws IOException {
        String fileName = baseFileName + ".html";
        Path filePath = Paths.get(config.getSnapshot().getOutput().getDirectory(), fileName);
        
        String html = generateHtmlContent(snapshot);
        Files.write(filePath, html.getBytes());
        logger.debug("Generated HTML snapshot: {}", filePath);
    }

    /**
     * Generates HTML content for the snapshot.
     */
    private String generateHtmlContent(ThreadMonitor.Snapshot snapshot) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>ThreadScope Snapshot - ").append(snapshot.getTimestamp()).append("</title>\n");
        html.append("    <style>\n");
        html.append(getCssStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("    <div class=\"header\">\n");
        html.append("        <h1>ThreadScope Snapshot</h1>\n");
        html.append("        <p>Generated: ").append(snapshot.getTimestamp()).append("</p>\n");
        html.append("        <p>Total Threads: ").append(snapshot.getTotalThreads()).append(" | ");
        html.append("Active Threads: ").append(snapshot.getActiveThreads()).append("</p>\n");
        html.append("    </div>\n");
        
        // Deadlocks section
        if (!snapshot.getDeadlocks().isEmpty()) {
            html.append("    <div class=\"deadlocks\">\n");
            html.append("        <h2>⚠️ Deadlocks Detected</h2>\n");
            for (com.threadscope.detectors.DeadlockDetector.DeadlockInfo deadlock : snapshot.getDeadlocks()) {
                html.append("        <div class=\"deadlock\">\n");
                html.append("            <h3>Deadlock Cycle ").append(deadlock.getCycleId()).append("</h3>\n");
                html.append("            <p>Threads involved: ").append(deadlock.getThreadIds()).append("</p>\n");
                html.append("        </div>\n");
            }
            html.append("    </div>\n");
        }
        
        // Threads section
        html.append("    <div class=\"threads\">\n");
        html.append("        <h2>Threads (").append(snapshot.getThreads().size()).append(")</h2>\n");
        
        for (ThreadInfo thread : snapshot.getThreads()) {
            html.append("        <div class=\"thread\">\n");
            html.append("            <h3>").append(escapeHtml(thread.getName())).append("</h3>\n");
            html.append("            <div class=\"thread-info\">\n");
            html.append("                <p><strong>ID:</strong> ").append(thread.getId()).append("</p>\n");
            html.append("                <p><strong>State:</strong> <span class=\"state-").append(thread.getState().toLowerCase()).append("\">").append(thread.getState()).append("</span></p>\n");
            html.append("                <p><strong>Priority:</strong> ").append(thread.getPriority()).append("</p>\n");
            html.append("                <p><strong>Daemon:</strong> ").append(thread.isDaemon()).append("</p>\n");
            
            if (thread.getLockName() != null) {
                html.append("                <p><strong>Waiting for lock:</strong> ").append(escapeHtml(thread.getLockName())).append("</p>\n");
            }
            
            if (thread.getLockOwnerName() != null) {
                html.append("                <p><strong>Lock owner:</strong> ").append(escapeHtml(thread.getLockOwnerName())).append("</p>\n");
            }
            
            if (thread.getCpuTime() > 0) {
                html.append("                <p><strong>CPU Time:</strong> ").append(thread.getCpuTime() / 1_000_000).append(" ms</p>\n");
            }
            
            // Stack trace
            if (thread.getStackTrace() != null && !thread.getStackTrace().isEmpty()) {
                html.append("                <div class=\"stack-trace\">\n");
                html.append("                    <h4>Stack Trace:</h4>\n");
                html.append("                    <pre>");
                for (StackTraceElement element : thread.getStackTrace()) {
                    html.append(escapeHtml(element.toString())).append("\n");
                }
                html.append("                    </pre>\n");
                html.append("                </div>\n");
            }
            
            html.append("            </div>\n");
            html.append("        </div>\n");
        }
        
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }

    /**
     * Returns CSS styles for the HTML snapshot.
     */
    private String getCssStyles() {
        return "body {\n" +
            "    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
            "    margin: 0;\n" +
            "    padding: 20px;\n" +
            "    background-color: #f5f5f5;\n" +
            "}\n" +
            ".header {\n" +
            "    background: white;\n" +
            "    padding: 20px;\n" +
            "    border-radius: 8px;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "    margin-bottom: 20px;\n" +
            "}\n" +
            ".header h1 {\n" +
            "    margin: 0 0 10px 0;\n" +
            "    color: #333;\n" +
            "}\n" +
            ".deadlocks {\n" +
            "    background: #fff3cd;\n" +
            "    border: 1px solid #ffeaa7;\n" +
            "    padding: 20px;\n" +
            "    border-radius: 8px;\n" +
            "    margin-bottom: 20px;\n" +
            "}\n" +
            ".deadlocks h2 {\n" +
            "    color: #856404;\n" +
            "    margin-top: 0;\n" +
            "}\n" +
            ".deadlock {\n" +
            "    background: white;\n" +
            "    padding: 15px;\n" +
            "    border-radius: 4px;\n" +
            "    margin: 10px 0;\n" +
            "}\n" +
            ".threads {\n" +
            "    background: white;\n" +
            "    padding: 20px;\n" +
            "    border-radius: 8px;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "}\n" +
            ".thread {\n" +
            "    border: 1px solid #e0e0e0;\n" +
            "    border-radius: 4px;\n" +
            "    margin: 15px 0;\n" +
            "    padding: 15px;\n" +
            "}\n" +
            ".thread h3 {\n" +
            "    margin: 0 0 10px 0;\n" +
            "    color: #333;\n" +
            "}\n" +
            ".thread-info p {\n" +
            "    margin: 5px 0;\n" +
            "}\n" +
            ".state-runnable { color: #28a745; font-weight: bold; }\n" +
            ".state-blocked { color: #dc3545; font-weight: bold; }\n" +
            ".state-waiting { color: #ffc107; font-weight: bold; }\n" +
            ".state-timed_waiting { color: #17a2b8; font-weight: bold; }\n" +
            ".state-terminated { color: #6c757d; font-weight: bold; }\n" +
            ".stack-trace {\n" +
            "    margin-top: 10px;\n" +
            "}\n" +
            ".stack-trace pre {\n" +
            "    background: #f8f9fa;\n" +
            "    padding: 10px;\n" +
            "    border-radius: 4px;\n" +
            "    overflow-x: auto;\n" +
            "    font-size: 12px;\n" +
            "}";
    }

    /**
     * Escapes HTML special characters.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}
