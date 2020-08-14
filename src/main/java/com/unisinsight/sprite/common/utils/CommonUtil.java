package com.unisinsight.sprite.common.utils;///*
// * www.unisinsight.com Inc.
// * Copyright (c) 2018 All Rights Reserved
// */
//package com.unisinsight.ic.commons.utils;
//
//import com.unisinsight.ic.commons.base.PushMessage;
//import com.unisinsight.ic.commons.constant.CommonContants;
//import com.unisinsight.ic.commons.enums.BaseResultCode;
//import com.unisinsight.ic.commons.exception.CommonException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeanUtils;
//import org.springframework.data.redis.core.RedisTemplate;
//
///**
// * description
// *
// * @author tangmingdong [458778648@qq.com]
// * @date 2019/05/09 14:56
// * @since 1.0
// */
//@Slf4j
//public class CommonUtil {
//    /**
//     * RedisTemplate
//     */
//    private static RedisTemplate<String, PushMessage> redisTemplate;
//    static {
//        try {
//            redisTemplate = ApplicationContextUtil.getBean("pushMessageTemplate", RedisTemplate.class);
//        } catch (Exception e) {
//           log.error(e.getMessage());
//        }
//    }
//
//    public static void push(PushMessage message) {
//        redisTemplate.convertAndSend(CommonContants.REDIS_PUSH_TOPIC, message);
//    }
//
//    /**
//     * 实体类转换工具，用于属性复制
//     * @param source Object
//     * @param targetClass Class
//     * @param <T> T
//     * @return T
//     */
//    public static <T> T convert(Object source, Class<T> targetClass) {
//        try {
//            T target = targetClass.newInstance();
//            BeanUtils.copyProperties(source, target);
//            return target;
//        } catch (Exception e) {
//            throw CommonException.of(BaseResultCode.CONVERT_BEAN_ERROR);
//        }
//    }
//}
