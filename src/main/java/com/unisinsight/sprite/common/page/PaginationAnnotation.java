package com.unisinsight.sprite.common.page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 在service方法上加此注解，可实现分页的设置，并自动包装为PageResult对象
 * 注意事项：
 *  1.mapper方法的返回结果类型必须为List<Map<String, Object>>
 *  2.service方法的返回结果类型必须为Object
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PaginationAnnotation {
    Class<?> dtoClass() default Map.class;
}
