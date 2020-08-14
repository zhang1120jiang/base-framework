package com.unisinsight.sprite.common.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description CacheUtils
 *
 * @author wenwenxian [KF.wenwenxian@h3c.com]
 * @date 2018/12/17 11:10
 * @since 1.0
 */
public final class CacheUtils {

    private CacheUtils() {

    }

    private static Map<String, CacheData> cache = new ConcurrentHashMap<>();

    /**
     * 启动定时任务清理过期缓存，避免内存溢出
     */
    static {
        new Timer().schedule(new ClearTimerTask(cache), 0, 60 * 1000);
    }

    /**
     * 设置缓存，不过期
     *
     * @param key
     * @param t
     */
    public static <T> void set(String key, T t) {
        cache.put(key, new CacheData<>(t, 0));
    }

    /**
     * 设置缓存，指定过期时间expire(单位毫秒)
     *
     * @param key
     * @param t
     * @param expire 过期时间
     */
    public static <T> void set(String key, T t, long expire) {
        cache.put(key, new CacheData<>(t, expire));
    }

    /**
     * 根据key获取指定缓存
     *
     * @param key
     * @return
     */
    public static <T> T get(String key) {
        CacheData<T> data = cache.get(key);
        if (null == data) {
            return null;
        }
        if (data.isExpire()) {
            remove(key);
            return null;
        }
        return data.getData();
    }

    /**
     * 移除指定key缓存
     *
     * @param key
     */
    public static void remove(String key) {
        cache.remove(key);
    }

    /**
     * 移除所有缓存
     */
    public static void removeAll() {
        cache.clear();
    }

    private static class CacheData<T> {

        // 缓存数据
        private T data;

        // 过期时间(单位，毫秒)
        private long expireTime;

        public CacheData(T t, long expire) {
            this.data = t;
            if (expire <= 0) {
                this.expireTime = 0L;
            } else {
                this.expireTime = Calendar.getInstance().getTimeInMillis() + expire;
            }
        }

        /**
         * 判断缓存数据是否过期
         *
         * @return true表示过期，false表示未过期
         */
        public boolean isExpire() {
            if (expireTime <= 0) {
                return false;
            }
            if (expireTime > Calendar.getInstance().getTimeInMillis()) {
                return false;
            }
            return true;
        }

        public T getData() {
            return data;
        }
    }

    /**
     * 清理过期数据定时任务
     *
     * @author zsc
     * @datetime 2018年2月9日 上午10:41:18
     */
    private static class ClearTimerTask extends TimerTask {

        private Map<String, CacheData> cache;

        private ClearTimerTask(Map<String, CacheData> cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            Set<String> keys = cache.keySet();
            for (String key : keys) {
                CacheData<?> data = cache.get(key);
                if (data.expireTime <= 0) {
                    continue;
                }
                if (data.expireTime > Calendar.getInstance().getTimeInMillis()) {
                    continue;
                }
                cache.remove(key);
            }
        }
    }
}
