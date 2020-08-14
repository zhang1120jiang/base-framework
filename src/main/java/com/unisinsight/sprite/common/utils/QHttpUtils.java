package com.unisinsight.sprite.common.utils;///*
// * www.unisinsight.com Inc.
// * Copyright (c) 2018 All Rights Reserved
// */
//package com.unisinsight.ic.commons.utils;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.HttpDelete;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.client.methods.HttpPut;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.util.EntityUtils;
//
///**
// *
// * @ClassName: HttpUtil
// * @Description:
// * @author 邱威武  weiwu
// * @date 2017年12月19日
// *
// */
//@Slf4j
//public class QHttpUtils {
//
//    static HttpClient httpClient = HttpClientBuilder.create().build();
//
//
//    /**
//     * @Description: 以http get的方式获取数据,
//     * @param @param url 访问地址，应该以http://host:port/name?name=value&name2=value2 形式
//     * @param @return   获得结果
//     * @return String
//     * @throws
//     * @author qiuweiwu
//     * @date 2018年1月2日
//     */
//    public static String httpGet(String url) {
//        // 指定get请求
//        HttpGet httpGet = new HttpGet(url);
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
//                .setSocketTimeout(5000).build();
//        httpGet.setConfig(requestConfig);
//        // 创建httpclient
//        // 发送请求
//        HttpResponse httpResponse;
//        //返回的json
//        String str = null;
//        try {
//            httpResponse = httpClient.execute(httpGet);
//            // 验证请求是否成功
//            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                // 得到请求响应信息
//                str = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//            }  else {
//                log.info("请求错误，错误代码： " + httpResponse.getStatusLine().getStatusCode());
//                httpGet.abort();
//                return null;
//            }
//        } catch (ClientProtocolException e) {
//            httpGet.abort();
//        } catch (IOException e) {
//            httpGet.abort();
//        }
//        return str;
//    }
//
//    /**
//     * @Description: 发送JSON字符串形式的http访问方式。
//     * @param @param url    访问地址
//     * @param @param jsonData  JSON字符串
//     * @param @return   返回结果
//     * @return String
//     * @throws
//     * @author qiuweiwu
//     * @date 2018年1月2日
//     */
//    public static String httpPost(String url, String jsonData) {
//        // 指定Post请求
//        HttpPost httpPost = new HttpPost(url);
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
//                .setSocketTimeout(5000).build();
//        httpPost.setConfig(requestConfig);
//
//        httpPost.addHeader("accept", "application/json; charset=UTF-8");
//        httpPost.addHeader("user-agent", "camera-java-sdk");
//
//        StringEntity entity = new StringEntity(jsonData,Charset.forName("UTF-8"));
//        entity.setContentType("application/json");
//        entity.setContentEncoding("UTF-8");
//
//        // 封装post请求数据
//        httpPost.setEntity(entity);
//        // 发送请求
//        HttpResponse httpResponse;
//        // 返回的json
//        String str = null;
//
//        try {
//            // 发送请求
//            httpResponse = httpClient.execute(httpPost);
//            // 判断请求是否成功
//            if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
//                // 得到请求响应信息
//                str = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//            } else {
//                log.info(String.valueOf(httpResponse.getEntity()));
//                log.info("请求错误，错误代码： " + httpResponse.getStatusLine().getStatusCode());
//                httpPost.abort();
//                return null;
//            }
//        } catch (ClientProtocolException e) {
//            log.error(e.getLocalizedMessage());
//            httpPost.abort();
//            return null;
//        } catch (IOException e) {
//            log.error(e.getLocalizedMessage());
//            return null;
//        }
//        return str;
//    }
//
//    public static String httpDelete(String url) {
//        // 指定Post请求
//        HttpDelete httpDelete = new HttpDelete(url);
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
//                .setSocketTimeout(5000).build();
//        httpDelete.setConfig(requestConfig);
//
//        httpDelete.addHeader("accept", "application/json; charset=UTF-8");
//        httpDelete.addHeader("user-agent", "camera-java-sdk");
//
//        // 封装post请求数据
//        // 发送请求
//        HttpResponse httpResponse;
//        // 返回的json
//        String str = null;
//        try {
//            // 发送请求
//            httpResponse = httpClient.execute(httpDelete);
//            // 判断请求是否成功
//            if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
//                // 得到请求响应信息
//                str = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//            } else {
//                log.info(String.valueOf(httpResponse.getEntity()));
//                log.info("请求错误，错误代码： " + httpResponse.getStatusLine().getStatusCode());
//                httpDelete.abort();
//                return null;
//            }
//        } catch (ClientProtocolException e) {
//            log.error(e.getLocalizedMessage());
//            httpDelete.abort();
//            return null;
//        } catch (IOException e) {
//            log.error(e.getLocalizedMessage());
//            return null;
//        }
//        return str;
//    }
//    public static String httpPut(String url, String jsonData) {
//        // 指定Post请求
//        HttpPut httpPut = new HttpPut(url);
//        RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
//                .setSocketTimeout(5000).build();
//        httpPut.setConfig(requestConfig);
//
//        httpPut.addHeader("accept", "application/json; charset=UTF-8");
//        httpPut.addHeader("user-agent", "camera-java-sdk");
//
//        StringEntity entity = new StringEntity(jsonData,Charset.forName("UTF-8"));
//        entity.setContentType("application/json");
//        entity.setContentEncoding("UTF-8");
//
//        // 封装post请求数据
//        // 发送请求
//        HttpResponse httpResponse;
//        // 返回的json
//        String str = null;
//        try {
//            // 发送请求
//            httpResponse = httpClient.execute(httpPut);
//            // 判断请求是否成功
//            if(httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_OK){
//                // 得到请求响应信息
//                str = EntityUtils.toString(httpResponse.getEntity(), "utf-8");
//            } else {
//                log.info(String.valueOf(httpResponse.getEntity()));
//                log.info("请求错误，错误代码： " + httpResponse.getStatusLine().getStatusCode());
//                httpPut.abort();
//                return null;
//            }
//        } catch (ClientProtocolException e) {
//            log.error(e.getLocalizedMessage());
//            httpPut.abort();
//            return null;
//        } catch (IOException e) {
//            log.error(e.getLocalizedMessage());
//            return null;
//        }
//        return str;
//    }
//
//
//    private QHttpUtils() {
//
//    }
//
//}