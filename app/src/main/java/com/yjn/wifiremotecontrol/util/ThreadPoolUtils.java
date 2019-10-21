package com.yjn.wifiremotecontrol.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 */
public class ThreadPoolUtils {
    private ThreadPoolUtils() {

    }

    private static int CORE_POOL_SIZE = 10;

    private static int MAX_POOL_SIZE = 100;

    private static int KEEP_ALIVE_TIME = 10000;

    private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(10);

    private static ThreadFactory threadFactory = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    };
    private static ThreadPoolExecutor threadPool;

    static {
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    public static void execute(Runnable runable) {
        threadPool.execute(runable);
    }
}
