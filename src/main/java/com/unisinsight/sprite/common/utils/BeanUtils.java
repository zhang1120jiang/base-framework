/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * description json转换
 *
 * @date 2018/9/6 17:12
 * @since 1.0
 */
public final class BeanUtils {

    /**
     * 构造方法
     */
    private BeanUtils() {
    }

    /**
     * json转换
     *
     * @param json  json
     * @param clazz class
     * @param <T>   T
     * @return T
     * @throws IOException
     */
    public static <T> T toBean(String json, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper.readValue(json, clazz);
    }

    /**
     * 转为list
     *
     * @param json  json
     * @param clazz class
     * @param <T>   T
     * @return List<T>
     * @throws IOException
     */
    public static <T> List<T> toList(String json, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        JavaType type = mapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
        return mapper.readValue(json, type);
    }
}
