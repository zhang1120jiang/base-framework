/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

import com.unisinsight.sprite.common.enums.BaseResultCode;
import com.unisinsight.sprite.common.exception.CommonException;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description 实体转换类
 *
 * @date 2018/9/6 17:12
 * @since 1.0
 */
public final class BeanConvert {

    /**
     * 构造方法私有化
     */
    private BeanConvert() {
    }

    /**
     * list 转换
     *
     * @param source 数据
     * @param c      class
     * @param <T>    T
     * @return List<T>
     */
    public static <T> List<T> convertList(Object source, Class<T> c) {

        if (null == source) {
            return Collections.emptyList();
        }
        if (!(source instanceof List)) {
            throw CommonException.of(BaseResultCode.LIST_CONVERT_ERROR, BaseResultCode.LIST_CONVERT_ERROR.getMessage());
        }

        List<Object> list = (List<Object>) source;

        return list.stream().map(model -> convert(model, c)).collect(Collectors.toList());
    }

    /**
     * 实体转换
     *
     * @param source 数据
     * @param c      class
     * @param <T>    T
     * @return T
     */
    public static <T> T convert(Object source, Class<T> c) {
        T target;
        try {
            target = c.newInstance();
            BeanUtils.copyProperties(source, target);
        } catch (InstantiationException e) {
            throw CommonException.of(BaseResultCode.INVALID_PARAM_ERROR, "实例化异常");
        } catch (IllegalAccessException e) {
            throw CommonException.of(BaseResultCode.INVALID_PARAM_ERROR, "非法访问异常");
        }
        return target;
    }

}
