package com.unisinsight.sprite.common.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * ClassName：ServletContextUtils
 * Description:
 * author: mayouwen
 * date: 2019/1/5
 */
public class ServletContextUtils {
    private ServletContextUtils() {
    }
    /**
     * 获取HttpServletRequest对象
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        return Objects.requireNonNull(attributes).getRequest();
    }

    /**
     * 获取HttpServletResponse对象
     *
     * @return
     */
    public static HttpServletResponse getResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        return Objects.requireNonNull(attributes).getResponse();
    }
}
