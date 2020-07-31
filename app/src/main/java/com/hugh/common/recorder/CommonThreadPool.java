package com.hugh.common.recorder;


import com.hugh.common.global.Constant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CommonThreadPool {
    private ExecutorService fixedExecutorService;
    private ExecutorService cachedExecutorService;
    // 工作线程
    private static CommonThreadPool threadPool;

    // 创建线程池,requestThreadCount为线程池中工作线程的个数
    private CommonThreadPool() {
        fixedExecutorService = Executors.newFixedThreadPool(Constant.ThreadPoolCount);
        cachedExecutorService = Executors.newCachedThreadPool();
    }

    // 单态模式，获得一个默认线程个数的线程池
    public static CommonThreadPool getThreadPool() {
        if (threadPool == null) {
            synchronized (CommonThreadPool.class) {
                if (threadPool == null) {
                    threadPool = new CommonThreadPool();
                }
            }
        }

        return threadPool;
    }

    // 销毁线程池,该方法保证在所有任务都完成的情况下才销毁所有线程，否则等待任务完成才销毁
    public synchronized static void Destroy() {
        if (threadPool != null) {
            threadPool.destroy();
        }

        threadPool = null;
    }

    private void destroy() {
        fixedExecutorService = null;
        cachedExecutorService = null;
        threadPool = null;
    }

    // 执行任务,其实只是把任务加入任务队列，什么时候执行有线程池管理器决定
    public void addFixedTask(Runnable task) {
        fixedExecutorService.execute(task);
    }

    // 执行任务,其实只是把任务加入任务队列，什么时候执行有线程池管理器决定
    public void addCachedTask(Runnable task) {
        cachedExecutorService.execute(task);
    }
}