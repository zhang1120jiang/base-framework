/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

///**
// * description 根据当天日期，拼接ID序列
// * 本地模式：以毫秒数为基准，用于单机运行
// * 共享模式：以redis缓存为基准，用于分布式运行
// * @author tangmingdong [458778648@qq.com]
// * @date 2019/03/22 16:18
// * @since 1.0
// */

/**
 * id生成器
 * 1. 采用redis自增序列
 * 2. getId为数字序列
 * 3. getSequence为日期序列，格式：日期8位+数字序列10位，数字序列长度超过10位将产生异常，谨慎使用！！！
 * 4. 使用时建议传入表名作为key，不建议使用默认key,除非明确知道该表的数据体量较小
 */
@Slf4j
public class IDGenerator {
    private static final int MAX_BATCH = 10000;//批量一次最大获取的id数量
    private static final String DEFAULT_SEQUENCE_KEY="default_id_key";

    private static RedisTemplate<String, String> redisTemplate = ApplicationContextUtil.getBean("redisTemplate", RedisTemplate.class);
    private static Map<String, Long> idMap = new ConcurrentHashMap<>();
    private static Map<String, String> sequenceMap = new HashMap<>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static int today;
    private static String date;


    /**
     * 获取id
     * @return
     */
    public static synchronized Long getId() {
        return getId(DEFAULT_SEQUENCE_KEY);
    }
    public static synchronized Long getId(String key) {
        Long id = idMap.remove(key);
        if (id == null) {
            Long[] ids = getIds(2, key);
            id = ids[0];
            idMap.put(key, ids[1]);
        }
        return id;
    }
    public static Long[] getIds(int batchCount) {
        return getIds(batchCount, DEFAULT_SEQUENCE_KEY);
    }
    public static Long[] getIds(String key, int batchCount) {
        return getIds(batchCount, key);
    }
    public static Long[] getIds(int batchCount, String key) {
        batchCount = Math.min(batchCount, MAX_BATCH);
        key = "sequence_" + key;
        Long v = redisTemplate.opsForValue().increment(key, batchCount);
        if (key.startsWith("sequence_temp")) {
            redisTemplate.expire(key, 2, TimeUnit.MINUTES);
        }
        Long[] ids = new Long[batchCount];
        Long id = v-batchCount+1;
        for (int i = 0; i < batchCount; i++) {
            ids[i] = id++;
        }
        return ids;
    }

