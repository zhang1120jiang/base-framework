package com.unisinsight.sprite.common.utils;//package com.unisinsight.ic.commons.utils;
//
///**
// * 坐标转换工具类，目前用于转换设备、抓拍、告警的坐标转换
// *
// * @author: qiuweiwu [qiu.weiwu@unisinsight.com]
// * @date: 2019/3/14 09:19
// */
//public final class GisUtil {
//    /**
//     * 构造器私有化
//     */
//    private GisUtil() {
//
//    }
//
//    /**
//     * 圆周率
//     */
//    private static final double PI = 3.1415926535897932384626;
//    /**
//     * Krasovsky 1940 (北京54)椭球长半轴 卫星椭球坐标投影到平面地图坐标系的投影因子
//     */
//    private static final double ELLIPSOID_PARAM_A = 6378245.0;
//    /**
//     * Krasovsky 1940 (北京54)椭球长半轴第一偏心率平方
//     * 计算方式：
//     * 长半轴：
//     * ELLIPSOID_PARAM_A = 6378245.0
//     * 扁率：
//     * 1/f = 298.3（变量相关计算为：(ELLIPSOID_PARAM_A-b)/ELLIPSOID_PARAM_A）
//     * 短半轴：
//     * b = 6356863.0188 (变量相关计算方法为：b = ELLIPSOID_PARAM_A * (1 - f))
//     * 第一偏心率平方:
//     * e2 = (ELLIPSOID_PARAM_A^2 - b^2) / ELLIPSOID_PARAM_A^2;
//     */
//    private static final double EE = 0.00669342162296594323;
//
//
//
//    /**
//     * 84坐标系纬度数据转换
//     *
//     * @param lat 纬度
//     * @param lon 经度
//     * @return double 转换后的纬度
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/5/9 13:56
//     */
//    public static double wGC84ToGCJ02ForLat(double lat, double lon) {
//        double dLat = transformLat(lon - 105.0, lat - 35.0);
//        double radLat = lat / 180.0 * PI;
//        double magic = Math.sin(radLat);
//        magic = 1 - EE * magic * magic;
//        double sqrtMagic = Math.sqrt(magic);
//        dLat = (dLat * 180.0) / ((ELLIPSOID_PARAM_A * (1 - EE)) / (magic * sqrtMagic) * PI);
//        return lat + dLat;
//    }
//    /**
//     * 84坐标系经度数据转换
//     *
//     * @param lat 纬度
//     * @param lon 经度
//     * @return double 转换后的经度
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/5/9 13:56
//     */
//    public static double wGC84ToGCJ02ForLon(double lat, double lon) {
//        double dLon = transformLon(lon - 105.0, lat - 35.0);
//        double radLat = lat / 180.0 * PI;
//        double magic = Math.sin(radLat);
//        magic = 1 - EE * magic * magic;
//        double sqrtMagic = Math.sqrt(magic);
//        dLon = (dLon * 180.0) / (ELLIPSOID_PARAM_A / sqrtMagic * Math.cos(radLat) * PI);
//        return lon + dLon;
//    }
//    /**
//     * 获取纬度
//     *
//     * @param x 经度
//     * @param y 纬度
//     * @return double 转换后的纬度
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/5/9 13:56
//     */
//    private static double transformLat(double x, double y) {
//        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
//        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
//        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
//        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
//        return ret;
//    }
//    /**
//     * 获取经度
//     *
//     * @param x 经度
//     * @param y 纬度
//     * @return double 转换后的经度
//     * @author qiuweiwu [qiuweiwu@unisinsight.com]
//     * @date 2019/5/9 13:56
//     */
//    private static double transformLon(double x, double y) {
//        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
//        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
//        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
//        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
//        return ret;
//    }
//
//}
