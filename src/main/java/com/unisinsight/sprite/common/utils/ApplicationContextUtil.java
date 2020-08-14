/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * description
 *
 * @author tangmingdong [458778648@qq.com]
 * @date 2019/01/08 18:05
 * @since 1.0
 */
public class ApplicationContextUtil implements ApplicationContextAware {
    /**
     * 上下文对象实例
     */
    private static ApplicationContext applicationContext;

    /**
     * 注入
     * @param applicationContext ApplicationContext
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取applicationContext
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取配置属性
     * @param key key
     * @return String
     */
    public static String getProperty(String key) {
        return getApplicationContext().getEnvironment().getProperty(key);
    }
    /**
     * 通过name获取 Bean.
     * @param name name
     * @return Object
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     * @param clazz Class
     * @param <T> t
     * @return T
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }
    /**
     * 通过name, class获取Bean.
     * @param name name
     * @param clazz Class
     * @param <T> t
     * @return T
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}
