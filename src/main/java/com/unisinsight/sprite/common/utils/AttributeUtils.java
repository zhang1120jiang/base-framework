package com.unisinsight.sprite.common.utils;///*
// * www.unisinsight.com Inc.
// * Copyright (c) 2018 All Rights Reserved
// */
//package com.unisinsight.ic.commons.utils;
//
//import com.unisinsight.ic.commons.constant.CommonContants;
//import com.unisinsight.ic.commons.base.AttributeDO;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * description
// *
// * @author xxxxx [xxxxx@unisinsight.com]
// * @date 2018/12/03 08:55
// * @since 1.0
// */
//public class AttributeUtils {
//
//    /**
//     * 遍历类的属性和值，返回值不为空的列表
//     * @author qiuweiwu 邱威武
//     * @date 2018/12/3 9:14
//     * @param model 类
//     * @return List<AttributeDO>
//     * @throws NoSuchMethodException 异常
//     * @throws IllegalAccessException 异常
//     * @throws IllegalArgumentException 异常
//     * @throws InvocationTargetException 异常
//     */
//    public static List<AttributeDO> getAttributes(Object model) throws NoSuchMethodException,
//            IllegalAccessException, InvocationTargetException {
//        List<AttributeDO> attributeDOList = new ArrayList<>();
//        Field[] fields = model.getClass().getDeclaredFields();
//
//        for (Field field : fields) {
//            String name = field.getName();
//            String upName = name.substring(0, 1).toUpperCase() + name.substring(1);
//
//            Method m = model.getClass().getMethod("get" + upName);
//            Object value = m.invoke(model);
//            if (value != null) {
//                AttributeDO attributeDO = new AttributeDO();
//                attributeDO.setName(camelToUnderline(name));
//                attributeDO.setValue(value);
//                attributeDOList.add(attributeDO);
//            }
//
//        }
//        return attributeDOList;
//    }
//
//    /**
//     * 驼峰式转下划线式的命名
//     *
//     * @author qiuweiwu 邱威武
//     * @date 2018/12/3 9:17
//     * @param param String，驼峰式命名
//     * @return String
//     */
//    public static String camelToUnderline(String param) {
//        if (param == null || "".equals(param.trim())) {
//            return "";
//        }
//        int len = param.length();
//        StringBuilder sb = new StringBuilder(len);
//        for (int i = 0; i < len; i++) {
//            char c = param.charAt(i);
//            if (Character.isUpperCase(c)) {
//                sb.append(CommonContants.UNDERLINE);
//                sb.append(Character.toLowerCase(c));
//            } else {
//                sb.append(c);
//            }
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 下划线格式字符串转换为驼峰格式字符串2
//     *
//     * @author qiuweiwu 邱威武
//     * @param param String，下划线命名
//     * @date 2018/12/3 9:17
//     * @return String
//     */
//    public static String underlineToCamel(String param) {
//        if (param == null || "".equals(param.trim())) {
//            return "";
//        }
//        StringBuilder sb = new StringBuilder(param);
//        Matcher mc = Pattern.compile("_").matcher(param);
//        int i = 0;
//        while (mc.find()) {
//            int position = mc.end() - (i++);
//            sb.replace(position - 1, position + 1, sb.substring(position, position + 1).toUpperCase());
//        }
//        return sb.toString();
//    }
//
//    /**
//     * 工具类构造器私有化
//     * @author qiuweiwu 邱威武
//     * @date 2018/12/3 9:45
//     */
//    private AttributeUtils() {
//
//    }
//}
