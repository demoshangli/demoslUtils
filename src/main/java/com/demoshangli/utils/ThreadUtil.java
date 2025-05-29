package com.demoshangli.utils;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 线程工具类：提供通用线程池封装、任务并发处理与异常保护机制。
 */
public class ThreadUtil {

    /**
     * 默认线程池（缓存线程池，线程自动扩展，适合轻量级异步任务）
     */
    private static final ExecutorService COMMON_POOL = Executors.newCachedThreadPool();

    /**
     * 创建固定大小的线程池（线程数固定，适合任务量较稳定的场景）
     *
     * @param nThreads         固定线程数量
     * @param threadNamePrefix 线程名前缀，便于日志排查
     * @return ExecutorService 实例
     */
    public static ExecutorService newFixedThreadPool(int nThreads, String threadNamePrefix) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(threadNamePrefix));
    }

    /**
     * 创建可缓存的线程池（线程数自动扩展，适合突发任务场景）
     *
     * @param threadNamePrefix 线程名前缀
     * @return ExecutorService 实例
     */
    public static ExecutorService newCachedThreadPool(String threadNamePrefix) {
        return Executors.newCachedThreadPool(new NamedThreadFactory(threadNamePrefix));
    }

    /**
     * 创建单线程池（顺序执行任务，适合串行任务）
     *
     * @param threadNamePrefix 线程名前缀
     * @return ExecutorService 实例
     */
    public static ExecutorService newSingleThreadExecutor(String threadNamePrefix) {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory(threadNamePrefix));
    }

    /**
     * 提交一个异步任务，使用默认线程池，自动捕获异常
     *
     * @param task Runnable 任务
     */
    public static void runAsync(Runnable task) {
        COMMON_POOL.submit(wrap(task));
    }

    /**
     * 并行执行任务列表（无返回值），适用于批量处理操作，如数据清洗、推送等
     *
     * @param dataList    待处理数据列表
     * @param threadCount 使用的线程数
     * @param taskConsumer 每条数据的处理逻辑（Lambda 表达式）
     * @param <T>         数据类型
     */
    public static <T> void runInParallel(List<T> dataList, int threadCount, Consumer<T> taskConsumer) {
        ExecutorService pool = newFixedThreadPool(threadCount, "batch-thread");
        CountDownLatch latch = new CountDownLatch(dataList.size());

        for (T data : dataList) {
            pool.execute(() -> {
                try {
                    taskConsumer.accept(data); // 处理数据
                } catch (Exception e) {
                    e.printStackTrace(); // 可替换为日志记录
                } finally {
                    latch.countDown(); // 计数器递减，表示该任务完成
                }
            });
        }

        try {
            latch.await(); // 等待所有任务完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 设置中断标志
        } finally {
            shutdown(pool); // 安全关闭线程池
        }
    }

    /**
     * 包装任务：为任务添加统一的异常捕获机制，避免线程池崩溃
     *
     * @param task 原始任务
     * @return 包装后的 Runnable
     */
    public static Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * 安全关闭线程池：先正常关闭，若超时未完成则强制关闭
     *
     * @param pool ExecutorService 实例
     */
    public static void shutdown(ExecutorService pool) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // 强制关闭
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 自定义线程工厂类：为线程池中的线程设置统一的命名规则
     */
    public static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;              // 线程名前缀
        private final AtomicInteger count = new AtomicInteger(0); // 线程编号

        public NamedThreadFactory(String prefix) {
            this.prefix = (prefix == null || prefix.isEmpty()) ? "thread" : prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + count.incrementAndGet());
            t.setDaemon(false); // 设置为非守护线程
            return t;
        }
    }
}
