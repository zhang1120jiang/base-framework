package com.unisinsight.sprite.common.utils;//package com.unisinsight.ic.commons.utils;
//
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Map;
//
///**
// * description json工具类
// *
// * @author zhangyuhe [zhang.yuhe@unisinsight.com]
// * @date 2019/04/25 14:54
// * @since 1.0
// */
//@Slf4j
//public class JsonUtils {
//    /**
//     * 工具类构造器私有化
//     */
//    private JsonUtils() {
//    }
//
//    /**
//     * 对象驼峰转下划线json
//     * @param obj 待转换对象
//     * @return 下划线json str
//     */
//    public static String obj2UnderlineStr(Object obj) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//        try {
//            return mapper.writeValueAsString(obj);
//        } catch (JsonProcessingException e) {
//            log.info("驼峰转下划线json转换错误:[{}]" + e.getStackTrace());
//        }
//        return null;
//    }
//
//    /**
//     * 对象转换为key值为下划线的map
//     * @param obj 待转换对象
//     * @return 下划线map
//     */
//    public static Map obj2UnderlineMap(Object obj) {
//        String obj2UnderlineStr = obj2UnderlineStr(obj);
//        JSONObject jsonObject = JSONObject.parseObject(obj2UnderlineStr);
//        Map<Object, Object> map = (Map)jsonObject;
//        return map;
//    }
//}
