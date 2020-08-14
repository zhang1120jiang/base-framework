/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

import com.unisinsight.sprite.common.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import javax.persistence.Column;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * description
 * map转实体类，用于mybatis参数自动映射
 * 未做数据类型判断，使用时需保证数据类型的一致性
 *
 * @author tangmingdong [458778648@qq.com]
 * @date 2019/01/05 09:32
 * @since 1.0
 */
@Slf4j
public class MapToObjectConvert {
    /**
     * 构造器私有化
     */
    private MapToObjectConvert() {

    }

    /**
     * map转实体类
     *
     * @param map Map
     * @return Object
     */
    public static Map<String, Object> convert(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return new HashMap<>(0);
        }
        Map<String, Object> resultMap = new HashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = StringUtils.withoutUnderscoreName(entry.getKey());
            resultMap.put(key, entry.getValue());
        }
        return resultMap;
    }

    /**
     * map转实体类
     *
     * @param map   Map
     * @param clazz Class
     * @param <T>   泛型
     * @return Object
     */
    public static <T> T convert(Map<String, Object> map, Class<T> clazz) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Constructor c = getConstructor(clazz);
        Field[] fs = clazz.getDeclaredFields();
        return fillFields(map, c, fs);
    }

    /**
     * list.map转成List实体数组
     *
     * @param list  List
     * @param clazz Class
     * @param <T>   t
     * @return t
     */
    public static <T> List convert(List<Map<String, Object>> list, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return new ArrayList();
        }
        if (clazz == null || Map.class.isAssignableFrom(clazz)) {
            return list.stream().map(MapToObjectConvert::convert).collect(Collectors.toList());
        } else {
            Constructor c = getConstructor(clazz);
            Field[] fs = clazz.getDeclaredFields();
            return list.stream().map(o -> fillFields(o, c, fs)).collect(Collectors.toList());
        }
    }

    /**
     * 获取实体类的构造方法，用于实例化
     *
     * @param clazz Class
     * @return 第一个
     */
    private static Constructor getConstructor(Class clazz) {
        Constructor[] cs = clazz.getConstructors();
        if (ArrayUtils.isEmpty(cs)) {
            throw new CommonException(clazz.getName() + " 0 constructors");
        }
        return cs[0];
    }

    /**
     * 对实体类的字段，进行数据填充
     *
     * @param map Map
     * @param c   构造方法
     * @param fs  字段列表
     * @param <T> t
     * @return t
     */
    private static <T> T fillFields(Map<String, Object> map, Constructor c, Field[] fs) {
        try {
            T t = (T) c.newInstance(new Object[c.getParameterCount()]);
            Map<String, Field> fieldMap = new HashMap<>();
            for (Field f : fs) {
                f.setAccessible(true);
                Column column = f.getAnnotation(Column.class);
                String fieldName = column == null ? f.getName() : column.name();
                Object obj = map.remove(fieldName);
                if (obj == null) {
                    fieldMap.put(f.getName(), f);
                } else {
                    f.set(t, obj);
                }
            }
            if (!map.isEmpty()) {
                Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> entry = it.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Field f = fieldMap.get(StringUtils.withoutUnderscoreName(key));
                    if (f != null) {
                        f.set(t, value);
                    }
                }
            }
            return t;
        } catch (Exception e) {
            log.error("{}", e);
            throw new CommonException(e.getMessage());
        }
    }
}
