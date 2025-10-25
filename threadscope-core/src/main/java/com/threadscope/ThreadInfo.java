package com.threadscope;

import java.time.Instant;
import java.util.List;

/**
 * Represents information about a thread at a specific point in time.
 */
public class ThreadInfo {
    private long id;
    private String name;
    private String state;
    private long cpuTime;
    private long userTime;
    private boolean isDaemon;
    private int priority;
    private String lockName;
    private long lockOwnerId;
    private String lockOwnerName;
    private boolean inNative;
    private boolean suspended;
    private List<StackTraceElement> stackTrace;
    private List<MonitorInfo> lockedMonitors;
    private List<LockInfo> lockedSynchronizers;
    private Instant timestamp;
    private boolean isAsyncThread;
    private String asyncThreadType;

    public ThreadInfo() {
        this.timestamp = Instant.now();
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
    }

    public long getUserTime() {
        return userTime;
    }

    public void setUserTime(long userTime) {
        this.userTime = userTime;
    }

    public boolean isDaemon() {
        return isDaemon;
    }

    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public void setLockOwnerName(String lockOwnerName) {
        this.lockOwnerName = lockOwnerName;
    }

    public boolean isInNative() {
        return inNative;
    }

    public void setInNative(boolean inNative) {
        this.inNative = inNative;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public List<StackTraceElement> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<StackTraceElement> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<MonitorInfo> getLockedMonitors() {
        return lockedMonitors;
    }

    public void setLockedMonitors(List<MonitorInfo> lockedMonitors) {
        this.lockedMonitors = lockedMonitors;
    }

    public List<LockInfo> getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    public void setLockedSynchronizers(List<LockInfo> lockedSynchronizers) {
        this.lockedSynchronizers = lockedSynchronizers;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAsyncThread() {
        return isAsyncThread;
    }

    public void setAsyncThread(boolean asyncThread) {
        isAsyncThread = asyncThread;
    }

    public String getAsyncThreadType() {
        return asyncThreadType;
    }

    public void setAsyncThreadType(String asyncThreadType) {
        this.asyncThreadType = asyncThreadType;
    }

    public static class MonitorInfo {
        private String className;
        private int identityHashCode;
        private int stackDepth;
        private StackTraceElement stackFrame;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public void setIdentityHashCode(int identityHashCode) {
            this.identityHashCode = identityHashCode;
        }

        public int getStackDepth() {
            return stackDepth;
        }

        public void setStackDepth(int stackDepth) {
            this.stackDepth = stackDepth;
        }

        public StackTraceElement getStackFrame() {
            return stackFrame;
        }

        public void setStackFrame(StackTraceElement stackFrame) {
            this.stackFrame = stackFrame;
        }
    }

    public static class LockInfo {
        private String className;
        private int identityHashCode;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public void setIdentityHashCode(int identityHashCode) {
            this.identityHashCode = identityHashCode;
        }
    }
}
