package com.unisinsight.sprite.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * Session处理类
 */
@Slf4j
public class SessionHelper {

    private SessionHelper() {
    }

    public static String getCookie(String key){
        Cookie[] cookies = getRequest().getCookies();
        if (ArrayUtils.isNotEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(key)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }
    public static String getHeader(String key) {
        return getRequest().getHeader(key);
    }
    public static String getParameter(String key) {
        return getRequest().getParameter(key);
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

    /**
     * 获取HttpSession
     *
     * @return
     */
    public static HttpSession getSession() {
        return Objects.requireNonNull(getRequest().getSession(true));
    }

    /**
     * 获取HttpSession
     *
     * @param b
     * @return
     */
    public static HttpSession getSession(boolean b) {
        return Objects.requireNonNull(getRequest()).getSession(b);
    }

    /**
     *
     * @param name
     * @param t 必须是对象 引用类型 不能是 String  int 等
     * @param <T>
     * @return
     */
    public static <T> T getAttribute(String name, Class<T> t) {
        try {
            HttpSession session = SessionHelper.getSession(true);
            String json = (String) session.getAttribute(name);
            JSONObject userJson = JSONObject.parseObject(json);
            return JSON.toJavaObject(userJson,t);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 获取session中的信息，返回json
     * @param name
     * @param <T>
     * @return
     */
    public static JSONObject getAttribute(String name) {
        try {
            HttpSession session = SessionHelper.getSession(true);
            String json = (String) session.getAttribute(name);
            return JSONObject.parseObject(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param name
     * @param t
     * @param <T>  t 必须是对象 引用类型 不能是 String  int 等
     */
    public static <T> void setAttribute(String name, T t) {
        HttpSession session = SessionHelper.getSession(true);
        try {
            ObjectMapper mapper = new ObjectMapper();
            session.setAttribute(name, mapper.writeValueAsString(t));
        }catch (Exception e){
            log.error("设置session出错："+e.getMessage());
            session.setAttribute(name,"");
        }



    }



}
