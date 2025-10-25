package com.threadscope.core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable thread information class.
 * Uses modern Java features for type safety and immutability.
 */
public final class ThreadInfo {
    
    private final long id;
    private final String name;
    private final ThreadState state;
    private final long cpuTime;
    private final long userTime;
    private final boolean isDaemon;
    private final int priority;
    private final String lockName;
    private final long lockOwnerId;
    private final String lockOwnerName;
    private final boolean inNative;
    private final boolean suspended;
    private final List<StackTraceElement> stackTrace;
    private final List<MonitorInfo> lockedMonitors;
    private final List<LockInfo> lockedSynchronizers;
    private final Instant timestamp;
    private final boolean isAsyncThread;
    private final AsyncThreadType asyncThreadType;

    public ThreadInfo(long id, String name, ThreadState state, long cpuTime, long userTime,
                     boolean isDaemon, int priority, String lockName, long lockOwnerId,
                     String lockOwnerName, boolean inNative, boolean suspended,
                     List<StackTraceElement> stackTrace, List<MonitorInfo> lockedMonitors,
                     List<LockInfo> lockedSynchronizers, Instant timestamp,
                     boolean isAsyncThread, AsyncThreadType asyncThreadType) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.cpuTime = cpuTime;
        this.userTime = userTime;
        this.isDaemon = isDaemon;
        this.priority = priority;
        this.lockName = lockName;
        this.lockOwnerId = lockOwnerId;
        this.lockOwnerName = lockOwnerName;
        this.inNative = inNative;
        this.suspended = suspended;
        this.stackTrace = stackTrace != null ? Collections.unmodifiableList(new ArrayList<>(stackTrace)) : Collections.emptyList();
        this.lockedMonitors = lockedMonitors != null ? Collections.unmodifiableList(new ArrayList<>(lockedMonitors)) : Collections.emptyList();
        this.lockedSynchronizers = lockedSynchronizers != null ? Collections.unmodifiableList(new ArrayList<>(lockedSynchronizers)) : Collections.emptyList();
        this.timestamp = timestamp;
        this.isAsyncThread = isAsyncThread;
        this.asyncThreadType = asyncThreadType;
    }
    
    // Getters
    public long id() { return id; }
    public String name() { return name; }
    public ThreadState state() { return state; }
    public long cpuTime() { return cpuTime; }
    public long userTime() { return userTime; }
    public boolean isDaemon() { return isDaemon; }
    public int priority() { return priority; }
    public String lockName() { return lockName; }
    public long lockOwnerId() { return lockOwnerId; }
    public String lockOwnerName() { return lockOwnerName; }
    public boolean inNative() { return inNative; }
    public boolean suspended() { return suspended; }
    public List<StackTraceElement> stackTrace() { return stackTrace; }
    public List<MonitorInfo> lockedMonitors() { return lockedMonitors; }
    public List<LockInfo> lockedSynchronizers() { return lockedSynchronizers; }
    public Instant timestamp() { return timestamp; }
    public boolean isAsyncThread() { return isAsyncThread; }
    public AsyncThreadType asyncThreadType() { return asyncThreadType; }
    
    /**
     * Creates a new ThreadInfo with updated async information.
     */
    public ThreadInfo withAsyncInfo(boolean isAsync, AsyncThreadType type) {
        return new ThreadInfo(
            id, name, state, cpuTime, userTime, isDaemon, priority,
            lockName, lockOwnerId, lockOwnerName, inNative, suspended,
            stackTrace, lockedMonitors, lockedSynchronizers, timestamp,
            isAsync, type
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadInfo that = (ThreadInfo) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ThreadInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", isAsyncThread=" + isAsyncThread +
                ", asyncThreadType=" + asyncThreadType +
                '}';
    }
    
    /**
     * Thread state enumeration for type safety.
     */
    public enum ThreadState {
        NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
    }
    
    /**
     * Async thread type enumeration.
     */
    public enum AsyncThreadType {
        COMPLETABLE_FUTURE,
        REACTIVE_STREAMS,
        THREAD_POOL,
        WEB_SERVER,
        SCHEDULED_TASK,
        OTHER_ASYNC,
        UNKNOWN
    }
    
    /**
     * Immutable monitor information.
     */
    public static final class MonitorInfo {
        private final String className;
        private final int identityHashCode;
        private final int stackDepth;
        private final StackTraceElement stackFrame;

        public MonitorInfo(String className, int identityHashCode, int stackDepth, StackTraceElement stackFrame) {
            this.className = className;
            this.identityHashCode = identityHashCode;
            this.stackDepth = stackDepth;
            this.stackFrame = stackFrame;
        }

        public String getClassName() { return className; }
        public int getIdentityHashCode() { return identityHashCode; }
        public int getStackDepth() { return stackDepth; }
        public StackTraceElement getStackFrame() { return stackFrame; }
    }
    
    /**
     * Immutable lock information.
     */
    public static final class LockInfo {
        private final String className;
        private final int identityHashCode;

        public LockInfo(String className, int identityHashCode) {
            this.className = className;
            this.identityHashCode = identityHashCode;
        }

        public String getClassName() { return className; }
        public int getIdentityHashCode() { return identityHashCode; }
    }
    
    /**
     * Factory method for creating ThreadInfo from management ThreadInfo.
     */
    public static ThreadInfo from(java.lang.management.ThreadInfo threadInfo) {
        List<StackTraceElement> stackTrace = threadInfo.getStackTrace() != null ? 
            Arrays.asList(threadInfo.getStackTrace()) : Collections.emptyList();
            
        List<MonitorInfo> monitors = Collections.emptyList();
        if (threadInfo.getLockedMonitors() != null) {
            monitors = new ArrayList<>();
            for (java.lang.management.MonitorInfo monitor : threadInfo.getLockedMonitors()) {
                monitors.add(new MonitorInfo(
                    monitor.getClassName(),
                    monitor.getIdentityHashCode(),
                    monitor.getLockedStackDepth(),
                    monitor.getLockedStackFrame()
                ));
            }
        }
        
        List<LockInfo> locks = Collections.emptyList();
        if (threadInfo.getLockedSynchronizers() != null) {
            locks = new ArrayList<>();
            for (java.lang.management.LockInfo lock : threadInfo.getLockedSynchronizers()) {
                locks.add(new LockInfo(
                    lock.getClassName(),
                    lock.getIdentityHashCode()
                ));
            }
        }
        
        return new ThreadInfo(
            threadInfo.getThreadId(),
            threadInfo.getThreadName(),
            ThreadState.valueOf(threadInfo.getThreadState().name()),
            0L, // Will be set separately if CPU time is available
            0L,
            threadInfo.isDaemon(),
            threadInfo.getPriority(),
            threadInfo.getLockName(),
            threadInfo.getLockOwnerId(),
            threadInfo.getLockOwnerName(),
            threadInfo.isInNative(),
            threadInfo.isSuspended(),
            stackTrace,
            monitors,
            locks,
            Instant.now(),
            false, // Will be determined by detector
            AsyncThreadType.UNKNOWN
        );
    }
}
