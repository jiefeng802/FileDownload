/*
 * Copyright (c) 2015 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liulishuo.filedownloader.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executors are used in entire FileDownloader internal for managing different threads.
 * <p>
 * All thread pools in FileDownloader will comply with:
 * <p>
 * The default thread count is 0, and the maximum pool size is {@code nThreads}; When there are less
 * than {@code nThreads} threads running, a new thread is created to handle the request, but when it
 * turn to idle and the interval time of waiting for new task more than {@code DEFAULT_IDLE_SECOND}
 * second, the thread will be terminate to reduce the cost of resources.
 */
public class FileDownloadExecutors {
    private static final int DEFAULT_IDLE_SECOND = 15;

//    系统中的newCachedThreadPool
//    及时性，不需要等待
//    执行很多短期任务
//    任务量不多，不然会一次性开很多线程
    public static ThreadPoolExecutor newCachedThreadPool(String prefix) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                DEFAULT_IDLE_SECOND, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new FileDownloadThreadFactory(prefix));
    }
    //    需要等待
//    执行长期的任务，性能好很多
    public static ThreadPoolExecutor newDefaultThreadPool(int nThreads, String prefix) {
        return newFixedThreadPool(nThreads, new LinkedBlockingQueue<Runnable>(), prefix);
    }

//    默认线程池为系统中的newFixedThreadPool
//    需要等待
    public static ThreadPoolExecutor newFixedThreadPool(int nThreads,
                                                        LinkedBlockingQueue<Runnable> queue,
                                                        String prefix) {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(nThreads, nThreads,
                DEFAULT_IDLE_SECOND, TimeUnit.SECONDS, queue,
                new FileDownloadThreadFactory(prefix));
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    static class FileDownloadThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final String namePrefix;
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        FileDownloadThreadFactory(String prefix) {
            group = Thread.currentThread().getThreadGroup();
            namePrefix = FileDownloadUtils.getThreadPoolName(prefix);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);

            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
