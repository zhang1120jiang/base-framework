/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.utils;

//import com.unisinsight.ic.commons.enums.BaseResultCode;
//import com.unisinsight.ic.commons.exception.CommonException;
//import org.apache.commons.lang3.RandomUtils;
//import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;

import java.util.UUID;

/**
 * description 字符串处理
 *
 * @author liuran [KF.liuran@h3c.com]
 * @date 2018/9/6 17:12
 * @since 1.0
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
    private StringUtils() {
    }

    public static String UUID() {
        //去除UUID中的-
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
//
//
//    /**
//     * 生成验证码，默认6位
//     * @return String
//     */
//    public static String getValidCode() {
//        return getValidCode(6);
//    }
//
//    /**
//     * 生成验证码，指定长度4-8位
//     * @param len 指定长度
//     * @return String
//     */
//    public static String getValidCode(int len) {
//        len = Math.min(len, 8);
//        len = Math.max(len, 4);
//        int min = (int) Math.pow(10,len - 1);
//        int max = (int) Math.pow(10,len);
//        return Integer.toString(RandomUtils.nextInt(min,max));
//    }
//
//    /**
//     * 生成业务流水号,默认18位
//     * @return 流水号
//     */
//    public static String getSerialNumber() {
//        return getSerialNumber(18);
//    }
//
//    /**
//     * 生成业务流水号
//     * @param len 字符长度，最小13(系统的毫秒数)，最大32
//     * @return 流水号
//     */
//    public static String getSerialNumber(int len) {
//        Long l = System.currentTimeMillis();
//        if (len <= 13) {
//            return l.toString();
//        }
//        int e = Math.min(len, 32) - 13;
//        return l.toString()+getRandomStr(e);
//    }
//
//    private static String getRandomStr(int len) {
//        len = Math.min(len, 19);
//        long max = new Double(Math.pow(10, len)).longValue();
//        long r = RandomUtils.nextLong(0, max);
//        return leftPad(Long.toString(r), len, '0');
//    }
//
//    public static List<Integer> stringToList(String str) {
//        String[] array = str.split(",");
//        List<Integer> ids = new ArrayList<>(array.length);
//        for (String val : array) {
//            try {
//                ids.add(Integer.parseInt(val));
//            } catch (Exception e) {
//                throw CommonException.of(BaseResultCode.INVALID_PARAM_ERROR);
//            }
//        }
//
//        return ids;
//    }

    public static String underscoreName(String name) {
        if (org.springframework.util.StringUtils.isEmpty(name)) {
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

    public static String withoutUnderscoreName(String name) {
        if (isBlank(name)) {
            return "";
        } else if (name.indexOf('_') < 0) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        boolean underscore = false;
        char[] cs = name.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            if (i == 0) {
                c = Character.toLowerCase(c);
            } else if ('_' == c) {
                underscore = true;
                continue;
            } else {
                if (underscore) {
                    c = Character.toUpperCase(c);
                }
                underscore = false;
            }
            result.append(c);
        }
        return result.toString();
    }

//    public static String encode(String str, String key) {
//        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
//        encryptor.setPassword(key);
//        return encryptor.encrypt(str);
//    }
//
//    public static String decode(String str, String key) {
//        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
//        encryptor.setPassword(key);
//        return encryptor.decrypt(str);
//    }
//
//    /**
//     * 判断是否是excel文件名
//     * @param fileName 文件名
//     * @return ture/false
//     */
//    public static Boolean isExcel(String fileName) {
//        if (fileName != null && !"".equals(fileName)) {
//            //lastIndexOf如果没有搜索到则返回为-1
//            if (fileName.lastIndexOf('.') != -1) {
//                String fileType = (fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length())).toLowerCase();
//                String[] suppotFile = new String[2];
//                suppotFile[0] = "xlsx";
//                suppotFile[1] = "xls";
//                for (String s : suppotFile) {
//                    if (s.equals(fileType)) {
//                        return true;
//                    }
//                }
//                return false;
//            } else {
//                return false;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 日期转换方法
//     * @param date 日期
//     * @param dateFormat 格式
//     * @return 日期字符串
//     */
//    public static String parseDate(Date date,String dateFormat) {
//        String dateStr = null;
//        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
//        dateStr = sdf.format(date);
//        return dateStr;
//    }
//
//    /**
//     * 对手机号中间四位做打码处理
//     * @param mobile 原始手机号
//     * @return 打码后的手机号
//     */
//    public static String parseMobile(String mobile) {
//        StringBuilder codeMobile = new StringBuilder();
//        codeMobile.append(mobile.substring(0, 3));
//        codeMobile.append("****");
//        codeMobile.append(mobile.substring(7, 11));
//        return codeMobile.toString();
//    }

    /**
     * 校验sql入参是否有通配符%_，有通配符的进行转义
     * @param sqlParam 入参
     * @return 转义后的参数
     */
    public static String checkSqlParam(String sqlParam) {
        sqlParam = sqlParam.replaceAll("%", "\\\\%");
        sqlParam = sqlParam.replaceAll("_", "\\\\_");
        return sqlParam;
    }

//    /**
//     * 转义正则特殊字符 （$()*+.[]?\^{},|）
//     *
//     * @param keyword 入参
//     * @return 转义后的字符串
//     */
//    public static String escapeExprSpecialWord(String keyword) {
//        String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
//        StringBuffer buffer = new StringBuffer();
//        for(int i = 0; i < keyword.length(); i++) {
//            char word = keyword.charAt(i);
//            for (String fbs : fbsArr) {
//                if (fbs.equals(String.valueOf(word))) {
//                    buffer.append("\\");
//                    break;
//                }
//            }
//            buffer.append(word);
//        }
//        return buffer.toString();
//    }
//
}
