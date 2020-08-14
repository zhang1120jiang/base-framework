package com.unisinsight.sprite.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池工具类
 * 借用newCachedThreadPool特性实现线程自动回收
 * 借用newSingleThreadExecutor特性实现瞬发控制
 * 建议项目中所有多线程编程，均使用此类的execute方法
 * @author tangmingdong
 */
@Slf4j
public class ThreadPoolUtils {
    private static ThreadPoolExecutor cachedThreadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static int sum = 0;
    private static int limit = 100;//毫秒内限流，初始值1000以内，影响不大

    private static int count=0;
    private static int difference = 0;
    private static long time = System.currentTimeMillis();

    /**
     * 外部限流，内部无限扩充，超时清理
     * 内部并行任务
     * 111.149测试极限为：25万/秒
     * @param runnable runnable
     */
    public static void execute(Runnable runnable) {
        singleThreadExecutor.execute(() -> {
            cachedThreadPool.execute(runnable);
            if (++sum % limit == 0) {
                Long current = System.currentTimeMillis();
                if (current - time < 100) { //如果时差在百毫秒内，识别为并发任务
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }

                    int activeCount = cachedThreadPool.getActiveCount();
                    difference = activeCount-count;
                    count = activeCount;
                    if (difference < 0) {
                        if (limit < 200) {
                            limit <<= 3;
                        } else if (limit < 3000) {
                            limit += 100;
                        } else {
                            limit *=0.9;
                        }
                    }
                } else if (limit > 100){
                    limit --;
                }
                time = current;
            }
        });
    }
}
