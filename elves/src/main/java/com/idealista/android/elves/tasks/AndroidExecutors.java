/*
 *  Copyright (c) 2014, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */
package com.idealista.android.elves.tasks;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This was created because the helper methods in {@link java.util.concurrent.Executors} do not work
 * as people would normally expect.
 *
 * Normally, you would think that a cached thread pool would create new threads when necessary,
 * queue them when the pool is full, and kill threads when they've been inactive for a certain
 * period of time. This is not how {@link java.util.concurrent.Executors#newCachedThreadPool()}
 * works.
 *
 * Instead, {@link java.util.concurrent.Executors#newCachedThreadPool()} executes all tasks on
 * a new or cached thread immediately because corePoolSize is 0, SynchronousQueue is a queue with
 * size 0 and maxPoolSize is Integer.MAX_VALUE. This is dangerous because it can create an
 * unchecked
 * amount of threads.
 */
public final class AndroidExecutors {

    private static final AndroidExecutors INSTANCE = new AndroidExecutors();

    private final Executor uiThread;

    private AndroidExecutors() {
        uiThread = new UIThreadExecutor();
    }

    /**
     * Nexus 5: Quad-Core
     * Moto X: Dual-Core
     *
     * AsyncTask:
     * CORE_POOL_SIZE = CPU_COUNT + 1
     * MAX_POOL_SIZE = CPU_COUNT * 2 + 1
     *
     * https://github.com/android/platform_frameworks_base/commit/719c44e03b97e850a46136ba336d729f5fbd1f47
     */
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /* package */ static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    /* package */ static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
    /* package */ static final long KEEP_ALIVE_TIME = 1L;

    /**
     * Creates a proper Cached Thread Pool. Tasks will reuse cached threads if available
     * or create new threads until the core pool is full. tasks will then be queued. If an
     * task cannot be queued, a new thread will be created unless this would exceed max pool
     * size, then the task will be rejected. Threads will time out after 1 second.
     *
     * Core thread timeout is only available on android-9+.
     *
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        allowCoreThreadTimeout(executor, true);

        return executor;
    }

    /**
     * Creates a proper Cached Thread Pool. Tasks will reuse cached threads if available
     * or create new threads until the core pool is full. tasks will then be queued. If an
     * task cannot be queued, a new thread will be created unless this would exceed max pool
     * size, then the task will be rejected. Threads will time out after 1 second.
     *
     * Core thread timeout is only available on android-9+.
     *
     * @param threadFactory the factory to use when creating new threads
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);

        allowCoreThreadTimeout(executor, true);

        return executor;
    }

    /**
     * Compatibility helper function for
     * {@link ThreadPoolExecutor#allowCoreThreadTimeOut(boolean)}
     *
     * Only available on android-9+.
     *
     * @param executor the {@link ThreadPoolExecutor}
     * @param value    true if should time out, else false
     */
    public static void allowCoreThreadTimeout(ThreadPoolExecutor executor, boolean value) {
        executor.allowCoreThreadTimeOut(value);
    }

    /**
     * An {@link Executor} that executes tasks on the UI thread.
     */
    public static Executor uiThread() {
        return INSTANCE.uiThread;
    }

    /**
     * An {@link Executor} that runs tasks on the UI thread.
     */
    public static class UIThreadExecutor implements Executor {

        private static Executor executor;

        public static void init(Executor executor) {
            UIThreadExecutor.executor = executor;
        }

        @Override
        public void execute(Runnable command) {
            if (executor == null) {
                throw new IllegalStateException("The UI executor is not initialized");
            }

            executor.execute(command);
        }
    }
}