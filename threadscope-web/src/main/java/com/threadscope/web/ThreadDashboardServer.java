package com.threadscope.web;

import com.threadscope.ThreadScopeBootstrap;
import com.threadscope.ThreadScopeConfig;
import com.threadscope.ThreadMonitor;
import com.threadscope.AsyncThreadDetector;
import com.threadscope.ThreadInfo;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedded HTTP server that provides a web dashboard for thread monitoring.
 */
public class ThreadDashboardServer {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadDashboardServer.class);
    
    private Javalin app;
    private final ThreadScopeConfig config;

    public ThreadDashboardServer(ThreadScopeConfig config) {
        this.config = config;
    }

    /**
     * Starts the dashboard server.
     */
    public void start() {
        if (!config.getDashboard().isEnabled()) {
            logger.info("Dashboard is disabled in configuration");
            return;
        }
        
        try {
            app = Javalin.create(config -> {
                config.staticFiles.add("/static");
                // CORS is enabled by default in Javalin
            });
            
            setupRoutes();
            
            int port = config.getDashboard().getPort();
            app.start(port);
            
            logger.info("ThreadScope dashboard started on port {}", port);
            
            if (config.getDashboard().isAutoOpenBrowser()) {
                openBrowser("http://localhost:" + port);
            }
            
        } catch (Exception e) {
            logger.error("Failed to start dashboard server", e);
            throw new RuntimeException("Dashboard server startup failed", e);
        }
    }

    /**
     * Stops the dashboard server.
     */
    public void stop() {
        if (app != null) {
            app.stop();
            logger.info("ThreadScope dashboard stopped");
        }
    }

    /**
     * Sets up the REST API routes.
     */
    private void setupRoutes() {
        // Serve the main dashboard page
        app.get("/", ctx -> {
            ctx.html(getDashboardHtml());
        });
        
        // API endpoints
        app.get("/api/threads", this::getThreads);
        app.get("/api/deadlocks", this::getDeadlocks);
        app.get("/api/config", this::getConfig);
        app.get("/api/status", this::getStatus);
        app.get("/api/async-stats", this::getAsyncStats);
        
        // Health check
        app.get("/health", ctx -> {
            ctx.json(Map.of("status", "UP", "timestamp", System.currentTimeMillis()));
        });
    }

    /**
     * Returns information about all threads.
     */
    private void getThreads(Context ctx) {
        try {
            ThreadMonitor monitor = ThreadScopeBootstrap.getThreadMonitor();
            if (monitor == null) {
                ctx.status(503).json(Map.of("error", "ThreadScope not initialized"));
                return;
            }
            
            List<com.threadscope.ThreadInfo> threads = monitor.getAllThreads();
            ctx.json(Map.of("threads", threads, "count", threads.size()));
            
        } catch (Exception e) {
            logger.error("Error getting threads", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns information about deadlocks.
     */
    private void getDeadlocks(Context ctx) {
        try {
            ThreadMonitor monitor = ThreadScopeBootstrap.getThreadMonitor();
            if (monitor == null) {
                ctx.status(503).json(Map.of("error", "ThreadScope not initialized"));
                return;
            }
            
            // Capture a snapshot to get current deadlock information
            monitor.captureSnapshot();
            
            // For now, return empty - in a real implementation, you'd store
            // deadlock information and return it here
            ctx.json(Map.of("deadlocks", List.of(), "count", 0));
            
        } catch (Exception e) {
            logger.error("Error getting deadlocks", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns the current configuration.
     */
    private void getConfig(Context ctx) {
        try {
            ThreadScopeConfig config = ThreadScopeBootstrap.getConfig();
            if (config == null) {
                ctx.status(503).json(Map.of("error", "ThreadScope not initialized"));
                return;
            }
            
            ctx.json(config);
            
        } catch (Exception e) {
            logger.error("Error getting config", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns the current status.
     */
    private void getStatus(Context ctx) {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("initialized", ThreadScopeBootstrap.isInitialized());
            status.put("timestamp", System.currentTimeMillis());
            
            if (ThreadScopeBootstrap.isInitialized()) {
                ThreadMonitor monitor = ThreadScopeBootstrap.getThreadMonitor();
                if (monitor != null) {
                    List<com.threadscope.ThreadInfo> threads = monitor.getAllThreads();
                    status.put("totalThreads", threads.size());
                    status.put("activeThreads", threads.stream()
                            .filter(t -> "RUNNABLE".equals(t.getState()))
                            .count());
                }
            }
            
            ctx.json(status);
            
        } catch (Exception e) {
            logger.error("Error getting status", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns async thread statistics.
     */
    private void getAsyncStats(Context ctx) {
        try {
            ThreadMonitor monitor = ThreadScopeBootstrap.getThreadMonitor();
            if (monitor == null) {
                ctx.status(503).json(Map.of("error", "ThreadScope not initialized"));
                return;
            }
            
            List<com.threadscope.ThreadInfo> threads = monitor.getAllThreads();
            AsyncThreadDetector.AsyncThreadStats stats = AsyncThreadDetector.getAsyncThreadStats(threads);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalAsyncThreads", stats.getTotalAsyncThreads());
            response.put("runnableAsyncThreads", stats.getRunnableAsyncThreads());
            response.put("blockedAsyncThreads", stats.getBlockedAsyncThreads());
            response.put("waitingAsyncThreads", stats.getWaitingAsyncThreads());
            response.put("timedWaitingAsyncThreads", stats.getTimedWaitingAsyncThreads());
            
            // Add async thread details
            List<Map<String, Object>> asyncThreads = threads.stream()
                .filter(ThreadInfo::isAsyncThread)
                .map(thread -> {
                    Map<String, Object> threadData = new HashMap<>();
                    threadData.put("id", thread.getId());
                    threadData.put("name", thread.getName());
                    threadData.put("state", thread.getState());
                    threadData.put("type", thread.getAsyncThreadType());
                    threadData.put("cpuTime", thread.getCpuTime());
                    return threadData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            response.put("asyncThreads", asyncThreads);
            
            ctx.json(response);
            
        } catch (Exception e) {
            logger.error("Error getting async stats", e);
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns the main dashboard HTML.
     */
    private String getDashboardHtml() {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>ThreadScope Dashboard</title>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <style>\n" +
            "        " + getDashboardCss() + "\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <header>\n" +
            "            <h1>🧵 ThreadScope Dashboard</h1>\n" +
            "            <div class=\"status\" id=\"status\">Loading...</div>\n" +
            "        </header>\n" +
            "        \n" +
            "        <div class=\"metrics\">\n" +
            "            <div class=\"metric-card\">\n" +
            "                <h3>Total Threads</h3>\n" +
            "                <div class=\"metric-value\" id=\"totalThreads\">-</div>\n" +
            "            </div>\n" +
            "            <div class=\"metric-card\">\n" +
            "                <h3>Active Threads</h3>\n" +
            "                <div class=\"metric-value\" id=\"activeThreads\">-</div>\n" +
            "            </div>\n" +
            "            <div class=\"metric-card\">\n" +
            "                <h3>Deadlocks</h3>\n" +
            "                <div class=\"metric-value\" id=\"deadlocks\">-</div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"tabs\">\n" +
            "            <button class=\"tab-button active\" onclick=\"showTab('threads')\">Threads</button>\n" +
            "            <button class=\"tab-button\" onclick=\"showTab('deadlocks')\">Deadlocks</button>\n" +
            "            <button class=\"tab-button\" onclick=\"showTab('config')\">Configuration</button>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"tab-content\" id=\"threads-tab\">\n" +
            "            <div class=\"threads-container\">\n" +
            "                <div id=\"threads-list\">Loading threads...</div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"tab-content\" id=\"deadlocks-tab\" style=\"display: none;\">\n" +
            "            <div id=\"deadlocks-list\">No deadlocks detected</div>\n" +
            "        </div>\n" +
            "        \n" +
            "        <div class=\"tab-content\" id=\"config-tab\" style=\"display: none;\">\n" +
            "            <pre id=\"config-json\">Loading configuration...</pre>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    \n" +
            "    <script>\n" +
            "        " + getDashboardJs() + "\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }

    /**
     * Returns CSS styles for the dashboard.
     */
    private String getDashboardCss() {
        return "* {\n" +
            "    margin: 0;\n" +
            "    padding: 0;\n" +
            "    box-sizing: border-box;\n" +
            "}\n" +
            "\n" +
            "body {\n" +
            "    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
            "    background: #f5f7fa;\n" +
            "    color: #333;\n" +
            "    line-height: 1.6;\n" +
            "}\n" +
            "\n" +
            ".container {\n" +
            "    max-width: 1200px;\n" +
            "    margin: 0 auto;\n" +
            "    padding: 20px;\n" +
            "}\n" +
            "\n" +
            "header {\n" +
            "    background: white;\n" +
            "    padding: 20px;\n" +
            "    border-radius: 8px;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "    margin-bottom: 20px;\n" +
            "    display: flex;\n" +
            "    justify-content: space-between;\n" +
            "    align-items: center;\n" +
            "}\n" +
            "\n" +
            "header h1 {\n" +
            "    color: #2c3e50;\n" +
            "    font-size: 2em;\n" +
            "}\n" +
            "\n" +
            ".status {\n" +
            "    padding: 8px 16px;\n" +
            "    border-radius: 20px;\n" +
            "    font-weight: bold;\n" +
            "    background: #e8f5e8;\n" +
            "    color: #27ae60;\n" +
            "}\n" +
            "\n" +
            ".metrics {\n" +
            "    display: grid;\n" +
            "    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
            "    gap: 20px;\n" +
            "    margin-bottom: 20px;\n" +
            "}\n" +
            "\n" +
            ".metric-card {\n" +
            "    background: white;\n" +
            "    padding: 20px;\n" +
            "    border-radius: 8px;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "    text-align: center;\n" +
            "}\n" +
            "\n" +
            ".metric-card h3 {\n" +
            "    color: #7f8c8d;\n" +
            "    margin-bottom: 10px;\n" +
            "    font-size: 0.9em;\n" +
            "    text-transform: uppercase;\n" +
            "    letter-spacing: 1px;\n" +
            "}\n" +
            "\n" +
            ".metric-value {\n" +
            "    font-size: 2em;\n" +
            "    font-weight: bold;\n" +
            "    color: #2c3e50;\n" +
            "}\n" +
            "\n" +
            ".tabs {\n" +
            "    background: white;\n" +
            "    border-radius: 8px 8px 0 0;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "    margin-bottom: 0;\n" +
            "}\n" +
            "\n" +
            ".tab-button {\n" +
            "    background: none;\n" +
            "    border: none;\n" +
            "    padding: 15px 20px;\n" +
            "    cursor: pointer;\n" +
            "    font-size: 1em;\n" +
            "    color: #7f8c8d;\n" +
            "    border-bottom: 3px solid transparent;\n" +
            "    transition: all 0.3s;\n" +
            "}\n" +
            "\n" +
            ".tab-button:hover {\n" +
            "    background: #f8f9fa;\n" +
            "    color: #2c3e50;\n" +
            "}\n" +
            "\n" +
            ".tab-button.active {\n" +
            "    color: #3498db;\n" +
            "    border-bottom-color: #3498db;\n" +
            "}\n" +
            "\n" +
            ".tab-content {\n" +
            "    background: white;\n" +
            "    padding: 20px;\n" +
            "    border-radius: 0 0 8px 8px;\n" +
            "    box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
            "    min-height: 400px;\n" +
            "}\n" +
            "\n" +
            ".thread {\n" +
            "    border: 1px solid #e0e0e0;\n" +
            "    border-radius: 4px;\n" +
            "    margin: 10px 0;\n" +
            "    padding: 15px;\n" +
            "    background: #fafafa;\n" +
            "}\n" +
            "\n" +
            ".thread-header {\n" +
            "    display: flex;\n" +
            "    justify-content: space-between;\n" +
            "    align-items: center;\n" +
            "    margin-bottom: 10px;\n" +
            "}\n" +
            "\n" +
            ".thread-name {\n" +
            "    font-weight: bold;\n" +
            "    color: #2c3e50;\n" +
            "}\n" +
            "\n" +
            ".thread-state {\n" +
            "    padding: 4px 8px;\n" +
            "    border-radius: 4px;\n" +
            "    font-size: 0.8em;\n" +
            "    font-weight: bold;\n" +
            "}\n" +
            "\n" +
            ".state-RUNNABLE { background: #d4edda; color: #155724; }\n" +
            ".state-BLOCKED { background: #f8d7da; color: #721c24; }\n" +
            ".state-WAITING { background: #fff3cd; color: #856404; }\n" +
            ".state-TIMED_WAITING { background: #d1ecf1; color: #0c5460; }\n" +
            ".state-TERMINATED { background: #e2e3e5; color: #383d41; }\n" +
            "\n" +
            ".thread-details {\n" +
            "    font-size: 0.9em;\n" +
            "    color: #6c757d;\n" +
            "}\n" +
            "\n" +
            ".thread-details p {\n" +
            "    margin: 5px 0;\n" +
            "}\n" +
            "\n" +
            ".stack-trace {\n" +
            "    margin-top: 10px;\n" +
            "    background: #f8f9fa;\n" +
            "    padding: 10px;\n" +
            "    border-radius: 4px;\n" +
            "    font-family: 'Courier New', monospace;\n" +
            "    font-size: 0.8em;\n" +
            "    max-height: 200px;\n" +
            "    overflow-y: auto;\n" +
            "}\n" +
            "\n" +
            "#config-json {\n" +
            "    background: #f8f9fa;\n" +
            "    padding: 15px;\n" +
            "    border-radius: 4px;\n" +
            "    overflow-x: auto;\n" +
            "    font-family: 'Courier New', monospace;\n" +
            "    font-size: 0.9em;\n" +
            "}\n" +
            "\n" +
            ".loading {\n" +
            "    text-align: center;\n" +
            "    color: #7f8c8d;\n" +
            "    font-style: italic;\n" +
            "}\n" +
            "\n" +
            ".error {\n" +
            "    color: #dc3545;\n" +
            "    background: #f8d7da;\n" +
            "    padding: 10px;\n" +
            "    border-radius: 4px;\n" +
            "    margin: 10px 0;\n" +
            "}";
    }

    /**
     * Returns JavaScript for the dashboard.
     */
    private String getDashboardJs() {
        return "let refreshInterval;\n" +
            "\n" +
            "function showTab(tabName) {\n" +
            "    // Hide all tabs\n" +
            "    document.querySelectorAll('.tab-content').forEach(tab => {\n" +
            "        tab.style.display = 'none';\n" +
            "    });\n" +
            "    \n" +
            "    // Remove active class from all buttons\n" +
            "    document.querySelectorAll('.tab-button').forEach(btn => {\n" +
            "        btn.classList.remove('active');\n" +
            "    });\n" +
            "    \n" +
            "    // Show selected tab\n" +
            "    document.getElementById(tabName + '-tab').style.display = 'block';\n" +
            "    \n" +
            "    // Add active class to clicked button\n" +
            "    event.target.classList.add('active');\n" +
            "    \n" +
            "    // Load tab content\n" +
            "    if (tabName === 'threads') {\n" +
            "        loadThreads();\n" +
            "    } else if (tabName === 'deadlocks') {\n" +
            "        loadDeadlocks();\n" +
            "    } else if (tabName === 'config') {\n" +
            "        loadConfig();\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "function loadStatus() {\n" +
            "    fetch('/api/status')\n" +
            "        .then(response => response.json())\n" +
            "        .then(data => {\n" +
            "            document.getElementById('totalThreads').textContent = data.totalThreads || 0;\n" +
            "            document.getElementById('activeThreads').textContent = data.activeThreads || 0;\n" +
            "            \n" +
            "            if (data.initialized) {\n" +
            "                document.getElementById('status').textContent = 'Running';\n" +
            "                document.getElementById('status').style.background = '#e8f5e8';\n" +
            "                document.getElementById('status').style.color = '#27ae60';\n" +
            "            } else {\n" +
            "                document.getElementById('status').textContent = 'Not Initialized';\n" +
            "                document.getElementById('status').style.background = '#f8d7da';\n" +
            "                document.getElementById('status').style.color = '#721c24';\n" +
            "            }\n" +
            "        })\n" +
            "        .catch(error => {\n" +
            "            console.error('Error loading status:', error);\n" +
            "            document.getElementById('status').textContent = 'Error';\n" +
            "            document.getElementById('status').style.background = '#f8d7da';\n" +
            "            document.getElementById('status').style.color = '#721c24';\n" +
            "        });\n" +
            "}\n" +
            "\n" +
            "function loadThreads() {\n" +
            "    fetch('/api/threads')\n" +
            "        .then(response => response.json())\n" +
            "        .then(data => {\n" +
            "            const container = document.getElementById('threads-list');\n" +
            "            if (data.error) {\n" +
            "                container.innerHTML = '<div class=\"error\">Error: ' + data.error + '</div>';\n" +
            "                return;\n" +
            "            }\n" +
            "            \n" +
            "            if (data.threads && data.threads.length > 0) {\n" +
            "                container.innerHTML = data.threads.map(thread => {\n" +
            "                    let stackTraceHtml = '';\n" +
            "                    if (thread.stackTrace && thread.stackTrace.length > 0) {\n" +
            "                        stackTraceHtml = '<div class=\"stack-trace\">' +\n" +
            "                            thread.stackTrace.map(frame => frame.toString()).join('\\n') +\n" +
            "                            '</div>';\n" +
            "                    }\n" +
            "                    \n" +
            "                    return '<div class=\"thread\">' +\n" +
            "                        '<div class=\"thread-header\">' +\n" +
            "                            '<div class=\"thread-name\">' + thread.name + '</div>' +\n" +
            "                            '<div class=\"thread-state state-' + thread.state + '\">' + thread.state + '</div>' +\n" +
            "                        '</div>' +\n" +
            "                        '<div class=\"thread-details\">' +\n" +
            "                            '<p><strong>ID:</strong> ' + thread.id + '</p>' +\n" +
            "                            '<p><strong>Priority:</strong> ' + thread.priority + '</p>' +\n" +
            "                            '<p><strong>Daemon:</strong> ' + thread.daemon + '</p>' +\n" +
            "                            (thread.lockName ? '<p><strong>Waiting for:</strong> ' + thread.lockName + '</p>' : '') +\n" +
            "                            (thread.lockOwnerName ? '<p><strong>Lock owner:</strong> ' + thread.lockOwnerName + '</p>' : '') +\n" +
            "                            (thread.cpuTime > 0 ? '<p><strong>CPU Time:</strong> ' + (thread.cpuTime / 1000000) + ' ms</p>' : '') +\n" +
            "                        '</div>' +\n" +
            "                        stackTraceHtml +\n" +
            "                        '</div>';\n" +
            "                }).join('');\n" +
            "            } else {\n" +
            "                container.innerHTML = '<div class=\"loading\">No threads found</div>';\n" +
            "            }\n" +
            "        })\n" +
            "        .catch(error => {\n" +
            "            console.error('Error loading threads:', error);\n" +
            "            document.getElementById('threads-list').innerHTML = '<div class=\"error\">Error loading threads</div>';\n" +
            "        });\n" +
            "}\n" +
            "\n" +
            "function loadDeadlocks() {\n" +
            "    fetch('/api/deadlocks')\n" +
            "        .then(response => response.json())\n" +
            "        .then(data => {\n" +
            "            const container = document.getElementById('deadlocks-list');\n" +
            "            if (data.error) {\n" +
            "                container.innerHTML = '<div class=\"error\">Error: ' + data.error + '</div>';\n" +
            "                return;\n" +
            "            }\n" +
            "            \n" +
            "            if (data.deadlocks && data.deadlocks.length > 0) {\n" +
            "                container.innerHTML = data.deadlocks.map(deadlock => {\n" +
            "                    return '<div class=\"thread\">' +\n" +
            "                        '<h3>Deadlock Cycle ' + deadlock.cycleId + '</h3>' +\n" +
            "                        '<p>Threads involved: ' + deadlock.threadIds.join(', ') + '</p>' +\n" +
            "                        '</div>';\n" +
            "                }).join('');\n" +
            "            } else {\n" +
            "                container.innerHTML = '<div class=\"loading\">No deadlocks detected</div>';\n" +
            "            }\n" +
            "        })\n" +
            "        .catch(error => {\n" +
            "            console.error('Error loading deadlocks:', error);\n" +
            "            document.getElementById('deadlocks-list').innerHTML = '<div class=\"error\">Error loading deadlocks</div>';\n" +
            "        });\n" +
            "}\n" +
            "\n" +
            "function loadConfig() {\n" +
            "    fetch('/api/config')\n" +
            "        .then(response => response.json())\n" +
            "        .then(data => {\n" +
            "            document.getElementById('config-json').textContent = JSON.stringify(data, null, 2);\n" +
            "        })\n" +
            "        .catch(error => {\n" +
            "            console.error('Error loading config:', error);\n" +
            "            document.getElementById('config-json').textContent = 'Error loading configuration';\n" +
            "        });\n" +
            "}\n" +
            "\n" +
            "// Initialize dashboard\n" +
            "document.addEventListener('DOMContentLoaded', function() {\n" +
            "    loadStatus();\n" +
            "    loadThreads();\n" +
            "    \n" +
            "    // Set up auto-refresh\n" +
            "    refreshInterval = setInterval(() => {\n" +
            "        loadStatus();\n" +
            "        if (document.getElementById('threads-tab').style.display !== 'none') {\n" +
            "            loadThreads();\n" +
            "        }\n" +
            "    }, 5000);\n" +
            "});\n" +
            "\n" +
            "// Clean up on page unload\n" +
            "window.addEventListener('beforeunload', function() {\n" +
            "    if (refreshInterval) {\n" +
            "        clearInterval(refreshInterval);\n" +
            "    }\n" +
            "});";
    }

    /**
     * Opens the browser to the specified URL.
     */
    private void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", url);
            } else {
                pb = new ProcessBuilder("xdg-open", url);
            }
            
            pb.start();
            logger.info("Opened browser to: {}", url);
            
        } catch (Exception e) {
            logger.warn("Failed to open browser: {}", e.getMessage());
        }
    }
}
