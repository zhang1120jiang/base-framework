/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.page;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.unisinsight.ic.commons.base.PageParam;
import com.unisinsight.ic.commons.utils.MapToObjectConvert;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * description
 *
 * @author tangmingdong [458778648@qq.com]
 * @since 1.0
 */
@Aspect
@Component
@Slf4j
public class PaginationAspect {
    /**
     * 对指定路径+annotation注解的方法进行拦截验证
     * @param joinPoint ProceedingJoinPoint
     * @param paginationAnnotation Pagination
     * @return 业务处理的返回结果
     * @throws Throwable 异常
     */
	@Around(value = "execution(* com.unisinsight.*.service.*.*(..)) && @annotation(paginationAnnotation)")
    public Object aroundMethod(ProceedingJoinPoint joinPoint, PaginationAnnotation paginationAnnotation) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Optional<Object> optional = Arrays.stream(args).filter(o -> PageParam.class.isAssignableFrom(o.getClass())).findAny();
        if (!optional.isPresent()) {
            return joinPoint.proceed();
        }
        PageQuery pageParam = (PageQuery) optional.get();
        int pageNum = pageParam.getPageNum() < 1 ? 1 : pageParam.getPageNum();
        int pageSize = pageParam.getPageSize() == 0 ? 20 : pageParam.getPageSize();
        Page<Object> page = PageHelper.startPage(pageNum, pageSize);
        Object resultList = joinPoint.proceed();
        List<Map<String, Object>> list = (List<Map<String, Object>>) resultList;
        log.info("分页列表查询结果：{}", list);
        return PageResult.of(MapToObjectConvert.convert(list, paginationAnnotation.dtoClass()), page);
	}
}