    /**
     * 获取序列
     * @return
     */
    public static synchronized String getSequence() {
        return getSequence(DEFAULT_SEQUENCE_KEY);
    }
    public static synchronized String getSequence(String key) {
        String id = sequenceMap.remove(key);
        if (id == null) {
            String[] ids = getSequences(2, key);
            id = ids[0];
            sequenceMap.put(key, ids[1]);
        }
        return id;
    }
    public static synchronized String[] getSequences(int batchCount) {
        return getSequences(batchCount, DEFAULT_SEQUENCE_KEY);
    }
    public static synchronized String[] getSequences(String key, int batchCount) {
        return getSequences(batchCount, key);
    }
    public static synchronized String[] getSequences(int batchCount, String key) {
        batchCount = Math.min(batchCount, MAX_BATCH);
        Calendar calendar = Calendar.getInstance();
        if (today != calendar.get(Calendar.DATE)) {
            today = calendar.get(Calendar.DATE);
            date = sdf.format(calendar.getTime());
        }
        key = "sequence_" + key + "_"+date;
        Long v = redisTemplate.opsForValue().increment(key, batchCount);
        String str = v.toString();
        if (str.length() > 10) {
            throw new RuntimeException("the sequences exhaustion, come back tomorrow");
        }
        if (key.startsWith("sequence_temp")) {
            redisTemplate.expire(key, 2, TimeUnit.MINUTES);
        } else if (v == batchCount) {
            redisTemplate.expire(key, 25, TimeUnit.HOURS);
        }
        String[] sequences = new String[batchCount];
        Long start = v-batchCount+1;
        for (int i = 0; i < batchCount; i++) {
            sequences[i] = date + StringUtils.leftPad(start.toString(), 10, '0');
            start++;
        }
        return sequences;
    }


//    private static int oneHour = 1000 * 60 * 60;
//    private static int oneDay = oneHour * 24;
//
//
//    private RedisTemplate<String, String> redisTemplate;
//    private String name;
//    private IDGeneratorBuilder proxy;
//    private ReentrantLock lock = new ReentrantLock();
//
//    /**
//     * 共享模式
//     * @param name
//     * @param redisTemplate
//     */
//    public IDGenerator(String name, RedisTemplate<String, String> redisTemplate) {
//        this.name = name;
//        this.redisTemplate = redisTemplate;
//        this.proxy = new RedisGenerator(name, redisTemplate);
//    }
//
//    /**
//     * 单机模式
//     */
//    IDGenerator() {
//        this.proxy = new StandaloneGenerator();
//    }
//    public String get() {
//        Calendar calendar = Calendar.getInstance();
//        check(calendar);
//        return proxy.get();
//    }
//
//    //单次获取不超过1000个
//    public String[] get(int batch) {
//        batch = Math.min(batch, maxBatch);
//        if (batch < 2) {
//            return new String[] {get()};
//        }
//        Calendar calendar = Calendar.getInstance();
//        check(calendar);
//        return proxy.get(batch);
//    }
//    private void check(Calendar calendar) {
//        if (!proxy.isToday(calendar)) {
//            lock.lock();
//            try {
//                if (!proxy.isToday(calendar)) {
//                    if (redisTemplate == null) {
//                        proxy = new StandaloneGenerator();
//                    } else {
//                        proxy = new RedisGenerator(name, redisTemplate);
//                    }
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage());
//            }
//            lock.unlock();
//        }
//    }
//
//    abstract class IDGeneratorBuilder {
//        protected SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
//        protected final ConcurrentLinkedQueue<String> cache = new ConcurrentLinkedQueue<>(); //缓存
//        protected final ReentrantLock lock = new ReentrantLock();
//        protected final Calendar calendar;
//        protected final int dayOfYear;
//        protected final String dateStr;
//
//        IDGeneratorBuilder(){
//            calendar = Calendar.getInstance();
//            dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
//            dateStr = sdf.format(calendar.getTime());
//        }
//
//        protected String get() {
//            String str = cache.poll();
//            if (StringUtils.isBlank(str)) {
//                str = get0();
//                cache.add(get0());//缓存一个
//            }
//            return dateStr+str;
//        }
//        protected String[] get(int batch) {
//            String[] arr = new String[batch];
//            if (getCache().size() >= batch) {
//                lock.lock();
//                if (getCache().size() >= batch) {
//                    for (int i = 0; i < batch; i++) {
//                        arr[i] = dateStr+getCache().poll();
//                    }
//                    lock.unlock();
//                    return arr;
//                }
//                lock.unlock();
//            }
//            for (int i = 0; i < batch; i++) {
//                String str = get0();
//                arr[i] = dateStr+str;
//                after(str);
//            }
//
//            return arr;
//        }
//
//        protected ConcurrentLinkedQueue<String> getCache() {
//            return cache;
//        }
//        protected boolean isToday(Calendar calendar) {
//            return dayOfYear == calendar.get(Calendar.DAY_OF_YEAR);
//        }
//        protected void after(String str) {}
//
//        protected abstract String get0();
//    }
//
//    class RedisGenerator extends IDGeneratorBuilder {
//        RedisTemplate<String, String> redisTemplate;
//        String key;
//
//        RedisGenerator(String name, RedisTemplate<String, String> redisTemplate) {
//            this.key = name + "_id_" + dayOfYear;
//            this.redisTemplate = redisTemplate;
//        }
//
//        @Override
//        protected String get0() {
//            Long v = redisTemplate.opsForValue().increment(key, 2);
//            cache.add(v.toString());
//            if (v < 4) {
//                if (key.startsWith("test")) {
//                    redisTemplate.expire(key, 2, TimeUnit.MINUTES);
//                } else {
//                    redisTemplate.expire(key, 25, TimeUnit.HOURS);//多一小时，避免过期时，正在调用
//                }
//            }
//            return (--v).toString();
//        }
//    }
//
//    class StandaloneGenerator extends IDGeneratorBuilder {
//        private AtomicLong timestamp = new AtomicLong();
//        private AtomicLong counter = new AtomicLong();
//        private ConcurrentLinkedQueue<String> cacheBatch = new ConcurrentLinkedQueue<>(); // 批量缓存
//
//
//        StandaloneGenerator() {
//            init();
//        }
//
//        private void init() {
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MILLISECOND, 0);
//            long l = calendar.getTime().getTime();
//            timestamp.set(System.currentTimeMillis());//记录当前时间戳
//            counter.set(timestamp.get()-l);//记录当日已过去的毫秒数，做为起点
//        }
//
//        protected String get0() {
//            long l = timestamp.incrementAndGet();
//            String str = counter.incrementAndGet()+"";
//
//            long current = System.currentTimeMillis();
//            /*
//             * 生成算法依赖于当前毫秒数，
//             * 如果并发get，很有可能timestamp会大于当前时间
//             * 为了保证强依赖，延迟返回
//             */
//            if (l > current) {
//                try {
//                    Thread.sleep(l-current);
//                } catch (Exception e) {
//                    log.error(e.getMessage());
//                }
//            }
//            return str;
//        }
//        @Override
//        protected ConcurrentLinkedQueue<String> getCache() {
//            return cacheBatch;
//        }
//
//        @Override
//        protected void after(String str) {
//            int v = Integer.parseInt(str);
//            cacheBatch.add((v+oneDay)+"");//缓存伴生对象，保证伪序列
//        }
//    }

}
