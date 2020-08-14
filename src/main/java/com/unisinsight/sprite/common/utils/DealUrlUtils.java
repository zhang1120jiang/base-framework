package com.unisinsight.sprite.common.utils;//package com.unisinsight.ic.commons.utils;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//import com.unisinsight.ic.commons.config.BaseUrlProperties;
//import com.unisinsight.ic.commons.enums.BaseUrlEnum;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.util.CollectionUtils;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.stream.IntStream;
//
///**
// * 功能描述: 地址操作类
// *
// * @author qiuweiwu [qiuweiwu@unisinsight.com]
// * @date 2019/9/9 11:34
// */
//@Slf4j
//public class DealUrlUtils {
//    /**
//     * 构造器私有化
//     */
//    private DealUrlUtils(){
//
//    }
//
//    /**
//     * redis 工具
//     */
//    private static BaseUrlProperties baseUrl =
//            ApplicationContextUtil.getBean(BaseUrlProperties.class);
//    /**
//     * 地址开始部分的正则表达式
//     */
//    private static String urlStartPartten =
//            "(http://)((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}(:)([1-9]\\d*)(/\\?)*";
//
//    /**
//     * 相对路径转绝对路径，单次访问云存储传入的最大地址数量
//     */
//    private static final int MAX_CLOUD_IMAGE_DEAL_LENGTH = 100;
//
//    /**
//     * http协议头
//     */
//    private static final String HTTP_PROXY = "http://";
//
//    /**
//     * 功能描述: 通过文件的相对地址获取绝对地址
//     *
//     * @param relativeAddr 文件的相对地址
//     * @param baseUrlEnum 需要返回的类型
//     * @return  {@link String}
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/9/9 10:19
//     */
//    public static String getAbsoluteFromRelative(String relativeAddr, BaseUrlEnum baseUrlEnum) {
//        if (StringUtils.isEmpty(relativeAddr)) {
//            log.warn("传入的相对地址为空: {}", relativeAddr);
//            return relativeAddr;
//        }
//        if (relativeAddr.startsWith(HTTP_PROXY)) {
//            log.warn("请勿在通过相对地址的接口传入绝对地址，地址：{}", relativeAddr);
//            return relativeAddr;
//        }
//        if (baseUrlEnum == BaseUrlEnum.CLOUD_URL) {
//            Map<String, String> result = convert2Absolute(new String[]{relativeAddr});
//            return result == null ? null : result.get(relativeAddr);
//        }
//        return baseUrl.getNginxUrl() + relativeAddr;
//    }
//
//    /**
//     * 功能描述: 通过文件的地址（不知是相对地址还是绝对地址）获取绝对地址
//     *
//     * @param addr 文件的地址（不知是相对地址还是绝对地址）
//     * @param baseUrlEnum 需要返回的类型
//     * @return  {@link String}
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/9/9 10:19
//     */
//    public static String getAbsoluteUrl(String addr, BaseUrlEnum baseUrlEnum) {
//        if (StringUtils.isEmpty(addr)) {
//            log.warn("传入的相对地址为空: {}", addr);
//            return addr;
//        }
//        if (addr.startsWith(HTTP_PROXY)) {
//            String relativeAddr = addr.replaceAll(urlStartPartten, "");
//            if (baseUrlEnum == BaseUrlEnum.CLOUD_URL) {
//                return getAbsoluteFromRelative(relativeAddr, BaseUrlEnum.CLOUD_URL);
//            }
//            return baseUrl.getNginxUrl() + relativeAddr;
//        }
//        if (baseUrlEnum == BaseUrlEnum.CLOUD_URL) {
//            return getAbsoluteFromRelative(addr, BaseUrlEnum.CLOUD_URL);
//        }
//        return baseUrl.getNginxUrl() + addr;
//    }
//    /**
//     * 功能描述: 通过绝对地址获取相对地址
//     *
//     * @param addr 文件的绝对地址
//     * @return  {@link String}
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/9/9 10:19
//     */
//    public static String getRelativeUrl(String addr) {
//        if (StringUtils.isEmpty(addr)) {
//            log.warn("传入的绝对地址为空: {}", addr);
//            return addr;
//        }
//        return addr.replaceAll(urlStartPartten, "");
//    }
//
//    public static String getAbsoluteUrlWithSeparator(String addr, BaseUrlEnum baseUrlEnum, String separator) {
//        if (StringUtils.isEmpty(addr)) {
//            log.warn("传入的绝对地址为空: {}", addr);
//            return addr;
//        }
//        StringBuilder result = new StringBuilder();
//        addr = addr.replaceAll(urlStartPartten, "");
//        String[] addrs = addr.split(StringUtils.escapeExprSpecialWord(separator));
//        int length = addrs.length;
//        // 如果地址为NGINX地址
//        if (baseUrlEnum == BaseUrlEnum.NGINX_URL) {
//            if (length  == 1){
//                result.append(getAbsoluteFromRelative(addrs[0], BaseUrlEnum.NGINX_URL));
//            } else {
//                result.append(getAbsoluteUrl(addrs[0], BaseUrlEnum.NGINX_URL));
//                IntStream.range(1, length).forEachOrdered(i -> result.append(separator)
//                        .append(getAbsoluteFromRelative(addrs[i], BaseUrlEnum.NGINX_URL)));
//            }
//        } else {
//            // 如果地址为云存储地址需要进行转换
//            if (length == 1) {
//                result.append(getAbsoluteFromRelative(addrs[0], BaseUrlEnum.CLOUD_URL));
//            } else {
//                Map<String, String> absoluteAddrMap = convert2Absolute(addrs);
//                if (absoluteAddrMap == null) {
//                    log.error("云存储地址获取失败！{}", addr);
//                    return addr;
//                }
//                for (String address: addrs) {
//                    result.append(absoluteAddrMap.get(getRelativeUrl(address))).append(separator);
//                }
//                // 循环处理后，直接返回，并剔除最后的分隔符
//                result.deleteCharAt(result.length() - separator.length());
//            }
//
//        }
//        return result.toString();
//    }
//
//    /**
//     * 功能描述: 云存储地址转换，数组长度不要超过100
//     *
//     * @param strings 地址数组
//     * @return  {@link TreeMap<String, String>}
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/9/11 13:41
//     */
//    public static Map<String, String> convert2Absolute(String[] strings) {
//        if (strings == null ||strings.length == 0) {
//            log.warn("传入的列表为空");
//            return new TreeMap<>();
//        }
//        String url = baseUrl.getIp() + baseUrl.getReqUrl();
//        StringBuilder sb = new StringBuilder();
//        sb.append(url);
//        for (String s : strings) {
//            // 去除绝对地址的部分
//            if (StringUtils.isNotEmpty(s) && !"null".equals(s)) {
//                sb.append(s.replaceAll(urlStartPartten, "")).append("&");
//            }
//        }
//        sb.deleteCharAt(sb.length() - 1);
//        MHttpClient mHttpClient = new MHttpClient();
//        String resString;
//        try {
//            resString = mHttpClient.get(sb.toString());
//        } catch (Exception e) {
//            log.error("相对路径从云存储请求绝对路径失败[{}]", url);
//            return null;
//        }
//        return JSON.parseObject(resString, new TypeReference<Map<String, String>>(){});
//    }
//
//
//    /**
//     * 功能描述: 云存储地址转换
//     *
//     * @param imgList 地址列表
//     * @return  {@link TreeMap<String, String>}
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/9/11 13:41
//     */
//    public static Map<String, String> convert2Absolute(List<String> imgList) {
//        if (CollectionUtils.isEmpty(imgList)) {
//            log.warn("传入的列表为空");
//            return new TreeMap<>();
//        }
//        Map<String, String> result = null;
//        List<List<String>> imageListArray = new ArrayList<>();
//        if (imgList.size() > MAX_CLOUD_IMAGE_DEAL_LENGTH) {
//            //若图片列表过大，超过100，需要进行分割
//            imageListArray = DealUrlUtils.splitList(imgList, MAX_CLOUD_IMAGE_DEAL_LENGTH);
//        } else {
//            imageListArray.add(imgList);
//        }
//        for (List<String> imageArray : imageListArray) {
//            String[] images = imageArray.toArray(new String[0]);
//            // 分别处理
//            if (result == null) {
//                result = DealUrlUtils.convert2Absolute(images);
//            } else {
//                Map<String, String> sortedMap = convert2Absolute(images);
//                if (sortedMap != null) {
//                    result.putAll(sortedMap);
//                }
//            }
//        }
//        return result;
//    }
//
//    /**
//     * 按指定大小，分隔集合，将集合按规定个数分为n个部分
//     * @param <T> 泛型
//     *
//     * @param list 需要分割的字符串
//     * @param len 每个集合的个数
//     * @return {@link List<List<T>>}
//     */
//    public static <T> List<List<T>> splitList(List<T> list, int len) {
//        if (list == null || list.isEmpty() || len < 1) {
//            return Collections.emptyList();
//        }
//        List<List<T>> result = new ArrayList<>();
//        int size = list.size();
//        int count = (size + len - 1) / len;
//        for (int i = 0; i < count; i++) {
//            List<T> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
//            result.add(subList);
//        }
//        return result;
//    }
//}
//
