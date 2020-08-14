/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 驼峰和下划线的转换
 *
 * @author t17153 [tan.gang@h3c.com]
 * @date 2018/9/7 14:48
 * @since 1.0
 */
public class SnakeAndCamelUtils {
    public static String toSnake(String input) {
        if (StringUtils.isBlank(input)) return "";
        if (input.equals(input.toUpperCase())) return input;
        int length = input.length();
        StringBuilder result = new StringBuilder(length * 2);
        int resultLength = 0;
        boolean wasPrevTranslated = false;

        for(int i = 0; i < length; ++i) {
            char c = input.charAt(i);
            if (i > 0 || c != '_') {
                if (Character.isUpperCase(c)) {
                    if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != '_') {
                        result.append('_');
                        ++resultLength;
                    }

                    c = Character.toLowerCase(c);
                    wasPrevTranslated = true;
                } else {
                    wasPrevTranslated = false;
                }

                result.append(c);
                ++resultLength;
            }
        }

        return resultLength > 0 ? result.toString() : input;
    }

    public static String convertCamelToSnake(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(name.substring(0, 1).toLowerCase());
        for (int i = 1; i < name.length(); ++i) {
            String s = name.substring(i, i + 1);
            String slc = s.toLowerCase();
            if (!(s.equals(slc))) {
                result.append("_").append(slc);
            } else {
                result.append(s);
            }
        }
        return result.toString();
    }

    public static String convertSnakeToCamel(String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        if (name.indexOf("_") < 0) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        result.append(name.substring(0, 1).toLowerCase());
        boolean underscore = false;
        for (int i = 1; i < name.length(); ++i) {
            String s = name.substring(i, i + 1);
            if ("_".equals(s)) {
                underscore = true;
                continue;
            } else {
                if (underscore) {
                    s = s.toUpperCase();
                }
                underscore = false;
            }
            result.append(s);
        }
        return result.toString();
    }
}
