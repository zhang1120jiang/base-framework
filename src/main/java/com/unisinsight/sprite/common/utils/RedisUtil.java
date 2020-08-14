package com.unisinsight.sprite.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis 工具类
 *
 * @author zhanghengyuan
 */
@Slf4j
public class RedisUtil {
    /**
     * 功能描述: 构造器私有化
     *
     * @author qiuweiwu [qiuweiwu@unisinsight.com]
     * @date 2019/8/1 13:56
     */
    private RedisUtil() {

    }

    /**
     * 拼接key值
     */
    public static String makeKey(String phone) {
        return "collect:sign:" + phone;
    }

    /**
     * redis 工具
     */
    private static RedisTemplate<String, String> redisTemplate =
            ApplicationContextUtil.getBean("stringRedisTemplate", RedisTemplate.class);

    /**
     * 读取缓存
     *
     * @param key 键
     * @return String
     */
    public static String get(final String key) {
        String result = null;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            result = operations.get(key);
        } catch (Exception e) {
            log.warn("redis链接异常：", e);
        }

        return result;
    }

    /**
     * 写入缓存
     *
     * @param key   键
     * @param value 值
     * @return boolean
     */
    public static boolean set(final String key, String value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            log.warn("redis链接异常：", e);
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param key   键
     * @param value 值
     * @return boolean
     */
    public static boolean set(final String key, String value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            // 负数过期时间则永不过期
            if (expireTime != null && expireTime > 0L) {
                redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            }
            result = true;
        } catch (Exception e) {
            log.warn("redis链接异常：", e);
        }
        return result;
    }


    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public static void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    /**
     * 批量删除key
     *
     * @param pattern
     */
    public static void removePattern(final String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (null != keys && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 删除对应的value
     *
     * @param key
     */
    public static void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     *
     * @param key 键
     * @return boolean
     */
    public static boolean exists(final String key) {

        if (null == redisTemplate || null == key) {
            return false;
        }
        Boolean existKey = redisTemplate.hasKey(key);
        return existKey != null && existKey;
    }

    /**
     * 将列表保存到缓存
     *
     * @param key    缓存key
     * @param expire 缓存时间
     * @param data   列表数据
     */
    public static <T> void setList2Cache(String key, long expire, List<T> data) {
        set(key, JSON.toJSONString(data), expire);
    }

    /**
     * 从缓存中获取列表
     *
     * @param key   缓存key
     * @param clazz class
     * @return List
     */
    public static <T> List<T> getListFromCache(String key, Class<T> clazz) {
        List<T> data = Lists.newArrayList();
        String result = get(key);
        if (StringUtils.isBlank(result)) {
            return data;
        }
        data = getListByJsonStr(result, clazz);
        return data;
    }

    /**
     * 从缓存中获取列表
     *
     * @param jsonStr 键
     * @param clazz   类
     * @return List
     */
    public static <T> List<T> getListByJsonStr(String jsonStr, Class<T> clazz) {
        List<T> data = Lists.newArrayList();
        if (StringUtils.isBlank(jsonStr)) {
            return data;
        }
        try {
            JSONArray jsonArray = JSON.parseArray(jsonStr);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    T elem = JSON.parseObject(jsonArray.get(i).toString(), clazz);
                    data.add(elem);
                }
            }
        } catch (Exception ex) {
            log.warn("从缓存中获取列表失败", ex);
        }
        return data;
    }

    /**
     * 将对象保存到缓存
     *
     * @param key    缓存key
     * @param expire 缓存时间
     * @param t      对象
     */
    public static <T> void setObject2Cache(String key, long expire, T t) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            set(key, mapper.writeValueAsString(t), expire);
        } catch (Exception e) {
            log.warn("设置session出错：", e);
        }
    }

    /**
     * 从缓存中获取对象
     *
     * @param key   缓存key
     * @param clazz
     * @return <T> T
     */
    public static <T> T getObjectFromCache(String key, Class<T> clazz) {
        String result = get(key);
        if (StringUtils.isBlank(result)) {
            return null;
        }
        T elem = null;
        try {
            elem = JSON.parseObject(result, clazz);
        } catch (Exception ex) {
            log.warn("从缓存中获取对象失败:", ex);
        }
        return elem;
    }

    /**
     * 把一个对象缓存到hash中
     *
     * @param key
     * @param obj
     * @param expire
     * @return Boolean
     */
    public static Boolean hsetObj(String key, Object obj, long expire) {
        HashMap parseObject = JSON.parseObject(JSON.toJSONString(obj), HashMap.class);
        Map<byte[], byte[]> byteMap = new HashMap();
        Set<Entry> entrySet = parseObject.entrySet();
        for (Entry entry : entrySet) {
            byte[] keyBytes = redisTemplate.getStringSerializer().serialize(entry.getKey().toString());
            byte[] valueBytes = redisTemplate.getStringSerializer().serialize(entry.getValue().toString());
            byteMap.put(keyBytes, valueBytes);
        }
        final byte[] rawKey = redisTemplate.getStringSerializer().serialize(key);
        return redisTemplate.execute(connection -> {
            connection.hMSet(rawKey, byteMap);
            if (expire > 0) {
                connection.expire(rawKey, expire);
            }
            return true;
        }, true);
    }

    /**
     * 在一个hash中获得一个对象
     *
     * @param key
     * @param clazzType
     * @return t
     */
    public static <T> T hgetObj(String key, Class<T> clazzType) {
        final byte[] rawKey = redisTemplate.getStringSerializer().serialize(key);
        final Map<byte[], byte[]> byteMap = redisTemplate.execute(connection -> connection.hGetAll(rawKey), true);
        if (byteMap.isEmpty()) {
            return null;
        }
        Set<Entry<byte[], byte[]>> entrySet = byteMap.entrySet();
        Map<String, String> resMap = new HashMap();
        for (Entry<byte[], byte[]> entry : entrySet) {
            String fKey = redisTemplate.getStringSerializer().deserialize(entry.getKey());
            String fValue = redisTemplate.getStringSerializer().deserialize(entry.getValue());
            resMap.put(fKey, fValue);
        }
        T t = null;
        try {
            t = JSON.parseObject(JSON.toJSONString(resMap), clazzType);
        } catch (Exception ex) {
            log.warn("在一个hash中获得一个对象失败:", ex);
        }
        return t;
    }

    /**
     * 获取List大小
     *
     * @param key
     * @return
     */
    public static Long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.warn("从redis中获取list长度出现异常, return -1。 Excepton :", e);
            return -1L;
        }
    }


    /**
     * 将list放入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean lSet(String key, List<String> value) {
        try {
            redisTemplate.opsForList().leftPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.warn("插入redis出现异常，key [{}] exception:", key, e);
            return false;
        }
    }


    /**
     * 将list放入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean rSet(String key, List<String> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.warn("插入redis出现异常，key [{}] exception:", key, e);
            return false;
        }
    }

    /**
     * 从list中取一个value并返回
     *
     * @param key
     * @return
     */
    public static String lPop(String key) {
        try {
            return String.valueOf(redisTemplate.opsForList().leftPop(key));
        } catch (Exception e) {
            log.warn("redis链接异常", e);
            return "";
        }
    }

    /**
     * 从list中取一个value并返回
     *
     * @param key
     * @return
     */
    public static String rPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key);
        } catch (Exception e) {
            log.warn("redis链接异常", e);
            return "";
        }
    }

    public static void hmset(String key, Map<String, String> value, Long time) {
        try {
            redisTemplate.opsForHash().putAll(key, value);
            redisTemplate.expire(key, time, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("存入redis异常", e);
        }
    }

    /**
     * 获取Hash类型的数据
     *
     * @param key   键
     * @param field 域
     */
    public static Object hmget(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.warn("redis读取异常", e);
            return null;
        }
    }

    public static String makePlaceKey(String type, String orgParentId) {
        return "collect_place_code_key:" + type + "_" + orgParentId;
    }

    public static String makeRegionPlaceKey(String type, Long orgParentId) {
        if (null == orgParentId) {
            return "collect_region_place_code_key:" + type;
        }
        return "collect_region_place_code_key:" + type + "_" + orgParentId;
    }
}