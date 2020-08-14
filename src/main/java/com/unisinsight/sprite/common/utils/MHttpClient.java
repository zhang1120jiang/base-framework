package com.unisinsight.sprite.common.utils;//package com.unisinsight.ic.commons.utils;
//
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.unisinsight.ic.commons.enums.BaseResultCode;
//import com.unisinsight.ic.commons.exception.CommonException;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.DeleteMethod;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.PutMethod;
//import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
//import org.springframework.stereotype.Component;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.Map;
//
//import static com.unisinsight.ic.commons.enums.BaseResultCode.HTTP_CONNECTION_ERROR;
//
//@Component
//@Slf4j
//public class MHttpClient {
//
//    private String httpContent = "Content-Type";
//    private String httpAuthorization = "Authorization";
//
//    public String get(String subUrl) {
//        return get(subUrl, null, null, null);
//    }
//
//    private String getErrorFromResult(String str) {
//        JSONObject result = JSONObject.parseObject(str);
//        String errorMess = result.getString("message");
//        throw CommonException.of(BaseResultCode.HTTP_CONNECTION_ERROR, errorMess);
//    }
//
//
//    /**
//     * get方法
//     */
//    public String get(String subUrl, String authorization, String contentType, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            GetMethod http;
//            http = new GetMethod(subUrl);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            if (params != null) {
//                for (Map.Entry entry : params.entrySet()) {
//                    String key = entry.getKey().toString();
//                    http.getParams().setParameter(key, params.get(key));
//                }
//            }
//            httpClient.executeMethod(http);
//
//            // 连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//
//    /**
//     * get方法
//     */
//    public String getToStream(String subUrl, String authorization, String contentType, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            GetMethod http;
//            http = new GetMethod(subUrl);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            if (params != null) {
//                for (Map.Entry entry : params.entrySet()) {
//                    String key = entry.getKey().toString();
//                    http.getParams().setParameter(key, params.get(key));
//                }
//            }
//            httpClient.executeMethod(http);
//
////            //连接成功
//            if (200 == http.getStatusCode()) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(http.getResponseBodyAsStream()));
//                StringBuilder sb = new StringBuilder();
//                String str = "";
//                while ((str = reader.readLine()) != null) {
//                    sb.append(str);
//                }
//                return sb.toString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//
//
//    public <T> String post(String subUrl, String contentType, String authorization, T t) {
//        try {
//            HttpClient httpClient = new HttpClient();
//            PostMethod http;
//            http = new PostMethod(subUrl);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            if (t != null) {
//                ObjectMapper mapper = new ObjectMapper();
//                String req = mapper.writeValueAsString(t);
//                http.setRequestBody(req);
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (
//                IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//    }
//
//
//    /**
//     * post方法
//     */
//    public String post(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            PostMethod http;
//            http = new PostMethod(subUrl);
//            HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();
//            // 设置连接超时时间(单位毫秒)
//            managerParams.setConnectionTimeout(5000);
//            // 设置读数据超时时间(单位毫秒)
//            managerParams.setSoTimeout(5000);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            if (params != null) {
//                http.setRequestBody(JSONObject.toJSONString(params));
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (
//                IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//
//    /**
//     * post方法
//     */
//    public String postForImage(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            PostMethod http;
//            http = new PostMethod(subUrl);
//            HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();
//            // 设置连接超时时间(单位毫秒)
//            managerParams.setConnectionTimeout(5000);
//            // 设置读数据超时时间(单位毫秒)
//            managerParams.setSoTimeout(5000);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            if (params != null) {
//                String jsonStr = JSONObject.toJSONString(params);
//                jsonStr = jsonStr.replaceAll("(\\\\r\\\\n|\\\\r|\\\\n|\\\\n\\\\r)", "");
//                http.setRequestBody(jsonStr);
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (
//                IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//
//    /**
//     * put方法
//     */
//    public String put(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            PutMethod http;
//            http = new PutMethod(subUrl);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            if (params != null) {
//                http.setRequestBody(JSONObject.toJSONString(params));
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//    /**
//     * patch方法
//     */
//    public String patch(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            PatchMethod http;
//            http = new PatchMethod(subUrl);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//            }
//            if (params != null) {
//                http.setRequestBody(JSONObject.toJSONString(params));
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//    /**
//     * patch方法(护校助理调用)
//     */
//    public String schoolPatch(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            PatchMethod http;
//            http = new PatchMethod(subUrl);
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                    http.addRequestHeader(httpAuthorization, "bearer 7b412253-7519-4b1e-9b36-f34dca05554a");
//                    http.addRequestHeader("user", authorization);
//            }
//            if (params != null) {
//                http.setRequestBody(JSONObject.toJSONString(params));
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//
//    /**
//     * delete方法
//     */
//    public String delete(String subUrl, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            DeleteMethod http;
//            http = new DeleteMethod(subUrl);
//            if (params != null) {
//                for (Map.Entry entry : params.entrySet()) {
//                    String key = entry.getKey().toString();
//                    http.getParams().setParameter(key, params.get(key));
//                }
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//
//    }
//
//    /**
//     * delete方法
//     */
//    public String delete(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//
//        try {
//            HttpClient httpClient = new HttpClient();
//            DeleteMethod http;
//            http = new DeleteMethod(subUrl);
//            if (params != null) {
//                for (Map.Entry entry : params.entrySet()) {
//                    String key = entry.getKey().toString();
//                    http.getParams().setParameter(key, params.get(key));
//                }
//            }
//            if (contentType != null) {
//                http.addRequestHeader(httpContent, contentType);
//            }
//            if (authorization != null) {
//                http.addRequestHeader(httpAuthorization, authorization);
//                http.addRequestHeader("user", authorization);
//            }
//            httpClient.executeMethod(http);
//
//            //连接成功
//            if (200 == http.getStatusCode()) {
//                return http.getResponseBodyAsString();
//            } else {
//                return getErrorFromResult(http.getResponseBodyAsString());
//            }
//        } catch (IOException io) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//    }
//
//
//    public void deleteByBody(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, JSONObject.toJSONString(params));
//        Request request = new Request.Builder()
//                .url(subUrl)
//                .delete(body)
//                .addHeader("Content-Type", contentType)
//                .build();
//        Response response = null;
//        try {
//            response = client.newCall(request).execute();
//        } catch (IOException e) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//        if (response.code() != 200) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//    }
//
//    /**
//     * post方法
//     */
//    public void deleteWithBody(String subUrl, String contentType, String authorization, Map<String, Object> params) {
//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody body = RequestBody.create(mediaType, JSONObject.toJSONString(params));
//        Request request = new Request.Builder()
//                .url(subUrl)
//                .delete(body)
//                .addHeader("Authorization", authorization)
//                .addHeader("Content-Type", contentType)
//                .addHeader("cache-control", "no-cache")
//                .addHeader("Postman-Token", "324f08fe-1b46-4af3-a4db-ca0ba344d34e")
//                .build();
//        Response response = null;
//        try {
//            response = client.newCall(request).execute();
//        } catch (IOException e) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//        if (response.code() != 200) {
//            throw CommonException.of(HTTP_CONNECTION_ERROR);
//        }
//    }
//
//}
