package com.unisinsight.sprite.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.unisinsight.sprite.common.enums.BaseResultCode;
import com.unisinsight.sprite.common.exception.CommonException;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 * 身份证号码验证
 * 　　1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，八位数字出生日期码，三位数字顺序码和一位数字校验码
 * 　　2、地址码(前六位数）表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行
 * 　　3、出生日期码（第七位至十四位）表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符
 * 　　4、顺序码（第十五位至十七位）表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 顺序码的奇数分配给男性，偶数分配给女性
 * 　　5、校验码（第十八位数）
 * 　　（1）十七位数字本体码加权求和公式 S = Sum(iDCardNo * wf), i = 0, ... , 16 ，先对前17位数字的权求和 iDCardNo:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 wf: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2
 * 　　（2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9 8 7 6 5 4 3 2
 */
public class IDCardUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static String[] wf = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    private static String[] checkCode = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};

    /**
     * 身份证验证
     */
    public static PersonInfo verification(String idStr) {
        String iDCardNo = "";
        //判断号码的长度 15位或18位
        if (!idStr.matches("\\d{15}(\\d{2}[0-9,X])?")) {
            throw CommonException.of(BaseResultCode.ID_NUMBER_ERROR, "身份证号码格式不对");
        }
        if (idStr.length() == 18) {
            iDCardNo = idStr.substring(0, 17);
        } else if (idStr.length() == 15) {
            iDCardNo = idStr.substring(0, 6) + "19" + idStr.substring(6, 15);
        }

        String birth = iDCardNo.substring(6, 14);
        PersonInfo personInfo = new PersonInfo();
        try {
            Date birthDay = sdf.parse(birth);
            if (!birth.equals(sdf.format(birthDay))) {
                throw CommonException.of(BaseResultCode.ID_NUMBER_ERROR, "身份证日期格式不对");
            }
            //判断出生年月
            String strYear = iDCardNo.substring(6, 10);// 年份

            Calendar calendar = Calendar.getInstance();
            int age = calendar.get(Calendar.YEAR) - Integer.parseInt(strYear);
            if (age > 150 || calendar.getTime().getTime() - birthDay.getTime() < 0) {
                throw CommonException.of(BaseResultCode.ID_NUMBER_ERROR, "身份证生日不在有效范围");
            }

            personInfo.setBirth(dateFormat.format(birthDay));
            personInfo.setBirthDay(birthDay);
            personInfo.setAge(age);
        } catch (ParseException e) {
            e.printStackTrace();
            throw CommonException.of(BaseResultCode.ID_NUMBER_ERROR, "身份证日期格式不对");
        }


        //判断地区码
        Hashtable<String, String> h = GetAreaCode();
        String proviceName = h.get(iDCardNo.substring(0, 2));
        if (proviceName == null) {
            throw CommonException.of(BaseResultCode.ID_NUMBER_ERROR, "身份证地区编码错误");
        }

        //判断最后一位
        if (idStr.length() == 18) {
            int theLastOne = 0;
            for (int i = 0; i < 17; i++) {
                theLastOne = theLastOne + Integer.parseInt(String.valueOf(iDCardNo.charAt(i))) * Integer.parseInt(checkCode[i]);
            }
            int modValue = theLastOne % 11;
            String strVerifyCode = wf[modValue];
            iDCardNo = iDCardNo + strVerifyCode;
            if (!iDCardNo.equals(idStr)) {
                throw CommonException.of(BaseResultCode.ID_NUMBER_ERROR, "身份证无效，不是合法的身份证号码");
            }
        }

        personInfo.setNativePlace(proviceName);
        personInfo.setIdNumber(idStr);
        personInfo.setGenderAndCode(iDCardNo.charAt(16) % 2);
        return personInfo;
    }

    @Data
    public static class PersonInfo {
        //身份证号码
        String IdNumber;
        //出生日期
        String birth;
        Date birthDay;
        //奇男/偶女
        String gender;
        int genderCode;
        //籍贯
        String nativePlace;
        //年龄
        int age;

        public void setGenderAndCode(int genderCode) {
            this.genderCode = genderCode;
            gender = genderCode == 1 ? "男" : "女";
        }

        public String toString() {
            return JSON.toJSONString(this, SerializerFeature.WriteDateUseDateFormat);
        }
    }

    /**
     * 地区代码
     *
     * @return Hashtable
     */
    private static Hashtable<String, String> GetAreaCode() {
        Hashtable<String, String> hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    public static String getNativePlace(int nativePlaceCode) {
        int shengCode = nativePlaceCode / 10000;
        int shiCode = nativePlaceCode / 100;
        int quxianCode = nativePlaceCode;
        String sheng = getNameString(shengCode);
        String shi = getNameString(shiCode);
        String quxian = getNameString(quxianCode);
        String nativePlace = "";
        if (sheng != null) {
            nativePlace += sheng;
        }
        if (shi != null) {
            nativePlace += shi;
        }
        if (quxian != null) {
            nativePlace += quxian;
        }
        return nativePlace;
    }

    /**
     * 里面有一些错误信息，暂不使用
     *
     * @param code
     * @return
     */
    private static String getNameString(int code) {
        switch (code) {
            case 11:
                return "北京市";
            case 1101:
                return "市辖区";
            case 110101:
                return "东城区";
            case 110102:
                return "西城区";
            case 110105:
                return "朝阳区";
            case 110106:
                return "丰台区";
            case 110107:
                return "石景山区";
            case 110108:
                return "海淀区";
            case 110109:
                return "门头沟区";
            case 110111:
                return "房山区";
            case 110112:
                return "通州区";
            case 110113:
                return "顺义区";
            case 110114:
                return "昌平区";
            case 110115:
                return "大兴区";
            case 110116:
                return "怀柔区";
            case 110117:
                return "平谷区";
            case 1102:
                return "县";
            case 110228:
                return "密云县";
            case 110229:
                return "延庆县";
            case 12:
                return "天津市";
            case 1201:
                return "市辖区";
            case 120101:
                return "和平区";
            case 120102:
                return "河东区";
            case 120103:
                return "河西区";
            case 120104:
                return "南开区";
            case 120105:
                return "河北区";
            case 120106:
                return "红桥区";
            case 120110:
                return "东丽区";
            case 120111:
                return "西青区";
            case 120112:
                return "津南区";
            case 120113:
                return "北辰区";
            case 120114:
                return "武清区";
            case 120115:
                return "宝坻区";
            case 120116:
                return "滨海新区";
            case 1202:
                return "县";
            case 120221:
                return "宁河县";
            case 120223:
                return "静海县";
            case 120225:
                return "蓟县";
            case 13:
                return "河北省";
            case 1301:
                return "石家庄市";
            case 130101:
                return "市辖区";
            case 130102:
                return "长安区";
            case 130104:
                return "桥西区";
            case 130105:
                return "新华区";
            case 130107:
                return "井陉矿区";
            case 130108:
                return "裕华区";
            case 130109:
                return "藁城区";
            case 130110:
                return "鹿泉区";
            case 130111:
                return "栾城区";
            case 130121:
                return "井陉县";
            case 130123:
                return "正定县";
            case 130125:
                return "行唐县";
            case 130126:
                return "灵寿县";
            case 130127:
                return "高邑县";
            case 130128:
                return "深泽县";
            case 130129:
                return "赞皇县";
            case 130130:
                return "无极县";
            case 130131:
                return "平山县";
            case 130132:
                return "元氏县";
            case 130133:
                return "赵县";
            case 130183:
                return "晋州市";
            case 130184:
                return "新乐市";
            case 1302:
                return "唐山市";
            case 130201:
                return "市辖区";
            case 130202:
                return "路南区";
            case 130203:
                return "路北区";
            case 130204:
                return "古冶区";
            case 130205:
                return "开平区";
            case 130207:
                return "丰南区";
            case 130208:
                return "丰润区";
            case 130209:
                return "曹妃甸区";
            case 130223:
                return "滦县";
            case 130224:
                return "滦南县";
            case 130225:
                return "乐亭县";
            case 130227:
                return "迁西县";
            case 130229:
                return "玉田县";
            case 130281:
                return "遵化市";
            case 130283:
                return "迁安市";
            case 1303:
                return "秦皇岛市";
            case 130301:
                return "市辖区";
            case 130302:
                return "海港区";
            case 130303:
                return "山海关区";
            case 130304:
                return "北戴河区";
            case 130321:
                return "青龙满族自治县";
            case 130322:
                return "昌黎县";
            case 130323:
                return "抚宁县";
            case 130324:
                return "卢龙县";
            case 1304:
                return "邯郸市";
            case 130401:
                return "市辖区";
            case 130402:
                return "邯山区";
            case 130403:
                return "丛台区";
            case 130404:
                return "复兴区";
            case 130406:
                return "峰峰矿区";
            case 130421:
                return "邯郸县";
            case 130423:
                return "临漳县";
            case 130424:
                return "成安县";
            case 130425:
                return "大名县";
            case 130426:
                return "涉县";
            case 130427:
                return "磁县";
            case 130428:
                return "肥乡县";
            case 130429:
                return "永年县";
            case 130430:
                return "邱县";
            case 130431:
                return "鸡泽县";
            case 130432:
                return "广平县";
            case 130433:
                return "馆陶县";
            case 130434:
                return "魏县";
            case 130435:
                return "曲周县";
            case 130481:
                return "武安市";
            case 1305:
                return "邢台市";
            case 130501:
                return "市辖区";
            case 130502:
                return "桥东区";
            case 130503:
                return "桥西区";
            case 130521:
                return "邢台县";
            case 130522:
                return "临城县";
            case 130523:
                return "内丘县";
            case 130524:
                return "柏乡县";
            case 130525:
                return "隆尧县";
            case 130526:
                return "任县";
            case 130527:
                return "南和县";
            case 130528:
                return "宁晋县";
            case 130529:
                return "巨鹿县";
            case 130530:
                return "新河县";
            case 130531:
                return "广宗县";
            case 130532:
                return "平乡县";
            case 130533:
                return "威县";
            case 130534:
                return "清河县";
            case 130535:
                return "临西县";
            case 130581:
                return "南宫市";
            case 130582:
                return "沙河市";
            case 1306:
                return "保定市";
            case 130601:
                return "市辖区";
            case 130602:
                return "新市区";
            case 130603:
                return "北市区";
            case 130604:
                return "南市区";
            case 130621:
                return "满城县";
            case 130622:
                return "清苑县";
            case 130623:
                return "涞水县";
            case 130624:
                return "阜平县";
            case 130625:
                return "徐水县";
            case 130626:
                return "定兴县";
            case 130627:
                return "唐县";
            case 130628:
                return "高阳县";
            case 130629:
                return "容城县";
            case 130630:
                return "涞源县";
            case 130631:
                return "望都县";
            case 130632:
                return "安新县";
            case 130633:
                return "易县";
            case 130634:
                return "曲阳县";
            case 130635:
                return "蠡县";
            case 130636:
                return "顺平县";
            case 130637:
                return "博野县";
            case 130638:
                return "雄县";
            case 130681:
                return "涿州市";
            case 130683:
                return "安国市";
            case 130684:
                return "高碑店市";
            case 1307:
                return "张家口市";
            case 130701:
                return "市辖区";
            case 130702:
                return "桥东区";
            case 130703:
                return "桥西区";
            case 130705:
                return "宣化区";
            case 130706:
                return "下花园区";
            case 130721:
                return "宣化县";
            case 130722:
                return "张北县";
            case 130723:
                return "康保县";
            case 130724:
                return "沽源县";
            case 130725:
                return "尚义县";
            case 130726:
                return "蔚县";
            case 130727:
                return "阳原县";
            case 130728:
                return "怀安县";
            case 130729:
                return "万全县";
            case 130730:
                return "怀来县";
            case 130731:
                return "涿鹿县";
            case 130732:
                return "赤城县";
            case 130733:
                return "崇礼县";
            case 1308:
                return "承德市";
            case 130801:
                return "市辖区";
            case 130802:
                return "双桥区";
            case 130803:
                return "双滦区";
            case 130804:
                return "鹰手营子矿区";
            case 130821:
                return "承德县";
            case 130822:
                return "兴隆县";
            case 130823:
                return "平泉县";
            case 130824:
                return "滦平县";
            case 130825:
                return "隆化县";
            case 130826:
                return "丰宁满族自治县";
            case 130827:
                return "宽城满族自治县";
            case 130828:
                return "围场满族蒙古族自治县";
            case 1309:
                return "沧州市";
            case 130901:
                return "市辖区";
            case 130902:
                return "新华区";
            case 130903:
                return "运河区";
            case 130921:
                return "沧县";
            case 130922:
                return "青县";
            case 130923:
                return "东光县";
            case 130924:
                return "海兴县";
            case 130925:
                return "盐山县";
            case 130926:
                return "肃宁县";
            case 130927:
                return "南皮县";
            case 130928:
                return "吴桥县";
            case 130929:
                return "献县";
            case 130930:
                return "孟村回族自治县";
            case 130981:
                return "泊头市";
            case 130982:
                return "任丘市";
            case 130983:
                return "黄骅市";
            case 130984:
                return "河间市";
            case 1310:
                return "廊坊市";
            case 131001:
                return "市辖区";
            case 131002:
                return "安次区";
            case 131003:
                return "广阳区";
            case 131022:
                return "固安县";
            case 131023:
                return "永清县";
            case 131024:
                return "香河县";
            case 131025:
                return "大城县";
            case 131026:
                return "文安县";
            case 131028:
                return "大厂回族自治县";
            case 131081:
                return "霸州市";
            case 131082:
                return "三河市";
            case 1311:
                return "衡水市";
            case 131101:
                return "市辖区";
            case 131102:
                return "桃城区";
            case 131121:
                return "枣强县";
            case 131122:
                return "武邑县";
            case 131123:
                return "武强县";
            case 131124:
                return "饶阳县";
            case 131125:
                return "安平县";
            case 131126:
                return "故城县";
            case 131127:
                return "景县";
            case 131128:
                return "阜城县";
            case 131181:
                return "冀州市";
            case 131182:
                return "深州市";
            case 1390:
                return "省直辖县级行政区划";
            case 139001:
                return "定州市";
            case 139002:
                return "辛集市";
            case 14:
                return "山西省";
            case 1401:
                return "太原市";
            case 140101:
                return "市辖区";
            case 140105:
                return "小店区";
            case 140106:
                return "迎泽区";
            case 140107:
                return "杏花岭区";
            case 140108:
                return "尖草坪区";
            case 140109:
                return "万柏林区";
            case 140110:
                return "晋源区";
            case 140121:
                return "清徐县";
            case 140122:
                return "阳曲县";
            case 140123:
                return "娄烦县";
            case 140181:
                return "古交市";
            case 1402:
                return "大同市";
            case 140201:
                return "市辖区";
            case 140202:
                return "城区";
            case 140203:
                return "矿区";
            case 140211:
                return "南郊区";
            case 140212:
                return "新荣区";
            case 140221:
                return "阳高县";
            case 140222:
                return "天镇县";
            case 140223:
                return "广灵县";
            case 140224:
                return "灵丘县";
            case 140225:
                return "浑源县";
            case 140226:
                return "左云县";
            case 140227:
                return "大同县";
            case 1403:
                return "阳泉市";
            case 140301:
                return "市辖区";
            case 140302:
                return "城区";
            case 140303:
                return "矿区";
            case 140311:
                return "郊区";
            case 140321:
                return "平定县";
            case 140322:
                return "盂县";
            case 1404:
                return "长治市";
            case 140401:
                return "市辖区";
            case 140402:
                return "城区";
            case 140411:
                return "郊区";
            case 140421:
                return "长治县";
            case 140423:
                return "襄垣县";
            case 140424:
                return "屯留县";
            case 140425:
                return "平顺县";
            case 140426:
                return "黎城县";
            case 140427:
                return "壶关县";
            case 140428:
                return "长子县";
            case 140429:
                return "武乡县";
            case 140430:
                return "沁县";
            case 140431:
                return "沁源县";
            case 140481:
                return "潞城市";
            case 1405:
                return "晋城市";
            case 140501:
                return "市辖区";
            case 140502:
                return "城区";
            case 140521:
                return "沁水县";
            case 140522:
                return "阳城县";
            case 140524:
                return "陵川县";
            case 140525:
                return "泽州县";
            case 140581:
                return "高平市";
            case 1406:
                return "朔州市";
            case 140601:
                return "市辖区";
            case 140602:
                return "朔城区";
            case 140603:
                return "平鲁区";
            case 140621:
                return "山阴县";
            case 140622:
                return "应县";
            case 140623:
                return "右玉县";
            case 140624:
                return "怀仁县";
            case 1407:
                return "晋中市";
            case 140701:
                return "市辖区";
            case 140702:
                return "榆次区";
            case 140721:
                return "榆社县";
            case 140722:
                return "左权县";
            case 140723:
                return "和顺县";
            case 140724:
                return "昔阳县";
            case 140725:
                return "寿阳县";
            case 140726:
                return "太谷县";
            case 140727:
                return "祁县";
            case 140728:
                return "平遥县";
            case 140729:
                return "灵石县";
            case 140781:
                return "介休市";
            case 1408:
                return "运城市";
            case 140801:
                return "市辖区";
            case 140802:
                return "盐湖区";
            case 140821:
                return "临猗县";
            case 140822:
                return "万荣县";
            case 140823:
                return "闻喜县";
            case 140824:
                return "稷山县";
            case 140825:
                return "新绛县";
            case 140826:
                return "绛县";
            case 140827:
                return "垣曲县";
            case 140828:
                return "夏县";
            case 140829:
                return "平陆县";
            case 140830:
                return "芮城县";
            case 140881:
                return "永济市";
            case 140882:
                return "河津市";
            case 1409:
                return "忻州市";
            case 140901:
                return "市辖区";
            case 140902:
                return "忻府区";
            case 140921:
                return "定襄县";
            case 140922:
                return "五台县";
            case 140923:
                return "代县";
            case 140924:
                return "繁峙县";
            case 140925:
                return "宁武县";
            case 140926:
                return "静乐县";
            case 140927:
                return "神池县";
            case 140928:
                return "五寨县";
            case 140929:
                return "岢岚县";
            case 140930:
                return "河曲县";
            case 140931:
                return "保德县";
            case 140932:
                return "偏关县";
            case 140981:
                return "原平市";
            case 1410:
                return "临汾市";
            case 141001:
                return "市辖区";
            case 141002:
                return "尧都区";
            case 141021:
                return "曲沃县";
            case 141022:
                return "翼城县";
            case 141023:
                return "襄汾县";
            case 141024:
                return "洪洞县";
            case 141025:
                return "古县";
            case 141026:
                return "安泽县";
            case 141027:
                return "浮山县";
            case 141028:
                return "吉县";
            case 141029:
                return "乡宁县";
            case 141030:
                return "大宁县";
            case 141031:
                return "隰县";
            case 141032:
                return "永和县";
            case 141033:
                return "蒲县";
            case 141034:
                return "汾西县";
            case 141081:
                return "侯马市";
            case 141082:
                return "霍州市";
            case 1411:
                return "吕梁市";
            case 141101:
                return "市辖区";
            case 141102:
                return "离石区";
            case 141121:
                return "文水县";
            case 141122:
                return "交城县";
            case 141123:
                return "兴县";
            case 141124:
                return "临县";
            case 141125:
                return "柳林县";
            case 141126:
                return "石楼县";
            case 141127:
                return "岚县";
            case 141128:
                return "方山县";
            case 141129:
                return "中阳县";
            case 141130:
                return "交口县";
            case 141181:
                return "孝义市";
            case 141182:
                return "汾阳市";
            case 15:
                return "内蒙古自治区";
            case 1501:
                return "呼和浩特市";
            case 150101:
                return "市辖区";
            case 150102:
                return "新城区";
            case 150103:
                return "回民区";
            case 150104:
                return "玉泉区";
            case 150105:
                return "赛罕区";
            case 150121:
                return "土默特左旗";
            case 150122:
                return "托克托县";
            case 150123:
                return "和林格尔县";
            case 150124:
                return "清水河县";
            case 150125:
                return "武川县";
            case 1502:
                return "包头市";
            case 150201:
                return "市辖区";
            case 150202:
                return "东河区";
            case 150203:
                return "昆都仑区";
            case 150204:
                return "青山区";
            case 150205:
                return "石拐区";
            case 150206:
                return "白云鄂博矿区";
            case 150207:
                return "九原区";
            case 150221:
                return "土默特右旗";
            case 150222:
                return "固阳县";
            case 150223:
                return "达尔罕茂明安联合旗";
            case 1503:
                return "乌海市";
            case 150301:
                return "市辖区";
            case 150302:
                return "海勃湾区";
            case 150303:
                return "海南区";
            case 150304:
                return "乌达区";
            case 1504:
                return "赤峰市";
            case 150401:
                return "市辖区";
            case 150402:
                return "红山区";
            case 150403:
                return "元宝山区";
            case 150404:
                return "松山区";
            case 150421:
                return "阿鲁科尔沁旗";
            case 150422:
                return "巴林左旗";
            case 150423:
                return "巴林右旗";
            case 150424:
                return "林西县";
            case 150425:
                return "克什克腾旗";
            case 150426:
                return "翁牛特旗";
            case 150428:
                return "喀喇沁旗";
            case 150429:
                return "宁城县";
            case 150430:
                return "敖汉旗";
            case 1505:
                return "通辽市";
            case 150501:
                return "市辖区";
            case 150502:
                return "科尔沁区";
            case 150521:
                return "科尔沁左翼中旗";
            case 150522:
                return "科尔沁左翼后旗";
            case 150523:
                return "开鲁县";
            case 150524:
                return "库伦旗";
            case 150525:
                return "奈曼旗";
            case 150526:
                return "扎鲁特旗";
            case 150581:
                return "霍林郭勒市";
            case 1506:
                return "鄂尔多斯市";
            case 150601:
                return "市辖区";
            case 150602:
                return "东胜区";
            case 150621:
                return "达拉特旗";
            case 150622:
                return "准格尔旗";
            case 150623:
                return "鄂托克前旗";
            case 150624:
                return "鄂托克旗";
            case 150625:
                return "杭锦旗";
            case 150626:
                return "乌审旗";
            case 150627:
                return "伊金霍洛旗";
            case 1507:
                return "呼伦贝尔市";
            case 150701:
                return "市辖区";
            case 150702:
                return "海拉尔区";
            case 150703:
                return "扎赉诺尔区";
            case 150721:
                return "阿荣旗";
            case 150722:
                return "莫力达瓦达斡尔族自治旗";
            case 150723:
                return "鄂伦春自治旗";
            case 150724:
                return "鄂温克族自治旗";
            case 150725:
                return "陈巴尔虎旗";
            case 150726:
                return "新巴尔虎左旗";
            case 150727:
                return "新巴尔虎右旗";
            case 150781:
                return "满洲里市";
            case 150782:
                return "牙克石市";
            case 150783:
                return "扎兰屯市";
            case 150784:
                return "额尔古纳市";
            case 150785:
                return "根河市";
            case 1508:
                return "巴彦淖尔市";
            case 150801:
                return "市辖区";
            case 150802:
                return "临河区";
            case 150821:
                return "五原县";
            case 150822:
                return "磴口县";
            case 150823:
                return "乌拉特前旗";
            case 150824:
                return "乌拉特中旗";
            case 150825:
                return "乌拉特后旗";
            case 150826:
                return "杭锦后旗";
            case 1509:
                return "乌兰察布市";
            case 150901:
                return "市辖区";
            case 150902:
                return "集宁区";
            case 150921:
                return "卓资县";
            case 150922:
                return "化德县";
            case 150923:
                return "商都县";
            case 150924:
                return "兴和县";
            case 150925:
                return "凉城县";
            case 150926:
                return "察哈尔右翼前旗";
            case 150927:
                return "察哈尔右翼中旗";
            case 150928:
                return "察哈尔右翼后旗";
            case 150929:
                return "四子王旗";
            case 150981:
                return "丰镇市";
            case 1522:
                return "兴安盟";
            case 152201:
                return "乌兰浩特市";
            case 152202:
                return "阿尔山市";
            case 152221:
                return "科尔沁右翼前旗";
            case 152222:
                return "科尔沁右翼中旗";
            case 152223:
                return "扎赉特旗";
            case 152224:
                return "突泉县";
            case 1525:
                return "锡林郭勒盟";
            case 152501:
                return "二连浩特市";
            case 152502:
                return "锡林浩特市";
            case 152522:
                return "阿巴嘎旗";
            case 152523:
                return "苏尼特左旗";
            case 152524:
                return "苏尼特右旗";
            case 152525:
                return "东乌珠穆沁旗";
            case 152526:
                return "西乌珠穆沁旗";
            case 152527:
                return "太仆寺旗";
            case 152528:
                return "镶黄旗";
            case 152529:
                return "正镶白旗";
            case 152530:
                return "正蓝旗";
            case 152531:
                return "多伦县";
            case 1529:
                return "阿拉善盟";
            case 152921:
                return "阿拉善左旗";
            case 152922:
                return "阿拉善右旗";
            case 152923:
                return "额济纳旗";
            case 21:
                return "辽宁省";
            case 2101:
                return "沈阳市";
            case 210101:
                return "市辖区";
            case 210102:
                return "和平区";
            case 210103:
                return "沈河区";
            case 210104:
                return "大东区";
            case 210105:
                return "皇姑区";
            case 210106:
                return "铁西区";
            case 210111:
                return "苏家屯区";
            case 210112:
                return "浑南区";
            case 210113:
                return "沈北新区";
            case 210114:
                return "于洪区";
            case 210122:
                return "辽中县";
            case 210123:
                return "康平县";
            case 210124:
                return "法库县";
            case 210181:
                return "新民市";
            case 2102:
                return "大连市";
            case 210201:
                return "市辖区";
            case 210202:
                return "中山区";
            case 210203:
                return "西岗区";
            case 210204:
                return "沙河口区";
            case 210211:
                return "甘井子区";
            case 210212:
                return "旅顺口区";
            case 210213:
                return "金州区";
            case 210224:
                return "长海县";
            case 210281:
                return "瓦房店市";
            case 210282:
                return "普兰店市";
            case 210283:
                return "庄河市";
            case 2103:
                return "鞍山市";
            case 210301:
                return "市辖区";
            case 210302:
                return "铁东区";
            case 210303:
                return "铁西区";
            case 210304:
                return "立山区";
            case 210311:
                return "千山区";
            case 210321:
                return "台安县";
            case 210323:
                return "岫岩满族自治县";
            case 210381:
                return "海城市";
            case 2104:
                return "抚顺市";
            case 210401:
                return "市辖区";
            case 210402:
                return "新抚区";
            case 210403:
                return "东洲区";
            case 210404:
                return "望花区";
            case 210411:
                return "顺城区";
            case 210421:
                return "抚顺县";
            case 210422:
                return "新宾满族自治县";
            case 210423:
                return "清原满族自治县";
            case 2105:
                return "本溪市";
            case 210501:
                return "市辖区";
            case 210502:
                return "平山区";
            case 210503:
                return "溪湖区";
            case 210504:
                return "明山区";
            case 210505:
                return "南芬区";
            case 210521:
                return "本溪满族自治县";
            case 210522:
                return "桓仁满族自治县";
            case 2106:
                return "丹东市";
            case 210601:
                return "市辖区";
            case 210602:
                return "元宝区";
            case 210603:
                return "振兴区";
            case 210604:
                return "振安区";
            case 210624:
                return "宽甸满族自治县";
            case 210681:
                return "东港市";
            case 210682:
                return "凤城市";
            case 2107:
                return "锦州市";
            case 210701:
                return "市辖区";
            case 210702:
                return "古塔区";
            case 210703:
                return "凌河区";
            case 210711:
                return "太和区";
            case 210726:
                return "黑山县";
            case 210727:
                return "义县";
            case 210781:
                return "凌海市";
            case 210782:
                return "北镇市";
            case 2108:
                return "营口市";
            case 210801:
                return "市辖区";
            case 210802:
                return "站前区";
            case 210803:
                return "西市区";
            case 210804:
                return "鲅鱼圈区";
            case 210811:
                return "老边区";
            case 210881:
                return "盖州市";
            case 210882:
                return "大石桥市";
            case 2109:
                return "阜新市";
            case 210901:
                return "市辖区";
            case 210902:
                return "海州区";
            case 210903:
                return "新邱区";
            case 210904:
                return "太平区";
            case 210905:
                return "清河门区";
            case 210911:
                return "细河区";
            case 210921:
                return "阜新蒙古族自治县";
            case 210922:
                return "彰武县";
            case 2110:
                return "辽阳市";
            case 211001:
                return "市辖区";
            case 211002:
                return "白塔区";
            case 211003:
                return "文圣区";
            case 211004:
                return "宏伟区";
            case 211005:
                return "弓长岭区";
            case 211011:
                return "太子河区";
            case 211021:
                return "辽阳县";
            case 211081:
                return "灯塔市";
            case 2111:
                return "盘锦市";
            case 211101:
                return "市辖区";
            case 211102:
                return "双台子区";
            case 211103:
                return "兴隆台区";
            case 211121:
                return "大洼县";
            case 211122:
                return "盘山县";
            case 2112:
                return "铁岭市";
            case 211201:
                return "市辖区";
            case 211202:
                return "银州区";
            case 211204:
                return "清河区";
            case 211221:
                return "铁岭县";
            case 211223:
                return "西丰县";
            case 211224:
                return "昌图县";
            case 211281:
                return "调兵山市";
            case 211282:
                return "开原市";
            case 2113:
                return "朝阳市";
            case 211301:
                return "市辖区";
            case 211302:
                return "双塔区";
            case 211303:
                return "龙城区";
            case 211321:
                return "朝阳县";
            case 211322:
                return "建平县";
            case 211324:
                return "喀喇沁左翼蒙古族自治县";
            case 211381:
                return "北票市";
            case 211382:
                return "凌源市";
            case 2114:
                return "葫芦岛市";
            case 211401:
                return "市辖区";
            case 211402:
                return "连山区";
            case 211403:
                return "龙港区";
            case 211404:
                return "南票区";
            case 211421:
                return "绥中县";
            case 211422:
                return "建昌县";
            case 211481:
                return "兴城市";
            case 22:
                return "吉林省";
            case 2201:
                return "长春市";
            case 220101:
                return "市辖区";
            case 220102:
                return "南关区";
            case 220103:
                return "宽城区";
            case 220104:
                return "朝阳区";
            case 220105:
                return "二道区";
            case 220106:
                return "绿园区";
            case 220112:
                return "双阳区";
            case 220113:
                return "九台区";
            case 220122:
                return "农安县";
            case 220182:
                return "榆树市";
            case 220183:
                return "德惠市";
            case 2202:
                return "吉林市";
            case 220201:
                return "市辖区";
            case 220202:
                return "昌邑区";
            case 220203:
                return "龙潭区";
            case 220204:
                return "船营区";
            case 220211:
                return "丰满区";
            case 220221:
                return "永吉县";
            case 220281:
                return "蛟河市";
            case 220282:
                return "桦甸市";
            case 220283:
                return "舒兰市";
            case 220284:
                return "磐石市";
            case 2203:
                return "四平市";
            case 220301:
                return "市辖区";
            case 220302:
                return "铁西区";
            case 220303:
                return "铁东区";
            case 220322:
                return "梨树县";
            case 220323:
                return "伊通满族自治县";
            case 220381:
                return "公主岭市";
            case 220382:
                return "双辽市";
            case 2204:
                return "辽源市";
            case 220401:
                return "市辖区";
            case 220402:
                return "龙山区";
            case 220403:
                return "西安区";
            case 220421:
                return "东丰县";
            case 220422:
                return "东辽县";
            case 2205:
                return "通化市";
            case 220501:
                return "市辖区";
            case 220502:
                return "东昌区";
            case 220503:
                return "二道江区";
            case 220521:
                return "通化县";
            case 220523:
                return "辉南县";
            case 220524:
                return "柳河县";
            case 220581:
                return "梅河口市";
            case 220582:
                return "集安市";
            case 2206:
                return "白山市";
            case 220601:
                return "市辖区";
            case 220602:
                return "浑江区";
            case 220605:
                return "江源区";
            case 220621:
                return "抚松县";
            case 220622:
                return "靖宇县";
            case 220623:
                return "长白朝鲜族自治县";
            case 220681:
                return "临江市";
            case 2207:
                return "松原市";
            case 220701:
                return "市辖区";
            case 220702:
                return "宁江区";
            case 220721:
                return "前郭尔罗斯蒙古族自治县";
            case 220722:
                return "长岭县";
            case 220723:
                return "乾安县";
            case 220781:
                return "扶余市";
            case 2208:
                return "白城市";
            case 220801:
                return "市辖区";
            case 220802:
                return "洮北区";
            case 220821:
                return "镇赉县";
            case 220822:
                return "通榆县";
            case 220881:
                return "洮南市";
            case 220882:
                return "大安市";
            case 2224:
                return "延边朝鲜族自治州";
            case 222401:
                return "延吉市";
            case 222402:
                return "图们市";
            case 222403:
                return "敦化市";
            case 222404:
                return "珲春市";
            case 222405:
                return "龙井市";
            case 222406:
                return "和龙市";
            case 222424:
                return "汪清县";
            case 222426:
                return "安图县";
            case 23:
                return "黑龙江省";
            case 2301:
                return "哈尔滨市";
            case 230101:
                return "市辖区";
            case 230102:
                return "道里区";
            case 230103:
                return "南岗区";
            case 230104:
                return "道外区";
            case 230108:
                return "平房区";
            case 230109:
                return "松北区";
            case 230110:
                return "香坊区";
            case 230111:
                return "呼兰区";
            case 230112:
                return "阿城区";
            case 230123:
                return "依兰县";
            case 230124:
                return "方正县";
            case 230125:
                return "宾县";
            case 230126:
                return "巴彦县";
            case 230127:
                return "木兰县";
            case 230128:
                return "通河县";
            case 230129:
                return "延寿县";
            case 230182:
                return "双城市";
            case 230183:
                return "尚志市";
            case 230184:
                return "五常市";
            case 2302:
                return "齐齐哈尔市";
            case 230201:
                return "市辖区";
            case 230202:
                return "龙沙区";
            case 230203:
                return "建华区";
            case 230204:
                return "铁锋区";
            case 230205:
                return "昂昂溪区";
            case 230206:
                return "富拉尔基区";
            case 230207:
                return "碾子山区";
            case 230208:
                return "梅里斯达斡尔族区";
            case 230221:
                return "龙江县";
            case 230223:
                return "依安县";
            case 230224:
                return "泰来县";
            case 230225:
                return "甘南县";
            case 230227:
                return "富裕县";
            case 230229:
                return "克山县";
            case 230230:
                return "克东县";
            case 230231:
                return "拜泉县";
            case 230281:
                return "讷河市";
            case 2303:
                return "鸡西市";
            case 230301:
                return "市辖区";
            case 230302:
                return "鸡冠区";
            case 230303:
                return "恒山区";
            case 230304:
                return "滴道区";
            case 230305:
                return "梨树区";
            case 230306:
                return "城子河区";
            case 230307:
                return "麻山区";
            case 230321:
                return "鸡东县";
            case 230381:
                return "虎林市";
            case 230382:
                return "密山市";
            case 2304:
                return "鹤岗市";
            case 230401:
                return "市辖区";
            case 230402:
                return "向阳区";
            case 230403:
                return "工农区";
            case 230404:
                return "南山区";
            case 230405:
                return "兴安区";
            case 230406:
                return "东山区";
            case 230407:
                return "兴山区";
            case 230421:
                return "萝北县";
            case 230422:
                return "绥滨县";
            case 2305:
                return "双鸭山市";
            case 230501:
                return "市辖区";
            case 230502:
                return "尖山区";
            case 230503:
                return "岭东区";
            case 230505:
                return "四方台区";
            case 230506:
                return "宝山区";
            case 230521:
                return "集贤县";
            case 230522:
                return "友谊县";
            case 230523:
                return "宝清县";
            case 230524:
                return "饶河县";
            case 2306:
                return "大庆市";
            case 230601:
                return "市辖区";
            case 230602:
                return "萨尔图区";
            case 230603:
                return "龙凤区";
            case 230604:
                return "让胡路区";
            case 230605:
                return "红岗区";
            case 230606:
                return "大同区";
            case 230621:
                return "肇州县";
            case 230622:
                return "肇源县";
            case 230623:
                return "林甸县";
            case 230624:
                return "杜尔伯特蒙古族自治县";
            case 2307:
                return "伊春市";
            case 230701:
                return "市辖区";
            case 230702:
                return "伊春区";
            case 230703:
                return "南岔区";
            case 230704:
                return "友好区";
            case 230705:
                return "西林区";
            case 230706:
                return "翠峦区";
            case 230707:
                return "新青区";
            case 230708:
                return "美溪区";
            case 230709:
                return "金山屯区";
            case 230710:
                return "五营区";
            case 230711:
                return "乌马河区";
            case 230712:
                return "汤旺河区";
            case 230713:
                return "带岭区";
            case 230714:
                return "乌伊岭区";
            case 230715:
                return "红星区";
            case 230716:
                return "上甘岭区";
            case 230722:
                return "嘉荫县";
            case 230781:
                return "铁力市";
            case 2308:
                return "佳木斯市";
            case 230801:
                return "市辖区";
            case 230803:
                return "向阳区";
            case 230804:
                return "前进区";
            case 230805:
                return "东风区";
            case 230811:
                return "郊区";
            case 230822:
                return "桦南县";
            case 230826:
                return "桦川县";
            case 230828:
                return "汤原县";
            case 230833:
                return "抚远县";
            case 230881:
                return "同江市";
            case 230882:
                return "富锦市";
            case 2309:
                return "七台河市";
            case 230901:
                return "市辖区";
            case 230902:
                return "新兴区";
            case 230903:
                return "桃山区";
            case 230904:
                return "茄子河区";
            case 230921:
                return "勃利县";
            case 2310:
                return "牡丹江市";
            case 231001:
                return "市辖区";
            case 231002:
                return "东安区";
            case 231003:
                return "阳明区";
            case 231004:
                return "爱民区";
            case 231005:
                return "西安区";
            case 231024:
                return "东宁县";
            case 231025:
                return "林口县";
            case 231081:
                return "绥芬河市";
            case 231083:
                return "海林市";
            case 231084:
                return "宁安市";
            case 231085:
                return "穆棱市";
            case 2311:
                return "黑河市";
            case 231101:
                return "市辖区";
            case 231102:
                return "爱辉区";
            case 231121:
                return "嫩江县";
            case 231123:
                return "逊克县";
            case 231124:
                return "孙吴县";
            case 231181:
                return "北安市";
            case 231182:
                return "五大连池市";
            case 2312:
                return "绥化市";
            case 231201:
                return "市辖区";
            case 231202:
                return "北林区";
            case 231221:
                return "望奎县";
            case 231222:
                return "兰西县";
            case 231223:
                return "青冈县";
            case 231224:
                return "庆安县";
            case 231225:
                return "明水县";
            case 231226:
                return "绥棱县";
            case 231281:
                return "安达市";
            case 231282:
                return "肇东市";
            case 231283:
                return "海伦市";
            case 2327:
                return "大兴安岭地区";
            case 232721:
                return "呼玛县";
            case 232722:
                return "塔河县";
            case 232723:
                return "漠河县";
            case 31:
                return "上海市";
            case 3101:
                return "市辖区";
            case 310101:
                return "黄浦区";
            case 310104:
                return "徐汇区";
            case 310105:
                return "长宁区";
            case 310106:
                return "静安区";
            case 310107:
                return "普陀区";
            case 310108:
                return "闸北区";
            case 310109:
                return "虹口区";
            case 310110:
                return "杨浦区";
            case 310112:
                return "闵行区";
            case 310113:
                return "宝山区";
            case 310114:
                return "嘉定区";
            case 310115:
                return "浦东新区";
            case 310116:
                return "金山区";
            case 310117:
                return "松江区";
            case 310118:
                return "青浦区";
            case 310120:
                return "奉贤区";
            case 3102:
                return "县";
            case 310230:
                return "崇明县";
            case 32:
                return "江苏省";
            case 3201:
                return "南京市";
            case 320101:
                return "市辖区";
            case 320102:
                return "玄武区";
            case 320104:
                return "秦淮区";
            case 320105:
                return "建邺区";
            case 320106:
                return "鼓楼区";
            case 320111:
                return "浦口区";
            case 320113:
                return "栖霞区";
            case 320114:
                return "雨花台区";
            case 320115:
                return "江宁区";
            case 320116:
                return "六合区";
            case 320117:
                return "溧水区";
            case 320118:
                return "高淳区";
            case 3202:
                return "无锡市";
            case 320201:
                return "市辖区";
            case 320202:
                return "崇安区";
            case 320203:
                return "南长区";
            case 320204:
                return "北塘区";
            case 320205:
                return "锡山区";
            case 320206:
                return "惠山区";
            case 320211:
                return "滨湖区";
            case 320281:
                return "江阴市";
            case 320282:
                return "宜兴市";
            case 3203:
                return "徐州市";
            case 320301:
                return "市辖区";
            case 320302:
                return "鼓楼区";
            case 320303:
                return "云龙区";
            case 320305:
                return "贾汪区";
            case 320311:
                return "泉山区";
            case 320312:
                return "铜山区";
            case 320321:
                return "丰县";
            case 320322:
                return "沛县";
            case 320324:
                return "睢宁县";
            case 320381:
                return "新沂市";
            case 320382:
                return "邳州市";
            case 3204:
                return "常州市";
            case 320401:
                return "市辖区";
            case 320402:
                return "天宁区";
            case 320404:
                return "钟楼区";
            case 320405:
                return "戚墅堰区";
            case 320411:
                return "新北区";
            case 320412:
                return "武进区";
            case 320481:
                return "溧阳市";
            case 320482:
                return "金坛市";
            case 3205:
                return "苏州市";
            case 320501:
                return "市辖区";
            case 320505:
                return "虎丘区";
            case 320506:
                return "吴中区";
            case 320507:
                return "相城区";
            case 320508:
                return "姑苏区";
            case 320509:
                return "吴江区";
            case 320581:
                return "常熟市";
            case 320582:
                return "张家港市";
            case 320583:
                return "昆山市";
            case 320585:
                return "太仓市";
            case 3206:
                return "南通市";
            case 320601:
                return "市辖区";
            case 320602:
                return "崇川区";
            case 320611:
                return "港闸区";
            case 320612:
                return "通州区";
            case 320621:
                return "海安县";
            case 320623:
                return "如东县";
            case 320681:
                return "启东市";
            case 320682:
                return "如皋市";
            case 320684:
                return "海门市";
            case 3207:
                return "连云港市";
            case 320701:
                return "市辖区";
            case 320703:
                return "连云区";
            case 320706:
                return "海州区";
            case 320707:
                return "赣榆区";
            case 320722:
                return "东海县";
            case 320723:
                return "灌云县";
            case 320724:
                return "灌南县";
            case 3208:
                return "淮安市";
            case 320801:
                return "市辖区";
            case 320802:
                return "清河区";
            case 320803:
                return "淮安区";
            case 320804:
                return "淮阴区";
            case 320811:
                return "清浦区";
            case 320826:
                return "涟水县";
            case 320829:
                return "洪泽县";
            case 320830:
                return "盱眙县";
            case 320831:
                return "金湖县";
            case 3209:
                return "盐城市";
            case 320901:
                return "市辖区";
            case 320902:
                return "亭湖区";
            case 320903:
                return "盐都区";
            case 320921:
                return "响水县";
            case 320922:
                return "滨海县";
            case 320923:
                return "阜宁县";
            case 320924:
                return "射阳县";
            case 320925:
                return "建湖县";
            case 320981:
                return "东台市";
            case 320982:
                return "大丰市";
            case 3210:
                return "扬州市";
            case 321001:
                return "市辖区";
            case 321002:
                return "广陵区";
            case 321003:
                return "邗江区";
            case 321012:
                return "江都区";
            case 321023:
                return "宝应县";
            case 321081:
                return "仪征市";
            case 321084:
                return "高邮市";
            case 3211:
                return "镇江市";
            case 321101:
                return "市辖区";
            case 321102:
                return "京口区";
            case 321111:
                return "润州区";
            case 321112:
                return "丹徒区";
            case 321181:
                return "丹阳市";
            case 321182:
                return "扬中市";
            case 321183:
                return "句容市";
            case 3212:
                return "泰州市";
            case 321201:
                return "市辖区";
            case 321202:
                return "海陵区";
            case 321203:
                return "高港区";
            case 321204:
                return "姜堰区";
            case 321281:
                return "兴化市";
            case 321282:
                return "靖江市";
            case 321283:
                return "泰兴市";
            case 3213:
                return "宿迁市";
            case 321301:
                return "市辖区";
            case 321302:
                return "宿城区";
            case 321311:
                return "宿豫区";
            case 321322:
                return "沭阳县";
            case 321323:
                return "泗阳县";
            case 321324:
                return "泗洪县";
            case 33:
                return "浙江省";
            case 3301:
                return "杭州市";
            case 330101:
                return "市辖区";
            case 330102:
                return "上城区";
            case 330103:
                return "下城区";
            case 330104:
                return "江干区";
            case 330105:
                return "拱墅区";
            case 330106:
                return "西湖区";
            case 330108:
                return "滨江区";
            case 330109:
                return "萧山区";
            case 330110:
                return "余杭区";
            case 330122:
                return "桐庐县";
            case 330127:
                return "淳安县";
            case 330182:
                return "建德市";
            case 330183:
                return "富阳市";
            case 330185:
                return "临安市";
            case 3302:
                return "宁波市";
            case 330201:
                return "市辖区";
            case 330203:
                return "海曙区";
            case 330204:
                return "江东区";
            case 330205:
                return "江北区";
            case 330206:
                return "北仑区";
            case 330211:
                return "镇海区";
            case 330212:
                return "鄞州区";
            case 330225:
                return "象山县";
            case 330226:
                return "宁海县";
            case 330281:
                return "余姚市";
            case 330282:
                return "慈溪市";
            case 330283:
                return "奉化市";
            case 3303:
                return "温州市";
            case 330301:
                return "市辖区";
            case 330302:
                return "鹿城区";
            case 330303:
                return "龙湾区";
            case 330304:
                return "瓯海区";
            case 330322:
                return "洞头县";
            case 330324:
                return "永嘉县";
            case 330326:
                return "平阳县";
            case 330327:
                return "苍南县";
            case 330328:
                return "文成县";
            case 330329:
                return "泰顺县";
            case 330381:
                return "瑞安市";
            case 330382:
                return "乐清市";
            case 3304:
                return "嘉兴市";
            case 330401:
                return "市辖区";
            case 330402:
                return "南湖区";
            case 330411:
                return "秀洲区";
            case 330421:
                return "嘉善县";
            case 330424:
                return "海盐县";
            case 330481:
                return "海宁市";
            case 330482:
                return "平湖市";
            case 330483:
                return "桐乡市";
            case 3305:
                return "湖州市";
            case 330501:
                return "市辖区";
            case 330502:
                return "吴兴区";
            case 330503:
                return "南浔区";
            case 330521:
                return "德清县";
            case 330522:
                return "长兴县";
            case 330523:
                return "安吉县";
            case 3306:
                return "绍兴市";
            case 330601:
                return "市辖区";
            case 330602:
                return "越城区";
            case 330603:
                return "柯桥区";
            case 330604:
                return "上虞区";
            case 330624:
                return "新昌县";
            case 330681:
                return "诸暨市";
            case 330683:
                return "嵊州市";
            case 3307:
                return "金华市";
            case 330701:
                return "市辖区";
            case 330702:
                return "婺城区";
            case 330703:
                return "金东区";
            case 330723:
                return "武义县";
            case 330726:
                return "浦江县";
            case 330727:
                return "磐安县";
            case 330781:
                return "兰溪市";
            case 330782:
                return "义乌市";
            case 330783:
                return "东阳市";
            case 330784:
                return "永康市";
            case 3308:
                return "衢州市";
            case 330801:
                return "市辖区";
            case 330802:
                return "柯城区";
            case 330803:
                return "衢江区";
            case 330822:
                return "常山县";
            case 330824:
                return "开化县";
            case 330825:
                return "龙游县";
            case 330881:
                return "江山市";
            case 3309:
                return "舟山市";
            case 330901:
                return "市辖区";
            case 330902:
                return "定海区";
            case 330903:
                return "普陀区";
            case 330921:
                return "岱山县";
            case 330922:
                return "嵊泗县";
            case 3310:
                return "台州市";
            case 331001:
                return "市辖区";
            case 331002:
                return "椒江区";
            case 331003:
                return "黄岩区";
            case 331004:
                return "路桥区";
            case 331021:
                return "玉环县";
            case 331022:
                return "三门县";
            case 331023:
                return "天台县";
            case 331024:
                return "仙居县";
            case 331081:
                return "温岭市";
            case 331082:
                return "临海市";
            case 3311:
                return "丽水市";
            case 331101:
                return "市辖区";
            case 331102:
                return "莲都区";
            case 331121:
                return "青田县";
            case 331122:
                return "缙云县";
            case 331123:
                return "遂昌县";
            case 331124:
                return "松阳县";
            case 331125:
                return "云和县";
            case 331126:
                return "庆元县";
            case 331127:
                return "景宁畲族自治县";
            case 331181:
                return "龙泉市";
            case 34:
                return "安徽省";
            case 3401:
                return "合肥市";
            case 340101:
                return "市辖区";
            case 340102:
                return "瑶海区";
            case 340103:
                return "庐阳区";
            case 340104:
                return "蜀山区";
            case 340111:
                return "包河区";
            case 340121:
                return "长丰县";
            case 340122:
                return "肥东县";
            case 340123:
                return "肥西县";
            case 340124:
                return "庐江县";
            case 340181:
                return "巢湖市";
            case 3402:
                return "芜湖市";
            case 340201:
                return "市辖区";
            case 340202:
                return "镜湖区";
            case 340203:
                return "弋江区";
            case 340207:
                return "鸠江区";
            case 340208:
                return "三山区";
            case 340221:
                return "芜湖县";
            case 340222:
                return "繁昌县";
            case 340223:
                return "南陵县";
            case 340225:
                return "无为县";
            case 3403:
                return "蚌埠市";
            case 340301:
                return "市辖区";
            case 340302:
                return "龙子湖区";
            case 340303:
                return "蚌山区";
            case 340304:
                return "禹会区";
            case 340311:
                return "淮上区";
            case 340321:
                return "怀远县";
            case 340322:
                return "五河县";
            case 340323:
                return "固镇县";
            case 3404:
                return "淮南市";
            case 340401:
                return "市辖区";
            case 340402:
                return "大通区";
            case 340403:
                return "田家庵区";
            case 340404:
                return "谢家集区";
            case 340405:
                return "八公山区";
            case 340406:
                return "潘集区";
            case 340421:
                return "凤台县";
            case 3405:
                return "马鞍山市";
            case 340501:
                return "市辖区";
            case 340503:
                return "花山区";
            case 340504:
                return "雨山区";
            case 340506:
                return "博望区";
            case 340521:
                return "当涂县";
            case 340522:
                return "含山县";
            case 340523:
                return "和县";
            case 3406:
                return "淮北市";
            case 340601:
                return "市辖区";
            case 340602:
                return "杜集区";
            case 340603:
                return "相山区";
            case 340604:
                return "烈山区";
            case 340621:
                return "濉溪县";
            case 3407:
                return "铜陵市";
            case 340701:
                return "市辖区";
            case 340702:
                return "铜官山区";
            case 340703:
                return "狮子山区";
            case 340711:
                return "郊区";
            case 340721:
                return "铜陵县";
            case 3408:
                return "安庆市";
            case 340801:
                return "市辖区";
            case 340802:
                return "迎江区";
            case 340803:
                return "大观区";
            case 340811:
                return "宜秀区";
            case 340822:
                return "怀宁县";
            case 340823:
                return "枞阳县";
            case 340824:
                return "潜山县";
            case 340825:
                return "太湖县";
            case 340826:
                return "宿松县";
            case 340827:
                return "望江县";
            case 340828:
                return "岳西县";
            case 340881:
                return "桐城市";
            case 3410:
                return "黄山市";
            case 341001:
                return "市辖区";
            case 341002:
                return "屯溪区";
            case 341003:
                return "黄山区";
            case 341004:
                return "徽州区";
            case 341021:
                return "歙县";
            case 341022:
                return "休宁县";
            case 341023:
                return "黟县";
            case 341024:
                return "祁门县";
            case 3411:
                return "滁州市";
            case 341101:
                return "市辖区";
            case 341102:
                return "琅琊区";
            case 341103:
                return "南谯区";
            case 341122:
                return "来安县";
            case 341124:
                return "全椒县";
            case 341125:
                return "定远县";
            case 341126:
                return "凤阳县";
            case 341181:
                return "天长市";
            case 341182:
                return "明光市";
            case 3412:
                return "阜阳市";
            case 341201:
                return "市辖区";
            case 341202:
                return "颍州区";
            case 341203:
                return "颍东区";
            case 341204:
                return "颍泉区";
            case 341221:
                return "临泉县";
            case 341222:
                return "太和县";
            case 341225:
                return "阜南县";
            case 341226:
                return "颍上县";
            case 341282:
                return "界首市";
            case 3413:
                return "宿州市";
            case 341301:
                return "市辖区";
            case 341302:
                return "埇桥区";
            case 341321:
                return "砀山县";
            case 341322:
                return "萧县";
            case 341323:
                return "灵璧县";
            case 341324:
                return "泗县";
            case 3415:
                return "六安市";
            case 341501:
                return "市辖区";
            case 341502:
                return "金安区";
            case 341503:
                return "裕安区";
            case 341521:
                return "寿县";
            case 341522:
                return "霍邱县";
            case 341523:
                return "舒城县";
            case 341524:
                return "金寨县";
            case 341525:
                return "霍山县";
            case 3416:
                return "亳州市";
            case 341601:
                return "市辖区";
            case 341602:
                return "谯城区";
            case 341621:
                return "涡阳县";
            case 341622:
                return "蒙城县";
            case 341623:
                return "利辛县";
            case 3417:
                return "池州市";
            case 341701:
                return "市辖区";
            case 341702:
                return "贵池区";
            case 341721:
                return "东至县";
            case 341722:
                return "石台县";
            case 341723:
                return "青阳县";
            case 3418:
                return "宣城市";
            case 341801:
                return "市辖区";
            case 341802:
                return "宣州区";
            case 341821:
                return "郎溪县";
            case 341822:
                return "广德县";
            case 341823:
                return "泾县";
            case 341824:
                return "绩溪县";
            case 341825:
                return "旌德县";
            case 341881:
                return "宁国市";
            case 35:
                return "福建省";
            case 3501:
                return "福州市";
            case 350101:
                return "市辖区";
            case 350102:
                return "鼓楼区";
            case 350103:
                return "台江区";
            case 350104:
                return "仓山区";
            case 350105:
                return "马尾区";
            case 350111:
                return "晋安区";
            case 350121:
                return "闽侯县";
            case 350122:
                return "连江县";
            case 350123:
                return "罗源县";
            case 350124:
                return "闽清县";
            case 350125:
                return "永泰县";
            case 350128:
                return "平潭县";
            case 350181:
                return "福清市";
            case 350182:
                return "长乐市";
            case 3502:
                return "厦门市";
            case 350201:
                return "市辖区";
            case 350203:
                return "思明区";
            case 350205:
                return "海沧区";
            case 350206:
                return "湖里区";
            case 350211:
                return "集美区";
            case 350212:
                return "同安区";
            case 350213:
                return "翔安区";
            case 3503:
                return "莆田市";
            case 350301:
                return "市辖区";
            case 350302:
                return "城厢区";
            case 350303:
                return "涵江区";
            case 350304:
                return "荔城区";
            case 350305:
                return "秀屿区";
            case 350322:
                return "仙游县";
            case 3504:
                return "三明市";
            case 350401:
                return "市辖区";
            case 350402:
                return "梅列区";
            case 350403:
                return "三元区";
            case 350421:
                return "明溪县";
            case 350423:
                return "清流县";
            case 350424:
                return "宁化县";
            case 350425:
                return "大田县";
            case 350426:
                return "尤溪县";
            case 350427:
                return "沙县";
            case 350428:
                return "将乐县";
            case 350429:
                return "泰宁县";
            case 350430:
                return "建宁县";
            case 350481:
                return "永安市";
            case 3505:
                return "泉州市";
            case 350501:
                return "市辖区";
            case 350502:
                return "鲤城区";
            case 350503:
                return "丰泽区";
            case 350504:
                return "洛江区";
            case 350505:
                return "泉港区";
            case 350521:
                return "惠安县";
            case 350524:
                return "安溪县";
            case 350525:
                return "永春县";
            case 350526:
                return "德化县";
            case 350527:
                return "金门县";
            case 350581:
                return "石狮市";
            case 350582:
                return "晋江市";
            case 350583:
                return "南安市";
            case 3506:
                return "漳州市";
            case 350601:
                return "市辖区";
            case 350602:
                return "芗城区";
            case 350603:
                return "龙文区";
            case 350622:
                return "云霄县";
            case 350623:
                return "漳浦县";
            case 350624:
                return "诏安县";
            case 350625:
                return "长泰县";
            case 350626:
                return "东山县";
            case 350627:
                return "南靖县";
            case 350628:
                return "平和县";
            case 350629:
                return "华安县";
            case 350681:
                return "龙海市";
            case 3507:
                return "南平市";
            case 350701:
                return "市辖区";
            case 350702:
                return "延平区";
            case 350721:
                return "顺昌县";
            case 350722:
                return "浦城县";
            case 350723:
                return "光泽县";
            case 350724:
                return "松溪县";
            case 350725:
                return "政和县";
            case 350781:
                return "邵武市";
            case 350782:
                return "武夷山市";
            case 350783:
                return "建瓯市";
            case 350784:
                return "建阳市";
            case 3508:
                return "龙岩市";
            case 350801:
                return "市辖区";
            case 350802:
                return "新罗区";
            case 350821:
                return "长汀县";
            case 350822:
                return "永定县";
            case 350823:
                return "上杭县";
            case 350824:
                return "武平县";
            case 350825:
                return "连城县";
            case 350881:
                return "漳平市";
            case 3509:
                return "宁德市";
            case 350901:
                return "市辖区";
            case 350902:
                return "蕉城区";
            case 350921:
                return "霞浦县";
            case 350922:
                return "古田县";
            case 350923:
                return "屏南县";
            case 350924:
                return "寿宁县";
            case 350925:
                return "周宁县";
            case 350926:
                return "柘荣县";
            case 350981:
                return "福安市";
            case 350982:
                return "福鼎市";
            case 36:
                return "江西省";
            case 3601:
                return "南昌市";
            case 360101:
                return "市辖区";
            case 360102:
                return "东湖区";
            case 360103:
                return "西湖区";
            case 360104:
                return "青云谱区";
            case 360105:
                return "湾里区";
            case 360111:
                return "青山湖区";
            case 360121:
                return "南昌县";
            case 360122:
                return "新建县";
            case 360123:
                return "安义县";
            case 360124:
                return "进贤县";
            case 3602:
                return "景德镇市";
            case 360201:
                return "市辖区";
            case 360202:
                return "昌江区";
            case 360203:
                return "珠山区";
            case 360222:
                return "浮梁县";
            case 360281:
                return "乐平市";
            case 3603:
                return "萍乡市";
            case 360301:
                return "市辖区";
            case 360302:
                return "安源区";
            case 360313:
                return "湘东区";
            case 360321:
                return "莲花县";
            case 360322:
                return "上栗县";
            case 360323:
                return "芦溪县";
            case 3604:
                return "九江市";
            case 360401:
                return "市辖区";
            case 360402:
                return "庐山区";
            case 360403:
                return "浔阳区";
            case 360421:
                return "九江县";
            case 360423:
                return "武宁县";
            case 360424:
                return "修水县";
            case 360425:
                return "永修县";
            case 360426:
                return "德安县";
            case 360427:
                return "星子县";
            case 360428:
                return "都昌县";
            case 360429:
                return "湖口县";
            case 360430:
                return "彭泽县";
            case 360481:
                return "瑞昌市";
            case 360482:
                return "共青城市";
            case 3605:
                return "新余市";
            case 360501:
                return "市辖区";
            case 360502:
                return "渝水区";
            case 360521:
                return "分宜县";
            case 3606:
                return "鹰潭市";
            case 360601:
                return "市辖区";
            case 360602:
                return "月湖区";
            case 360622:
                return "余江县";
            case 360681:
                return "贵溪市";
            case 3607:
                return "赣州市";
            case 360701:
                return "市辖区";
            case 360702:
                return "章贡区";
            case 360703:
                return "南康区";
            case 360721:
                return "赣县";
            case 360722:
                return "信丰县";
            case 360723:
                return "大余县";
            case 360724:
                return "上犹县";
            case 360725:
                return "崇义县";
            case 360726:
                return "安远县";
            case 360727:
                return "龙南县";
            case 360728:
                return "定南县";
            case 360729:
                return "全南县";
            case 360730:
                return "宁都县";
            case 360731:
                return "于都县";
            case 360732:
                return "兴国县";
            case 360733:
                return "会昌县";
            case 360734:
                return "寻乌县";
            case 360735:
                return "石城县";
            case 360781:
                return "瑞金市";
            case 3608:
                return "吉安市";
            case 360801:
                return "市辖区";
            case 360802:
                return "吉州区";
            case 360803:
                return "青原区";
            case 360821:
                return "吉安县";
            case 360822:
                return "吉水县";
            case 360823:
                return "峡江县";
            case 360824:
                return "新干县";
            case 360825:
                return "永丰县";
            case 360826:
                return "泰和县";
            case 360827:
                return "遂川县";
            case 360828:
                return "万安县";
            case 360829:
                return "安福县";
            case 360830:
                return "永新县";
            case 360881:
                return "井冈山市";
            case 3609:
                return "宜春市";
            case 360901:
                return "市辖区";
            case 360902:
                return "袁州区";
            case 360921:
                return "奉新县";
            case 360922:
                return "万载县";
            case 360923:
                return "上高县";
            case 360924:
                return "宜丰县";
            case 360925:
                return "靖安县";
            case 360926:
                return "铜鼓县";
            case 360981:
                return "丰城市";
            case 360982:
                return "樟树市";
            case 360983:
                return "高安市";
            case 3610:
                return "抚州市";
            case 361001:
                return "市辖区";
            case 361002:
                return "临川区";
            case 361021:
                return "南城县";
            case 361022:
                return "黎川县";
            case 361023:
                return "南丰县";
            case 361024:
                return "崇仁县";
            case 361025:
                return "乐安县";
            case 361026:
                return "宜黄县";
            case 361027:
                return "金溪县";
            case 361028:
                return "资溪县";
            case 361029:
                return "东乡县";
            case 361030:
                return "广昌县";
            case 3611:
                return "上饶市";
            case 361101:
                return "市辖区";
            case 361102:
                return "信州区";
            case 361121:
                return "上饶县";
            case 361122:
                return "广丰县";
            case 361123:
                return "玉山县";
            case 361124:
                return "铅山县";
            case 361125:
                return "横峰县";
            case 361126:
                return "弋阳县";
            case 361127:
                return "余干县";
            case 361128:
                return "鄱阳县";
            case 361129:
                return "万年县";
            case 361130:
                return "婺源县";
            case 361181:
                return "德兴市";
            case 37:
                return "山东省";
            case 3701:
                return "济南市";
            case 370101:
                return "市辖区";
            case 370102:
                return "历下区";
            case 370103:
                return "市中区";
            case 370104:
                return "槐荫区";
            case 370105:
                return "天桥区";
            case 370112:
                return "历城区";
            case 370113:
                return "长清区";
            case 370124:
                return "平阴县";
            case 370125:
                return "济阳县";
            case 370126:
                return "商河县";
            case 370181:
                return "章丘市";
            case 3702:
                return "青岛市";
            case 370201:
                return "市辖区";
            case 370202:
                return "市南区";
            case 370203:
                return "市北区";
            case 370211:
                return "黄岛区";
            case 370212:
                return "崂山区";
            case 370213:
                return "李沧区";
            case 370214:
                return "城阳区";
            case 370281:
                return "胶州市";
            case 370282:
                return "即墨市";
            case 370283:
                return "平度市";
            case 370285:
                return "莱西市";
            case 3703:
                return "淄博市";
            case 370301:
                return "市辖区";
            case 370302:
                return "淄川区";
            case 370303:
                return "张店区";
            case 370304:
                return "博山区";
            case 370305:
                return "临淄区";
            case 370306:
                return "周村区";
            case 370321:
                return "桓台县";
            case 370322:
                return "高青县";
            case 370323:
                return "沂源县";
            case 3704:
                return "枣庄市";
            case 370401:
                return "市辖区";
            case 370402:
                return "市中区";
            case 370403:
                return "薛城区";
            case 370404:
                return "峄城区";
            case 370405:
                return "台儿庄区";
            case 370406:
                return "山亭区";
            case 370481:
                return "滕州市";
            case 3705:
                return "东营市";
            case 370501:
                return "市辖区";
            case 370502:
                return "东营区";
            case 370503:
                return "河口区";
            case 370521:
                return "垦利县";
            case 370522:
                return "利津县";
            case 370523:
                return "广饶县";
            case 3706:
                return "烟台市";
            case 370601:
                return "市辖区";
            case 370602:
                return "芝罘区";
            case 370611:
                return "福山区";
            case 370612:
                return "牟平区";
            case 370613:
                return "莱山区";
            case 370634:
                return "长岛县";
            case 370681:
                return "龙口市";
            case 370682:
                return "莱阳市";
            case 370683:
                return "莱州市";
            case 370684:
                return "蓬莱市";
            case 370685:
                return "招远市";
            case 370686:
                return "栖霞市";
            case 370687:
                return "海阳市";
            case 3707:
                return "潍坊市";
            case 370701:
                return "市辖区";
            case 370702:
                return "潍城区";
            case 370703:
                return "寒亭区";
            case 370704:
                return "坊子区";
            case 370705:
                return "奎文区";
            case 370724:
                return "临朐县";
            case 370725:
                return "昌乐县";
            case 370781:
                return "青州市";
            case 370782:
                return "诸城市";
            case 370783:
                return "寿光市";
            case 370784:
                return "安丘市";
            case 370785:
                return "高密市";
            case 370786:
                return "昌邑市";
            case 3708:
                return "济宁市";
            case 370801:
                return "市辖区";
            case 370811:
                return "任城区";
            case 370812:
                return "兖州区";
            case 370826:
                return "微山县";
            case 370827:
                return "鱼台县";
            case 370828:
                return "金乡县";
            case 370829:
                return "嘉祥县";
            case 370830:
                return "汶上县";
            case 370831:
                return "泗水县";
            case 370832:
                return "梁山县";
            case 370881:
                return "曲阜市";
            case 370883:
                return "邹城市";
            case 3709:
                return "泰安市";
            case 370901:
                return "市辖区";
            case 370902:
                return "泰山区";
            case 370911:
                return "岱岳区";
            case 370921:
                return "宁阳县";
            case 370923:
                return "东平县";
            case 370982:
                return "新泰市";
            case 370983:
                return "肥城市";
            case 3710:
                return "威海市";
            case 371001:
                return "市辖区";
            case 371002:
                return "环翠区";
            case 371003:
                return "文登区";
            case 371082:
                return "荣成市";
            case 371083:
                return "乳山市";
            case 3711:
                return "日照市";
            case 371101:
                return "市辖区";
            case 371102:
                return "东港区";
            case 371103:
                return "岚山区";
            case 371121:
                return "五莲县";
            case 371122:
                return "莒县";
            case 3712:
                return "莱芜市";
            case 371201:
                return "市辖区";
            case 371202:
                return "莱城区";
            case 371203:
                return "钢城区";
            case 3713:
                return "临沂市";
            case 371301:
                return "市辖区";
            case 371302:
                return "兰山区";
            case 371311:
                return "罗庄区";
            case 371312:
                return "河东区";
            case 371321:
                return "沂南县";
            case 371322:
                return "郯城县";
            case 371323:
                return "沂水县";
            case 371324:
                return "兰陵县";
            case 371325:
                return "费县";
            case 371326:
                return "平邑县";
            case 371327:
                return "莒南县";
            case 371328:
                return "蒙阴县";
            case 371329:
                return "临沭县";
            case 3714:
                return "德州市";
            case 371401:
                return "市辖区";
            case 371402:
                return "德城区";
            case 371403:
                return "陵城区";
            case 371422:
                return "宁津县";
            case 371423:
                return "庆云县";
            case 371424:
                return "临邑县";
            case 371425:
                return "齐河县";
            case 371426:
                return "平原县";
            case 371427:
                return "夏津县";
            case 371428:
                return "武城县";
            case 371481:
                return "乐陵市";
            case 371482:
                return "禹城市";
            case 3715:
                return "聊城市";
            case 371501:
                return "市辖区";
            case 371502:
                return "东昌府区";
            case 371521:
                return "阳谷县";
            case 371522:
                return "莘县";
            case 371523:
                return "茌平县";
            case 371524:
                return "东阿县";
            case 371525:
                return "冠县";
            case 371526:
                return "高唐县";
            case 371581:
                return "临清市";
            case 3716:
                return "滨州市";
            case 371601:
                return "市辖区";
            case 371602:
                return "滨城区";
            case 371603:
                return "沾化区";
            case 371621:
                return "惠民县";
            case 371622:
                return "阳信县";
            case 371623:
                return "无棣县";
            case 371625:
                return "博兴县";
            case 371626:
                return "邹平县";
            case 3717:
                return "菏泽市";
            case 371701:
                return "市辖区";
            case 371702:
                return "牡丹区";
            case 371721:
                return "曹县";
            case 371722:
                return "单县";
            case 371723:
                return "成武县";
            case 371724:
                return "巨野县";
            case 371725:
                return "郓城县";
            case 371726:
                return "鄄城县";
            case 371727:
                return "定陶县";
            case 371728:
                return "东明县";
            case 41:
                return "河南省";
            case 4101:
                return "郑州市";
            case 410101:
                return "市辖区";
            case 410102:
                return "中原区";
            case 410103:
                return "二七区";
            case 410104:
                return "管城回族区";
            case 410105:
                return "金水区";
            case 410106:
                return "上街区";
            case 410108:
                return "惠济区";
            case 410122:
                return "中牟县";
            case 410181:
                return "巩义市";
            case 410182:
                return "荥阳市";
            case 410183:
                return "新密市";
            case 410184:
                return "新郑市";
            case 410185:
                return "登封市";
            case 4102:
                return "开封市";
            case 410201:
                return "市辖区";
            case 410202:
                return "龙亭区";
            case 410203:
                return "顺河回族区";
            case 410204:
                return "鼓楼区";
            case 410205:
                return "禹王台区";
            case 410211:
                return "金明区";
            case 410221:
                return "杞县";
            case 410222:
                return "通许县";
            case 410223:
                return "尉氏县";
            case 410224:
                return "开封县";
            case 410225:
                return "兰考县";
            case 4103:
                return "洛阳市";
            case 410301:
                return "市辖区";
            case 410302:
                return "老城区";
            case 410303:
                return "西工区";
            case 410304:
                return "瀍河回族区";
            case 410305:
                return "涧西区";
            case 410306:
                return "吉利区";
            case 410311:
                return "洛龙区";
            case 410322:
                return "孟津县";
            case 410323:
                return "新安县";
            case 410324:
                return "栾川县";
            case 410325:
                return "嵩县";
            case 410326:
                return "汝阳县";
            case 410327:
                return "宜阳县";
            case 410328:
                return "洛宁县";
            case 410329:
                return "伊川县";
            case 410381:
                return "偃师市";
            case 4104:
                return "平顶山市";
            case 410401:
                return "市辖区";
            case 410402:
                return "新华区";
            case 410403:
                return "卫东区";
            case 410404:
                return "石龙区";
            case 410411:
                return "湛河区";
            case 410421:
                return "宝丰县";
            case 410422:
                return "叶县";
            case 410423:
                return "鲁山县";
            case 410425:
                return "郏县";
            case 410481:
                return "舞钢市";
            case 410482:
                return "汝州市";
            case 4105:
                return "安阳市";
            case 410501:
                return "市辖区";
            case 410502:
                return "文峰区";
            case 410503:
                return "北关区";
            case 410505:
                return "殷都区";
            case 410506:
                return "龙安区";
            case 410522:
                return "安阳县";
            case 410523:
                return "汤阴县";
            case 410526:
                return "滑县";
            case 410527:
                return "内黄县";
            case 410581:
                return "林州市";
            case 4106:
                return "鹤壁市";
            case 410601:
                return "市辖区";
            case 410602:
                return "鹤山区";
            case 410603:
                return "山城区";
            case 410611:
                return "淇滨区";
            case 410621:
                return "浚县";
            case 410622:
                return "淇县";
            case 4107:
                return "新乡市";
            case 410701:
                return "市辖区";
            case 410702:
                return "红旗区";
            case 410703:
                return "卫滨区";
            case 410704:
                return "凤泉区";
            case 410711:
                return "牧野区";
            case 410721:
                return "新乡县";
            case 410724:
                return "获嘉县";
            case 410725:
                return "原阳县";
            case 410726:
                return "延津县";
            case 410727:
                return "封丘县";
            case 410728:
                return "长垣县";
            case 410781:
                return "卫辉市";
            case 410782:
                return "辉县市";
            case 4108:
                return "焦作市";
            case 410801:
                return "市辖区";
            case 410802:
                return "解放区";
            case 410803:
                return "中站区";
            case 410804:
                return "马村区";
            case 410811:
                return "山阳区";
            case 410821:
                return "修武县";
            case 410822:
                return "博爱县";
            case 410823:
                return "武陟县";
            case 410825:
                return "温县";
            case 410882:
                return "沁阳市";
            case 410883:
                return "孟州市";
            case 4109:
                return "濮阳市";
            case 410901:
                return "市辖区";
            case 410902:
                return "华龙区";
            case 410922:
                return "清丰县";
            case 410923:
                return "南乐县";
            case 410926:
                return "范县";
            case 410927:
                return "台前县";
            case 410928:
                return "濮阳县";
            case 4110:
                return "许昌市";
            case 411001:
                return "市辖区";
            case 411002:
                return "魏都区";
            case 411023:
                return "许昌县";
            case 411024:
                return "鄢陵县";
            case 411025:
                return "襄城县";
            case 411081:
                return "禹州市";
            case 411082:
                return "长葛市";
            case 4111:
                return "漯河市";
            case 411101:
                return "市辖区";
            case 411102:
                return "源汇区";
            case 411103:
                return "郾城区";
            case 411104:
                return "召陵区";
            case 411121:
                return "舞阳县";
            case 411122:
                return "临颍县";
            case 4112:
                return "三门峡市";
            case 411201:
                return "市辖区";
            case 411202:
                return "湖滨区";
            case 411221:
                return "渑池县";
            case 411222:
                return "陕县";
            case 411224:
                return "卢氏县";
            case 411281:
                return "义马市";
            case 411282:
                return "灵宝市";
            case 4113:
                return "南阳市";
            case 411301:
                return "市辖区";
            case 411302:
                return "宛城区";
            case 411303:
                return "卧龙区";
            case 411321:
                return "南召县";
            case 411322:
                return "方城县";
            case 411323:
                return "西峡县";
            case 411324:
                return "镇平县";
            case 411325:
                return "内乡县";
            case 411326:
                return "淅川县";
            case 411327:
                return "社旗县";
            case 411328:
                return "唐河县";
            case 411329:
                return "新野县";
            case 411330:
                return "桐柏县";
            case 411381:
                return "邓州市";
            case 4114:
                return "商丘市";
            case 411401:
                return "市辖区";
            case 411402:
                return "梁园区";
            case 411403:
                return "睢阳区";
            case 411421:
                return "民权县";
            case 411422:
                return "睢县";
            case 411423:
                return "宁陵县";
            case 411424:
                return "柘城县";
            case 411425:
                return "虞城县";
            case 411426:
                return "夏邑县";
            case 411481:
                return "永城市";
            case 4115:
                return "信阳市";
            case 411501:
                return "市辖区";
            case 411502:
                return "浉河区";
            case 411503:
                return "平桥区";
            case 411521:
                return "罗山县";
            case 411522:
                return "光山县";
            case 411523:
                return "新县";
            case 411524:
                return "商城县";
            case 411525:
                return "固始县";
            case 411526:
                return "潢川县";
            case 411527:
                return "淮滨县";
            case 411528:
                return "息县";
            case 4116:
                return "周口市";
            case 411601:
                return "市辖区";
            case 411602:
                return "川汇区";
            case 411621:
                return "扶沟县";
            case 411622:
                return "西华县";
            case 411623:
                return "商水县";
            case 411624:
                return "沈丘县";
            case 411625:
                return "郸城县";
            case 411626:
                return "淮阳县";
            case 411627:
                return "太康县";
            case 411628:
                return "鹿邑县";
            case 411681:
                return "项城市";
            case 4117:
                return "驻马店市";
            case 411701:
                return "市辖区";
            case 411702:
                return "驿城区";
            case 411721:
                return "西平县";
            case 411722:
                return "上蔡县";
            case 411723:
                return "平舆县";
            case 411724:
                return "正阳县";
            case 411725:
                return "确山县";
            case 411726:
                return "泌阳县";
            case 411727:
                return "汝南县";
            case 411728:
                return "遂平县";
            case 411729:
                return "新蔡县";
            case 4190:
                return "省直辖县级行政区划";
            case 419001:
                return "济源市";
            case 42:
                return "湖北省";
            case 4201:
                return "武汉市";
            case 420101:
                return "市辖区";
            case 420102:
                return "江岸区";
            case 420103:
                return "江汉区";
            case 420104:
                return "硚口区";
            case 420105:
                return "汉阳区";
            case 420106:
                return "武昌区";
            case 420107:
                return "青山区";
            case 420111:
                return "洪山区";
            case 420112:
                return "东西湖区";
            case 420113:
                return "汉南区";
            case 420114:
                return "蔡甸区";
            case 420115:
                return "江夏区";
            case 420116:
                return "黄陂区";
            case 420117:
                return "新洲区";
            case 4202:
                return "黄石市";
            case 420201:
                return "市辖区";
            case 420202:
                return "黄石港区";
            case 420203:
                return "西塞山区";
            case 420204:
                return "下陆区";
            case 420205:
                return "铁山区";
            case 420222:
                return "阳新县";
            case 420281:
                return "大冶市";
            case 4203:
                return "十堰市";
            case 420301:
                return "市辖区";
            case 420302:
                return "茅箭区";
            case 420303:
                return "张湾区";
            case 420304:
                return "郧阳区";
            case 420322:
                return "郧西县";
            case 420323:
                return "竹山县";
            case 420324:
                return "竹溪县";
            case 420325:
                return "房县";
            case 420381:
                return "丹江口市";
            case 4205:
                return "宜昌市";
            case 420501:
                return "市辖区";
            case 420502:
                return "西陵区";
            case 420503:
                return "伍家岗区";
            case 420504:
                return "点军区";
            case 420505:
                return "猇亭区";
            case 420506:
                return "夷陵区";
            case 420525:
                return "远安县";
            case 420526:
                return "兴山县";
            case 420527:
                return "秭归县";
            case 420528:
                return "长阳土家族自治县";
            case 420529:
                return "五峰土家族自治县";
            case 420581:
                return "宜都市";
            case 420582:
                return "当阳市";
            case 420583:
                return "枝江市";
            case 4206:
                return "襄阳市";
            case 420601:
                return "市辖区";
            case 420602:
                return "襄城区";
            case 420606:
                return "樊城区";
            case 420607:
                return "襄州区";
            case 420624:
                return "南漳县";
            case 420625:
                return "谷城县";
            case 420626:
                return "保康县";
            case 420682:
                return "老河口市";
            case 420683:
                return "枣阳市";
            case 420684:
                return "宜城市";
            case 4207:
                return "鄂州市";
            case 420701:
                return "市辖区";
            case 420702:
                return "梁子湖区";
            case 420703:
                return "华容区";
            case 420704:
                return "鄂城区";
            case 4208:
                return "荆门市";
            case 420801:
                return "市辖区";
            case 420802:
                return "东宝区";
            case 420804:
                return "掇刀区";
            case 420821:
                return "京山县";
            case 420822:
                return "沙洋县";
            case 420881:
                return "钟祥市";
            case 4209:
                return "孝感市";
            case 420901:
                return "市辖区";
            case 420902:
                return "孝南区";
            case 420921:
                return "孝昌县";
            case 420922:
                return "大悟县";
            case 420923:
                return "云梦县";
            case 420981:
                return "应城市";
            case 420982:
                return "安陆市";
            case 420984:
                return "汉川市";
            case 4210:
                return "荆州市";
            case 421001:
                return "市辖区";
            case 421002:
                return "沙市区";
            case 421003:
                return "荆州区";
            case 421022:
                return "公安县";
            case 421023:
                return "监利县";
            case 421024:
                return "江陵县";
            case 421081:
                return "石首市";
            case 421083:
                return "洪湖市";
            case 421087:
                return "松滋市";
            case 4211:
                return "黄冈市";
            case 421101:
                return "市辖区";
            case 421102:
                return "黄州区";
            case 421121:
                return "团风县";
            case 421122:
                return "红安县";
            case 421123:
                return "罗田县";
            case 421124:
                return "英山县";
            case 421125:
                return "浠水县";
            case 421126:
                return "蕲春县";
            case 421127:
                return "黄梅县";
            case 421181:
                return "麻城市";
            case 421182:
                return "武穴市";
            case 4212:
                return "咸宁市";
            case 421201:
                return "市辖区";
            case 421202:
                return "咸安区";
            case 421221:
                return "嘉鱼县";
            case 421222:
                return "通城县";
            case 421223:
                return "崇阳县";
            case 421224:
                return "通山县";
            case 421281:
                return "赤壁市";
            case 4213:
                return "随州市";
            case 421301:
                return "市辖区";
            case 421303:
                return "曾都区";
            case 421321:
                return "随县";
            case 421381:
                return "广水市";
            case 4228:
                return "恩施土家族苗族自治州";
            case 422801:
                return "恩施市";
            case 422802:
                return "利川市";
            case 422822:
                return "建始县";
            case 422823:
                return "巴东县";
            case 422825:
                return "宣恩县";
            case 422826:
                return "咸丰县";
            case 422827:
                return "来凤县";
            case 422828:
                return "鹤峰县";
            case 4290:
                return "省直辖县级行政区划";
            case 429004:
                return "仙桃市";
            case 429005:
                return "潜江市";
            case 429006:
                return "天门市";
            case 429021:
                return "神农架林区";
            case 43:
                return "湖南省";
            case 4301:
                return "长沙市";
            case 430101:
                return "市辖区";
            case 430102:
                return "芙蓉区";
            case 430103:
                return "天心区";
            case 430104:
                return "岳麓区";
            case 430105:
                return "开福区";
            case 430111:
                return "雨花区";
            case 430112:
                return "望城区";
            case 430121:
                return "长沙县";
            case 430124:
                return "宁乡县";
            case 430181:
                return "浏阳市";
            case 4302:
                return "株洲市";
            case 430201:
                return "市辖区";
            case 430202:
                return "荷塘区";
            case 430203:
                return "芦淞区";
            case 430204:
                return "石峰区";
            case 430211:
                return "天元区";
            case 430221:
                return "株洲县";
            case 430223:
                return "攸县";
            case 430224:
                return "茶陵县";
            case 430225:
                return "炎陵县";
            case 430281:
                return "醴陵市";
            case 4303:
                return "湘潭市";
            case 430301:
                return "市辖区";
            case 430302:
                return "雨湖区";
            case 430304:
                return "岳塘区";
            case 430321:
                return "湘潭县";
            case 430381:
                return "湘乡市";
            case 430382:
                return "韶山市";
            case 4304:
                return "衡阳市";
            case 430401:
                return "市辖区";
            case 430405:
                return "珠晖区";
            case 430406:
                return "雁峰区";
            case 430407:
                return "石鼓区";
            case 430408:
                return "蒸湘区";
            case 430412:
                return "南岳区";
            case 430421:
                return "衡阳县";
            case 430422:
                return "衡南县";
            case 430423:
                return "衡山县";
            case 430424:
                return "衡东县";
            case 430426:
                return "祁东县";
            case 430481:
                return "耒阳市";
            case 430482:
                return "常宁市";
            case 4305:
                return "邵阳市";
            case 430501:
                return "市辖区";
            case 430502:
                return "双清区";
            case 430503:
                return "大祥区";
            case 430511:
                return "北塔区";
            case 430521:
                return "邵东县";
            case 430522:
                return "新邵县";
            case 430523:
                return "邵阳县";
            case 430524:
                return "隆回县";
            case 430525:
                return "洞口县";
            case 430527:
                return "绥宁县";
            case 430528:
                return "新宁县";
            case 430529:
                return "城步苗族自治县";
            case 430581:
                return "武冈市";
            case 4306:
                return "岳阳市";
            case 430601:
                return "市辖区";
            case 430602:
                return "岳阳楼区";
            case 430603:
                return "云溪区";
            case 430611:
                return "君山区";
            case 430621:
                return "岳阳县";
            case 430623:
                return "华容县";
            case 430624:
                return "湘阴县";
            case 430626:
                return "平江县";
            case 430681:
                return "汨罗市";
            case 430682:
                return "临湘市";
            case 4307:
                return "常德市";
            case 430701:
                return "市辖区";
            case 430702:
                return "武陵区";
            case 430703:
                return "鼎城区";
            case 430721:
                return "安乡县";
            case 430722:
                return "汉寿县";
            case 430723:
                return "澧县";
            case 430724:
                return "临澧县";
            case 430725:
                return "桃源县";
            case 430726:
                return "石门县";
            case 430781:
                return "津市市";
            case 4308:
                return "张家界市";
            case 430801:
                return "市辖区";
            case 430802:
                return "永定区";
            case 430811:
                return "武陵源区";
            case 430821:
                return "慈利县";
            case 430822:
                return "桑植县";
            case 4309:
                return "益阳市";
            case 430901:
                return "市辖区";
            case 430902:
                return "资阳区";
            case 430903:
                return "赫山区";
            case 430921:
                return "南县";
            case 430922:
                return "桃江县";
            case 430923:
                return "安化县";
            case 430981:
                return "沅江市";
            case 4310:
                return "郴州市";
            case 431001:
                return "市辖区";
            case 431002:
                return "北湖区";
            case 431003:
                return "苏仙区";
            case 431021:
                return "桂阳县";
            case 431022:
                return "宜章县";
            case 431023:
                return "永兴县";
            case 431024:
                return "嘉禾县";
            case 431025:
                return "临武县";
            case 431026:
                return "汝城县";
            case 431027:
                return "桂东县";
            case 431028:
                return "安仁县";
            case 431081:
                return "资兴市";
            case 4311:
                return "永州市";
            case 431101:
                return "市辖区";
            case 431102:
                return "零陵区";
            case 431103:
                return "冷水滩区";
            case 431121:
                return "祁阳县";
            case 431122:
                return "东安县";
            case 431123:
                return "双牌县";
            case 431124:
                return "道县";
            case 431125:
                return "江永县";
            case 431126:
                return "宁远县";
            case 431127:
                return "蓝山县";
            case 431128:
                return "新田县";
            case 431129:
                return "江华瑶族自治县";
            case 4312:
                return "怀化市";
            case 431201:
                return "市辖区";
            case 431202:
                return "鹤城区";
            case 431221:
                return "中方县";
            case 431222:
                return "沅陵县";
            case 431223:
                return "辰溪县";
            case 431224:
                return "溆浦县";
            case 431225:
                return "会同县";
            case 431226:
                return "麻阳苗族自治县";
            case 431227:
                return "新晃侗族自治县";
            case 431228:
                return "芷江侗族自治县";
            case 431229:
                return "靖州苗族侗族自治县";
            case 431230:
                return "通道侗族自治县";
            case 431281:
                return "洪江市";
            case 4313:
                return "娄底市";
            case 431301:
                return "市辖区";
            case 431302:
                return "娄星区";
            case 431321:
                return "双峰县";
            case 431322:
                return "新化县";
            case 431381:
                return "冷水江市";
            case 431382:
                return "涟源市";
            case 4331:
                return "湘西土家族苗族自治州";
            case 433101:
                return "吉首市";
            case 433122:
                return "泸溪县";
            case 433123:
                return "凤凰县";
            case 433124:
                return "花垣县";
            case 433125:
                return "保靖县";
            case 433126:
                return "古丈县";
            case 433127:
                return "永顺县";
            case 433130:
                return "龙山县";
            case 44:
                return "广东省";
            case 4401:
                return "广州市";
            case 440101:
                return "市辖区";
            case 440103:
                return "荔湾区";
            case 440104:
                return "越秀区";
            case 440105:
                return "海珠区";
            case 440106:
                return "天河区";
            case 440111:
                return "白云区";
            case 440112:
                return "黄埔区";
            case 440113:
                return "番禺区";
            case 440114:
                return "花都区";
            case 440115:
                return "南沙区";
            case 440116:
                return "萝岗区";
            case 440117:
                return "从化区";
            case 440118:
                return "增城区";
            case 4402:
                return "韶关市";
            case 440201:
                return "市辖区";
            case 440203:
                return "武江区";
            case 440204:
                return "浈江区";
            case 440205:
                return "曲江区";
            case 440222:
                return "始兴县";
            case 440224:
                return "仁化县";
            case 440229:
                return "翁源县";
            case 440232:
                return "乳源瑶族自治县";
            case 440233:
                return "新丰县";
            case 440281:
                return "乐昌市";
            case 440282:
                return "南雄市";
            case 4403:
                return "深圳市";
            case 440301:
                return "市辖区";
            case 440303:
                return "罗湖区";
            case 440304:
                return "福田区";
            case 440305:
                return "南山区";
            case 440306:
                return "宝安区";
            case 440307:
                return "龙岗区";
            case 440308:
                return "盐田区";
            case 4404:
                return "珠海市";
            case 440401:
                return "市辖区";
            case 440402:
                return "香洲区";
            case 440403:
                return "斗门区";
            case 440404:
                return "金湾区";
            case 4405:
                return "汕头市";
            case 440501:
                return "市辖区";
            case 440507:
                return "龙湖区";
            case 440511:
                return "金平区";
            case 440512:
                return "濠江区";
            case 440513:
                return "潮阳区";
            case 440514:
                return "潮南区";
            case 440515:
                return "澄海区";
            case 440523:
                return "南澳县";
            case 4406:
                return "佛山市";
            case 440601:
                return "市辖区";
            case 440604:
                return "禅城区";
            case 440605:
                return "南海区";
            case 440606:
                return "顺德区";
            case 440607:
                return "三水区";
            case 440608:
                return "高明区";
            case 4407:
                return "江门市";
            case 440701:
                return "市辖区";
            case 440703:
                return "蓬江区";
            case 440704:
                return "江海区";
            case 440705:
                return "新会区";
            case 440781:
                return "台山市";
            case 440783:
                return "开平市";
            case 440784:
                return "鹤山市";
            case 440785:
                return "恩平市";
            case 4408:
                return "湛江市";
            case 440801:
                return "市辖区";
            case 440802:
                return "赤坎区";
            case 440803:
                return "霞山区";
            case 440804:
                return "坡头区";
            case 440811:
                return "麻章区";
            case 440823:
                return "遂溪县";
            case 440825:
                return "徐闻县";
            case 440881:
                return "廉江市";
            case 440882:
                return "雷州市";
            case 440883:
                return "吴川市";
            case 4409:
                return "茂名市";
            case 440901:
                return "市辖区";
            case 440902:
                return "茂南区";
            case 440904:
                return "电白区";
            case 440981:
                return "高州市";
            case 440982:
                return "化州市";
            case 440983:
                return "信宜市";
            case 4412:
                return "肇庆市";
            case 441201:
                return "市辖区";
            case 441202:
                return "端州区";
            case 441203:
                return "鼎湖区";
            case 441223:
                return "广宁县";
            case 441224:
                return "怀集县";
            case 441225:
                return "封开县";
            case 441226:
                return "德庆县";
            case 441283:
                return "高要市";
            case 441284:
                return "四会市";
            case 4413:
                return "惠州市";
            case 441301:
                return "市辖区";
            case 441302:
                return "惠城区";
            case 441303:
                return "惠阳区";
            case 441322:
                return "博罗县";
            case 441323:
                return "惠东县";
            case 441324:
                return "龙门县";
            case 4414:
                return "梅州市";
            case 441401:
                return "市辖区";
            case 441402:
                return "梅江区";
            case 441403:
                return "梅县区";
            case 441422:
                return "大埔县";
            case 441423:
                return "丰顺县";
            case 441424:
                return "五华县";
            case 441426:
                return "平远县";
            case 441427:
                return "蕉岭县";
            case 441481:
                return "兴宁市";
            case 4415:
                return "汕尾市";
            case 441501:
                return "市辖区";
            case 441502:
                return "城区";
            case 441521:
                return "海丰县";
            case 441523:
                return "陆河县";
            case 441581:
                return "陆丰市";
            case 4416:
                return "河源市";
            case 441601:
                return "市辖区";
            case 441602:
                return "源城区";
            case 441621:
                return "紫金县";
            case 441622:
                return "龙川县";
            case 441623:
                return "连平县";
            case 441624:
                return "和平县";
            case 441625:
                return "东源县";
            case 4417:
                return "阳江市";
            case 441701:
                return "市辖区";
            case 441702:
                return "江城区";
            case 441721:
                return "阳西县";
            case 441723:
                return "阳东县";
            case 441781:
                return "阳春市";
            case 4418:
                return "清远市";
            case 441801:
                return "市辖区";
            case 441802:
                return "清城区";
            case 441803:
                return "清新区";
            case 441821:
                return "佛冈县";
            case 441823:
                return "阳山县";
            case 441825:
                return "连山壮族瑶族自治县";
            case 441826:
                return "连南瑶族自治县";
            case 441881:
                return "英德市";
            case 441882:
                return "连州市";
            case 4419:
                return "东莞市";
            case 4420:
                return "中山市";
            case 4451:
                return "潮州市";
            case 445101:
                return "市辖区";
            case 445102:
                return "湘桥区";
            case 445103:
                return "潮安区";
            case 445122:
                return "饶平县";
            case 4452:
                return "揭阳市";
            case 445201:
                return "市辖区";
            case 445202:
                return "榕城区";
            case 445203:
                return "揭东区";
            case 445222:
                return "揭西县";
            case 445224:
                return "惠来县";
            case 445281:
                return "普宁市";
            case 4453:
                return "云浮市";
            case 445301:
                return "市辖区";
            case 445302:
                return "云城区";
            case 445303:
                return "云安区";
            case 445321:
                return "新兴县";
            case 445322:
                return "郁南县";
            case 445381:
                return "罗定市";
            case 45:
                return "广西壮族自治区";
            case 4501:
                return "南宁市";
            case 450101:
                return "市辖区";
            case 450102:
                return "兴宁区";
            case 450103:
                return "青秀区";
            case 450105:
                return "江南区";
            case 450107:
                return "西乡塘区";
            case 450108:
                return "良庆区";
            case 450109:
                return "邕宁区";
            case 450122:
                return "武鸣县";
            case 450123:
                return "隆安县";
            case 450124:
                return "马山县";
            case 450125:
                return "上林县";
            case 450126:
                return "宾阳县";
            case 450127:
                return "横县";
            case 4502:
                return "柳州市";
            case 450201:
                return "市辖区";
            case 450202:
                return "城中区";
            case 450203:
                return "鱼峰区";
            case 450204:
                return "柳南区";
            case 450205:
                return "柳北区";
            case 450221:
                return "柳江县";
            case 450222:
                return "柳城县";
            case 450223:
                return "鹿寨县";
            case 450224:
                return "融安县";
            case 450225:
                return "融水苗族自治县";
            case 450226:
                return "三江侗族自治县";
            case 4503:
                return "桂林市";
            case 450301:
                return "市辖区";
            case 450302:
                return "秀峰区";
            case 450303:
                return "叠彩区";
            case 450304:
                return "象山区";
            case 450305:
                return "七星区";
            case 450311:
                return "雁山区";
            case 450312:
                return "临桂区";
            case 450321:
                return "阳朔县";
            case 450323:
                return "灵川县";
            case 450324:
                return "全州县";
            case 450325:
                return "兴安县";
            case 450326:
                return "永福县";
            case 450327:
                return "灌阳县";
            case 450328:
                return "龙胜各族自治县";
            case 450329:
                return "资源县";
            case 450330:
                return "平乐县";
            case 450331:
                return "荔浦县";
            case 450332:
                return "恭城瑶族自治县";
            case 4504:
                return "梧州市";
            case 450401:
                return "市辖区";
            case 450403:
                return "万秀区";
            case 450405:
                return "长洲区";
            case 450406:
                return "龙圩区";
            case 450421:
                return "苍梧县";
            case 450422:
                return "藤县";
            case 450423:
                return "蒙山县";
            case 450481:
                return "岑溪市";
            case 4505:
                return "北海市";
            case 450501:
                return "市辖区";
            case 450502:
                return "海城区";
            case 450503:
                return "银海区";
            case 450512:
                return "铁山港区";
            case 450521:
                return "合浦县";
            case 4506:
                return "防城港市";
            case 450601:
                return "市辖区";
            case 450602:
                return "港口区";
            case 450603:
                return "防城区";
            case 450621:
                return "上思县";
            case 450681:
                return "东兴市";
            case 4507:
                return "钦州市";
            case 450701:
                return "市辖区";
            case 450702:
                return "钦南区";
            case 450703:
                return "钦北区";
            case 450721:
                return "灵山县";
            case 450722:
                return "浦北县";
            case 4508:
                return "贵港市";
            case 450801:
                return "市辖区";
            case 450802:
                return "港北区";
            case 450803:
                return "港南区";
            case 450804:
                return "覃塘区";
            case 450821:
                return "平南县";
            case 450881:
                return "桂平市";
            case 4509:
                return "玉林市";
            case 450901:
                return "市辖区";
            case 450902:
                return "玉州区";
            case 450903:
                return "福绵区";
            case 450921:
                return "容县";
            case 450922:
                return "陆川县";
            case 450923:
                return "博白县";
            case 450924:
                return "兴业县";
            case 450981:
                return "北流市";
            case 4510:
                return "百色市";
            case 451001:
                return "市辖区";
            case 451002:
                return "右江区";
            case 451021:
                return "田阳县";
            case 451022:
                return "田东县";
            case 451023:
                return "平果县";
            case 451024:
                return "德保县";
            case 451025:
                return "靖西县";
            case 451026:
                return "那坡县";
            case 451027:
                return "凌云县";
            case 451028:
                return "乐业县";
            case 451029:
                return "田林县";
            case 451030:
                return "西林县";
            case 451031:
                return "隆林各族自治县";
            case 4511:
                return "贺州市";
            case 451101:
                return "市辖区";
            case 451102:
                return "八步区";
            case 451121:
                return "昭平县";
            case 451122:
                return "钟山县";
            case 451123:
                return "富川瑶族自治县";
            case 4512:
                return "河池市";
            case 451201:
                return "市辖区";
            case 451202:
                return "金城江区";
            case 451221:
                return "南丹县";
            case 451222:
                return "天峨县";
            case 451223:
                return "凤山县";
            case 451224:
                return "东兰县";
            case 451225:
                return "罗城仫佬族自治县";
            case 451226:
                return "环江毛南族自治县";
            case 451227:
                return "巴马瑶族自治县";
            case 451228:
                return "都安瑶族自治县";
            case 451229:
                return "大化瑶族自治县";
            case 451281:
                return "宜州市";
            case 4513:
                return "来宾市";
            case 451301:
                return "市辖区";
            case 451302:
                return "兴宾区";
            case 451321:
                return "忻城县";
            case 451322:
                return "象州县";
            case 451323:
                return "武宣县";
            case 451324:
                return "金秀瑶族自治县";
            case 451381:
                return "合山市";
            case 4514:
                return "崇左市";
            case 451401:
                return "市辖区";
            case 451402:
                return "江州区";
            case 451421:
                return "扶绥县";
            case 451422:
                return "宁明县";
            case 451423:
                return "龙州县";
            case 451424:
                return "大新县";
            case 451425:
                return "天等县";
            case 451481:
                return "凭祥市";
            case 46:
                return "海南省";
            case 4601:
                return "海口市";
            case 460101:
                return "市辖区";
            case 460105:
                return "秀英区";
            case 460106:
                return "龙华区";
            case 460107:
                return "琼山区";
            case 460108:
                return "美兰区";
            case 4602:
                return "三亚市";
            case 460201:
                return "市辖区";
            case 460202:
                return "海棠区";
            case 460203:
                return "吉阳区";
            case 460204:
                return "天涯区";
            case 460205:
                return "崖州区";
            case 4603:
                return "三沙市";
            case 4690:
                return "省直辖县级行政区划";
            case 469001:
                return "五指山市";
            case 469002:
                return "琼海市";
            case 469003:
                return "儋州市";
            case 469005:
                return "文昌市";
            case 469006:
                return "万宁市";
            case 469007:
                return "东方市";
            case 469021:
                return "定安县";
            case 469022:
                return "屯昌县";
            case 469023:
                return "澄迈县";
            case 469024:
                return "临高县";
            case 469025:
                return "白沙黎族自治县";
            case 469026:
                return "昌江黎族自治县";
            case 469027:
                return "乐东黎族自治县";
            case 469028:
                return "陵水黎族自治县";
            case 469029:
                return "保亭黎族苗族自治县";
            case 469030:
                return "琼中黎族苗族自治县";
            case 50:
                return "重庆市";
            case 5001:
                return "市辖区";
            case 500101:
                return "万州区";
            case 500102:
                return "涪陵区";
            case 500103:
                return "渝中区";
            case 500104:
                return "大渡口区";
            case 500105:
                return "江北区";
            case 500106:
                return "沙坪坝区";
            case 500107:
                return "九龙坡区";
            case 500108:
                return "南岸区";
            case 500109:
                return "北碚区";
            case 500110:
                return "綦江区";
            case 500111:
                return "大足区";
            case 500112:
                return "渝北区";
            case 500113:
                return "巴南区";
            case 500114:
                return "黔江区";
            case 500115:
                return "长寿区";
            case 500116:
                return "江津区";
            case 500117:
                return "合川区";
            case 500118:
                return "永川区";
            case 500119:
                return "南川区";
            case 500120:
                return "璧山区";
            case 500151:
                return "铜梁区";
            case 5002:
                return "县";
            case 500223:
                return "潼南县";
            case 500226:
                return "荣昌县";
            case 500228:
                return "梁平县";
            case 500229:
                return "城口县";
            case 500230:
                return "丰都县";
            case 500231:
                return "垫江县";
            case 500232:
                return "武隆县";
            case 500233:
                return "忠县";
            case 500234:
                return "开县";
            case 500235:
                return "云阳县";
            case 500236:
                return "奉节县";
            case 500237:
                return "巫山县";
            case 500238:
                return "巫溪县";
            case 500240:
                return "石柱土家族自治县";
            case 500241:
                return "秀山土家族苗族自治县";
            case 500242:
                return "酉阳土家族苗族自治县";
            case 500243:
                return "彭水苗族土家族自治县";
            case 51:
                return "四川省";
            case 5101:
                return "成都市";
            case 510101:
                return "市辖区";
            case 510104:
                return "锦江区";
            case 510105:
                return "青羊区";
            case 510106:
                return "金牛区";
            case 510107:
                return "武侯区";
            case 510108:
                return "成华区";
            case 510112:
                return "龙泉驿区";
            case 510113:
                return "青白江区";
            case 510114:
                return "新都区";
            case 510115:
                return "温江区";
            case 510121:
                return "金堂县";
            case 510122:
                return "双流县";
            case 510124:
                return "郫县";
            case 510129:
                return "大邑县";
            case 510131:
                return "蒲江县";
            case 510132:
                return "新津县";
            case 510181:
                return "都江堰市";
            case 510182:
                return "彭州市";
            case 510183:
                return "邛崃市";
            case 510184:
                return "崇州市";
            case 5103:
                return "自贡市";
            case 510301:
                return "市辖区";
            case 510302:
                return "自流井区";
            case 510303:
                return "贡井区";
            case 510304:
                return "大安区";
            case 510311:
                return "沿滩区";
            case 510321:
                return "荣县";
            case 510322:
                return "富顺县";
            case 5104:
                return "攀枝花市";
            case 510401:
                return "市辖区";
            case 510402:
                return "东区";
            case 510403:
                return "西区";
            case 510411:
                return "仁和区";
            case 510421:
                return "米易县";
            case 510422:
                return "盐边县";
            case 5105:
                return "泸州市";
            case 510501:
                return "市辖区";
            case 510502:
                return "江阳区";
            case 510503:
                return "纳溪区";
            case 510504:
                return "龙马潭区";
            case 510521:
                return "泸县";
            case 510522:
                return "合江县";
            case 510524:
                return "叙永县";
            case 510525:
                return "古蔺县";
            case 5106:
                return "德阳市";
            case 510601:
                return "市辖区";
            case 510603:
                return "旌阳区";
            case 510623:
                return "中江县";
            case 510626:
                return "罗江县";
            case 510681:
                return "广汉市";
            case 510682:
                return "什邡市";
            case 510683:
                return "绵竹市";
            case 5107:
                return "绵阳市";
            case 510701:
                return "市辖区";
            case 510703:
                return "涪城区";
            case 510704:
                return "游仙区";
            case 510722:
                return "三台县";
            case 510723:
                return "盐亭县";
            case 510724:
                return "安县";
            case 510725:
                return "梓潼县";
            case 510726:
                return "北川羌族自治县";
            case 510727:
                return "平武县";
            case 510781:
                return "江油市";
            case 5108:
                return "广元市";
            case 510801:
                return "市辖区";
            case 510802:
                return "利州区";
            case 510811:
                return "昭化区";
            case 510812:
                return "朝天区";
            case 510821:
                return "旺苍县";
            case 510822:
                return "青川县";
            case 510823:
                return "剑阁县";
            case 510824:
                return "苍溪县";
            case 5109:
                return "遂宁市";
            case 510901:
                return "市辖区";
            case 510903:
                return "船山区";
            case 510904:
                return "安居区";
            case 510921:
                return "蓬溪县";
            case 510922:
                return "射洪县";
            case 510923:
                return "大英县";
            case 5110:
                return "内江市";
            case 511001:
                return "市辖区";
            case 511002:
                return "市中区";
            case 511011:
                return "东兴区";
            case 511024:
                return "威远县";
            case 511025:
                return "资中县";
            case 511028:
                return "隆昌县";
            case 5111:
                return "乐山市";
            case 511101:
                return "市辖区";
            case 511102:
                return "市中区";
            case 511111:
                return "沙湾区";
            case 511112:
                return "五通桥区";
            case 511113:
                return "金口河区";
            case 511123:
                return "犍为县";
            case 511124:
                return "井研县";
            case 511126:
                return "夹江县";
            case 511129:
                return "沐川县";
            case 511132:
                return "峨边彝族自治县";
            case 511133:
                return "马边彝族自治县";
            case 511181:
                return "峨眉山市";
            case 5113:
                return "南充市";
            case 511301:
                return "市辖区";
            case 511302:
                return "顺庆区";
            case 511303:
                return "高坪区";
            case 511304:
                return "嘉陵区";
            case 511321:
                return "南部县";
            case 511322:
                return "营山县";
            case 511323:
                return "蓬安县";
            case 511324:
                return "仪陇县";
            case 511325:
                return "西充县";
            case 511381:
                return "阆中市";
            case 5114:
                return "眉山市";
            case 511401:
                return "市辖区";
            case 511402:
                return "东坡区";
            case 511421:
                return "仁寿县";
            case 511422:
                return "彭山县";
            case 511423:
                return "洪雅县";
            case 511424:
                return "丹棱县";
            case 511425:
                return "青神县";
            case 5115:
                return "宜宾市";
            case 511501:
                return "市辖区";
            case 511502:
                return "翠屏区";
            case 511503:
                return "南溪区";
            case 511521:
                return "宜宾县";
            case 511523:
                return "江安县";
            case 511524:
                return "长宁县";
            case 511525:
                return "高县";
            case 511526:
                return "珙县";
            case 511527:
                return "筠连县";
            case 511528:
                return "兴文县";
            case 511529:
                return "屏山县";
            case 5116:
                return "广安市";
            case 511601:
                return "市辖区";
            case 511602:
                return "广安区";
            case 511603:
                return "前锋区";
            case 511621:
                return "岳池县";
            case 511622:
                return "武胜县";
            case 511623:
                return "邻水县";
            case 511681:
                return "华蓥市";
            case 5117:
                return "达州市";
            case 511701:
                return "市辖区";
            case 511702:
                return "通川区";
            case 511703:
                return "达川区";
            case 511722:
                return "宣汉县";
            case 511723:
                return "开江县";
            case 511724:
                return "大竹县";
            case 511725:
                return "渠县";
            case 511781:
                return "万源市";
            case 5118:
                return "雅安市";
            case 511801:
                return "市辖区";
            case 511802:
                return "雨城区";
            case 511803:
                return "名山区";
            case 511822:
                return "荥经县";
            case 511823:
                return "汉源县";
            case 511824:
                return "石棉县";
            case 511825:
                return "天全县";
            case 511826:
                return "芦山县";
            case 511827:
                return "宝兴县";
            case 5119:
                return "巴中市";
            case 511901:
                return "市辖区";
            case 511902:
                return "巴州区";
            case 511903:
                return "恩阳区";
            case 511921:
                return "通江县";
            case 511922:
                return "南江县";
            case 511923:
                return "平昌县";
            case 5120:
                return "资阳市";
            case 512001:
                return "市辖区";
            case 512002:
                return "雁江区";
            case 512021:
                return "安岳县";
            case 512022:
                return "乐至县";
            case 512081:
                return "简阳市";
            case 5132:
                return "阿坝藏族羌族自治州";
            case 513221:
                return "汶川县";
            case 513222:
                return "理县";
            case 513223:
                return "茂县";
            case 513224:
                return "松潘县";
            case 513225:
                return "九寨沟县";
            case 513226:
                return "金川县";
            case 513227:
                return "小金县";
            case 513228:
                return "黑水县";
            case 513229:
                return "马尔康县";
            case 513230:
                return "壤塘县";
            case 513231:
                return "阿坝县";
            case 513232:
                return "若尔盖县";
            case 513233:
                return "红原县";
            case 5133:
                return "甘孜藏族自治州";
            case 513321:
                return "康定县";
            case 513322:
                return "泸定县";
            case 513323:
                return "丹巴县";
            case 513324:
                return "九龙县";
            case 513325:
                return "雅江县";
            case 513326:
                return "道孚县";
            case 513327:
                return "炉霍县";
            case 513328:
                return "甘孜县";
            case 513329:
                return "新龙县";
            case 513330:
                return "德格县";
            case 513331:
                return "白玉县";
            case 513332:
                return "石渠县";
            case 513333:
                return "色达县";
            case 513334:
                return "理塘县";
            case 513335:
                return "巴塘县";
            case 513336:
                return "乡城县";
            case 513337:
                return "稻城县";
            case 513338:
                return "得荣县";
            case 5134:
                return "凉山彝族自治州";
            case 513401:
                return "西昌市";
            case 513422:
                return "木里藏族自治县";
            case 513423:
                return "盐源县";
            case 513424:
                return "德昌县";
            case 513425:
                return "会理县";
            case 513426:
                return "会东县";
            case 513427:
                return "宁南县";
            case 513428:
                return "普格县";
            case 513429:
                return "布拖县";
            case 513430:
                return "金阳县";
            case 513431:
                return "昭觉县";
            case 513432:
                return "喜德县";
            case 513433:
                return "冕宁县";
            case 513434:
                return "越西县";
            case 513435:
                return "甘洛县";
            case 513436:
                return "美姑县";
            case 513437:
                return "雷波县";
            case 52:
                return "贵州省";
            case 5201:
                return "贵阳市";
            case 520101:
                return "市辖区";
            case 520102:
                return "南明区";
            case 520103:
                return "云岩区";
            case 520111:
                return "花溪区";
            case 520112:
                return "乌当区";
            case 520113:
                return "白云区";
            case 520115:
                return "观山湖区";
            case 520121:
                return "开阳县";
            case 520122:
                return "息烽县";
            case 520123:
                return "修文县";
            case 520181:
                return "清镇市";
            case 5202:
                return "六盘水市";
            case 520201:
                return "钟山区";
            case 520203:
                return "六枝特区";
            case 520221:
                return "水城县";
            case 520222:
                return "盘县";
            case 5203:
                return "遵义市";
            case 520301:
                return "市辖区";
            case 520302:
                return "红花岗区";
            case 520303:
                return "汇川区";
            case 520321:
                return "遵义县";
            case 520322:
                return "桐梓县";
            case 520323:
                return "绥阳县";
            case 520324:
                return "正安县";
            case 520325:
                return "道真仡佬族苗族自治县";
            case 520326:
                return "务川仡佬族苗族自治县";
            case 520327:
                return "凤冈县";
            case 520328:
                return "湄潭县";
            case 520329:
                return "余庆县";
            case 520330:
                return "习水县";
            case 520381:
                return "赤水市";
            case 520382:
                return "仁怀市";
            case 5204:
                return "安顺市";
            case 520401:
                return "市辖区";
            case 520402:
                return "西秀区";
            case 520421:
                return "平坝县";
            case 520422:
                return "普定县";
            case 520423:
                return "镇宁布依族苗族自治县";
            case 520424:
                return "关岭布依族苗族自治县";
            case 520425:
                return "紫云苗族布依族自治县";
            case 5205:
                return "毕节市";
            case 520501:
                return "市辖区";
            case 520502:
                return "七星关区";
            case 520521:
                return "大方县";
            case 520522:
                return "黔西县";
            case 520523:
                return "金沙县";
            case 520524:
                return "织金县";
            case 520525:
                return "纳雍县";
            case 520526:
                return "威宁彝族回族苗族自治县";
            case 520527:
                return "赫章县";
            case 5206:
                return "铜仁市";
            case 520601:
                return "市辖区";
            case 520602:
                return "碧江区";
            case 520603:
                return "万山区";
            case 520621:
                return "江口县";
            case 520622:
                return "玉屏侗族自治县";
            case 520623:
                return "石阡县";
            case 520624:
                return "思南县";
            case 520625:
                return "印江土家族苗族自治县";
            case 520626:
                return "德江县";
            case 520627:
                return "沿河土家族自治县";
            case 520628:
                return "松桃苗族自治县";
            case 5223:
                return "黔西南布依族苗族自治州";
            case 522301:
                return "兴义市";
            case 522322:
                return "兴仁县";
            case 522323:
                return "普安县";
            case 522324:
                return "晴隆县";
            case 522325:
                return "贞丰县";
            case 522326:
                return "望谟县";
            case 522327:
                return "册亨县";
            case 522328:
                return "安龙县";
            case 5226:
                return "黔东南苗族侗族自治州";
            case 522601:
                return "凯里市";
            case 522622:
                return "黄平县";
            case 522623:
                return "施秉县";
            case 522624:
                return "三穗县";
            case 522625:
                return "镇远县";
            case 522626:
                return "岑巩县";
            case 522627:
                return "天柱县";
            case 522628:
                return "锦屏县";
            case 522629:
                return "剑河县";
            case 522630:
                return "台江县";
            case 522631:
                return "黎平县";
            case 522632:
                return "榕江县";
            case 522633:
                return "从江县";
            case 522634:
                return "雷山县";
            case 522635:
                return "麻江县";
            case 522636:
                return "丹寨县";
            case 5227:
                return "黔南布依族苗族自治州";
            case 522701:
                return "都匀市";
            case 522702:
                return "福泉市";
            case 522722:
                return "荔波县";
            case 522723:
                return "贵定县";
            case 522725:
                return "瓮安县";
            case 522726:
                return "独山县";
            case 522727:
                return "平塘县";
            case 522728:
                return "罗甸县";
            case 522729:
                return "长顺县";
            case 522730:
                return "龙里县";
            case 522731:
                return "惠水县";
            case 522732:
                return "三都水族自治县";
            case 53:
                return "云南省";
            case 5301:
                return "昆明市";
            case 530101:
                return "市辖区";
            case 530102:
                return "五华区";
            case 530103:
                return "盘龙区";
            case 530111:
                return "官渡区";
            case 530112:
                return "西山区";
            case 530113:
                return "东川区";
            case 530114:
                return "呈贡区";
            case 530122:
                return "晋宁县";
            case 530124:
                return "富民县";
            case 530125:
                return "宜良县";
            case 530126:
                return "石林彝族自治县";
            case 530127:
                return "嵩明县";
            case 530128:
                return "禄劝彝族苗族自治县";
            case 530129:
                return "寻甸回族彝族自治县";
            case 530181:
                return "安宁市";
            case 5303:
                return "曲靖市";
            case 530301:
                return "市辖区";
            case 530302:
                return "麒麟区";
            case 530321:
                return "马龙县";
            case 530322:
                return "陆良县";
            case 530323:
                return "师宗县";
            case 530324:
                return "罗平县";
            case 530325:
                return "富源县";
            case 530326:
                return "会泽县";
            case 530328:
                return "沾益县";
            case 530381:
                return "宣威市";
            case 5304:
                return "玉溪市";
            case 530401:
                return "市辖区";
            case 530402:
                return "红塔区";
            case 530421:
                return "江川县";
            case 530422:
                return "澄江县";
            case 530423:
                return "通海县";
            case 530424:
                return "华宁县";
            case 530425:
                return "易门县";
            case 530426:
                return "峨山彝族自治县";
            case 530427:
                return "新平彝族傣族自治县";
            case 530428:
                return "元江哈尼族彝族傣族自治县";
            case 5305:
                return "保山市";
            case 530501:
                return "市辖区";
            case 530502:
                return "隆阳区";
            case 530521:
                return "施甸县";
            case 530522:
                return "腾冲县";
            case 530523:
                return "龙陵县";
            case 530524:
                return "昌宁县";
            case 5306:
                return "昭通市";
            case 530601:
                return "市辖区";
            case 530602:
                return "昭阳区";
            case 530621:
                return "鲁甸县";
            case 530622:
                return "巧家县";
            case 530623:
                return "盐津县";
            case 530624:
                return "大关县";
            case 530625:
                return "永善县";
            case 530626:
                return "绥江县";
            case 530627:
                return "镇雄县";
            case 530628:
                return "彝良县";
            case 530629:
                return "威信县";
            case 530630:
                return "水富县";
            case 5307:
                return "丽江市";
            case 530701:
                return "市辖区";
            case 530702:
                return "古城区";
            case 530721:
                return "玉龙纳西族自治县";
            case 530722:
                return "永胜县";
            case 530723:
                return "华坪县";
            case 530724:
                return "宁蒗彝族自治县";
            case 5308:
                return "普洱市";
            case 530801:
                return "市辖区";
            case 530802:
                return "思茅区";
            case 530821:
                return "宁洱哈尼族彝族自治县";
            case 530822:
                return "墨江哈尼族自治县";
            case 530823:
                return "景东彝族自治县";
            case 530824:
                return "景谷傣族彝族自治县";
            case 530825:
                return "镇沅彝族哈尼族拉祜族自治县";
            case 530826:
                return "江城哈尼族彝族自治县";
            case 530827:
                return "孟连傣族拉祜族佤族自治县";
            case 530828:
                return "澜沧拉祜族自治县";
            case 530829:
                return "西盟佤族自治县";
            case 5309:
                return "临沧市";
            case 530901:
                return "市辖区";
            case 530902:
                return "临翔区";
            case 530921:
                return "凤庆县";
            case 530922:
                return "云县";
            case 530923:
                return "永德县";
            case 530924:
                return "镇康县";
            case 530925:
                return "双江拉祜族佤族布朗族傣族自治县";
            case 530926:
                return "耿马傣族佤族自治县";
            case 530927:
                return "沧源佤族自治县";
            case 5323:
                return "楚雄彝族自治州";
            case 532301:
                return "楚雄市";
            case 532322:
                return "双柏县";
            case 532323:
                return "牟定县";
            case 532324:
                return "南华县";
            case 532325:
                return "姚安县";
            case 532326:
                return "大姚县";
            case 532327:
                return "永仁县";
            case 532328:
                return "元谋县";
            case 532329:
                return "武定县";
            case 532331:
                return "禄丰县";
            case 5325:
                return "红河哈尼族彝族自治州";
            case 532501:
                return "个旧市";
            case 532502:
                return "开远市";
            case 532503:
                return "蒙自市";
            case 532504:
                return "弥勒市";
            case 532523:
                return "屏边苗族自治县";
            case 532524:
                return "建水县";
            case 532525:
                return "石屏县";
            case 532527:
                return "泸西县";
            case 532528:
                return "元阳县";
            case 532529:
                return "红河县";
            case 532530:
                return "金平苗族瑶族傣族自治县";
            case 532531:
                return "绿春县";
            case 532532:
                return "河口瑶族自治县";
            case 5326:
                return "文山壮族苗族自治州";
            case 532601:
                return "文山市";
            case 532622:
                return "砚山县";
            case 532623:
                return "西畴县";
            case 532624:
                return "麻栗坡县";
            case 532625:
                return "马关县";
            case 532626:
                return "丘北县";
            case 532627:
                return "广南县";
            case 532628:
                return "富宁县";
            case 5328:
                return "西双版纳傣族自治州";
            case 532801:
                return "景洪市";
            case 532822:
                return "勐海县";
            case 532823:
                return "勐腊县";
            case 5329:
                return "大理白族自治州";
            case 532901:
                return "大理市";
            case 532922:
                return "漾濞彝族自治县";
            case 532923:
                return "祥云县";
            case 532924:
                return "宾川县";
            case 532925:
                return "弥渡县";
            case 532926:
                return "南涧彝族自治县";
            case 532927:
                return "巍山彝族回族自治县";
            case 532928:
                return "永平县";
            case 532929:
                return "云龙县";
            case 532930:
                return "洱源县";
            case 532931:
                return "剑川县";
            case 532932:
                return "鹤庆县";
            case 5331:
                return "德宏傣族景颇族自治州";
            case 533102:
                return "瑞丽市";
            case 533103:
                return "芒市";
            case 533122:
                return "梁河县";
            case 533123:
                return "盈江县";
            case 533124:
                return "陇川县";
            case 5333:
                return "怒江傈僳族自治州";
            case 533321:
                return "泸水县";
            case 533323:
                return "福贡县";
            case 533324:
                return "贡山独龙族怒族自治县";
            case 533325:
                return "兰坪白族普米族自治县";
            case 5334:
                return "迪庆藏族自治州";
            case 533421:
                return "香格里拉县";
            case 533422:
                return "德钦县";
            case 533423:
                return "维西傈僳族自治县";
            case 54:
                return "西藏自治区";
            case 5401:
                return "拉萨市";
            case 540101:
                return "市辖区";
            case 540102:
                return "城关区";
            case 540121:
                return "林周县";
            case 540122:
                return "当雄县";
            case 540123:
                return "尼木县";
            case 540124:
                return "曲水县";
            case 540125:
                return "堆龙德庆县";
            case 540126:
                return "达孜县";
            case 540127:
                return "墨竹工卡县";
            case 5402:
                return "日喀则市";
            case 540202:
                return "桑珠孜区";
            case 540221:
                return "南木林县";
            case 540222:
                return "江孜县";
            case 540223:
                return "定日县";
            case 540224:
                return "萨迦县";
            case 540225:
                return "拉孜县";
            case 540226:
                return "昂仁县";
            case 540227:
                return "谢通门县";
            case 540228:
                return "白朗县";
            case 540229:
                return "仁布县";
            case 540230:
                return "康马县";
            case 540231:
                return "定结县";
            case 540232:
                return "仲巴县";
            case 540233:
                return "亚东县";
            case 540234:
                return "吉隆县";
            case 540235:
                return "聂拉木县";
            case 540236:
                return "萨嘎县";
            case 540237:
                return "岗巴县";
            case 5421:
                return "昌都地区";
            case 542121:
                return "昌都县";
            case 542122:
                return "江达县";
            case 542123:
                return "贡觉县";
            case 542124:
                return "类乌齐县";
            case 542125:
                return "丁青县";
            case 542126:
                return "察雅县";
            case 542127:
                return "八宿县";
            case 542128:
                return "左贡县";
            case 542129:
                return "芒康县";
            case 542132:
                return "洛隆县";
            case 542133:
                return "边坝县";
            case 5422:
                return "山南地区";
            case 542221:
                return "乃东县";
            case 542222:
                return "扎囊县";
            case 542223:
                return "贡嘎县";
            case 542224:
                return "桑日县";
            case 542225:
                return "琼结县";
            case 542226:
                return "曲松县";
            case 542227:
                return "措美县";
            case 542228:
                return "洛扎县";
            case 542229:
                return "加查县";
            case 542231:
                return "隆子县";
            case 542232:
                return "错那县";
            case 542233:
                return "浪卡子县";
            case 5424:
                return "那曲地区";
            case 542421:
                return "那曲县";
            case 542422:
                return "嘉黎县";
            case 542423:
                return "比如县";
            case 542424:
                return "聂荣县";
            case 542425:
                return "安多县";
            case 542426:
                return "申扎县";
            case 542427:
                return "索县";
            case 542428:
                return "班戈县";
            case 542429:
                return "巴青县";
            case 542430:
                return "尼玛县";
            case 542431:
                return "双湖县";
            case 5425:
                return "阿里地区";
            case 542521:
                return "普兰县";
            case 542522:
                return "札达县";
            case 542523:
                return "噶尔县";
            case 542524:
                return "日土县";
            case 542525:
                return "革吉县";
            case 542526:
                return "改则县";
            case 542527:
                return "措勤县";
            case 5426:
                return "林芝地区";
            case 542621:
                return "林芝县";
            case 542622:
                return "工布江达县";
            case 542623:
                return "米林县";
            case 542624:
                return "墨脱县";
            case 542625:
                return "波密县";
            case 542626:
                return "察隅县";
            case 542627:
                return "朗县";
            case 61:
                return "陕西省";
            case 6101:
                return "西安市";
            case 610101:
                return "市辖区";
            case 610102:
                return "新城区";
            case 610103:
                return "碑林区";
            case 610104:
                return "莲湖区";
            case 610111:
                return "灞桥区";
            case 610112:
                return "未央区";
            case 610113:
                return "雁塔区";
            case 610114:
                return "阎良区";
            case 610115:
                return "临潼区";
            case 610116:
                return "长安区";
            case 610122:
                return "蓝田县";
            case 610124:
                return "周至县";
            case 610125:
                return "户县";
            case 610126:
                return "高陵县";
            case 6102:
                return "铜川市";
            case 610201:
                return "市辖区";
            case 610202:
                return "王益区";
            case 610203:
                return "印台区";
            case 610204:
                return "耀州区";
            case 610222:
                return "宜君县";
            case 6103:
                return "宝鸡市";
            case 610301:
                return "市辖区";
            case 610302:
                return "渭滨区";
            case 610303:
                return "金台区";
            case 610304:
                return "陈仓区";
            case 610322:
                return "凤翔县";
            case 610323:
                return "岐山县";
            case 610324:
                return "扶风县";
            case 610326:
                return "眉县";
            case 610327:
                return "陇县";
            case 610328:
                return "千阳县";
            case 610329:
                return "麟游县";
            case 610330:
                return "凤县";
            case 610331:
                return "太白县";
            case 6104:
                return "咸阳市";
            case 610401:
                return "市辖区";
            case 610402:
                return "秦都区";
            case 610403:
                return "杨陵区";
            case 610404:
                return "渭城区";
            case 610422:
                return "三原县";
            case 610423:
                return "泾阳县";
            case 610424:
                return "乾县";
            case 610425:
                return "礼泉县";
            case 610426:
                return "永寿县";
            case 610427:
                return "彬县";
            case 610428:
                return "长武县";
            case 610429:
                return "旬邑县";
            case 610430:
                return "淳化县";
            case 610431:
                return "武功县";
            case 610481:
                return "兴平市";
            case 6105:
                return "渭南市";
            case 610501:
                return "市辖区";
            case 610502:
                return "临渭区";
            case 610521:
                return "华县";
            case 610522:
                return "潼关县";
            case 610523:
                return "大荔县";
            case 610524:
                return "合阳县";
            case 610525:
                return "澄城县";
            case 610526:
                return "蒲城县";
            case 610527:
                return "白水县";
            case 610528:
                return "富平县";
            case 610581:
                return "韩城市";
            case 610582:
                return "华阴市";
            case 6106:
                return "延安市";
            case 610601:
                return "市辖区";
            case 610602:
                return "宝塔区";
            case 610621:
                return "延长县";
            case 610622:
                return "延川县";
            case 610623:
                return "子长县";
            case 610624:
                return "安塞县";
            case 610625:
                return "志丹县";
            case 610626:
                return "吴起县";
            case 610627:
                return "甘泉县";
            case 610628:
                return "富县";
            case 610629:
                return "洛川县";
            case 610630:
                return "宜川县";
            case 610631:
                return "黄龙县";
            case 610632:
                return "黄陵县";
            case 6107:
                return "汉中市";
            case 610701:
                return "市辖区";
            case 610702:
                return "汉台区";
            case 610721:
                return "南郑县";
            case 610722:
                return "城固县";
            case 610723:
                return "洋县";
            case 610724:
                return "西乡县";
            case 610725:
                return "勉县";
            case 610726:
                return "宁强县";
            case 610727:
                return "略阳县";
            case 610728:
                return "镇巴县";
            case 610729:
                return "留坝县";
            case 610730:
                return "佛坪县";
            case 6108:
                return "榆林市";
            case 610801:
                return "市辖区";
            case 610802:
                return "榆阳区";
            case 610821:
                return "神木县";
            case 610822:
                return "府谷县";
            case 610823:
                return "横山县";
            case 610824:
                return "靖边县";
            case 610825:
                return "定边县";
            case 610826:
                return "绥德县";
            case 610827:
                return "米脂县";
            case 610828:
                return "佳县";
            case 610829:
                return "吴堡县";
            case 610830:
                return "清涧县";
            case 610831:
                return "子洲县";
            case 6109:
                return "安康市";
            case 610901:
                return "市辖区";
            case 610902:
                return "汉滨区";
            case 610921:
                return "汉阴县";
            case 610922:
                return "石泉县";
            case 610923:
                return "宁陕县";
            case 610924:
                return "紫阳县";
            case 610925:
                return "岚皋县";
            case 610926:
                return "平利县";
            case 610927:
                return "镇坪县";
            case 610928:
                return "旬阳县";
            case 610929:
                return "白河县";
            case 6110:
                return "商洛市";
            case 611001:
                return "市辖区";
            case 611002:
                return "商州区";
            case 611021:
                return "洛南县";
            case 611022:
                return "丹凤县";
            case 611023:
                return "商南县";
            case 611024:
                return "山阳县";
            case 611025:
                return "镇安县";
            case 611026:
                return "柞水县";
            case 62:
                return "甘肃省";
            case 6201:
                return "兰州市";
            case 620101:
                return "市辖区";
            case 620102:
                return "城关区";
            case 620103:
                return "七里河区";
            case 620104:
                return "西固区";
            case 620105:
                return "安宁区";
            case 620111:
                return "红古区";
            case 620121:
                return "永登县";
            case 620122:
                return "皋兰县";
            case 620123:
                return "榆中县";
            case 6202:
                return "嘉峪关市";
            case 620201:
                return "市辖区";
            case 6203:
                return "金昌市";
            case 620301:
                return "市辖区";
            case 620302:
                return "金川区";
            case 620321:
                return "永昌县";
            case 6204:
                return "白银市";
            case 620401:
                return "市辖区";
            case 620402:
                return "白银区";
            case 620403:
                return "平川区";
            case 620421:
                return "靖远县";
            case 620422:
                return "会宁县";
            case 620423:
                return "景泰县";
            case 6205:
                return "天水市";
            case 620501:
                return "市辖区";
            case 620502:
                return "秦州区";
            case 620503:
                return "麦积区";
            case 620521:
                return "清水县";
            case 620522:
                return "秦安县";
            case 620523:
                return "甘谷县";
            case 620524:
                return "武山县";
            case 620525:
                return "张家川回族自治县";
            case 6206:
                return "武威市";
            case 620601:
                return "市辖区";
            case 620602:
                return "凉州区";
            case 620621:
                return "民勤县";
            case 620622:
                return "古浪县";
            case 620623:
                return "天祝藏族自治县";
            case 6207:
                return "张掖市";
            case 620701:
                return "市辖区";
            case 620702:
                return "甘州区";
            case 620721:
                return "肃南裕固族自治县";
            case 620722:
                return "民乐县";
            case 620723:
                return "临泽县";
            case 620724:
                return "高台县";
            case 620725:
                return "山丹县";
            case 6208:
                return "平凉市";
            case 620801:
                return "市辖区";
            case 620802:
                return "崆峒区";
            case 620821:
                return "泾川县";
            case 620822:
                return "灵台县";
            case 620823:
                return "崇信县";
            case 620824:
                return "华亭县";
            case 620825:
                return "庄浪县";
            case 620826:
                return "静宁县";
            case 6209:
                return "酒泉市";
            case 620901:
                return "市辖区";
            case 620902:
                return "肃州区";
            case 620921:
                return "金塔县";
            case 620922:
                return "瓜州县";
            case 620923:
                return "肃北蒙古族自治县";
            case 620924:
                return "阿克塞哈萨克族自治县";
            case 620981:
                return "玉门市";
            case 620982:
                return "敦煌市";
            case 6210:
                return "庆阳市";
            case 621001:
                return "市辖区";
            case 621002:
                return "西峰区";
            case 621021:
                return "庆城县";
            case 621022:
                return "环县";
            case 621023:
                return "华池县";
            case 621024:
                return "合水县";
            case 621025:
                return "正宁县";
            case 621026:
                return "宁县";
            case 621027:
                return "镇原县";
            case 6211:
                return "定西市";
            case 621101:
                return "市辖区";
            case 621102:
                return "安定区";
            case 621121:
                return "通渭县";
            case 621122:
                return "陇西县";
            case 621123:
                return "渭源县";
            case 621124:
                return "临洮县";
            case 621125:
                return "漳县";
            case 621126:
                return "岷县";
            case 6212:
                return "陇南市";
            case 621201:
                return "市辖区";
            case 621202:
                return "武都区";
            case 621221:
                return "成县";
            case 621222:
                return "文县";
            case 621223:
                return "宕昌县";
            case 621224:
                return "康县";
            case 621225:
                return "西和县";
            case 621226:
                return "礼县";
            case 621227:
                return "徽县";
            case 621228:
                return "两当县";
            case 6229:
                return "临夏回族自治州";
            case 622901:
                return "临夏市";
            case 622921:
                return "临夏县";
            case 622922:
                return "康乐县";
            case 622923:
                return "永靖县";
            case 622924:
                return "广河县";
            case 622925:
                return "和政县";
            case 622926:
                return "东乡族自治县";
            case 622927:
                return "积石山保安族东乡族撒拉族自治县";
            case 6230:
                return "甘南藏族自治州";
            case 623001:
                return "合作市";
            case 623021:
                return "临潭县";
            case 623022:
                return "卓尼县";
            case 623023:
                return "舟曲县";
            case 623024:
                return "迭部县";
            case 623025:
                return "玛曲县";
            case 623026:
                return "碌曲县";
            case 623027:
                return "夏河县";
            case 63:
                return "青海省";
            case 6301:
                return "西宁市";
            case 630101:
                return "市辖区";
            case 630102:
                return "城东区";
            case 630103:
                return "城中区";
            case 630104:
                return "城西区";
            case 630105:
                return "城北区";
            case 630121:
                return "大通回族土族自治县";
            case 630122:
                return "湟中县";
            case 630123:
                return "湟源县";
            case 6302:
                return "海东市";
            case 630202:
                return "乐都区";
            case 630221:
                return "平安县";
            case 630222:
                return "民和回族土族自治县";
            case 630223:
                return "互助土族自治县";
            case 630224:
                return "化隆回族自治县";
            case 630225:
                return "循化撒拉族自治县";
            case 6322:
                return "海北藏族自治州";
            case 632221:
                return "门源回族自治县";
            case 632222:
                return "祁连县";
            case 632223:
                return "海晏县";
            case 632224:
                return "刚察县";
            case 6323:
                return "黄南藏族自治州";
            case 632321:
                return "同仁县";
            case 632322:
                return "尖扎县";
            case 632323:
                return "泽库县";
            case 632324:
                return "河南蒙古族自治县";
            case 6325:
                return "海南藏族自治州";
            case 632521:
                return "共和县";
            case 632522:
                return "同德县";
            case 632523:
                return "贵德县";
            case 632524:
                return "兴海县";
            case 632525:
                return "贵南县";
            case 6326:
                return "果洛藏族自治州";
            case 632621:
                return "玛沁县";
            case 632622:
                return "班玛县";
            case 632623:
                return "甘德县";
            case 632624:
                return "达日县";
            case 632625:
                return "久治县";
            case 632626:
                return "玛多县";
            case 6327:
                return "玉树藏族自治州";
            case 632701:
                return "玉树市";
            case 632722:
                return "杂多县";
            case 632723:
                return "称多县";
            case 632724:
                return "治多县";
            case 632725:
                return "囊谦县";
            case 632726:
                return "曲麻莱县";
            case 6328:
                return "海西蒙古族藏族自治州";
            case 632801:
                return "格尔木市";
            case 632802:
                return "德令哈市";
            case 632821:
                return "乌兰县";
            case 632822:
                return "都兰县";
            case 632823:
                return "天峻县";
            case 64:
                return "宁夏回族自治区";
            case 6401:
                return "银川市";
            case 640101:
                return "市辖区";
            case 640104:
                return "兴庆区";
            case 640105:
                return "西夏区";
            case 640106:
                return "金凤区";
            case 640121:
                return "永宁县";
            case 640122:
                return "贺兰县";
            case 640181:
                return "灵武市";
            case 6402:
                return "石嘴山市";
            case 640201:
                return "市辖区";
            case 640202:
                return "大武口区";
            case 640205:
                return "惠农区";
            case 640221:
                return "平罗县";
            case 6403:
                return "吴忠市";
            case 640301:
                return "市辖区";
            case 640302:
                return "利通区";
            case 640303:
                return "红寺堡区";
            case 640323:
                return "盐池县";
            case 640324:
                return "同心县";
            case 640381:
                return "青铜峡市";
            case 6404:
                return "固原市";
            case 640401:
                return "市辖区";
            case 640402:
                return "原州区";
            case 640422:
                return "西吉县";
            case 640423:
                return "隆德县";
            case 640424:
                return "泾源县";
            case 640425:
                return "彭阳县";
            case 6405:
                return "中卫市";
            case 640501:
                return "市辖区";
            case 640502:
                return "沙坡头区";
            case 640521:
                return "中宁县";
            case 640522:
                return "海原县";
            case 65:
                return "新疆维吾尔自治区";
            case 6501:
                return "乌鲁木齐市";
            case 650101:
                return "市辖区";
            case 650102:
                return "天山区";
            case 650103:
                return "沙依巴克区";
            case 650104:
                return "新市区";
            case 650105:
                return "水磨沟区";
            case 650106:
                return "头屯河区";
            case 650107:
                return "达坂城区";
            case 650109:
                return "米东区";
            case 650121:
                return "乌鲁木齐县";
            case 6502:
                return "克拉玛依市";
            case 650201:
                return "市辖区";
            case 650202:
                return "独山子区";
            case 650203:
                return "克拉玛依区";
            case 650204:
                return "白碱滩区";
            case 650205:
                return "乌尔禾区";
            case 6521:
                return "吐鲁番地区";
            case 652101:
                return "吐鲁番市";
            case 652122:
                return "鄯善县";
            case 652123:
                return "托克逊县";
            case 6522:
                return "哈密地区";
            case 652201:
                return "哈密市";
            case 652222:
                return "巴里坤哈萨克自治县";
            case 652223:
                return "伊吾县";
            case 6523:
                return "昌吉回族自治州";
            case 652301:
                return "昌吉市";
            case 652302:
                return "阜康市";
            case 652323:
                return "呼图壁县";
            case 652324:
                return "玛纳斯县";
            case 652325:
                return "奇台县";
            case 652327:
                return "吉木萨尔县";
            case 652328:
                return "木垒哈萨克自治县";
            case 6527:
                return "博尔塔拉蒙古自治州";
            case 652701:
                return "博乐市";
            case 652702:
                return "阿拉山口市";
            case 652722:
                return "精河县";
            case 652723:
                return "温泉县";
            case 6528:
                return "巴音郭楞蒙古自治州";
            case 652801:
                return "库尔勒市";
            case 652822:
                return "轮台县";
            case 652823:
                return "尉犁县";
            case 652824:
                return "若羌县";
            case 652825:
                return "且末县";
            case 652826:
                return "焉耆回族自治县";
            case 652827:
                return "和静县";
            case 652828:
                return "和硕县";
            case 652829:
                return "博湖县";
            case 6529:
                return "阿克苏地区";
            case 652901:
                return "阿克苏市";
            case 652922:
                return "温宿县";
            case 652923:
                return "库车县";
            case 652924:
                return "沙雅县";
            case 652925:
                return "新和县";
            case 652926:
                return "拜城县";
            case 652927:
                return "乌什县";
            case 652928:
                return "阿瓦提县";
            case 652929:
                return "柯坪县";
            case 6530:
                return "克孜勒苏柯尔克孜自治州";
            case 653001:
                return "阿图什市";
            case 653022:
                return "阿克陶县";
            case 653023:
                return "阿合奇县";
            case 653024:
                return "乌恰县";
            case 6531:
                return "喀什地区";
            case 653101:
                return "喀什市";
            case 653121:
                return "疏附县";
            case 653122:
                return "疏勒县";
            case 653123:
                return "英吉沙县";
            case 653124:
                return "泽普县";
            case 653125:
                return "莎车县";
            case 653126:
                return "叶城县";
            case 653127:
                return "麦盖提县";
            case 653128:
                return "岳普湖县";
            case 653129:
                return "伽师县";
            case 653130:
                return "巴楚县";
            case 653131:
                return "塔什库尔干塔吉克自治县";
            case 6532:
                return "和田地区";
            case 653201:
                return "和田市";
            case 653221:
                return "和田县";
            case 653222:
                return "墨玉县";
            case 653223:
                return "皮山县";
            case 653224:
                return "洛浦县";
            case 653225:
                return "策勒县";
            case 653226:
                return "于田县";
            case 653227:
                return "民丰县";
            case 6540:
                return "伊犁哈萨克自治州";
            case 654002:
                return "伊宁市";
            case 654003:
                return "奎屯市";
            case 654021:
                return "伊宁县";
            case 654022:
                return "察布查尔锡伯自治县";
            case 654023:
                return "霍城县";
            case 654024:
                return "巩留县";
            case 654025:
                return "新源县";
            case 654026:
                return "昭苏县";
            case 654027:
                return "特克斯县";
            case 654028:
                return "尼勒克县";
            case 6542:
                return "塔城地区";
            case 654201:
                return "塔城市";
            case 654202:
                return "乌苏市";
            case 654221:
                return "额敏县";
            case 654223:
                return "沙湾县";
            case 654224:
                return "托里县";
            case 654225:
                return "裕民县";
            case 654226:
                return "和布克赛尔蒙古自治县";
            case 6543:
                return "阿勒泰地区";
            case 654301:
                return "阿勒泰市";
            case 654321:
                return "布尔津县";
            case 654322:
                return "富蕴县";
            case 654323:
                return "福海县";
            case 654324:
                return "哈巴河县";
            case 654325:
                return "青河县";
            case 654326:
                return "吉木乃县";
            case 6590:
                return "自治区直辖县级行政区划";
            case 659001:
                return "石河子市";
            case 659002:
                return "阿拉尔市";
            case 659003:
                return "图木舒克市";
            case 659004:
                return "五家渠市";
            case 71:
                return "台湾省";
            case 81:
                return "香港特别行政区";
            case 82:
                return "澳门特别行政区";
            default:
                return null;
        }
    }
    /**
     * 110000 北京市
     * 110100 北京市市辖区
     * 110101 北京市东城区
     * 110102 北京市西城区
     * 110103 北京市崇文区
     * 110104 北京市宣武区
     * 110105 北京市朝阳区
     * 110106 北京市丰台区
     * 110107 北京市石景山区
     * 110108 北京市海淀区
     * 110109 北京市门头沟区
     * 110111 北京市房山区
     * 110112 北京市通州区
     * 110113 北京市顺义区
     * 110200 北京市县
     * 110221 北京市昌平县
     * 110224 北京市大兴县
     * 110226 北京市平谷县
     * 110227 北京市怀柔县
     * 110228 北京市密云县
     * 110229 北京市延庆县
     * 120000 天津市
     * 120100 天津市市辖区
     * 120101 天津市和平区
     * 120102 天津市河东区
     * 120103 天津市河西区
     * 120104 天津市南开区
     * 120105 天津市河北区
     * 120106 天津市红桥区
     * 120107 天津市塘沽区
     * 120108 天津市汉沽区
     * 120109 天津市大港区
     * 120110 天津市东丽区
     * 120111 天津市西青区
     * 120112 天津市津南区
     * 120113 天津市北辰区
     * 120200 天津市县
     * 120221 天津市宁河县
     * 120222 天津市武清县
     * 120223 天津市静海县
     * 120224 天津市宝坻县
     * 120225 天津市蓟县
     * 130000 河北省
     * 130100 河北省石家庄市
     * 130101 河北省石家庄市市辖区
     * 130102 河北省石家庄市长安区
     * 130103 河北省石家庄市桥东区
     * 130104 河北省石家庄市桥西区
     * 130105 河北省石家庄市新华区
     * 130106 河北省石家庄市郊区
     * 130107 河北省石家庄市井陉矿区
     * 130121 河北省石家庄市井陉县
     * 130123 河北省石家庄市正定县
     * 130124 河北省石家庄市栾城县
     * 130125 河北省石家庄市行唐县
     * 130126 河北省石家庄市灵寿县
     * 130127 河北省石家庄市高邑县
     * 130128 河北省石家庄市深泽县
     * 130129 河北省石家庄市赞皇县
     * 130130 河北省石家庄市无极县
     * 130131 河北省石家庄市平山县
     * 130132 河北省石家庄市元氏县
     * 130133 河北省石家庄市赵县
     * 130181 河北省石家庄市辛集市
     * 130182 河北省石家庄市藁城市
     * 130183 河北省石家庄市晋州市
     * 130184 河北省石家庄市新乐市
     * 130185 河北省石家庄市鹿泉市
     * 130200 河北省唐山市
     * 130201 河北省唐山市市辖区
     * 130202 河北省唐山市路南区
     * 130203 河北省唐山市路北区
     * 130204 河北省唐山市古冶区
     * 130205 河北省唐山市开平区
     * 130206 河北省唐山市新区
     * 130221 河北省唐山市丰润县
     * 130223 河北省唐山市滦县
     * 130224 河北省唐山市滦南县
     * 130225 河北省唐山市乐亭县
     * 130227 河北省唐山市迁西县
     * 130229 河北省唐山市玉田县
     * 130230 河北省唐山市唐海县
     * 130281 河北省唐山市遵化市
     * 130282 河北省唐山市丰南市
     * 130283 河北省唐山市迁安市
     * 130300 河北省秦皇岛市秦皇岛市
     * 130301 河北省秦皇岛市市辖区
     * 130302 河北省秦皇岛市海港区
     * 130303 河北省秦皇岛市山海关区
     * 130304 河北省秦皇岛市北戴河区
     * 130321 河北省秦皇岛市青龙满族自治县
     * 130322 河北省秦皇岛市昌黎县
     * 130323 河北省秦皇岛市抚宁县
     * 130324 河北省秦皇岛市卢龙县
     * 130400 河北省邯郸市邯郸市
     * 130401 河北省邯郸市市辖区
     * 130402 河北省邯郸市邯山区
     * 130403 河北省邯郸市丛台区
     * 130404 河北省邯郸市复兴区
     * 130406 河北省邯郸市峰峰矿区
     * 130421 河北省邯郸市邯郸县
     * 130423 河北省邯郸市临漳县
     * 130424 河北省邯郸市成安县
     * 130425 河北省邯郸市大名县
     * 130426 河北省邯郸市涉县
     * 130427 河北省邯郸市磁县
     * 130428 河北省邯郸市肥乡县
     * 130429 河北省邯郸市永年县
     * 130430 河北省邯郸市邱县
     * 130431 河北省邯郸市鸡泽县
     * 130432 河北省邯郸市广平县
     * 130433 河北省邯郸市馆陶县
     * 130434 河北省邯郸市魏县
     * 130435 河北省邯郸市曲周县
     * 130481 河北省邯郸市武安市
     * 130500 河北省邢台市
     * 130501 河北省邢台市市辖区
     * 130502 河北省邢台市桥东区
     * 130503 河北省邢台市桥西区
     * 130521 河北省邢台市邢台县
     * 130522 河北省邢台市临城县
     * 130523 河北省邢台市内丘县
     * 130524 河北省邢台市柏乡县
     * 130525 河北省邢台市隆尧县
     * 130526 河北省邢台市任县
     * 130527 河北省邢台市南和县
     * 130528 河北省邢台市宁晋县
     * 130529 河北省邢台市巨鹿县
     * 130530 河北省邢台市新河县
     * 130531 河北省邢台市广宗县
     * 130532 河北省邢台市平乡县
     * 130533 河北省邢台市威县
     * 130534 河北省邢台市清河县
     * 130535 河北省邢台市临西县
     * 130581 河北省邢台市南宫市
     * 130582 河北省邢台市沙河市
     * 130600 河北省保定市
     * 130601 河北省保定市市辖区
     * 130602 河北省保定市新市区
     * 130603 河北省保定市北市区
     * 130604 河北省保定市南市区
     * 130621 河北省保定市满城县
     * 130622 河北省保定市清苑县
     * 130623 河北省保定市涞水县
     * 130624 河北省保定市阜平县
     * 130625 河北省保定市徐水县
     * 130626 河北省保定市定兴县
     * 130627 河北省保定市唐县
     * 130628 河北省保定市高阳县
     * 130629 河北省保定市容城县
     * 130630 河北省保定市涞源县
     * 130631 河北省保定市望都县
     * 130632 河北省保定市安新县
     * 130633 河北省保定市易县
     * 130634 河北省保定市曲阳县
     * 130635 河北省保定市蠡县
     * 130636 河北省保定市顺平县
     * 130637 河北省保定市博野县
     * 130638 河北省保定市雄县
     * 130681 河北省保定市涿州市
     * 130682 河北省保定市定州市
     * 130683 河北省保定市安国市
     * 130684 河北省保定市高碑店市
     * 130700 河北省张家口市
     * 130701 河北省张家口市市辖区
     * 130702 河北省张家口市桥东区
     * 130703 河北省张家口市桥西区
     * 130705 河北省张家口市宣化区
     * 130706 河北省张家口市下花园区
     * 130721 河北省张家口市宣化县
     * 130722 河北省张家口市张北县
     * 130723 河北省张家口市康保县
     * 130724 河北省张家口市沽源县
     * 130725 河北省张家口市尚义县
     * 130726 河北省张家口市蔚县
     * 130727 河北省张家口市阳原县
     * 130728 河北省张家口市怀安县
     * 130729 河北省张家口市万全县
     * 130730 河北省张家口市怀来县
     * 130731 河北省张家口市涿鹿县
     * 130732 河北省张家口市赤城县
     * 130733 河北省张家口市崇礼县
     * 130800 河北省承德市
     * 130801 河北省承德市市辖区
     * 130802 河北省承德市双桥区
     * 130803 河北省承德市双滦区
     * 130804 河北省承德市鹰手营子矿区
     * 130821 河北省承德市承德县
     * 130822 河北省承德市兴隆县
     * 130823 河北省承德市平泉县
     * 130824 河北省承德市滦平县
     * 130825 河北省承德市隆化县
     * 130826 河北省承德市丰宁满族自治县
     * 130827 河北省承德市宽城满族自治县
     * 130828 河北省承德市围场满族蒙古族自治县
     * 130900 河北省沧州市
     * 130901 河北省沧州市市辖区
     * 130902 河北省沧州市新华区
     * 130903 河北省沧州市运河区
     * 130921 河北省沧州市沧县
     * 130922 河北省沧州市青县
     * 130923 河北省沧州市东光县
     * 130924 河北省沧州市海兴县
     * 130925 河北省沧州市盐山县
     * 130926 河北省沧州市肃宁县
     * 130927 河北省沧州市南皮县
     * 130928 河北省沧州市吴桥县
     * 130929 河北省沧州市献县
     * 130930 河北省沧州市孟村回族自治县
     * 130981 河北省沧州市泊头市
     * 130982 河北省沧州市任丘市
     * 130983 河北省沧州市黄骅市
     * 130984 河北省沧州市河间市
     * 131000 河北省廊坊市
     * 131001 河北省廊坊市市辖区
     * 131002 河北省廊坊市安次区
     * 131022 河北省廊坊市固安县
     * 131023 河北省廊坊市永清县
     * 131024 河北省廊坊市香河县
     * 131025 河北省廊坊市大城县
     * 131026 河北省廊坊市文安县
     * 131028 河北省廊坊市大厂回族自治县
     * 131081 河北省廊坊市霸州市
     * 131082 河北省廊坊市三河市
     * 131100 河北省衡水市
     * 131101 河北省衡水市市辖区
     * 131102 河北省衡水市桃城区
     * 131121 河北省衡水市枣强县
     * 131122 河北省衡水市武邑县
     * 131123 河北省衡水市武强县
     * 131124 河北省衡水市饶阳县
     * 131125 河北省衡水市安平县
     * 131126 河北省衡水市故城县
     * 131127 河北省衡水市景县
     * 131128 河北省衡水市阜城县
     * 131181 河北省衡水市冀州市
     * 131182 河北省衡水市深州市
     * 140000 山西省
     * 140100 山西省太原市
     * 140101 山西省太原市市辖区
     * 140105 山西省太原市小店区
     * 140106 山西省太原市迎泽区
     * 140107 山西省太原市杏花岭区
     * 140108 山西省太原市尖草坪区
     * 140109 山西省太原市万柏林区
     * 140110 山西省太原市晋源区
     * 140121 山西省太原市清徐县
     * 140122 山西省太原市阳曲县
     * 140123 山西省太原市娄烦县
     * 140181 山西省太原市古交市
     * 140200 山西省大同市
     * 140201 山西省大同市市辖区
     * 140202 山西省大同市城区
     * 140203 山西省大同市矿区
     * 140211 山西省大同市南郊区
     * 140212 山西省大同市新荣区
     * 140221 山西省大同市阳高县
     * 140222 山西省大同市天镇县
     * 140223 山西省大同市广灵县
     * 140224 山西省大同市灵丘县
     * 140225 山西省大同市浑源县
     * 140226 山西省大同市左云县
     * 140227 山西省大同市大同县
     * 140300 山西省阳泉市
     * 140301 山西省阳泉市市辖区
     * 140302 山西省阳泉市城区
     * 140303 山西省阳泉市矿区
     * 140311 山西省阳泉市郊区
     * 140321 山西省阳泉市平定县
     * 140322 山西省阳泉市盂县
     * 140400 山西省长治市
     * 140401 山西省长治市市辖区
     * 140402 山西省长治市城区
     * 140411 山西省长治市郊区
     * 140421 山西省长治市长治县
     * 140423 山西省长治市襄垣县
     * 140424 山西省长治市屯留县
     * 140425 山西省长治市平顺县
     * 140426 山西省长治市黎城县
     * 140427 山西省长治市壶关县
     * 140428 山西省长治市长子县
     * 140429 山西省长治市武乡县
     * 140430 山西省长治市沁县
     * 140431 山西省长治市沁源县
     * 140481 山西省长治市潞城市
     * 140500 山西省晋城市
     * 140501 山西省晋城市市辖区
     * 140502 山西省晋城市城区
     * 140521 山西省晋城市沁水县
     * 140522 山西省晋城市阳城县
     * 140524 山西省晋城市陵川县
     * 140525 山西省晋城市泽州县
     * 140581 山西省晋城市高平市
     * 140600 山西省晋城市朔州市
     * 140601 山西省晋城市市辖区
     * 140602 山西省晋城市朔城区
     * 140603 山西省晋城市平鲁区
     * 140621 山西省晋城市山阴县
     * 140622 山西省晋城市应县
     * 140623 山西省晋城市右玉县
     * 140624 山西省晋城市怀仁县
     * 142200 山西省忻州地区
     * 142201 山西省忻州地区忻州市
     * 142202 山西省忻州地区原平市
     * 142222 山西省忻州地区定襄县
     * 142223 山西省忻州地区五台县
     * 142225 山西省忻州地区代县
     * 142226 山西省忻州地区繁峙县
     * 142227 山西省忻州地区宁武县
     * 142228 山西省忻州地区静乐县
     * 142229 山西省忻州地区神池县
     * 142230 山西省忻州地区五寨县
     * 142231 山西省忻州地区岢岚县
     * 142232 山西省忻州地区河曲县
     * 142233 山西省忻州地区保德县
     * 142234 山西省忻州地区偏关县
     * 142300 山西省忻州地区吕梁地区
     * 142301 山西省忻州地区孝义市
     * 142302 山西省忻州地区离石市
     * 142303 山西省忻州地区汾阳市
     * 142322 山西省忻州地区文水县
     * 142323 山西省忻州地区交城县
     * 142325 山西省忻州地区兴县
     * 142326 山西省忻州地区临县
     * 142327 山西省忻州地区柳林县
     * 142328 山西省忻州地区石楼县
     * 142329 山西省忻州地区岚县
     * 142330 山西省忻州地区方山县
     * 142332 山西省忻州地区中阳县
     * 142333 山西省忻州地区交口县
     * 142400 山西省晋中地区
     * 142401 山西省晋中地区榆次市
     * 142402 山西省晋中地区介休市
     * 142421 山西省晋中地区榆社县
     * 142422 山西省晋中地区左权县
     * 142423 山西省晋中地区和顺县
     * 142424 山西省晋中地区昔阳县
     * 142427 山西省晋中地区寿阳县
     * 142429 山西省晋中地区太谷县
     * 142430 山西省晋中地区祁县
     * 142431 山西省晋中地区平遥县
     * 142433 山西省晋中地区灵石县
     * 142600 山西省临汾地区
     * 142601 山西省临汾地区临汾市
     * 142602 山西省临汾地区侯马市
     * 142603 山西省临汾地区霍州市
     * 142621 山西省临汾地区曲沃县
     * 142622 山西省临汾地区翼城县
     * 142623 山西省临汾地区襄汾县
     * 142625 山西省临汾地区洪洞县
     * 142627 山西省临汾地区古县
     * 142628 山西省临汾地区安泽县
     * 142629 山西省临汾地区浮山县
     * 142630 山西省临汾地区吉县
     * 142631 山西省临汾地区乡宁县
     * 142632 山西省临汾地区蒲县
     * 142633 山西省临汾地区大宁县
     * 142634 山西省临汾地区永和县
     * 142635 山西省临汾地区隰县
     * 142636 山西省临汾地区汾西县
     * 142700 山西省运城地区
     * 142701 山西省运城地区运城市
     * 142702 山西省运城地区永济市
     * 142703 山西省运城地区河津市
     * 142723 山西省运城地区芮城县
     * 142724 山西省运城地区临猗县
     * 142725 山西省运城地区万荣县
     * 142726 山西省运城地区新绛县
     * 142727 山西省运城地区稷山县
     * 142729 山西省运城地区闻喜县
     * 142730 山西省运城地区夏县
     * 142731 山西省运城地区绛县
     * 142732 山西省运城地区平陆县
     * 142733 山西省运城地区垣曲县
     * 150000 内蒙古自治区
     * 150100 内蒙古自治区呼和浩特市
     * 150101 内蒙古自治区呼和浩特市市辖区
     * 150102 内蒙古自治区呼和浩特市新城区
     * 150103 内蒙古自治区呼和浩特市回民区
     * 150104 内蒙古自治区呼和浩特市玉泉区
     * 150105 内蒙古自治区呼和浩特市郊区
     * 150121 内蒙古自治区呼和浩特市土默特左旗
     * 150122 内蒙古自治区呼和浩特市托克托县
     * 150123 内蒙古自治区呼和浩特市和林格尔县
     * 150124 内蒙古自治区呼和浩特市清水河县
     * 150125 内蒙古自治区呼和浩特市武川县
     * 150200 内蒙古自治区包头市
     * 150201 内蒙古自治区包头市市辖区
     * 150202 内蒙古自治区包头市东河区
     * 150203 内蒙古自治区包头市昆都伦区
     * 150204 内蒙古自治区包头市青山区
     * 150205 内蒙古自治区包头市石拐矿区
     * 150206 内蒙古自治区包头市白云矿区
     * 150207 内蒙古自治区包头市郊区
     * 150221 内蒙古自治区包头市土默特右旗
     * 150222 内蒙古自治区包头市固阳县
     * 150223 内蒙古自治区包头市达尔罕茂明安联合旗
     * 150300 内蒙古自治区乌海市
     * 150301 内蒙古自治区乌海市市辖区
     * 150302 内蒙古自治区乌海市海勃湾区
     * 150303 内蒙古自治区乌海市海南区
     * 150304 内蒙古自治区乌海市乌达区
     * 150400 内蒙古自治区赤峰市
     * 150401 内蒙古自治区赤峰市市辖区
     * 150402 内蒙古自治区赤峰市红山区
     * 150403 内蒙古自治区赤峰市元宝山区
     * 150404 内蒙古自治区赤峰市松山区
     * 150421 内蒙古自治区赤峰市阿鲁科尔沁旗
     * 150422 内蒙古自治区赤峰市巴林左旗
     * 150423 内蒙古自治区赤峰市巴林右旗
     * 150424 内蒙古自治区赤峰市林西县
     * 150425 内蒙古自治区赤峰市克什克腾旗
     * 150426 内蒙古自治区赤峰市翁牛特旗
     * 150428 内蒙古自治区赤峰市喀喇沁旗
     * 150429 内蒙古自治区赤峰市宁城县
     * 150430 内蒙古自治区赤峰市敖汉旗
     * 152100 内蒙古自治区呼伦贝尔盟
     * 152101 内蒙古自治区呼伦贝尔盟海拉尔市
     * 152102 内蒙古自治区呼伦贝尔盟满洲里市
     * 152103 内蒙古自治区呼伦贝尔盟扎兰屯市
     * 152104 内蒙古自治区呼伦贝尔盟牙克石市
     * 152105 内蒙古自治区呼伦贝尔盟根河市
     * 152106 内蒙古自治区呼伦贝尔盟额尔古纳市
     * 152122 内蒙古自治区呼伦贝尔盟阿荣旗
     * 152123 内蒙古自治区呼伦贝尔盟莫力达瓦达斡尔族自治旗
     * 152127 内蒙古自治区呼伦贝尔盟鄂伦春自治旗
     * 152128 内蒙古自治区呼伦贝尔盟鄂温克族自治旗
     * 152129 内蒙古自治区呼伦贝尔盟新巴尔虎右旗
     * 152130 内蒙古自治区呼伦贝尔盟新巴尔虎左旗
     * 152131 内蒙古自治区呼伦贝尔盟陈巴尔虎旗
     * 152200 内蒙古自治区兴安盟
     * 152201 内蒙古自治区兴安盟乌兰浩特市
     * 152202 内蒙古自治区兴安盟阿尔山市
     * 152221 内蒙古自治区兴安盟科尔沁右翼前旗
     * 152222 内蒙古自治区兴安盟科尔沁右翼中旗
     * 152223 内蒙古自治区兴安盟扎赉特旗
     * 152224 内蒙古自治区兴安盟突泉县
     * 152300 内蒙古自治区哲里木盟
     * 152301 内蒙古自治区哲里木盟通辽市
     * 152302 内蒙古自治区哲里木盟霍林郭勒市
     * 152322 内蒙古自治区哲里木盟科尔沁左翼中旗
     * 152323 内蒙古自治区哲里木盟科尔沁左翼后旗
     * 152324 内蒙古自治区哲里木盟开鲁县
     * 152325 内蒙古自治区哲里木盟库伦旗
     * 152326 内蒙古自治区哲里木盟奈曼旗
     * 152327 内蒙古自治区哲里木盟扎鲁特旗
     * 152500 内蒙古自治区锡林郭勒盟
     * 152501 内蒙古自治区锡林郭勒盟二连浩特市
     * 152502 内蒙古自治区锡林郭勒盟锡林浩特市
     * 152522 内蒙古自治区锡林郭勒盟阿巴嘎旗
     * 152523 内蒙古自治区锡林郭勒盟苏尼特左旗
     * 152524 内蒙古自治区锡林郭勒盟苏尼特右旗
     * 152525 内蒙古自治区锡林郭勒盟东乌珠穆沁旗
     * 152526 内蒙古自治区锡林郭勒盟西乌珠穆沁旗
     * 152527 内蒙古自治区锡林郭勒盟太仆寺旗
     * 152528 内蒙古自治区锡林郭勒盟镶黄旗
     * 152529 内蒙古自治区锡林郭勒盟正镶白旗
     * 152530 内蒙古自治区锡林郭勒盟正蓝旗
     * 152531 内蒙古自治区锡林郭勒盟多伦县
     * 152600 内蒙古自治区乌兰察布盟
     * 152601 内蒙古自治区乌兰察布盟集宁市
     * 152602 内蒙古自治区乌兰察布盟丰镇市
     * 152624 内蒙古自治区乌兰察布盟卓资县
     * 152625 内蒙古自治区乌兰察布盟化德县
     * 152626 内蒙古自治区乌兰察布盟商都县
     * 152627 内蒙古自治区乌兰察布盟兴和县
     * 152629 内蒙古自治区乌兰察布盟凉城县
     * 152630 内蒙古自治区乌兰察布盟察哈尔右翼前旗
     * 152631 内蒙古自治区乌兰察布盟察哈尔右翼中旗
     * 152632 内蒙古自治区乌兰察布盟察哈尔右翼后旗
     * 152634 内蒙古自治区乌兰察布盟四子王旗
     * 152700 内蒙古自治区伊克昭盟
     * 152701 内蒙古自治区伊克昭盟东胜市
     * 152722 内蒙古自治区伊克昭盟达拉特旗
     * 152723 内蒙古自治区伊克昭盟准格尔旗
     * 152724 内蒙古自治区伊克昭盟鄂托克前旗
     * 152725 内蒙古自治区伊克昭盟鄂托克旗
     * 152726 内蒙古自治区伊克昭盟杭锦旗
     * 152727 内蒙古自治区伊克昭盟乌审旗
     * 152728 内蒙古自治区伊克昭盟伊金霍洛旗
     * 152800 内蒙古自治区巴彦淖尔盟
     * 152801 内蒙古自治区巴彦淖尔盟临河市
     * 152822 内蒙古自治区巴彦淖尔盟五原县
     * 152823 内蒙古自治区巴彦淖尔盟磴口县
     * 152824 内蒙古自治区巴彦淖尔盟乌拉特前旗
     * 152825 内蒙古自治区巴彦淖尔盟乌拉特中旗
     * 152826 内蒙古自治区巴彦淖尔盟乌拉特后旗
     * 152827 内蒙古自治区巴彦淖尔盟杭锦后旗
     * 152900 内蒙古自治区阿拉善盟
     * 152921 内蒙古自治区阿拉善盟阿拉善左旗
     * 152922 内蒙古自治区阿拉善盟阿拉善右旗
     * 152923 内蒙古自治区阿拉善盟额济纳旗
     * 210000 辽宁省
     * 210100 辽宁省沈阳市
     * 210101 辽宁省沈阳市市辖区
     * 210102 辽宁省沈阳市和平区
     * 210103 辽宁省沈阳市沈河区
     * 210104 辽宁省沈阳市大东区
     * 210105 辽宁省沈阳市皇姑区
     * 210106 辽宁省沈阳市铁西区
     * 210111 辽宁省沈阳市苏家屯区
     * 210112 辽宁省沈阳市东陵区
     * 210113 辽宁省沈阳市新城子区
     * 210114 辽宁省沈阳市于洪区
     * 210122 辽宁省沈阳市辽中县
     * 210123 辽宁省沈阳市康平县
     * 210124 辽宁省沈阳市法库县
     * 210181 辽宁省沈阳市新民市
     * 210200 辽宁省大连市
     * 210201 辽宁省大连市市辖区
     * 210202 辽宁省大连市中山区
     * 210203 辽宁省大连市西岗区
     * 210204 辽宁省大连市沙河口区
     * 210211 辽宁省大连市甘井子区
     * 210212 辽宁省大连市旅顺口区
     * 210213 辽宁省大连市金州区
     * 210224 辽宁省大连市长海县
     * 210281 辽宁省大连市瓦房店市
     * 210282 辽宁省大连市普兰店市
     * 210283 辽宁省大连市庄河市
     * 210300 辽宁省鞍山市
     * 210301 辽宁省鞍山市市辖区
     * 210302 辽宁省鞍山市铁东区
     * 210303 辽宁省鞍山市铁西区
     * 210304 辽宁省鞍山市立山区
     * 210311 辽宁省鞍山市千山区
     * 210321 辽宁省鞍山市台安县
     * 210323 辽宁省鞍山市岫岩满族自治县
     * 210381 辽宁省鞍山市海城市
     * 210400 辽宁省抚顺市
     * 210401 辽宁省抚顺市市辖区
     * 210402 辽宁省抚顺市新抚区
     * 210403 辽宁省抚顺市露天区
     * 210404 辽宁省抚顺市望花区
     * 210411 辽宁省抚顺市顺城区
     * 210421 辽宁省抚顺市抚顺县
     * 210422 辽宁省抚顺市新宾满族自治县
     * 210423 辽宁省抚顺市清原满族自治县
     * 210500 辽宁省本溪市
     * 210501 辽宁省本溪市市辖区
     * 210502 辽宁省本溪市平山区
     * 210503 辽宁省本溪市溪湖区
     * 210504 辽宁省本溪市明山区
     * 210505 辽宁省本溪市南芬区
     * 210521 辽宁省本溪市本溪满族自治县
     * 210522 辽宁省本溪市桓仁满族自治县
     * 210600 辽宁省丹东市
     * 210601 辽宁省丹东市市辖区
     * 210602 辽宁省丹东市元宝区
     * 210603 辽宁省丹东市振兴区
     * 210604 辽宁省丹东市振安区
     * 210624 辽宁省丹东市宽甸满族自治县
     * 210681 辽宁省丹东市东港市
     * 210682 辽宁省丹东市凤城市
     * 210700 辽宁省锦州市
     * 210701 辽宁省锦州市市辖区
     * 210702 辽宁省锦州市古塔区
     * 210703 辽宁省锦州市凌河区
     * 210711 辽宁省锦州市太和区
     * 210726 辽宁省锦州市黑山县
     * 210727 辽宁省锦州市义县
     * 210781 辽宁省锦州市凌海市
     * 210782 辽宁省锦州市北宁市
     * 210800 辽宁省营口市
     * 210801 辽宁省营口市市辖区
     * 210802 辽宁省营口市站前区
     * 210803 辽宁省营口市西市区
     * 210804 辽宁省营口市鲅鱼圈区
     * 210811 辽宁省营口市老边区
     * 210881 辽宁省营口市盖州市
     * 210882 辽宁省营口市大石桥市
     * 210900 辽宁省阜新市
     * 210901 辽宁省阜新市市辖区
     * 210902 辽宁省阜新市海州区
     * 210903 辽宁省阜新市新邱区
     * 210904 辽宁省阜新市太平区
     * 210905 辽宁省阜新市清河门区
     * 210911 辽宁省阜新市细河区
     * 210921 辽宁省阜新市阜新蒙古族自治县
     * 210922 辽宁省阜新市彰武县
     * 211000 辽宁省辽阳市
     * 211001 辽宁省辽阳市市辖区
     * 211002 辽宁省辽阳市白塔区
     * 211003 辽宁省辽阳市文圣区
     * 211004 辽宁省辽阳市宏伟区
     * 211005 辽宁省辽阳市弓长岭区
     * 211011 辽宁省辽阳市太子河区
     * 211021 辽宁省辽阳市辽阳县
     * 211081 辽宁省辽阳市灯塔市
     * 211100 辽宁省盘锦市
     * 211101 辽宁省盘锦市市辖区
     * 211102 辽宁省盘锦市双台子区
     * 211103 辽宁省盘锦市兴隆台区
     * 211121 辽宁省盘锦市大洼县
     * 211122 辽宁省盘锦市盘山县
     * 211200 辽宁省铁岭市
     * 211201 辽宁省铁岭市市辖区
     * 211202 辽宁省铁岭市银州区
     * 211204 辽宁省铁岭市清河区
     * 211221 辽宁省铁岭市铁岭县
     * 211223 辽宁省铁岭市西丰县
     * 211224 辽宁省铁岭市昌图县
     * 211281 辽宁省铁岭市铁法市
     * 211282 辽宁省铁岭市开原市
     * 211300 辽宁省朝阳市
     * 211301 辽宁省朝阳市市辖区
     * 211302 辽宁省朝阳市双塔区
     * 211303 辽宁省朝阳市龙城区
     * 211321 辽宁省朝阳市朝阳县
     * 211322 辽宁省朝阳市建平县
     * 211324 辽宁省朝阳市喀喇沁左翼蒙古族自治县
     * 211381 辽宁省朝阳市北票市
     * 211382 辽宁省朝阳市凌源市
     * 211400 辽宁省葫芦岛市
     * 211401 辽宁省葫芦岛市市辖区
     * 211402 辽宁省葫芦岛市连山区
     * 211403 辽宁省葫芦岛市龙港区
     * 211404 辽宁省葫芦岛市南票区
     * 211421 辽宁省葫芦岛市绥中县
     * 211422 辽宁省葫芦岛市建昌县
     * 211481 辽宁省葫芦岛市兴城市
     * 220000 吉林省
     * 220100 吉林省长春市
     * 220101 吉林省长春市市辖区
     * 220102 吉林省长春市南关区
     * 220103 吉林省长春市宽城区
     * 220104 吉林省长春市朝阳区
     * 220105 吉林省长春市二道区
     * 220106 吉林省长春市绿园区
     * 220112 吉林省长春市双阳区
     * 220122 吉林省长春市农安县
     * 220181 吉林省长春市九台市
     * 220182 吉林省长春市榆树市
     * 220183 吉林省长春市德惠市
     * 220200 吉林省吉林市
     * 220201 吉林省吉林市市辖区
     * 220202 吉林省吉林市昌邑区
     * 220203 吉林省吉林市龙潭区
     * 220204 吉林省吉林市船营区
     * 220211 吉林省吉林市丰满区
     * 220221 吉林省吉林市永吉县
     * 220281 吉林省吉林市蛟河市
     * 220282 吉林省吉林市桦甸市
     * 220283 吉林省吉林市舒兰市
     * 220284 吉林省吉林市磐石市
     * 220300 吉林省四平市
     * 220301 吉林省四平市市辖区
     * 220302 吉林省四平市铁西区
     * 220303 吉林省四平市铁东区
     * 220322 吉林省四平市梨树县
     * 220323 吉林省四平市伊通满族自治县
     * 220381 吉林省四平市公主岭市
     * 220382 吉林省四平市双辽市
     * 220400 吉林省辽源市
     * 220401 吉林省辽源市市辖区
     * 220402 吉林省辽源市龙山区
     * 220403 吉林省辽源市西安区
     * 220421 吉林省辽源市东丰县
     * 220422 吉林省辽源市东辽县
     * 220500 吉林省通化市
     * 220501 吉林省通化市市辖区
     * 220502 吉林省通化市东昌区
     * 220503 吉林省通化市二道江区
     * 220521 吉林省通化市通化县
     * 220523 吉林省通化市辉南县
     * 220524 吉林省通化市柳河县
     * 220581 吉林省通化市梅河口市
     * 220582 吉林省通化市集安市
     * 220600 吉林省白山市
     * 220601 吉林省白山市市辖区
     * 220602 吉林省白山市八道江区
     * 220621 吉林省白山市抚松县
     * 220622 吉林省白山市靖宇县
     * 220623 吉林省白山市长白朝鲜族自治县
     * 220625 吉林省白山市江源县
     * 220681 吉林省白山市临江市
     * 220700 吉林省松原市
     * 220701 吉林省松原市市辖区
     * 220702 吉林省松原市宁江区
     * 220721 吉林省松原市前郭尔罗斯蒙古族自治县
     * 220722 吉林省松原市长岭县
     * 220723 吉林省松原市乾安县
     * 220724 吉林省松原市扶余县
     * 220800 吉林省白城市
     * 220801 吉林省白城市市辖区
     * 220802 吉林省白城市洮北区
     * 220821 吉林省白城市镇赉县
     * 220822 吉林省白城市通榆县
     * 220881 吉林省白城市洮南市
     * 220882 吉林省白城市大安市
     * 222400 吉林省延边朝鲜族自治州
     * 222401 吉林省延边朝鲜族自治州延吉市
     * 222402 吉林省延边朝鲜族自治州图们市
     * 222403 吉林省延边朝鲜族自治州敦化市
     * 222404 吉林省延边朝鲜族自治州珲春市
     * 222405 吉林省延边朝鲜族自治州龙井市
     * 222406 吉林省延边朝鲜族自治州和龙市
     * 222424 吉林省延边朝鲜族自治州汪清县
     * 222426 吉林省延边朝鲜族自治州安图县
     * 230000 黑龙江省
     * 230100 黑龙江省哈尔滨市
     * 230101 黑龙江省哈尔滨市市辖区
     * 230102 黑龙江省哈尔滨市道里区
     * 230103 黑龙江省哈尔滨市南岗区
     * 230104 黑龙江省哈尔滨市道外区
     * 230105 黑龙江省哈尔滨市太平区
     * 230106 黑龙江省哈尔滨市香坊区
     * 230107 黑龙江省哈尔滨市动力区
     * 230108 黑龙江省哈尔滨市平房区
     * 230121 黑龙江省哈尔滨市呼兰县
     * 230123 黑龙江省哈尔滨市依兰县
     * 230124 黑龙江省哈尔滨市方正县
     * 230125 黑龙江省哈尔滨市宾县
     * 230126 黑龙江省哈尔滨市巴彦县
     * 230127 黑龙江省哈尔滨市木兰县
     * 230128 黑龙江省哈尔滨市通河县
     * 230129 黑龙江省哈尔滨市延寿县
     * 230181 黑龙江省哈尔滨市阿城市
     * 230182 黑龙江省哈尔滨市双城市
     * 230183 黑龙江省哈尔滨市尚志市
     * 230184 黑龙江省哈尔滨市五常市
     * 230200 黑龙江省齐齐哈尔市
     * 230201 黑龙江省齐齐哈尔市市辖区
     * 230202 黑龙江省齐齐哈尔市龙沙区
     * 230203 黑龙江省齐齐哈尔市建华区
     * 230204 黑龙江省齐齐哈尔市铁锋区
     * 230205 黑龙江省齐齐哈尔市昂昂溪区
     * 230206 黑龙江省齐齐哈尔市富拉尔基区
     * 230207 黑龙江省齐齐哈尔市碾子山区
     * 230208 黑龙江省齐齐哈尔市梅里斯达斡尔族区
     * 230221 黑龙江省齐齐哈尔市龙江县
     * 230223 黑龙江省齐齐哈尔市依安县
     * 230224 黑龙江省齐齐哈尔市泰来县
     * 230225 黑龙江省齐齐哈尔市甘南县
     * 230227 黑龙江省齐齐哈尔市富裕县
     * 230229 黑龙江省齐齐哈尔市克山县
     * 230230 黑龙江省齐齐哈尔市克东县
     * 230231 黑龙江省齐齐哈尔市拜泉县
     * 230281 黑龙江省齐齐哈尔市讷河市
     * 230300 黑龙江省鸡西市
     * 230301 黑龙江省鸡西市市辖区
     * 230302 黑龙江省鸡西市鸡冠区
     * 230303 黑龙江省鸡西市恒山区
     * 230304 黑龙江省鸡西市滴道区
     * 230305 黑龙江省鸡西市梨树区
     * 230306 黑龙江省鸡西市城子河区
     * 230307 黑龙江省鸡西市麻山区
     * 230321 黑龙江省鸡西市鸡东县
     * 230381 黑龙江省鸡西市虎林市
     * 230382 黑龙江省鸡西市密山市
     * 230400 黑龙江省鹤岗市
     * 230401 黑龙江省鹤岗市市辖区
     * 230402 黑龙江省鹤岗市向阳区
     * 230403 黑龙江省鹤岗市工农区
     * 230404 黑龙江省鹤岗市南山区
     * 230405 黑龙江省鹤岗市兴安区
     * 230406 黑龙江省鹤岗市东山区
     * 230407 黑龙江省鹤岗市兴山区
     * 230421 黑龙江省鹤岗市萝北县
     * 230422 黑龙江省鹤岗市绥滨县
     * 230500 黑龙江省双鸭山市
     * 230501 黑龙江省双鸭山市市辖区
     * 230502 黑龙江省双鸭山市尖山区
     * 230503 黑龙江省双鸭山市岭东区
     * 230505 黑龙江省双鸭山市四方台区
     * 230506 黑龙江省双鸭山市宝山区
     * 230521 黑龙江省双鸭山市集贤县
     * 230522 黑龙江省双鸭山市友谊县
     * 230523 黑龙江省双鸭山市宝清县
     * 230524 黑龙江省双鸭山市饶河县
     * 230600 黑龙江省大庆市
     * 230601 黑龙江省大庆市市辖区
     * 230602 黑龙江省大庆市萨尔图区
     * 230603 黑龙江省大庆市龙凤区
     * 230604 黑龙江省大庆市让胡路区
     * 230605 黑龙江省大庆市红岗区
     * 230606 黑龙江省大庆市大同区
     * 230621 黑龙江省大庆市肇州县
     * 230622 黑龙江省大庆市肇源县
     * 230623 黑龙江省大庆市林甸县
     * 230624 黑龙江省大庆市杜尔伯特蒙古族自治县
     * 230700 黑龙江省伊春市
     * 230701 黑龙江省伊春市市辖区
     * 230702 黑龙江省伊春市伊春区
     * 230703 黑龙江省伊春市南岔区
     * 230704 黑龙江省伊春市友好区
     * 230705 黑龙江省伊春市西林区
     * 230706 黑龙江省伊春市翠峦区
     * 230707 黑龙江省伊春市新青区
     * 230708 黑龙江省伊春市美溪区
     * 230709 黑龙江省伊春市金山屯区
     * 230710 黑龙江省伊春市五营区
     * 230711 黑龙江省伊春市乌马河区
     * 230712 黑龙江省伊春市汤旺河区
     * 230713 黑龙江省伊春市带岭区
     * 230714 黑龙江省伊春市乌伊岭区
     * 230715 黑龙江省伊春市红星区
     * 230716 黑龙江省伊春市上甘岭区
     * 230722 黑龙江省伊春市嘉荫县
     * 230781 黑龙江省伊春市铁力市
     * 230800 黑龙江省佳木斯市
     * 230801 黑龙江省佳木斯市市辖区
     * 230802 黑龙江省佳木斯市永红区
     * 230803 黑龙江省佳木斯市向阳区
     * 230804 黑龙江省佳木斯市前进区
     * 230805 黑龙江省佳木斯市东风区
     * 230811 黑龙江省佳木斯市郊区
     * 230822 黑龙江省佳木斯市桦南县
     * 230826 黑龙江省佳木斯市桦川县
     * 230828 黑龙江省佳木斯市汤原县
     * 230833 黑龙江省佳木斯市抚远县
     * 230881 黑龙江省佳木斯市同江市
     * 230882 黑龙江省佳木斯市富锦市
     * 230900 黑龙江省七台河市
     * 230901 黑龙江省七台河市市辖区
     * 230902 黑龙江省七台河市新兴区
     * 230903 黑龙江省七台河市桃山区
     * 230904 黑龙江省七台河市茄子河区
     * 230921 黑龙江省七台河市勃利县
     * 231000 黑龙江省牡丹江市
     * 231001 黑龙江省牡丹江市市辖区
     * 231002 黑龙江省牡丹江市东安区
     * 231003 黑龙江省牡丹江市阳明区
     * 231004 黑龙江省牡丹江市爱民区
     * 231005 黑龙江省牡丹江市西安区
     * 231024 黑龙江省牡丹江市东宁县
     * 231025 黑龙江省牡丹江市林口县
     * 231081 黑龙江省牡丹江市绥芬河市
     * 231083 黑龙江省牡丹江市海林市
     * 231084 黑龙江省牡丹江市宁安市
     * 231085 黑龙江省牡丹江市穆棱市
     * 231100 黑龙江省黑河市
     * 231101 黑龙江省黑河市市辖区
     * 231102 黑龙江省黑河市爱辉区
     * 231121 黑龙江省黑河市嫩江县
     * 231123 黑龙江省黑河市逊克县
     * 231124 黑龙江省黑河市孙吴县
     * 231181 黑龙江省黑河市北安市
     * 231182 黑龙江省黑河市五大连池市
     * 232300 黑龙江省绥化地区
     * 232301 黑龙江省绥化地区绥化市
     * 232302 黑龙江省绥化地区安达市
     * 232303 黑龙江省绥化地区肇东市
     * 232304 黑龙江省绥化地区海伦市
     * 232324 黑龙江省绥化地区望奎县
     * 232325 黑龙江省绥化地区兰西县
     * 232326 黑龙江省绥化地区青冈县
     * 232330 黑龙江省绥化地区庆安县
     * 232331 黑龙江省绥化地区明水县
     * 232332 黑龙江省绥化地区绥棱县
     * 232700 黑龙江省大兴安岭地区
     * 232721 黑龙江省大兴安岭地区呼玛县
     * 232722 黑龙江省大兴安岭地区塔河县
     * 232723 黑龙江省大兴安岭地区漠河县
     * 310000 上海市
     * 310100 上海市市辖区
     * 310101 上海市黄浦区
     * 310102 上海市南市区
     * 310103 上海市卢湾区
     * 310104 上海市徐汇区
     * 310105 上海市长宁区
     * 310106 上海市静安区
     * 310107 上海市普陀区
     * 310108 上海市闸北区
     * 310109 上海市虹口区
     * 310110 上海市杨浦区
     * 310112 上海市闵行区
     * 310113 上海市宝山区
     * 310114 上海市嘉定区
     * 310115 上海市浦东新区
     * 310116 上海市金山区
     * 310117 上海市松江区
     * 310200 上海市县
     * 310225 上海市南汇县
     * 310226 上海市奉贤县
     * 310229 上海市青浦县
     * 310230 上海市崇明县
     * 320000 江苏省
     * 320100 江苏省南京市
     * 320101 江苏省南京市市辖区
     * 320102 江苏省南京市玄武区
     * 320103 江苏省南京市白下区
     * 320104 江苏省南京市秦淮区
     * 320105 江苏省南京市建邺区
     * 320106 江苏省南京市鼓楼区
     * 320107 江苏省南京市下关区
     * 320111 江苏省南京市浦口区
     * 320112 江苏省南京市大厂区
     * 320113 江苏省南京市栖霞区
     * 320114 江苏省南京市雨花台区
     * 320121 江苏省南京市江宁县
     * 320122 江苏省南京市江浦县
     * 320123 江苏省南京市六合县
     * 320124 江苏省南京市溧水县
     * 320125 江苏省南京市高淳县
     * 320200 江苏省无锡市
     * 320201 江苏省无锡市市辖区
     * 320202 江苏省无锡市崇安区
     * 320203 江苏省无锡市南长区
     * 320204 江苏省无锡市北塘区
     * 320211 江苏省无锡市郊区
     * 320281 江苏省无锡市江阴市
     * 320282 江苏省无锡市宜兴市
     * 320283 江苏省无锡市锡山市
     * 320300 江苏省徐州市
     * 320301 江苏省徐州市市辖区
     * 320302 江苏省徐州市鼓楼区
     * 320303 江苏省徐州市云龙区
     * 320304 江苏省徐州市九里区
     * 320305 江苏省徐州市贾汪区
     * 320311 江苏省徐州市泉山区
     * 320321 江苏省徐州市丰县
     * 320322 江苏省徐州市沛县
     * 320323 江苏省徐州市铜山县
     * 320324 江苏省徐州市睢宁县
     * 320381 江苏省徐州市新沂市
     * 320382 江苏省徐州市邳州市
     * 320400 江苏省常州市
     * 320401 江苏省常州市市辖区
     * 320402 江苏省常州市天宁区
     * 320404 江苏省常州市钟楼区
     * 320405 江苏省常州市戚墅堰区
     * 320411 江苏省常州市郊区
     * 320481 江苏省常州市溧阳市
     * 320482 江苏省常州市金坛市
     * 320483 江苏省常州市武进市
     * 320500 江苏省苏州市
     * 320501 江苏省苏州市市辖区
     * 320502 江苏省苏州市沧浪区
     * 320503 江苏省苏州市平江区
     * 320504 江苏省苏州市金阊区
     * 320511 江苏省苏州市郊区
     * 320581 江苏省苏州市常熟市
     * 320582 江苏省苏州市张家港市
     * 320583 江苏省苏州市昆山市
     * 320584 江苏省苏州市吴江市
     * 320585 江苏省苏州市太仓市
     * 320586 江苏省苏州市吴县市
     * 320600 江苏省南通市
     * 320601 江苏省南通市市辖区
     * 320602 江苏省南通市崇川区
     * 320611 江苏省南通市港闸区
     * 320621 江苏省南通市海安县
     * 320623 江苏省南通市如东县
     * 320681 江苏省南通市启东市
     * 320682 江苏省南通市如皋市
     * 320683 江苏省南通市通州市
     * 320684 江苏省南通市海门市
     * 320700 江苏省连云港市
     * 320701 江苏省连云港市市辖区
     * 320703 江苏省连云港市连云区
     * 320704 江苏省连云港市云台区
     * 320705 江苏省连云港市新浦区
     * 320706 江苏省连云港市海州区
     * 320721 江苏省连云港市赣榆县
     * 320722 江苏省连云港市东海县
     * 320723 江苏省连云港市灌云县
     * 320724 江苏省连云港市灌南县
     * 320800 江苏省淮阴市
     * 320801 江苏省淮阴市市辖区
     * 320802 江苏省淮阴市清河区
     * 320811 江苏省淮阴市清浦区
     * 320821 江苏省淮阴市淮阴县
     * 320826 江苏省淮阴市涟水县
     * 320829 江苏省淮阴市洪泽县
     * 320830 江苏省淮阴市盱眙县
     * 320831 江苏省淮阴市金湖县
     * 320882 江苏省淮阴市淮安市
     * 320900 江苏省盐城市
     * 320901 江苏省盐城市市辖区
     * 320902 江苏省盐城市城区
     * 320921 江苏省盐城市响水县
     * 320922 江苏省盐城市滨海县
     * 320923 江苏省盐城市阜宁县
     * 320924 江苏省盐城市射阳县
     * 320925 江苏省盐城市建湖县
     * 320928 江苏省盐城市盐都县
     * 320981 江苏省盐城市东台市
     * 320982 江苏省盐城市大丰市
     * 321000 江苏省扬州市
     * 321001 江苏省扬州市市辖区
     * 321002 江苏省扬州市广陵区
     * 321011 江苏省扬州市郊区
     * 321023 江苏省扬州市宝应县
     * 321027 江苏省扬州市邗江县
     * 321081 江苏省扬州市仪征市
     * 321084 江苏省扬州市高邮市
     * 321088 江苏省扬州市江都市
     * 321100 江苏省镇江市
     * 321101 江苏省镇江市市辖区
     * 321102 江苏省镇江市京口区
     * 321111 江苏省镇江市润州区
     * 321121 江苏省镇江市丹徒县
     * 321181 江苏省镇江市丹阳市
     * 321182 江苏省镇江市扬中市
     * 321183 江苏省镇江市句容市
     * 321200 江苏省泰州市
     * 321201 江苏省泰州市市辖区
     * 321202 江苏省泰州市海陵区
     * 321203 江苏省泰州市高港区
     * 321281 江苏省泰州市兴化市
     * 321282 江苏省泰州市靖江市
     * 321283 江苏省泰州市泰兴市
     * 321284 江苏省泰州市姜堰市
     * 321300 江苏省宿迁市
     * 321301 江苏省宿迁市市辖区
     * 321302 江苏省宿迁市宿城区
     * 321321 江苏省宿迁市宿豫县
     * 321322 江苏省宿迁市沭阳县
     * 321323 江苏省宿迁市泗阳县
     * 321324 江苏省宿迁市泗洪县
     * 330000 浙江省
     * 330100 浙江省杭州市
     * 330101 浙江省杭州市市辖区
     * 330102 浙江省杭州市上城区
     * 330103 浙江省杭州市下城区
     * 330104 浙江省杭州市江干区
     * 330105 浙江省杭州市拱墅区
     * 330106 浙江省杭州市西湖区
     * 330108 浙江省杭州市滨江区
     * 330122 浙江省杭州市桐庐县
     * 330127 浙江省杭州市淳安县
     * 330181 浙江省杭州市萧山市
     * 330182 浙江省杭州市建德市
     * 330183 浙江省杭州市富阳市
     * 330184 浙江省杭州市余杭市
     * 330185 浙江省杭州市临安市
     * 330200 浙江省宁波市
     * 330201 浙江省宁波市市辖区
     * 330203 浙江省宁波市海曙区
     * 330204 浙江省宁波市江东区
     * 330205 浙江省宁波市江北区
     * 330206 浙江省宁波市北仑区
     * 330211 浙江省宁波市镇海区
     * 330225 浙江省宁波市象山县
     * 330226 浙江省宁波市宁海县
     * 330227 浙江省宁波市鄞县
     * 330281 浙江省宁波市余姚市
     * 330282 浙江省宁波市慈溪市
     * 330283 浙江省宁波市奉化市
     * 330300 浙江省温州市
     * 330301 浙江省温州市市辖区
     * 330302 浙江省温州市鹿城区
     * 330303 浙江省温州市龙湾区
     * 330304 浙江省温州市瓯海区
     * 330322 浙江省温州市洞头县
     * 330324 浙江省温州市永嘉县
     * 330326 浙江省温州市平阳县
     * 330327 浙江省温州市苍南县
     * 330328 浙江省温州市文成县
     * 330329 浙江省温州市泰顺县
     * 330381 浙江省温州市瑞安市
     * 330382 浙江省温州市乐清市
     * 330400 浙江省嘉兴市
     * 330401 浙江省嘉兴市市辖区
     * 330402 浙江省嘉兴市秀城区
     * 330411 浙江省嘉兴市郊区
     * 330421 浙江省嘉兴市嘉善县
     * 330424 浙江省嘉兴市海盐县
     * 330481 浙江省嘉兴市海宁市
     * 330482 浙江省嘉兴市平湖市
     * 330483 浙江省嘉兴市桐乡市
     * 330500 浙江省湖州市
     * 330501 浙江省湖州市市辖区
     * 330521 浙江省湖州市德清县
     * 330522 浙江省湖州市长兴县
     * 330523 浙江省湖州市安吉县
     * 330600 浙江省绍兴市
     * 330601 浙江省绍兴市市辖区
     * 330602 浙江省绍兴市越城区
     * 330621 浙江省绍兴市绍兴县
     * 330624 浙江省绍兴市新昌县
     * 330681 浙江省绍兴市诸暨市
     * 330682 浙江省绍兴市上虞市
     * 330683 浙江省绍兴市嵊州市
     * 330700 浙江省金华市
     * 330701 浙江省金华市市辖区
     * 330702 浙江省金华市婺城区
     * 330721 浙江省金华市金华县
     * 330723 浙江省金华市武义县
     * 330726 浙江省金华市浦江县
     * 330727 浙江省金华市磐安县
     * 330781 浙江省金华市兰溪市
     * 330782 浙江省金华市义乌市
     * 330783 浙江省金华市东阳市
     * 330784 浙江省金华市永康市
     * 330800 浙江省衢州市
     * 330801 浙江省衢州市市辖区
     * 330802 浙江省衢州市柯城区
     * 330821 浙江省衢州市衢县
     * 330822 浙江省衢州市常山县
     * 330824 浙江省衢州市开化县
     * 330825 浙江省衢州市龙游县
     * 330881 浙江省衢州市江山市
     * 330900 浙江省舟山市
     * 330901 浙江省舟山市市辖区
     * 330902 浙江省舟山市定海区
     * 330903 浙江省舟山市普陀区
     * 330921 浙江省舟山市岱山县
     * 330922 浙江省舟山市嵊泗县
     * 331000 浙江省台州市
     * 331001 浙江省台州市市辖区
     * 331002 浙江省台州市椒江区
     * 331003 浙江省台州市黄岩区
     * 331004 浙江省台州市路桥区
     * 331021 浙江省台州市玉环县
     * 331022 浙江省台州市三门县
     * 331023 浙江省台州市天台县
     * 331024 浙江省台州市仙居县
     * 331081 浙江省台州市温岭市
     * 331082 浙江省台州市临海市
     * 332500 浙江省丽水地区
     * 332501 浙江省丽水地区丽水市
     * 332502 浙江省丽水地区龙泉市
     * 332522 浙江省丽水地区青田县
     * 332523 浙江省丽水地区云和县
     * 332525 浙江省丽水地区庆元县
     * 332526 浙江省丽水地区缙云县
     * 332527 浙江省丽水地区遂昌县
     * 332528 浙江省丽水地区松阳县
     * 332529 浙江省丽水地区景宁畲族自治县
     * 340000 安徽省
     * 340100 安徽省合肥市
     * 340101 安徽省合肥市市辖区
     * 340102 安徽省合肥市东市区
     * 340103 安徽省合肥市中市区
     * 340104 安徽省合肥市西市区
     * 340111 安徽省合肥市郊区
     * 340121 安徽省合肥市长丰县
     * 340122 安徽省合肥市肥东县
     * 340123 安徽省合肥市肥西县
     * 340200 安徽省芜湖市
     * 340201 安徽省芜湖市市辖区
     * 340202 安徽省芜湖市镜湖区
     * 340203 安徽省芜湖市马塘区
     * 340204 安徽省芜湖市新芜区
     * 340207 安徽省芜湖市鸠江区
     * 340221 安徽省芜湖市芜湖县
     * 340222 安徽省芜湖市繁昌县
     * 340223 安徽省芜湖市南陵县
     * 340300 安徽省蚌埠市
     * 340301 安徽省蚌埠市市辖区
     * 340302 安徽省蚌埠市东市区
     * 340303 安徽省蚌埠市中市区
     * 340304 安徽省蚌埠市西市区
     * 340311 安徽省蚌埠市郊区
     * 340321 安徽省蚌埠市怀远县
     * 340322 安徽省蚌埠市五河县
     * 340323 安徽省蚌埠市固镇县
     * 340400 安徽省淮南市
     * 340401 安徽省淮南市市辖区
     * 340402 安徽省淮南市大通区
     * 340403 安徽省淮南市田家庵区
     * 340404 安徽省淮南市谢家集区
     * 340405 安徽省淮南市八公山区
     * 340406 安徽省淮南市潘集区
     * 340421 安徽省淮南市凤台县
     * 340500 安徽省马鞍山市
     * 340501 安徽省马鞍山市市辖区
     * 340502 安徽省马鞍山市金家庄区
     * 340503 安徽省马鞍山市花山区
     * 340504 安徽省马鞍山市雨山区
     * 340505 安徽省马鞍山市向山区
     * 340521 安徽省马鞍山市当涂县
     * 340600 安徽省淮北市
     * 340601 安徽省淮北市市辖区
     * 340602 安徽省淮北市杜集区
     * 340603 安徽省淮北市相山区
     * 340604 安徽省淮北市烈山区
     * 340621 安徽省淮北市濉溪县
     * 340700 安徽省铜陵市
     * 340701 安徽省铜陵市市辖区
     * 340702 安徽省铜陵市铜官山区
     * 340703 安徽省铜陵市狮子山区
     * 340711 安徽省铜陵市郊区
     * 340721 安徽省铜陵市铜陵县
     * 340800 安徽省安庆市
     * 340801 安徽省安庆市市辖区
     * 340802 安徽省安庆市迎江区
     * 340803 安徽省安庆市大观区
     * 340811 安徽省安庆市郊区
     * 340822 安徽省安庆市怀宁县
     * 340823 安徽省安庆市枞阳县
     * 340824 安徽省安庆市潜山县
     * 340825 安徽省安庆市太湖县
     * 340826 安徽省安庆市宿松县
     * 340827 安徽省安庆市望江县
     * 340828 安徽省安庆市岳西县
     * 340881 安徽省安庆市桐城市
     * 341000 安徽省黄山市
     * 341001 安徽省黄山市市辖区
     * 341002 安徽省黄山市屯溪区
     * 341003 安徽省黄山市黄山区
     * 341004 安徽省黄山市徽州区
     * 341021 安徽省黄山市歙县
     * 341022 安徽省黄山市休宁县
     * 341023 安徽省黄山市黟县
     * 341024 安徽省黄山市祁门县
     * 341100 安徽省滁州市
     * 341101 安徽省滁州市市辖区
     * 341102 安徽省滁州市琅琊区
     * 341103 安徽省滁州市南谯区
     * 341122 安徽省滁州市来安县
     * 341124 安徽省滁州市全椒县
     * 341125 安徽省滁州市定远县
     * 341126 安徽省滁州市凤阳县
     * 341181 安徽省滁州市天长市
     * 341182 安徽省滁州市明光市
     * 341200 安徽省阜阳市
     * 341201 安徽省阜阳市市辖区
     * 341202 安徽省阜阳市颍州区
     * 341203 安徽省阜阳市颍东区
     * 341204 安徽省阜阳市颍泉区
     * 341221 安徽省阜阳市临泉县
     * 341222 安徽省阜阳市太和县
     * 341223 安徽省阜阳市涡阳县
     * 341224 安徽省阜阳市蒙城县
     * 341225 安徽省阜阳市阜南县
     * 341226 安徽省阜阳市颍上县
     * 341227 安徽省阜阳市利辛县
     * 341281 安徽省阜阳市亳州市
     * 341282 安徽省阜阳市界首市
     * 341300 安徽省宿州市
     * 341301 安徽省宿州市市辖区
     * 341302 安徽省宿州市甬桥区
     * 341321 安徽省宿州市砀山县
     * 341322 安徽省宿州市萧县
     * 341323 安徽省宿州市灵璧县
     * 341324 安徽省宿州市泗县
     * 342400 安徽省六安地区
     * 342401 安徽省六安地区六安市
     * 342422 安徽省六安地区寿县
     * 342423 安徽省六安地区霍邱县
     * 342425 安徽省六安地区舒城县
     * 342426 安徽省六安地区金寨县
     * 342427 安徽省六安地区霍山县
     * 342500 安徽省宣城地区
     * 342501 安徽省宣城地区宣州市
     * 342502 安徽省宣城地区宁国市
     * 342522 安徽省宣城地区郎溪县
     * 342523 安徽省宣城地区广德县
     * 342529 安徽省宣城地区泾县
     * 342530 安徽省宣城地区旌德县
     * 342531 安徽省宣城地区绩溪县
     * 342600 安徽省巢湖地区
     * 342601 安徽省巢湖地区巢湖市
     * 342622 安徽省巢湖地区庐江县
     * 342623 安徽省巢湖地区无为县
     * 342625 安徽省巢湖地区含山县
     * 342626 安徽省巢湖地区和县
     * 342900 安徽省池州地区
     * 342901 安徽省池州地区贵池市
     * 342921 安徽省池州地区东至县
     * 342922 安徽省池州地区石台县
     * 342923 安徽省池州地区青阳县
     * 350000 福建省
     * 350100 福建省福州市
     * 350101 福建省福州市市辖区
     * 350102 福建省福州市鼓楼区
     * 350103 福建省福州市台江区
     * 350104 福建省福州市仓山区
     * 350105 福建省福州市马尾区
     * 350111 福建省福州市晋安区
     * 350121 福建省福州市闽侯县
     * 350122 福建省福州市连江县
     * 350123 福建省福州市罗源县
     * 350124 福建省福州市闽清县
     * 350125 福建省福州市永泰县
     * 350128 福建省福州市平潭县
     * 350181 福建省福州市福清市
     * 350182 福建省福州市长乐市
     * 350200 福建省厦门市
     * 350201 福建省厦门市市辖区
     * 350202 福建省厦门市鼓浪屿区
     * 350203 福建省厦门市思明区
     * 350204 福建省厦门市开元区
     * 350205 福建省厦门市杏林区
     * 350206 福建省厦门市湖里区
     * 350211 福建省厦门市集美区
     * 350212 福建省厦门市同安区
     * 350300 福建省莆田市
     * 350301 福建省莆田市市辖区
     * 350302 福建省莆田市城厢区
     * 350303 福建省莆田市涵江区
     * 350321 福建省莆田市莆田县
     * 350322 福建省莆田市仙游县
     * 350400 福建省三明市
     * 350401 福建省三明市市辖区
     * 350402 福建省三明市梅列区
     * 350403 福建省三明市三元区
     * 350421 福建省三明市明溪县
     * 350423 福建省三明市清流县
     * 350424 福建省三明市宁化县
     * 350425 福建省三明市大田县
     * 350426 福建省三明市尤溪县
     * 350427 福建省三明市沙县
     * 350428 福建省三明市将乐县
     * 350429 福建省三明市泰宁县
     * 350430 福建省三明市建宁县
     * 350481 福建省三明市永安市
     * 350500 福建省泉州市
     * 350501 福建省泉州市市辖区
     * 350502 福建省泉州市鲤城区
     * 350503 福建省泉州市丰泽区
     * 350504 福建省泉州市洛江区
     * 350521 福建省泉州市惠安县
     * 350524 福建省泉州市安溪县
     * 350525 福建省泉州市永春县
     * 350526 福建省泉州市德化县
     * 350527 福建省泉州市金门县
     * 350581 福建省泉州市石狮市
     * 350582 福建省泉州市晋江市
     * 350583 福建省泉州市南安市
     * 350600 福建省漳州市
     * 350601 福建省漳州市市辖区
     * 350602 福建省漳州市芗城区
     * 350603 福建省漳州市龙文区
     * 350622 福建省漳州市云霄县
     * 350623 福建省漳州市漳浦县
     * 350624 福建省漳州市诏安县
     * 350625 福建省漳州市长泰县
     * 350626 福建省漳州市东山县
     * 350627 福建省漳州市南靖县
     * 350628 福建省漳州市平和县
     * 350629 福建省漳州市华安县
     * 350681 福建省漳州市龙海市
     * 350700 福建省南平市
     * 350701 福建省南平市市辖区
     * 350702 福建省南平市延平区
     * 350721 福建省南平市顺昌县
     * 350722 福建省南平市浦城县
     * 350723 福建省南平市光泽县
     * 350724 福建省南平市松溪县
     * 350725 福建省南平市政和县
     * 350781 福建省南平市邵武市
     * 350782 福建省南平市武夷山市
     * 350783 福建省南平市建瓯市
     * 350784 福建省南平市建阳市
     * 350800 福建省龙岩市
     * 350801 福建省龙岩市市辖区
     * 350802 福建省龙岩市新罗区
     * 350821 福建省龙岩市长汀县
     * 350822 福建省龙岩市永定县
     * 350823 福建省龙岩市上杭县
     * 350824 福建省龙岩市武平县
     * 350825 福建省龙岩市连城县
     * 350881 福建省龙岩市漳平市
     * 352200 福建省宁德地区
     * 352201 福建省宁德地区宁德市
     * 352202 福建省宁德地区福安市
     * 352203 福建省宁德地区福鼎市
     * 352225 福建省宁德地区霞浦县
     * 352227 福建省宁德地区古田县
     * 352228 福建省宁德地区屏南县
     * 352229 福建省宁德地区寿宁县
     * 352230 福建省宁德地区周宁县
     * 352231 福建省宁德地区柘荣县
     * 360000 江西省
     * 360100 江西省南昌市
     * 360101 江西省南昌市市辖区
     * 360102 江西省南昌市东湖区
     * 360103 江西省南昌市西湖区
     * 360104 江西省南昌市青云谱区
     * 360105 江西省南昌市湾里区
     * 360111 江西省南昌市郊区
     * 360121 江西省南昌市南昌县
     * 360122 江西省南昌市新建县
     * 360123 江西省南昌市安义县
     * 360124 江西省南昌市进贤县
     * 360200 江西省景德镇市
     * 360201 江西省景德镇市市辖区
     * 360202 江西省景德镇市昌江区
     * 360203 江西省景德镇市珠山区
     * 360222 江西省景德镇市浮梁县
     * 360281 江西省景德镇市乐平市
     * 360300 江西省萍乡市
     * 360301 江西省萍乡市市辖区
     * 360302 江西省萍乡市安源区
     * 360313 江西省萍乡市湘东区
     * 360321 江西省萍乡市莲花县
     * 360322 江西省萍乡市上栗县
     * 360323 江西省萍乡市芦溪县
     * 360400 江西省九江市
     * 360401 江西省九江市市辖区
     * 360402 江西省九江市庐山区
     * 360403 江西省九江市浔阳区
     * 360421 江西省九江市九江县
     * 360423 江西省九江市武宁县
     * 360424 江西省九江市修水县
     * 360425 江西省九江市永修县
     * 360426 江西省九江市德安县
     * 360427 江西省九江市星子县
     * 360428 江西省九江市都昌县
     * 360429 江西省九江市湖口县
     * 360430 江西省九江市彭泽县
     * 360481 江西省九江市瑞昌市
     * 360500 江西省新余市
     * 360501 江西省新余市市辖区
     * 360502 江西省新余市渝水区
     * 360521 江西省新余市分宜县
     * 360600 江西省鹰潭市
     * 360601 江西省鹰潭市市辖区
     * 360602 江西省鹰潭市月湖区
     * 360622 江西省鹰潭市余江县
     * 360681 江西省鹰潭市贵溪市
     * 360700 江西省赣州市
     * 360701 江西省赣州市市辖区
     * 360702 江西省赣州市章贡区
     * 360721 江西省赣州市赣县
     * 360722 江西省赣州市信丰县
     * 360723 江西省赣州市大余县
     * 360724 江西省赣州市上犹县
     * 360725 江西省赣州市崇义县
     * 360726 江西省赣州市安远县
     * 360727 江西省赣州市龙南县
     * 360728 江西省赣州市定南县
     * 360729 江西省赣州市全南县
     * 360730 江西省赣州市宁都县
     * 360731 江西省赣州市于都县
     * 360732 江西省赣州市兴国县
     * 360733 江西省赣州市会昌县
     * 360734 江西省赣州市寻乌县
     * 360735 江西省赣州市石城县
     * 360781 江西省赣州市瑞金市
     * 360782 江西省赣州市南康市
     * 362200 江西省宜春地区
     * 362201 江西省宜春地区宜春市
     * 362202 江西省宜春地区丰城市
     * 362203 江西省宜春地区樟树市
     * 362204 江西省宜春地区高安市
     * 362226 江西省宜春地区奉新县
     * 362227 江西省宜春地区万载县
     * 362228 江西省宜春地区上高县
     * 362229 江西省宜春地区宜丰县
     * 362232 江西省宜春地区靖安县
     * 362233 江西省宜春地区铜鼓县
     * 362300 江西省上饶地区
     * 362301 江西省上饶地区上饶市
     * 362302 江西省上饶地区德兴市
     * 362321 江西省上饶地区上饶县
     * 362322 江西省上饶地区广丰县
     * 362323 江西省上饶地区玉山县
     * 362324 江西省上饶地区铅山县
     * 362325 江西省上饶地区横峰县
     * 362326 江西省上饶地区弋阳县
     * 362329 江西省上饶地区余干县
     * 362330 江西省上饶地区波阳县
     * 362331 江西省上饶地区万年县
     * 362334 江西省上饶地区婺源县
     * 362400 江西省吉安地区
     * 362401 江西省吉安地区吉安市
     * 362402 江西省吉安地区井冈山市
     * 362421 江西省吉安地区吉安县
     * 362422 江西省吉安地区吉水县
     * 362423 江西省吉安地区峡江县
     * 362424 江西省吉安地区新干县
     * 362425 江西省吉安地区永丰县
     * 362426 江西省吉安地区泰和县
     * 362427 江西省吉安地区遂川县
     * 362428 江西省吉安地区万安县
     * 362429 江西省吉安地区安福县
     * 362430 江西省吉安地区永新县
     * 362432 江西省吉安地区宁冈县
     * 362500 江西省抚州地区
     * 362502 江西省抚州地区临川市
     * 362522 江西省抚州地区南城县
     * 362523 江西省抚州地区黎川县
     * 362524 江西省抚州地区南丰县
     * 362525 江西省抚州地区崇仁县
     * 362526 江西省抚州地区乐安县
     * 362527 江西省抚州地区宜黄县
     * 362528 江西省抚州地区金溪县
     * 362529 江西省抚州地区资溪县
     * 362531 江西省抚州地区东乡县
     * 362532 江西省抚州地区广昌县
     * 370000 山东省
     * 370100 山东省济南市
     * 370101 山东省济南市市辖区
     * 370102 山东省济南市历下区
     * 370103 山东省济南市市中区
     * 370104 山东省济南市槐荫区
     * 370105 山东省济南市天桥区
     * 370112 山东省济南市历城区
     * 370123 山东省济南市长清县
     * 370124 山东省济南市平阴县
     * 370125 山东省济南市济阳县
     * 370126 山东省济南市商河县
     * 370181 山东省济南市章丘市
     * 370200 山东省青岛市
     * 370201 山东省青岛市市辖区
     * 370202 山东省青岛市市南区
     * 370203 山东省青岛市市北区
     * 370205 山东省青岛市四方区
     * 370211 山东省青岛市黄岛区
     * 370212 山东省青岛市崂山区
     * 370213 山东省青岛市李沧区
     * 370214 山东省青岛市城阳区
     * 370281 山东省青岛市胶州市
     * 370282 山东省青岛市即墨市
     * 370283 山东省青岛市平度市
     * 370284 山东省青岛市胶南市
     * 370285 山东省青岛市莱西市
     * 370300 山东省淄博市
     * 370301 山东省淄博市市辖区
     * 370302 山东省淄博市淄川区
     * 370303 山东省淄博市张店区
     * 370304 山东省淄博市博山区
     * 370305 山东省淄博市临淄区
     * 370306 山东省淄博市周村区
     * 370321 山东省淄博市桓台县
     * 370322 山东省淄博市高青县
     * 370323 山东省淄博市沂源县
     * 370400 山东省枣庄市
     * 370401 山东省枣庄市市辖区
     * 370402 山东省枣庄市市中区
     * 370403 山东省枣庄市薛城区
     * 370404 山东省枣庄市峄城区
     * 370405 山东省枣庄市台儿庄区
     * 370406 山东省枣庄市山亭区
     * 370481 山东省枣庄市滕州市
     * 370500 山东省东营市
     * 370501 山东省东营市市辖区
     * 370502 山东省东营市东营区
     * 370503 山东省东营市河口区
     * 370521 山东省东营市垦利县
     * 370522 山东省东营市利津县
     * 370523 山东省东营市广饶县
     * 370600 山东省烟台市
     * 370601 山东省烟台市市辖区
     * 370602 山东省烟台市芝罘区
     * 370611 山东省烟台市福山区
     * 370612 山东省烟台市牟平区
     * 370613 山东省烟台市莱山区
     * 370634 山东省烟台市长岛县
     * 370681 山东省烟台市龙口市
     * 370682 山东省烟台市莱阳市
     * 370683 山东省烟台市莱州市
     * 370684 山东省烟台市蓬莱市
     * 370685 山东省烟台市招远市
     * 370686 山东省烟台市栖霞市
     * 370687 山东省烟台市海阳市
     * 370700 山东省潍坊市
     * 370701 山东省潍坊市市辖区
     * 370702 山东省潍坊市潍城区
     * 370703 山东省潍坊市寒亭区
     * 370704 山东省潍坊市坊子区
     * 370705 山东省潍坊市奎文区
     * 370724 山东省潍坊市临朐县
     * 370725 山东省潍坊市昌乐县
     * 370781 山东省潍坊市青州市
     * 370782 山东省潍坊市诸城市
     * 370783 山东省潍坊市寿光市
     * 370784 山东省潍坊市安丘市
     * 370785 山东省潍坊市高密市
     * 370786 山东省潍坊市昌邑市
     * 370800 山东省济宁市
     * 370801 山东省济宁市市辖区
     * 370802 山东省济宁市市中区
     * 370811 山东省济宁市任城区
     * 370826 山东省济宁市微山县
     * 370827 山东省济宁市鱼台县
     * 370828 山东省济宁市金乡县
     * 370829 山东省济宁市嘉祥县
     * 370830 山东省济宁市汶上县
     * 370831 山东省济宁市泗水县
     * 370832 山东省济宁市梁山县
     * 370881 山东省济宁市曲阜市
     * 370882 山东省济宁市兖州市
     * 370883 山东省济宁市邹城市
     * 370900 山东省泰安市
     * 370901 山东省泰安市市辖区
     * 370902 山东省泰安市泰山区
     * 370911 山东省泰安市郊区
     * 370921 山东省泰安市宁阳县
     * 370923 山东省泰安市东平县
     * 370982 山东省泰安市新泰市
     * 370983 山东省泰安市肥城市
     * 371000 山东省威海市
     * 371001 山东省威海市市辖区
     * 371002 山东省威海市环翠区
     * 371081 山东省威海市文登市
     * 371082 山东省威海市荣成市
     * 371083 山东省威海市乳山市
     * 371100 山东省日照市
     * 371101 山东省日照市市辖区
     * 371102 山东省日照市东港区
     * 371121 山东省日照市五莲县
     * 371122 山东省日照市莒县
     * 371200 山东省莱芜市
     * 371201 山东省莱芜市市辖区
     * 371202 山东省莱芜市莱城区
     * 371203 山东省莱芜市钢城区
     * 371300 山东省临沂市
     * 371301 山东省临沂市市辖区
     * 371302 山东省临沂市兰山区
     * 371311 山东省临沂市罗庄区
     * 371312 山东省临沂市河东区
     * 371321 山东省临沂市沂南县
     * 371322 山东省临沂市郯城县
     * 371323 山东省临沂市沂水县
     * 371324 山东省临沂市苍山县
     * 371325 山东省临沂市费县
     * 371326 山东省临沂市平邑县
     * 371327 山东省临沂市莒南县
     * 371328 山东省临沂市蒙阴县
     * 371329 山东省临沂市临沭县
     * 371400 山东省德州市
     * 371401 山东省德州市市辖区
     * 371402 山东省德州市德城区
     * 371421 山东省德州市陵县
     * 371422 山东省德州市宁津县
     * 371423 山东省德州市庆云县
     * 371424 山东省德州市临邑县
     * 371425 山东省德州市齐河县
     * 371426 山东省德州市平原县
     * 371427 山东省德州市夏津县
     * 371428 山东省德州市武城县
     * 371481 山东省德州市乐陵市
     * 371482 山东省德州市禹城市
     * 371500 山东省聊城市
     * 371501 山东省聊城市市辖区
     * 371502 山东省聊城市东昌府区
     * 371521 山东省聊城市阳谷县
     * 371522 山东省聊城市莘县
     * 371523 山东省聊城市茌平县
     * 371524 山东省聊城市东阿县
     * 371525 山东省聊城市冠县
     * 371526 山东省聊城市高唐县
     * 371581 山东省聊城市临清市
     * 372300 山东省滨州地区
     * 372301 山东省滨州地区滨州市
     * 372321 山东省滨州地区惠民县
     * 372323 山东省滨州地区阳信县
     * 372324 山东省滨州地区无棣县
     * 372325 山东省滨州地区沾化县
     * 372328 山东省滨州地区博兴县
     * 372330 山东省滨州地区邹平县
     * 372900 山东省菏泽地区
     * 372901 山东省菏泽地区菏泽市
     * 372922 山东省菏泽地区曹县
     * 372923 山东省菏泽地区定陶县
     * 372924 山东省菏泽地区成武县
     * 372925 山东省菏泽地区单县
     * 372926 山东省菏泽地区巨野县
     * 372928 山东省菏泽地区郓城县
     * 372929 山东省菏泽地区鄄城县
     * 372930 山东省菏泽地区东明县
     * 410000 河南省
     * 410100 河南省郑州市
     * 410101 河南省郑州市市辖区
     * 410102 河南省郑州市中原区
     * 410103 河南省郑州市二七区
     * 410104 河南省郑州市管城回族区
     * 410105 河南省郑州市金水区
     * 410106 河南省郑州市上街区
     * 410108 河南省郑州市邙山区
     * 410122 河南省郑州市中牟县
     * 410181 河南省郑州市巩义市
     * 410182 河南省郑州市荥阳市
     * 410183 河南省郑州市新密市
     * 410184 河南省郑州市新郑市
     * 410185 河南省郑州市登封市
     * 410200 河南省开封市
     * 410201 河南省开封市市辖区
     * 410202 河南省开封市龙亭区
     * 410203 河南省开封市顺河回族区
     * 410204 河南省开封市鼓楼区
     * 410205 河南省开封市南关区
     * 410211 河南省开封市郊区
     * 410221 河南省开封市杞县
     * 410222 河南省开封市通许县
     * 410223 河南省开封市尉氏县
     * 410224 河南省开封市开封县
     * 410225 河南省开封市兰考县
     * 410300 河南省洛阳市
     * 410301 河南省洛阳市市辖区
     * 410302 河南省洛阳市老城区
     * 410303 河南省洛阳市西工区
     * 410304 河南省洛阳市廛河回族区
     * 410305 河南省洛阳市涧西区
     * 410306 河南省洛阳市吉利区
     * 410311 河南省洛阳市郊区
     * 410322 河南省洛阳市孟津县
     * 410323 河南省洛阳市新安县
     * 410324 河南省洛阳市栾川县
     * 410325 河南省洛阳市嵩县
     * 410326 河南省洛阳市汝阳县
     * 410327 河南省洛阳市宜阳县
     * 410328 河南省洛阳市洛宁县
     * 410329 河南省洛阳市伊川县
     * 410381 河南省洛阳市偃师市
     * 410400 河南省平顶山市
     * 410401 河南省平顶山市市辖区
     * 410402 河南省平顶山市新华区
     * 410403 河南省平顶山市卫东区
     * 410404 河南省平顶山市石龙区
     * 410411 河南省平顶山市湛河区
     * 410421 河南省平顶山市宝丰县
     * 410422 河南省平顶山市叶县
     * 410423 河南省平顶山市鲁山县
     * 410425 河南省平顶山市郏县
     * 410481 河南省平顶山市舞钢市
     * 410482 河南省平顶山市汝州市
     * 410500 河南省安阳市
     * 410501 河南省安阳市市辖区
     * 410502 河南省安阳市文峰区
     * 410503 河南省安阳市北关区
     * 410504 河南省安阳市铁西区
     * 410511 河南省安阳市郊区
     * 410522 河南省安阳市安阳县
     * 410523 河南省安阳市汤阴县
     * 410526 河南省安阳市滑县
     * 410527 河南省安阳市内黄县
     * 410581 河南省安阳市林州市
     * 410600 河南省鹤壁市
     * 410601 河南省鹤壁市市辖区
     * 410602 河南省鹤壁市鹤山区
     * 410603 河南省鹤壁市山城区
     * 410611 河南省鹤壁市郊区
     * 410621 河南省鹤壁市浚县
     * 410622 河南省鹤壁市淇县
     * 410700 河南省新乡市
     * 410701 河南省新乡市市辖区
     * 410702 河南省新乡市红旗区
     * 410703 河南省新乡市新华区
     * 410704 河南省新乡市北站区
     * 410711 河南省新乡市郊区
     * 410721 河南省新乡市新乡县
     * 410724 河南省新乡市获嘉县
     * 410725 河南省新乡市原阳县
     * 410726 河南省新乡市延津县
     * 410727 河南省新乡市封丘县
     * 410728 河南省新乡市长垣县
     * 410781 河南省新乡市卫辉市
     * 410782 河南省新乡市辉县市
     * 410800 河南省焦作市
     * 410801 河南省焦作市市辖区
     * 410802 河南省焦作市解放区
     * 410803 河南省焦作市中站区
     * 410804 河南省焦作市马村区
     * 410811 河南省焦作市山阳区
     * 410821 河南省焦作市修武县
     * 410822 河南省焦作市博爱县
     * 410823 河南省焦作市武陟县
     * 410825 河南省焦作市温县
     * 410881 河南省焦作市济源市
     * 410882 河南省焦作市沁阳市
     * 410883 河南省焦作市孟州市
     * 410900 河南省濮阳市
     * 410901 河南省濮阳市市辖区
     * 410902 河南省濮阳市市区
     * 410922 河南省濮阳市清丰县
     * 410923 河南省濮阳市南乐县
     * 410926 河南省濮阳市范县
     * 410927 河南省濮阳市台前县
     * 410928 河南省濮阳市濮阳县
     * 411000 河南省许昌市
     * 411001 河南省许昌市市辖区
     * 411002 河南省许昌市魏都区
     * 411023 河南省许昌市许昌县
     * 411024 河南省许昌市鄢陵县
     * 411025 河南省许昌市襄城县
     * 411081 河南省许昌市禹州市
     * 411082 河南省许昌市长葛市
     * 411100 河南省漯河市
     * 411101 河南省漯河市市辖区
     * 411102 河南省漯河市源汇区
     * 411121 河南省漯河市舞阳县
     * 411122 河南省漯河市临颍县
     * 411123 河南省漯河市郾城县
     * 411200 河南省三门峡市
     * 411201 河南省三门峡市市辖区
     * 411202 河南省三门峡市湖滨区
     * 411221 河南省三门峡市渑池县
     * 411222 河南省三门峡市陕县
     * 411224 河南省三门峡市卢氏县
     * 411281 河南省三门峡市义马市
     * 411282 河南省三门峡市灵宝市
     * 411300 河南省南阳市
     * 411301 河南省南阳市市辖区
     * 411302 河南省南阳市宛城区
     * 411303 河南省南阳市卧龙区
     * 411321 河南省南阳市南召县
     * 411322 河南省南阳市方城县
     * 411323 河南省南阳市西峡县
     * 411324 河南省南阳市镇平县
     * 411325 河南省南阳市内乡县
     * 411326 河南省南阳市淅川县
     * 411327 河南省南阳市社旗县
     * 411328 河南省南阳市唐河县
     * 411329 河南省南阳市新野县
     * 411330 河南省南阳市桐柏县
     * 411381 河南省南阳市邓州市
     * 411400 河南省商丘市
     * 411401 河南省商丘市市辖区
     * 411402 河南省商丘市梁园区
     * 411403 河南省商丘市睢阳区
     * 411421 河南省商丘市民权县
     * 411422 河南省商丘市睢县
     * 411423 河南省商丘市宁陵县
     * 411424 河南省商丘市柘城县
     * 411425 河南省商丘市虞城县
     * 411426 河南省商丘市夏邑县
     * 411481 河南省商丘市永城市
     * 411500 河南省信阳市
     * 411501 河南省信阳市市辖区
     * 411502 河南省信阳市师河区
     * 411503 河南省信阳市平桥区
     * 411521 河南省信阳市罗山县
     * 411522 河南省信阳市光山县
     * 411523 河南省信阳市新县
     * 411524 河南省信阳市商城县
     * 411525 河南省信阳市固始县
     * 411526 河南省信阳市潢川县
     * 411527 河南省信阳市淮滨县
     * 411528 河南省信阳市息县
     * 412700 河南省周口地区
     * 412701 河南省周口地区周口市
     * 412702 河南省周口地区项城市
     * 412721 河南省周口地区扶沟县
     * 412722 河南省周口地区西华县
     * 412723 河南省周口地区商水县
     * 412724 河南省周口地区太康县
     * 412725 河南省周口地区鹿邑县
     * 412726 河南省周口地区郸城县
     * 412727 河南省周口地区淮阳县
     * 412728 河南省周口地区沈丘县
     * 412800 河南省驻马店地区
     * 412801 河南省驻马店地区驻马店市
     * 412821 河南省驻马店地区确山县
     * 412822 河南省驻马店地区泌阳县
     * 412823 河南省驻马店地区遂平县
     * 412824 河南省驻马店地区西平县
     * 412825 河南省驻马店地区上蔡县
     * 412826 河南省驻马店地区汝南县
     * 412827 河南省驻马店地区平舆县
     * 412828 河南省驻马店地区新蔡县
     * 412829 河南省驻马店地区正阳县
     * 420000 湖北省
     * 420100 湖北省武汉市
     * 420101 湖北省武汉市市辖区
     * 420102 湖北省武汉市江岸区
     * 420103 湖北省武汉市江汉区
     * 420104 湖北省武汉市乔口区
     * 420105 湖北省武汉市汉阳区
     * 420106 湖北省武汉市武昌区
     * 420107 湖北省武汉市青山区
     * 420111 湖北省武汉市洪山区
     * 420112 湖北省武汉市东西湖区
     * 420113 湖北省武汉市汉南区
     * 420114 湖北省武汉市蔡甸区
     * 420115 湖北省武汉市江夏区
     * 420116 湖北省武汉市黄陂区
     * 420117 湖北省武汉市新洲区
     * 420200 湖北省黄石市
     * 420201 湖北省黄石市市辖区
     * 420202 湖北省黄石市黄石港区
     * 420203 湖北省黄石市石灰窑区
     * 420204 湖北省黄石市下陆区
     * 420205 湖北省黄石市铁山区
     * 420222 湖北省黄石市阳新县
     * 420281 湖北省黄石市大冶市
     * 420300 湖北省十堰市
     * 420301 湖北省十堰市市辖区
     * 420302 湖北省十堰市茅箭区
     * 420303 湖北省十堰市张湾区
     * 420321 湖北省十堰市郧县
     * 420322 湖北省十堰市郧西县
     * 420323 湖北省十堰市竹山县
     * 420324 湖北省十堰市竹溪县
     * 420325 湖北省十堰市房县
     * 420381 湖北省十堰市丹江口市
     * 420500 湖北省宜昌市
     * 420501 湖北省宜昌市市辖区
     * 420502 湖北省宜昌市西陵区
     * 420503 湖北省宜昌市伍家岗区
     * 420504 湖北省宜昌市点军区
     * 420505 湖北省宜昌市虎亭区
     * 420521 湖北省宜昌市宜昌县
     * 420525 湖北省宜昌市远安县
     * 420526 湖北省宜昌市兴山县
     * 420527 湖北省宜昌市秭归县
     * 420528 湖北省宜昌市长阳土家族自治县
     * 420529 湖北省宜昌市五峰土家族自治县
     * 420581 湖北省宜昌市宜都市
     * 420582 湖北省宜昌市当阳市
     * 420583 湖北省宜昌市枝江市
     * 420600 湖北省襄樊市
     * 420601 湖北省襄樊市市辖区
     * 420602 湖北省襄樊市襄城区
     * 420606 湖北省襄樊市樊城区
     * 420621 湖北省襄樊市襄阳县
     * 420624 湖北省襄樊市南漳县
     * 420625 湖北省襄樊市谷城县
     * 420626 湖北省襄樊市保康县
     * 420682 湖北省襄樊市老河口市
     * 420683 湖北省襄樊市枣阳市
     * 420684 湖北省襄樊市宜城市
     * 420700 湖北省鄂州市
     * 420701 湖北省鄂州市市辖区
     * 420702 湖北省鄂州市梁子湖区
     * 420703 湖北省鄂州市华容区
     * 420704 湖北省鄂州市鄂城区
     * 420800 湖北省荆门市
     * 420801 湖北省荆门市市辖区
     * 420802 湖北省荆门市东宝区
     * 420821 湖北省荆门市京山县
     * 420822 湖北省荆门市沙洋县
     * 420881 湖北省荆门市钟祥市
     * 420900 湖北省孝感市
     * 420901 湖北省孝感市市辖区
     * 420902 湖北省孝感市孝南区
     * 420921 湖北省孝感市孝昌县
     * 420922 湖北省孝感市大悟县
     * 420923 湖北省孝感市云梦县
     * 420981 湖北省孝感市应城市
     * 420982 湖北省孝感市安陆市
     * 420983 湖北省孝感市广水市
     * 420984 湖北省孝感市汉川市
     * 421000 湖北省荆州市
     * 421001 湖北省荆州市市辖区
     * 421002 湖北省荆州市沙市区
     * 421003 湖北省荆州市荆州区
     * 421022 湖北省荆州市公安县
     * 421023 湖北省荆州市监利县
     * 421024 湖北省荆州市江陵县
     * 421081 湖北省荆州市石首市
     * 421083 湖北省荆州市洪湖市
     * 421087 湖北省荆州市松滋市
     * 421100 湖北省黄冈市
     * 421101 湖北省黄冈市市辖区
     * 421102 湖北省黄冈市黄州区
     * 421121 湖北省黄冈市团风县
     * 421122 湖北省黄冈市红安县
     * 421123 湖北省黄冈市罗田县
     * 421124 湖北省黄冈市英山县
     * 421125 湖北省黄冈市浠水县
     * 421126 湖北省黄冈市蕲春县
     * 421127 湖北省黄冈市黄梅县
     * 421181 湖北省黄冈市麻城市
     * 421182 湖北省黄冈市武穴市
     * 421200 湖北省咸宁市
     * 421201 湖北省咸宁市市辖区
     * 421202 湖北省咸宁市咸安区
     * 421221 湖北省咸宁市嘉鱼县
     * 421222 湖北省咸宁市通城县
     * 421223 湖北省咸宁市崇阳县
     * 421224 湖北省咸宁市通山县
     * 422800 湖北省施土家族苗族自治州
     * 422801 湖北省恩施土家族苗族自治州恩施县
     * 422802 湖北省恩施土家族苗族自治州利川市
     * 422822 湖北省恩施土家族苗族自治州建始县
     * 422823 湖北省恩施土家族苗族自治州巴东县
     * 422825 湖北省恩施土家族苗族自治州宣恩县
     * 422826 湖北省恩施土家族苗族自治州咸丰县
     * 422827 湖北省恩施土家族苗族自治州来凤县
     * 422828 湖北省恩施土家族苗族自治州鹤峰县
     * 429000 湖北省省直辖县级行政单位
     * 429001 湖北省随州市
     * 429004 湖北省仙桃市
     * 429005 湖北省潜江市
     * 429006 湖北省天门市
     * 429021 湖北省神农架林区
     * 430000 湖南省
     * 430100 湖南省长沙市
     * 430101 湖南省长沙市市辖区
     * 430102 湖南省长沙市芙蓉区
     * 430103 湖南省长沙市天心区
     * 430104 湖南省长沙市岳麓区
     * 430105 湖南省长沙市开福区
     * 430111 湖南省长沙市雨花区
     * 430121 湖南省长沙市长沙县
     * 430122 湖南省长沙市望城县
     * 430124 湖南省长沙市宁乡县
     * 430181 湖南省长沙市浏阳市
     * 430200 湖南省株洲市
     * 430201 湖南省株洲市市辖区
     * 430202 湖南省株洲市荷塘区
     * 430203 湖南省株洲市芦淞区
     * 430204 湖南省株洲市石峰区
     * 430211 湖南省株洲市天元区
     * 430221 湖南省株洲市株洲县
     * 430223 湖南省株洲市攸县
     * 430224 湖南省株洲市茶陵县
     * 430225 湖南省株洲市炎陵县
     * 430281 湖南省株洲市醴陵市
     * 430300 湖南省湘潭市
     * 430301 湖南省湘潭市市辖区
     * 430302 湖南省湘潭市雨湖区
     * 430304 湖南省湘潭市岳塘区
     * 430321 湖南省湘潭市湘潭县
     * 430381 湖南省湘潭市湘乡市
     * 430382 湖南省湘潭市韶山市
     * 430400 湖南省衡阳市
     * 430401 湖南省衡阳市市辖区
     * 430402 湖南省衡阳市江东区
     * 430403 湖南省衡阳市城南区
     * 430404 湖南省衡阳市城北区
     * 430411 湖南省衡阳市郊区
     * 430412 湖南省衡阳市南岳区
     * 430421 湖南省衡阳市衡阳县
     * 430422 湖南省衡阳市衡南县
     * 430423 湖南省衡阳市衡山县
     * 430424 湖南省衡阳市衡东县
     * 430426 湖南省衡阳市祁东县
     * 430481 湖南省衡阳市耒阳市
     * 430482 湖南省衡阳市常宁市
     * 430500 湖南省邵阳市
     * 430501 湖南省邵阳市市辖区
     * 430502 湖南省邵阳市双清区
     * 430503 湖南省邵阳市大祥区
     * 430511 湖南省邵阳市北塔区
     * 430521 湖南省邵阳市邵东县
     * 430522 湖南省邵阳市新邵县
     * 430523 湖南省邵阳市邵阳县
     * 430524 湖南省邵阳市隆回县
     * 430525 湖南省邵阳市洞口县
     * 430527 湖南省邵阳市绥宁县
     * 430528 湖南省邵阳市新宁县
     * 430529 湖南省邵阳市城步苗族自治县
     * 430581 湖南省邵阳市武冈市
     * 430600 湖南省岳阳市
     * 430601 湖南省岳阳市市辖区
     * 430602 湖南省岳阳市岳阳楼区
     * 430603 湖南省岳阳市云溪区
     * 430611 湖南省岳阳市君山区
     * 430621 湖南省岳阳市岳阳县
     * 430623 湖南省岳阳市华容县
     * 430624 湖南省岳阳市湘阴县
     * 430626 湖南省岳阳市平江县
     * 430681 湖南省岳阳市汨罗市
     * 430682 湖南省岳阳市临湘市
     * 430700 湖南省常德市
     * 430701 湖南省常德市市辖区
     * 430702 湖南省常德市武陵区
     * 430703 湖南省常德市鼎城区
     * 430721 湖南省常德市安乡县
     * 430722 湖南省常德市汉寿县
     * 430723 湖南省常德市澧县
     * 430724 湖南省常德市临澧县
     * 430725 湖南省常德市桃源县
     * 430726 湖南省常德市石门县
     * 430781 湖南省常德市津市市
     * 430800 湖南省张家界市
     * 430801 湖南省张家界市市辖区
     * 430802 湖南省张家界市永定区
     * 430811 湖南省张家界市武陵源区
     * 430821 湖南省张家界市慈利县
     * 430822 湖南省张家界市桑植县
     * 430900 湖南省益阳市
     * 430901 湖南省益阳市市辖区
     * 430902 湖南省益阳市资阳区
     * 430903 湖南省益阳市赫山区
     * 430921 湖南省益阳市南县
     * 430922 湖南省益阳市桃江县
     * 430923 湖南省益阳市安化县
     * 430981 湖南省益阳市沅江市
     * 431000 湖南省郴州市
     * 431001 湖南省郴州市市辖区
     * 431002 湖南省郴州市北湖区
     * 431003 湖南省郴州市苏仙区
     * 431021 湖南省郴州市桂阳县
     * 431022 湖南省郴州市宜章县
     * 431023 湖南省郴州市永兴县
     * 431024 湖南省郴州市嘉禾县
     * 431025 湖南省郴州市临武县
     * 431026 湖南省郴州市汝城县
     * 431027 湖南省郴州市桂东县
     * 431028 湖南省郴州市安仁县
     * 431081 湖南省郴州市资兴市
     * 431100 湖南省永州市
     * 431101 湖南省永州市市辖区
     * 431102 湖南省永州市芝山区
     * 431103 湖南省永州市冷水滩区
     * 431121 湖南省永州市祁阳县
     * 431122 湖南省永州市东安县
     * 431123 湖南省永州市双牌县
     * 431124 湖南省永州市道县
     * 431125 湖南省永州市江永县
     * 431126 湖南省永州市宁远县
     * 431127 湖南省永州市蓝山县
     * 431128 湖南省永州市新田县
     * 431129 湖南省永州市江华瑶族自治县
     * 431200 湖南省怀化市
     * 431201 湖南省怀化市市辖区
     * 431202 湖南省怀化市鹤城区
     * 431221 湖南省怀化市中方县
     * 431222 湖南省怀化市沅陵县
     * 431223 湖南省怀化市辰溪县
     * 431224 湖南省怀化市溆浦县
     * 431225 湖南省怀化市会同县
     * 431226 湖南省怀化市麻阳苗族自治县
     * 431227 湖南省怀化市新晃侗族自治县
     * 431228 湖南省怀化市芷江侗族自治县
     * 431229 湖南省怀化市靖州苗族侗族自治县
     * 431230 湖南省怀化市通道侗族自治县
     * 431281 湖南省怀化市洪江市
     * 432500 湖南省娄底地区
     * 432501 湖南省娄底地区娄底市
     * 432502 湖南省娄底地区冷水江市
     * 432503 湖南省娄底地区涟源市
     * 432522 湖南省娄底地区双峰县
     * 432524 湖南省娄底地区新化县
     * 433000 湖南省怀化市
     * 433001 湖南省怀化市
     * 433100 湖南省湘西土家族苗族自治州
     * 433101 湖南省湘西土家族苗族自治州吉首市
     * 433122 湖南省湘西土家族苗族自治州泸溪县
     * 433123 湖南省湘西土家族苗族自治州凤凰县
     * 433124 湖南省湘西土家族苗族自治州花垣县
     * 433125 湖南省湘西土家族苗族自治州保靖县
     * 433126 湖南省湘西土家族苗族自治州古丈县
     * 433127 湖南省湘西土家族苗族自治州永顺县
     * 433130 湖南省湘西土家族苗族自治州龙山县
     * 440000 广东省
     * 440100 广东省广州市
     * 440101 广东省广州市市辖区
     * 440102 广东省广州市东山区
     * 440103 广东省广州市荔湾区
     * 440104 广东省广州市越秀区
     * 440105 广东省广州市海珠区
     * 440106 广东省广州市天河区
     * 440107 广东省广州市芳村区
     * 440111 广东省广州市白云区
     * 440112 广东省广州市黄埔区
     * 440181 广东省广州市番禺市
     * 440182 广东省广州市花都市
     * 440183 广东省广州市增城市
     * 440184 广东省广州市从化市
     * 440200 广东省韶关市
     * 440201 广东省韶关市市辖区
     * 440202 广东省韶关市北江区
     * 440203 广东省韶关市武江区
     * 440204 广东省韶关市浈江区
     * 440221 广东省韶关市曲江县
     * 440222 广东省韶关市始兴县
     * 440224 广东省韶关市仁化县
     * 440229 广东省韶关市翁源县
     * 440232 广东省韶关市乳源瑶族自治县
     * 440233 广东省韶关市新丰县
     * 440281 广东省韶关市乐昌市
     * 440282 广东省韶关市南雄市
     * 440300 广东省深圳市
     * 440301 广东省深圳市市辖区
     * 440303 广东省深圳市罗湖区
     * 440304 广东省深圳市福田区
     * 440305 广东省深圳市南山区
     * 440306 广东省深圳市宝安区
     * 440307 广东省深圳市龙岗区
     * 440308 广东省深圳市盐田区
     * 440400 广东省珠海市
     * 440401 广东省珠海市市辖区
     * 440402 广东省珠海市香洲区
     * 440421 广东省珠海市斗门县
     * 440500 广东省汕头市
     * 440501 广东省汕头市市辖区
     * 440506 广东省汕头市达濠区
     * 440507 广东省汕头市龙湖区
     * 440508 广东省汕头市金园区
     * 440509 广东省汕头市升平区
     * 440510 广东省汕头市河浦区
     * 440523 广东省汕头市南澳县
     * 440582 广东省汕头市潮阳市
     * 440583 广东省汕头市澄海市
     * 440600 广东省佛山市
     * 440601 广东省佛山市市辖区
     * 440602 广东省佛山市城区
     * 440603 广东省佛山市石湾区
     * 440681 广东省佛山市顺德市
     * 440682 广东省佛山市南海市
     * 440683 广东省佛山市三水市
     * 440684 广东省佛山市高明市
     * 440700 广东省江门市
     * 440701 广东省江门市市辖区
     * 440703 广东省江门市蓬江区
     * 440704 广东省江门市江海区
     * 440781 广东省江门市台山市
     * 440782 广东省江门市新会市
     * 440783 广东省江门市开平市
     * 440784 广东省江门市鹤山市
     * 440785 广东省江门市恩平市
     * 440800 广东省湛江市
     * 440801 广东省湛江市市辖区
     * 440802 广东省湛江市赤坎区
     * 440803 广东省湛江市霞山区
     * 440804 广东省湛江市坡头区
     * 440811 广东省湛江市麻章区
     * 440823 广东省湛江市遂溪县
     * 440825 广东省湛江市徐闻县
     * 440881 广东省湛江市廉江市
     * 440882 广东省湛江市雷州市
     * 440883 广东省湛江市吴川市
     * 440900 广东省茂名市
     * 440901 广东省茂名市市辖区
     * 440902 广东省茂名市茂南区
     * 440923 广东省茂名市电白县
     * 440981 广东省茂名市高州市
     * 440982 广东省茂名市化州市
     * 440983 广东省茂名市信宜市
     * 441200 广东省肇庆市
     * 441201 广东省肇庆市市辖区
     * 441202 广东省肇庆市端州区
     * 441203 广东省肇庆市鼎湖区
     * 441223 广东省肇庆市广宁县
     * 441224 广东省肇庆市怀集县
     * 441225 广东省肇庆市封开县
     * 441226 广东省肇庆市德庆县
     * 441283 广东省肇庆市高要市
     * 441284 广东省肇庆市四会市
     * 441300 广东省惠州市
     * 441301 广东省惠州市市辖区
     * 441302 广东省惠州市惠城区
     * 441322 广东省惠州市博罗县
     * 441323 广东省惠州市惠东县
     * 441324 广东省惠州市龙门县
     * 441381 广东省惠州市惠阳市
     * 441400 广东省梅州市
     * 441401 广东省梅州市市辖区
     * 441402 广东省梅州市梅江区
     * 441421 广东省梅州市梅县
     * 441422 广东省梅州市大埔县
     * 441423 广东省梅州市丰顺县
     * 441424 广东省梅州市五华县
     * 441426 广东省梅州市平远县
     * 441427 广东省梅州市蕉岭县
     * 441481 广东省梅州市兴宁市
     * 441500 广东省汕尾市
     * 441501 广东省汕尾市市辖区
     * 441502 广东省汕尾市城区
     * 441521 广东省汕尾市海丰县
     * 441523 广东省汕尾市陆河县
     * 441581 广东省汕尾市陆丰市
     * 441600 广东省河源市
     * 441601 广东省河源市市辖区
     * 441602 广东省河源市源城区
     * 441621 广东省河源市紫金县
     * 441622 广东省河源市龙川县
     * 441623 广东省河源市连平县
     * 441624 广东省河源市和平县
     * 441625 广东省河源市东源县
     * 441700 广东省阳江市
     * 441701 广东省阳江市市辖区
     * 441702 广东省阳江市江城区
     * 441721 广东省阳江市阳西县
     * 441723 广东省阳江市阳东县
     * 441781 广东省阳江市阳春市
     * 441800 广东省清远市
     * 441801 广东省清远市市辖区
     * 441802 广东省清远市清城区
     * 441821 广东省清远市佛冈县
     * 441823 广东省清远市阳山县
     * 441825 广东省清远市连山壮族瑶族自治县
     * 441826 广东省清远市连南瑶族自治县
     * 441827 广东省清远市清新县
     * 441881 广东省清远市英德市
     * 441882 广东省清远市连州市
     * 441900 广东省东莞市
     * 441901 广东省东莞市市辖区
     * 442000 广东省中山市
     * 442001 广东省中山市市辖区
     * 445100 广东省潮州市
     * 445101 广东省潮州市市辖区
     * 445102 广东省潮州市湘桥区
     * 445121 广东省潮州市潮安县
     * 445122 广东省潮州市饶平县
     * 445200 广东省揭阳市
     * 445201 广东省揭阳市市辖区
     * 445202 广东省揭阳市榕城区
     * 445221 广东省揭阳市揭东县
     * 445222 广东省揭阳市揭西县
     * 445224 广东省揭阳市惠来县
     * 445281 广东省揭阳市普宁市
     * 445300 广东省云浮市
     * 445301 广东省云浮市市辖区
     * 445302 广东省云浮市云城区
     * 445321 广东省云浮市新兴县
     * 445322 广东省云浮市郁南县
     * 445323 广东省云浮市云安县
     * 445381 广东省云浮市罗定市
     * 450000 广西壮族自治区
     * 450100 广西壮族自治区南宁市
     * 450101 广西壮族自治区南宁市市辖区
     * 450102 广西壮族自治区南宁市兴宁区
     * 450103 广西壮族自治区南宁市新城区
     * 450104 广西壮族自治区南宁市城北区
     * 450105 广西壮族自治区南宁市江南区
     * 450106 广西壮族自治区南宁市永新区
     * 450111 广西壮族自治区南宁市市郊区
     * 450121 广西壮族自治区南宁市邕宁县
     * 450122 广西壮族自治区南宁市武鸣县
     * 450200 广西壮族自治区柳州市
     * 450201 广西壮族自治区柳州市市辖区
     * 450202 广西壮族自治区柳州市城中区
     * 450203 广西壮族自治区柳州市鱼峰区
     * 450204 广西壮族自治区柳州市柳南区
     * 450205 广西壮族自治区柳州市柳北区
     * 450211 广西壮族自治区柳州市市郊区
     * 450221 广西壮族自治区柳州市柳江县
     * 450222 广西壮族自治区柳州市柳城县
     * 450300 广西壮族自治区桂林市
     * 450301 广西壮族自治区桂林市市辖区
     * 450302 广西壮族自治区桂林市秀峰区
     * 450303 广西壮族自治区桂林市叠彩区
     * 450304 广西壮族自治区桂林市象山区
     * 450305 广西壮族自治区桂林市七星区
     * 450311 广西壮族自治区桂林市雁山区
     * 450321 广西壮族自治区桂林市阳朔县
     * 450322 广西壮族自治区桂林市临桂县
     * 450323 广西壮族自治区桂林市灵川县
     * 450324 广西壮族自治区桂林市全州县
     * 450325 广西壮族自治区桂林市兴安县
     * 450326 广西壮族自治区桂林市永福县
     * 450327 广西壮族自治区桂林市灌阳县
     * 450328 广西壮族自治区桂林市龙胜各族自治县
     * 450329 广西壮族自治区桂林市资源县
     * 450330 广西壮族自治区桂林市平乐县
     * 450331 广西壮族自治区桂林市荔浦县
     * 450332 广西壮族自治区桂林市恭城瑶族自治县
     * 450400 广西壮族自治区梧州市
     * 450401 广西壮族自治区梧州市市辖区
     * 450403 广西壮族自治区梧州市万秀区
     * 450404 广西壮族自治区梧州市蝶山区
     * 450411 广西壮族自治区梧州市市郊区
     * 450421 广西壮族自治区梧州市苍梧县
     * 450422 广西壮族自治区梧州市藤县
     * 450423 广西壮族自治区梧州市蒙山县
     * 450481 广西壮族自治区梧州市岑溪市
     * 450500 广西壮族自治区北海市
     * 450501 广西壮族自治区北海市市辖区
     * 450502 广西壮族自治区北海市海城区
     * 450503 广西壮族自治区北海市银海区
     * 450512 广西壮族自治区北海市铁山港区
     * 450521 广西壮族自治区北海市合浦县
     * 450600 广西壮族自治区防城港市
     * 450601 广西壮族自治区防城港市市辖区
     * 450602 广西壮族自治区防城港市港口区
     * 450603 广西壮族自治区防城港市防城区
     * 450621 广西壮族自治区防城港市上思县
     * 450681 广西壮族自治区防城港市东兴市
     * 450700 广西壮族自治区钦州市
     * 450701 广西壮族自治区钦州市市辖区
     * 450702 广西壮族自治区钦州市钦南区
     * 450703 广西壮族自治区钦州市钦北区
     * 450721 广西壮族自治区钦州市灵山县
     * 450722 广西壮族自治区钦州市浦北县
     * 450800 广西壮族自治区贵港市
     * 450801 广西壮族自治区贵港市市辖区
     * 450802 广西壮族自治区贵港市港北区
     * 450803 广西壮族自治区贵港市港南区
     * 450821 广西壮族自治区贵港市平南县
     * 450881 广西壮族自治区贵港市桂平市
     * 450900 广西壮族自治区玉林市
     * 450901 广西壮族自治区玉林市市辖区
     * 450902 广西壮族自治区玉林市玉州区
     * 450921 广西壮族自治区玉林市容县
     * 450922 广西壮族自治区玉林市陆川县
     * 450923 广西壮族自治区玉林市博白县
     * 450924 广西壮族自治区玉林市兴业县
     * 450981 广西壮族自治区玉林市北流市
     * 452100 广西壮族自治区南宁地区
     * 452101 广西壮族自治区南宁地区凭祥市
     * 452122 广西壮族自治区南宁地区横县
     * 452123 广西壮族自治区南宁地区宾阳县
     * 452124 广西壮族自治区南宁地区上林县
     * 452126 广西壮族自治区南宁地区隆安县
     * 452127 广西壮族自治区南宁地区马山县
     * 452128 广西壮族自治区南宁地区扶绥县
     * 452129 广西壮族自治区南宁地区崇左县
     * 452130 广西壮族自治区南宁地区大新县
     * 452131 广西壮族自治区南宁地区天等县
     * 452132 广西壮族自治区南宁地区宁明县
     * 452133 广西壮族自治区南宁地区龙州县
     * 452200 广西壮族自治区柳州地区
     * 452201 广西壮族自治区柳州地区合山市
     * 452223 广西壮族自治区柳州地区鹿寨县
     * 452224 广西壮族自治区柳州地区象州县
     * 452225 广西壮族自治区柳州地区武宣县
     * 452226 广西壮族自治区柳州地区来宾县
     * 452227 广西壮族自治区柳州地区融安县
     * 452228 广西壮族自治区柳州地区三江侗族自治县
     * 452229 广西壮族自治区柳州地区融水苗族自治县
     * 452230 广西壮族自治区柳州地区金秀瑶族自治县
     * 452231 广西壮族自治区柳州地区忻城县
     * 452400 广西壮族自治区贺州地区
     * 452402 广西壮族自治区贺州地区贺州市
     * 452424 广西壮族自治区贺州地区昭平县
     * 452427 广西壮族自治区贺州地区钟山县
     * 452428 广西壮族自治区贺州地区富川瑶族自治县
     * 452600 广西壮族自治区百色地区
     * 452601 广西壮族自治区百色地区百色市
     * 452622 广西壮族自治区百色地区田阳县
     * 452623 广西壮族自治区百色地区田东县
     * 452624 广西壮族自治区百色地区平果县
     * 452625 广西壮族自治区百色地区德保县
     * 452626 广西壮族自治区百色地区靖西县
     * 452627 广西壮族自治区百色地区那坡县
     * 452628 广西壮族自治区百色地区凌云县
     * 452629 广西壮族自治区百色地区乐业县
     * 452630 广西壮族自治区百色地区田林县
     * 452631 广西壮族自治区百色地区隆林各族自治县
     * 452632 广西壮族自治区百色地区西林县
     * 452700 广西壮族自治区河池地区
     * 452701 广西壮族自治区河池地区河池市
     * 452702 广西壮族自治区河池地区宜州市
     * 452723 广西壮族自治区河池地区罗城仫佬族自治县
     * 452724 广西壮族自治区河池地区环江毛南族自治县
     * 452725 广西壮族自治区河池地区南丹县
     * 452726 广西壮族自治区河池地区天峨县
     * 452727 广西壮族自治区河池地区凤山县
     * 452728 广西壮族自治区河池地区东兰县
     * 452729 广西壮族自治区河池地区巴马瑶族自治县
     * 452730 广西壮族自治区河池地区都安瑶族自治县
     * 452731 广西壮族自治区河池地区大化瑶族自治县
     * 460000 海南省
     * 460001 海南省三亚市通什市
     * 460002 海南省三亚市琼海市
     * 460003 海南省三亚市儋州市
     * 460004 海南省三亚市琼山市
     * 460005 海南省三亚市文昌市
     * 460006 海南省三亚市万宁市
     * 460007 海南省三亚市东方市
     * 460025 海南省三亚市定安县
     * 460026 海南省三亚市屯昌县
     * 460027 海南省三亚市澄迈县
     * 460028 海南省三亚市临高县
     * 460030 海南省三亚市白沙黎族自治县
     * 460031 海南省三亚市昌江黎族自治县
     * 460033 海南省三亚市乐东黎族自治县
     * 460034 海南省三亚市陵水黎族自治县
     * 460035 海南省三亚市保亭黎族苗族自治县
     * 460036 海南省三亚市琼中黎族苗族自治县
     * 460037 海南省西沙群岛
     * 460038 海南省南沙群岛
     * 460039 海南省中沙群岛的岛礁及其海域
     * 460100 海南省海口市
     * 460101 海南省海口市市辖区
     * 460102 海南省海口市振东区
     * 460103 海南省海口市新华区
     * 460104 海南省海口市秀英区
     * 460200 海南省三亚市
     * 460201 海南省三亚市市辖区
     * 500000 重庆市
     * 500100 重庆市市辖区
     * 500101 重庆市万州区
     * 500102 重庆市涪陵区
     * 500103 重庆市渝中区
     * 500104 重庆市大渡口区
     * 500105 重庆市江北区
     * 500106 重庆市沙坪坝区
     * 500107 重庆市九龙坡区
     * 500108 重庆市南岸区
     * 500109 重庆市北碚区
     * 500110 重庆市万盛区
     * 500111 重庆市双桥区
     * 500112 重庆市渝北区
     * 500113 重庆市巴南区
     * 500200 重庆市县
     * 500221 重庆市长寿县
     * 500222 重庆市綦江县
     * 500223 重庆市潼南县
     * 500224 重庆市铜梁县
     * 500225 重庆市大足县
     * 500226 重庆市荣昌县
     * 500227 重庆市璧山县
     * 500228 重庆市梁平县
     * 500229 重庆市城口县
     * 500230 重庆市丰都县
     * 500231 重庆市垫江县
     * 500232 重庆市武隆县
     * 500233 重庆市忠县
     * 500234 重庆市开县
     * 500235 重庆市云阳县
     * 500236 重庆市奉节县
     * 500237 重庆市巫山县
     * 500238 重庆市巫溪县
     * 500239 重庆市黔江土家族苗族自治县
     * 500240 重庆市石柱土家族自治县
     * 500241 重庆市秀山土家族苗族自治县
     * 500242 重庆市酉阳土家族苗族自治县
     * 500243 重庆市彭水苗族土家族自治县
     * 500300 重庆市(市)
     * 500381 重庆市江津市
     * 500382 重庆市合川市
     * 500383 重庆市永川市
     * 500384 重庆市南川市
     * 510000 四川省
     * 510100 四川省成都市
     * 510101 四川省成都市市辖区
     * 510104 四川省成都市锦江区
     * 510105 四川省成都市青羊区
     * 510106 四川省成都市金牛区
     * 510107 四川省成都市武侯区
     * 510108 四川省成都市成华区
     * 510112 四川省成都市龙泉驿区
     * 510113 四川省成都市青白江区
     * 510121 四川省成都市金堂县
     * 510122 四川省成都市双流县
     * 510123 四川省成都市温江县
     * 510124 四川省成都市郫县
     * 510125 四川省成都市新都县
     * 510129 四川省成都市大邑县
     * 510131 四川省成都市蒲江县
     * 510132 四川省成都市新津县
     * 510181 四川省成都市都江堰市
     * 510182 四川省成都市彭州市
     * 510183 四川省成都市邛崃市
     * 510184 四川省成都市崇州市
     * 510300 四川省自贡市
     * 510301 四川省自贡市市辖区
     * 510302 四川省自贡市自流井区
     * 510303 四川省自贡市贡井区
     * 510304 四川省自贡市大安区
     * 510311 四川省自贡市沿滩区
     * 510321 四川省自贡市荣县
     * 510322 四川省自贡市富顺县
     * 510400 四川省攀枝花市
     * 510401 四川省攀枝花市市辖区
     * 510402 四川省攀枝花市东区
     * 510403 四川省攀枝花市西区
     * 510411 四川省攀枝花市仁和区
     * 510421 四川省攀枝花市米易县
     * 510422 四川省攀枝花市盐边县
     * 510500 四川省泸州市
     * 510501 四川省泸州市市辖区
     * 510502 四川省泸州市江阳区
     * 510503 四川省泸州市纳溪区
     * 510504 四川省泸州市龙马潭区
     * 510521 四川省泸州市泸县
     * 510522 四川省泸州市合江县
     * 510524 四川省泸州市叙永县
     * 510525 四川省泸州市古蔺县
     * 510600 四川省德阳市
     * 510601 四川省德阳市市辖区
     * 510603 四川省德阳市旌阳区
     * 510623 四川省德阳市中江县
     * 510626 四川省德阳市罗江县
     * 510681 四川省德阳市广汉市
     * 510682 四川省德阳市什邡市
     * 510683 四川省德阳市绵竹市
     * 510700 四川省绵阳市
     * 510701 四川省绵阳市市辖区
     * 510703 四川省绵阳市涪城区
     * 510704 四川省绵阳市游仙区
     * 510722 四川省绵阳市三台县
     * 510723 四川省绵阳市盐亭县
     * 510724 四川省绵阳市安县
     * 510725 四川省绵阳市梓潼县
     * 510726 四川省绵阳市北川县
     * 510727 四川省绵阳市平武县
     * 510781 四川省绵阳市江油市
     * 510800 四川省广元市
     * 510801 四川省广元市市辖区
     * 510802 四川省广元市市中区
     * 510811 四川省广元市元坝区
     * 510812 四川省广元市朝天区
     * 510821 四川省广元市旺苍县
     * 510822 四川省广元市青川县
     * 510823 四川省广元市剑阁县
     * 510824 四川省广元市苍溪县
     * 510900 四川省遂宁市
     * 510901 四川省遂宁市市辖区
     * 510902 四川省遂宁市市中区
     * 510921 四川省遂宁市蓬溪县
     * 510922 四川省遂宁市射洪县
     * 510923 四川省遂宁市大英县
     * 511000 四川省内江市
     * 511001 四川省内江市市辖区
     * 511002 四川省内江市市中区
     * 511011 四川省内江市东兴区
     * 511024 四川省内江市威远县
     * 511025 四川省内江市资中县
     * 511028 四川省内江市隆昌县
     * 511100 四川省乐山市
     * 511101 四川省乐山市市辖区
     * 511102 四川省乐山市市中区
     * 511111 四川省乐山市沙湾区
     * 511112 四川省乐山市五通桥区
     * 511113 四川省乐山市金口河区
     * 511123 四川省乐山市犍为县
     * 511124 四川省乐山市井研县
     * 511126 四川省乐山市夹江县
     * 511129 四川省乐山市沐川县
     * 511132 四川省乐山市峨边彝族自治县
     * 511133 四川省乐山市马边彝族自治县
     * 511181 四川省乐山市峨眉山市
     * 511300 四川省南充市
     * 511301 四川省南充市市辖区
     * 511302 四川省南充市顺庆区
     * 511303 四川省南充市高坪区
     * 511304 四川省南充市嘉陵区
     * 511321 四川省南充市南部县
     * 511322 四川省南充市营山县
     * 511323 四川省南充市蓬安县
     * 511324 四川省南充市仪陇县
     * 511325 四川省南充市西充县
     * 511381 四川省南充市阆中市
     * 511500 四川省宜宾市
     * 511501 四川省宜宾市市辖区
     * 511502 四川省宜宾市翠屏区
     * 511521 四川省宜宾市宜宾县
     * 511522 四川省宜宾市南溪县
     * 511523 四川省宜宾市江安县
     * 511524 四川省宜宾市长宁县
     * 511525 四川省宜宾市高县
     * 511526 四川省宜宾市珙县
     * 511527 四川省宜宾市筠连县
     * 511528 四川省宜宾市兴文县
     * 511529 四川省宜宾市屏山县
     * 511600 四川省广安市
     * 511601 四川省广安市市辖区
     * 511602 四川省广安市广安区
     * 511621 四川省广安市岳池县
     * 511622 四川省广安市武胜县
     * 511623 四川省广安市邻水县
     * 511681 四川省广安市华蓥市
     * 513000 四川省达川地区
     * 513001 四川省达川地区达川市
     * 513002 四川省达川地区万源市
     * 513021 四川省达川地区达县
     * 513022 四川省达川地区宣汉县
     * 513023 四川省达川地区开江县
     * 513029 四川省达川地区大竹县
     * 513030 四川省达川地区渠县
     * 513100 四川省雅安地区
     * 513101 四川省雅安地区雅安市
     * 513122 四川省雅安地区名山县
     * 513123 四川省雅安地区荥经县
     * 513124 四川省雅安地区汉源县
     * 513125 四川省雅安地区石棉县
     * 513126 四川省雅安地区天全县
     * 513127 四川省雅安地区芦山县
     * 513128 四川省雅安地区宝兴县
     * 513200 四川省阿坝藏族羌族自治州
     * 513221 四川省阿坝藏族羌族自治州汶川县
     * 513222 四川省阿坝藏族羌族自治州理县
     * 513223 四川省阿坝藏族羌族自治州茂县
     * 513224 四川省阿坝藏族羌族自治州松潘县
     * 513225 四川省阿坝藏族羌族自治州九寨沟县
     * 513226 四川省阿坝藏族羌族自治州金川县
     * 513227 四川省阿坝藏族羌族自治州小金县
     * 513228 四川省阿坝藏族羌族自治州黑水县
     * 513229 四川省阿坝藏族羌族自治州马尔康县
     * 513230 四川省阿坝藏族羌族自治州壤塘县
     * 513231 四川省阿坝藏族羌族自治州阿坝县
     * 513232 四川省阿坝藏族羌族自治州若尔盖县
     * 513233 四川省阿坝藏族羌族自治州红原县
     * 513300 四川省甘孜藏族自治州
     * 513321 四川省甘孜藏族自治州康定县
     * 513322 四川省甘孜藏族自治州泸定县
     * 513323 四川省甘孜藏族自治州丹巴县
     * 513324 四川省甘孜藏族自治州九龙县
     * 513325 四川省甘孜藏族自治州雅江县
     * 513326 四川省甘孜藏族自治州道孚县
     * 513327 四川省甘孜藏族自治州炉霍县
     * 513328 四川省甘孜藏族自治州甘孜县
     * 513329 四川省甘孜藏族自治州新龙县
     * 513330 四川省甘孜藏族自治州德格县
     * 513331 四川省甘孜藏族自治州白玉县
     * 513332 四川省甘孜藏族自治州石渠县
     * 513333 四川省甘孜藏族自治州色达县
     * 513334 四川省甘孜藏族自治州理塘县
     * 513335 四川省甘孜藏族自治州巴塘县
     * 513336 四川省甘孜藏族自治州乡城县
     * 513337 四川省甘孜藏族自治州稻城县
     * 513338 四川省甘孜藏族自治州得荣县
     * 513400 四川省凉山彝族自治州
     * 513401 四川省凉山彝族自治州西昌市
     * 513422 四川省凉山彝族自治州木里藏族自治县
     * 513423 四川省凉山彝族自治州盐源县
     * 513424 四川省凉山彝族自治州德昌县
     * 513425 四川省凉山彝族自治州会理县
     * 513426 四川省凉山彝族自治州会东县
     * 513427 四川省凉山彝族自治州宁南县
     * 513428 四川省凉山彝族自治州普格县
     * 513429 四川省凉山彝族自治州布拖县
     * 513430 四川省凉山彝族自治州金阳县
     * 513431 四川省凉山彝族自治州昭觉县
     * 513432 四川省凉山彝族自治州喜德县
     * 513433 四川省凉山彝族自治州冕宁县
     * 513434 四川省凉山彝族自治州越西县
     * 513435 四川省凉山彝族自治州甘洛县
     * 513436 四川省凉山彝族自治州美姑县
     * 513437 四川省凉山彝族自治州雷波县
     * 513700 四川省巴中地区
     * 513701 四川省巴中地区巴中市
     * 513721 四川省巴中地区通江县
     * 513722 四川省巴中地区南江县
     * 513723 四川省巴中地区平昌县
     * 513800 四川省眉山地区
     * 513821 四川省眉山地区眉山县
     * 513822 四川省眉山地区仁寿县
     * 513823 四川省眉山地区彭山县
     * 513824 四川省眉山地区洪雅县
     * 513825 四川省眉山地区丹棱县
     * 513826 四川省眉山地区青神县
     * 513900 四川省眉山地区资阳地区
     * 513901 四川省眉山地区资阳市
     * 513902 四川省眉山地区简阳市
     * 513921 四川省眉山地区安岳县
     * 513922 四川省眉山地区乐至县
     * 520000 贵州省
     * 520100 贵州省贵阳市
     * 520101 贵州省贵阳市市辖区
     * 520102 贵州省贵阳市南明区
     * 520103 贵州省贵阳市云岩区
     * 520111 贵州省贵阳市花溪区
     * 520112 贵州省贵阳市乌当区
     * 520113 贵州省贵阳市白云区
     * 520121 贵州省贵阳市开阳县
     * 520122 贵州省贵阳市息烽县
     * 520123 贵州省贵阳市修文县
     * 520181 贵州省贵阳市清镇市
     * 520200 贵州省六盘水市
     * 520201 贵州省六盘水市钟山区
     * 520202 贵州省六盘水市盘县特区
     * 520203 贵州省六盘水市六枝特区
     * 520221 贵州省六盘水市水城县
     * 520300 贵州省遵义市
     * 520301 贵州省遵义市市辖区
     * 520302 贵州省遵义市红花岗区
     * 520321 贵州省遵义市遵义县
     * 520322 贵州省遵义市桐梓县
     * 520323 贵州省遵义市绥阳县
     * 520324 贵州省遵义市正安县
     * 520325 贵州省遵义市道真仡佬族苗族自治县
     * 520326 贵州省遵义市务川仡佬族苗族自治县
     * 520327 贵州省遵义市凤冈县
     * 520328 贵州省遵义市湄潭县
     * 520329 贵州省遵义市余庆县
     * 520330 贵州省遵义市习水县
     * 520381 贵州省遵义市赤水市
     * 520382 贵州省遵义市仁怀市
     * 522200 贵州省铜仁地区
     * 522201 贵州省铜仁地区铜仁市
     * 522222 贵州省铜仁地区江口县
     * 522223 贵州省铜仁地区玉屏侗族自治县
     * 522224 贵州省铜仁地区石阡县
     * 522225 贵州省铜仁地区思南县
     * 522226 贵州省铜仁地区印江土家族苗族自治县
     * 522227 贵州省铜仁地区德江县
     * 522228 贵州省铜仁地区沿河土家族自治县
     * 522229 贵州省铜仁地区松桃苗族自治县
     * 522230 贵州省铜仁地区万山特区
     * 522300 贵州省黔西南布依族苗族自治州
     * 522301 贵州省黔西南布依族苗族自治州兴义市
     * 522322 贵州省黔西南布依族苗族自治州兴仁县
     * 522323 贵州省黔西南布依族苗族自治州普安县
     * 522324 贵州省黔西南布依族苗族自治州晴隆县
     * 522325 贵州省黔西南布依族苗族自治州贞丰县
     * 522326 贵州省黔西南布依族苗族自治州望谟县
     * 522327 贵州省黔西南布依族苗族自治州册亨县
     * 522328 贵州省黔西南布依族苗族自治州安龙县
     * 522400 贵州省毕节地区
     * 522401 贵州省毕节地区毕节市
     * 522422 贵州省毕节地区大方县
     * 522423 贵州省毕节地区黔西县
     * 522424 贵州省毕节地区金沙县
     * 522425 贵州省毕节地区织金县
     * 522426 贵州省毕节地区纳雍县
     * 522427 贵州省毕节地区威宁彝族回族苗族自治县
     * 522428 贵州省毕节地区赫章县
     * 522500 贵州省安顺地区
     * 522501 贵州省安顺地区安顺市
     * 522526 贵州省安顺地区平坝县
     * 522527 贵州省安顺地区普定县
     * 522528 贵州省安顺地区关岭布依族苗族自治县
     * 522529 贵州省安顺地区镇宁布依族苗族自治县
     * 522530 贵州省安顺地区紫云苗族布依族自治县
     * 522600 贵州省黔东南苗族侗族自治州
     * 522601 贵州省黔东南苗族侗族自治州凯里市
     * 522622 贵州省黔东南苗族侗族自治州黄平县
     * 522623 贵州省黔东南苗族侗族自治州施秉县
     * 522624 贵州省黔东南苗族侗族自治州三穗县
     * 522625 贵州省黔东南苗族侗族自治州镇远县
     * 522626 贵州省黔东南苗族侗族自治州岑巩县
     * 522627 贵州省黔东南苗族侗族自治州天柱县
     * 522628 贵州省黔东南苗族侗族自治州锦屏县
     * 522629 贵州省黔东南苗族侗族自治州剑河县
     * 522630 贵州省黔东南苗族侗族自治州台江县
     * 522631 贵州省黔东南苗族侗族自治州黎平县
     * 522632 贵州省黔东南苗族侗族自治州榕江县
     * 522633 贵州省黔东南苗族侗族自治州从江县
     * 522634 贵州省黔东南苗族侗族自治州雷山县
     * 522635 贵州省黔东南苗族侗族自治州麻江县
     * 522636 贵州省黔东南苗族侗族自治州丹寨县
     * 522700 贵州省黔南布依族苗族自治州
     * 522701 贵州省黔南布依族苗族自治州都匀市
     * 522702 贵州省黔南布依族苗族自治州福泉市
     * 522722 贵州省黔南布依族苗族自治州荔波县
     * 522723 贵州省黔南布依族苗族自治州贵定县
     * 522725 贵州省黔南布依族苗族自治州瓮安县
     * 522726 贵州省黔南布依族苗族自治州独山县
     * 522727 贵州省黔南布依族苗族自治州平塘县
     * 522728 贵州省黔南布依族苗族自治州罗甸县
     * 522729 贵州省黔南布依族苗族自治州长顺县
     * 522730 贵州省黔南布依族苗族自治州龙里县
     * 522731 贵州省黔南布依族苗族自治州惠水县
     * 522732 贵州省黔南布依族苗族自治州三都水族自治县
     * 530000 云南省
     * 530100 云南省昆明市
     * 530101 云南省昆明市市辖区
     * 530102 云南省昆明市五华区
     * 530103 云南省昆明市盘龙区
     * 530111 云南省昆明市官渡区
     * 530112 云南省昆明市西山区
     * 530113 云南省昆明市东川区
     * 530121 云南省昆明市呈贡县
     * 530122 云南省昆明市晋宁县
     * 530124 云南省昆明市富民县
     * 530125 云南省昆明市宜良县
     * 530126 云南省昆明市石林彝族自治县
     * 530127 云南省昆明市嵩明县
     * 530128 云南省昆明市禄劝彝族苗族自治县
     * 530129 云南省昆明市寻甸回族彝族自治县
     * 530181 云南省昆明市安宁市
     * 530300 云南省曲靖市
     * 530301 云南省曲靖市市辖区
     * 530302 云南省曲靖市麒麟区
     * 530321 云南省曲靖市马龙县
     * 530322 云南省曲靖市陆良县
     * 530323 云南省曲靖市师宗县
     * 530324 云南省曲靖市罗平县
     * 530325 云南省曲靖市富源县
     * 530326 云南省曲靖市会泽县
     * 530328 云南省曲靖市沾益县
     * 530381 云南省曲靖市宣威市
     * 530400 云南省玉溪市
     * 530401 云南省玉溪市市辖区
     * 530402 云南省玉溪市红塔区
     * 530421 云南省玉溪市江川县
     * 530422 云南省玉溪市澄江县
     * 530423 云南省玉溪市通海县
     * 530424 云南省玉溪市华宁县
     * 530425 云南省玉溪市易门县
     * 530426 云南省玉溪市峨山彝族自治县
     * 530427 云南省玉溪市新平彝族傣族自治县
     * 530428 云南省玉溪市元江哈尼族彝族傣族自治县
     * 532100 云南省昭通地区
     * 532101 云南省昭通地区昭通市
     * 532122 云南省昭通地区鲁甸县
     * 532123 云南省昭通地区巧家县
     * 532124 云南省昭通地区盐津县
     * 532125 云南省昭通地区大关县
     * 532126 云南省昭通地区永善县
     * 532127 云南省昭通地区绥江县
     * 532128 云南省昭通地区镇雄县
     * 532129 云南省昭通地区彝良县
     * 532130 云南省昭通地区威信县
     * 532131 云南省昭通地区水富县
     * 532300 云南省楚雄彝族自治州
     * 532301 云南省楚雄彝族自治州楚雄市
     * 532322 云南省楚雄彝族自治州双柏县
     * 532323 云南省楚雄彝族自治州牟定县
     * 532324 云南省楚雄彝族自治州南华县
     * 532325 云南省楚雄彝族自治州姚安县
     * 532326 云南省楚雄彝族自治州大姚县
     * 532327 云南省楚雄彝族自治州永仁县
     * 532328 云南省楚雄彝族自治州元谋县
     * 532329 云南省楚雄彝族自治州武定县
     * 532331 云南省楚雄彝族自治州禄丰县
     * 532500 云南省红河哈尼族彝族自治州
     * 532501 云南省红河哈尼族彝族自治州个旧市
     * 532502 云南省红河哈尼族彝族自治州开远市
     * 532522 云南省红河哈尼族彝族自治州蒙自县
     * 532523 云南省红河哈尼族彝族自治州屏边苗族自治县
     * 532524 云南省红河哈尼族彝族自治州建水县
     * 532525 云南省红河哈尼族彝族自治州石屏县
     * 532526 云南省红河哈尼族彝族自治州弥勒县
     * 532527 云南省红河哈尼族彝族自治州泸西县
     * 532528 云南省红河哈尼族彝族自治州元阳县
     * 532529 云南省红河哈尼族彝族自治州红河县
     * 532530 云南省红河哈尼族彝族自治州金平苗族瑶族傣族自治县
     * 532531 云南省红河哈尼族彝族自治州绿春县
     * 532532 云南省红河哈尼族彝族自治州河口瑶族自治县
     * 532600 云南省文山壮族苗族自治州
     * 532621 云南省文山壮族苗族自治州文山县
     * 532622 云南省文山壮族苗族自治州砚山县
     * 532623 云南省文山壮族苗族自治州西畴县
     * 532624 云南省文山壮族苗族自治州麻栗坡县
     * 532625 云南省文山壮族苗族自治州马关县
     * 532626 云南省文山壮族苗族自治州丘北县
     * 532627 云南省文山壮族苗族自治州广南县
     * 532628 云南省文山壮族苗族自治州富宁县
     * 532700 云南省思茅地区
     * 532701 云南省思茅地区思茅市
     * 532722 云南省思茅地区普洱哈尼族彝族自治县
     * 532723 云南省思茅地区墨江哈尼族自治县
     * 532724 云南省思茅地区景东彝族自治县
     * 532725 云南省思茅地区景谷傣族彝族自治县
     * 532726 云南省思茅地区镇沅彝族哈尼族拉祜族自治县
     * 532727 云南省思茅地区江城哈尼族彝族自治县
     * 532728 云南省思茅地区孟连傣族拉祜族佤族自治县
     * 532729 云南省思茅地区澜沧拉祜族自治县
     * 532730 云南省思茅地区西盟佤族自治县
     * 532800 云南省西双版纳傣族自治州
     * 532801 云南省西双版纳傣族自治州景洪市
     * 532822 云南省西双版纳傣族自治州勐海县
     * 532823 云南省西双版纳傣族自治州勐腊县
     * 532900 云南省大理白族自治州
     * 532901 云南省大理白族自治州大理市
     * 532922 云南省大理白族自治州漾濞彝族自治县
     * 532923 云南省大理白族自治州祥云县
     * 532924 云南省大理白族自治州宾川县
     * 532925 云南省大理白族自治州弥渡县
     * 532926 云南省大理白族自治州南涧彝族自治县
     * 532927 云南省大理白族自治州巍山彝族回族自治县
     * 532928 云南省大理白族自治州永平县
     * 532929 云南省大理白族自治州云龙县
     * 532930 云南省大理白族自治州洱源县
     * 532931 云南省大理白族自治州剑川县
     * 532932 云南省大理白族自治州鹤庆县
     * 533000 云南省保山地区
     * 533001 云南省保山地区保山市
     * 533022 云南省保山地区施甸县
     * 533023 云南省保山地区腾冲县
     * 533024 云南省保山地区龙陵县
     * 533025 云南省保山地区昌宁县
     * 533100 云南省德宏傣族景颇族自治州
     * 533101 云南省德宏傣族景颇族自治州畹町市
     * 533102 云南省德宏傣族景颇族自治州瑞丽市
     * 533103 云南省德宏傣族景颇族自治州潞西市
     * 533122 云南省德宏傣族景颇族自治州梁河县
     * 533123 云南省德宏傣族景颇族自治州盈江县
     * 533124 云南省德宏傣族景颇族自治州陇川县
     * 533200 云南省丽江地区
     * 533221 云南省丽江地区丽江纳西族自治县
     * 533222 云南省丽江地区永胜县
     * 533223 云南省丽江地区华坪县
     * 533224 云南省丽江地区宁蒗彝族自治县
     * 533300 云南省怒江傈僳族自治州
     * 533321 云南省怒江傈僳族自治州泸水县
     * 533323 云南省怒江傈僳族自治州福贡县
     * 533324 云南省怒江傈僳族自治州贡山独龙族怒族自治县
     * 533325 云南省怒江傈僳族自治州兰坪白族普米族自治县
     * 533400 云南省迪庆藏族自治州
     * 533421 云南省迪庆藏族自治州中甸县
     * 533422 云南省迪庆藏族自治州德钦县
     * 533423 云南省迪庆藏族自治州维西傈僳族自治县
     * 533500 云南省临沧地区
     * 533521 云南省临沧地区临沧县
     * 533522 云南省临沧地区凤庆县
     * 533523 云南省临沧地区云县
     * 533524 云南省临沧地区永德县
     * 533525 云南省临沧地区镇康县
     * 533526 云南省临沧地区双江拉祜族佤族布朗族傣族自治县
     * 533527 云南省临沧地区耿马傣族佤族自治县
     * 533528 云南省临沧地区沧源佤族自治县
     * 540000 西藏自治区
     * 540100 西藏自治区拉萨市
     * 540101 西藏自治区拉萨市市辖区
     * 540102 西藏自治区拉萨市城关区
     * 540121 西藏自治区拉萨市林周县
     * 540122 西藏自治区拉萨市当雄县
     * 540123 西藏自治区拉萨市尼木县
     * 540124 西藏自治区拉萨市曲水县
     * 540125 西藏自治区拉萨市堆龙德庆县
     * 540126 西藏自治区拉萨市达孜县
     * 540127 西藏自治区拉萨市墨竹工卡县
     * 542100 西藏自治区昌都地区
     * 542121 西藏自治区昌都地区昌都县
     * 542122 西藏自治区昌都地区江达县
     * 542123 西藏自治区昌都地区贡觉县
     * 542124 西藏自治区昌都地区类乌齐县
     * 542125 西藏自治区昌都地区丁青县
     * 542126 西藏自治区昌都地区察雅县
     * 542127 西藏自治区昌都地区八宿县
     * 542128 西藏自治区昌都地区左贡县
     * 542129 西藏自治区昌都地区芒康县
     * 542132 西藏自治区昌都地区洛隆县
     * 542133 西藏自治区昌都地区边坝县
     * 542134 西藏自治区昌都地区盐井县
     * 542135 西藏自治区昌都地区碧土县
     * 542136 西藏自治区昌都地区妥坝县
     * 542137 西藏自治区昌都地区生达县
     * 542200 西藏自治区山南地区
     * 542221 西藏自治区山南地区乃东县
     * 542222 西藏自治区山南地区扎囊县
     * 542223 西藏自治区山南地区贡嘎县
     * 542224 西藏自治区山南地区桑日县
     * 542225 西藏自治区山南地区琼结县
     * 542226 西藏自治区山南地区曲松县
     * 542227 西藏自治区山南地区措美县
     * 542228 西藏自治区山南地区洛扎县
     * 542229 西藏自治区山南地区加查县
     * 542231 西藏自治区山南地区隆子县
     * 542232 西藏自治区山南地区错那县
     * 542233 西藏自治区山南地区浪卡子县
     * 542300 西藏自治区日喀则地区
     * 542301 西藏自治区日喀则地区日喀则市
     * 542322 西藏自治区日喀则地区南木林县
     * 542323 西藏自治区日喀则地区江孜县
     * 542324 西藏自治区日喀则地区定日县
     * 542325 西藏自治区日喀则地区萨迦县
     * 542326 西藏自治区日喀则地区拉孜县
     * 542327 西藏自治区日喀则地区昂仁县
     * 542328 西藏自治区日喀则地区谢通门县
     * 542329 西藏自治区日喀则地区白朗县
     * 542330 西藏自治区日喀则地区仁布县
     * 542331 西藏自治区日喀则地区康马县
     * 542332 西藏自治区日喀则地区定结县
     * 542333 西藏自治区日喀则地区仲巴县
     * 542334 西藏自治区日喀则地区亚东县
     * 542335 西藏自治区日喀则地区吉隆县
     * 542336 西藏自治区日喀则地区聂拉木县
     * 542337 西藏自治区日喀则地区萨嘎县
     * 542338 西藏自治区日喀则地区岗巴县
     * 542400 西藏自治区那曲地区
     * 542421 西藏自治区那曲地区那曲县
     * 542422 西藏自治区那曲地区嘉黎县
     * 542423 西藏自治区那曲地区比如县
     * 542424 西藏自治区那曲地区聂荣县
     * 542425 西藏自治区那曲地区安多县
     * 542426 西藏自治区那曲地区申扎县
     * 542427 西藏自治区那曲地区索县
     * 542428 西藏自治区那曲地区班戈县
     * 542429 西藏自治区那曲地区巴青县
     * 542430 西藏自治区那曲地区尼玛县
     * 542500 西藏自治区阿里地区
     * 542521 西藏自治区阿里地区普兰县
     * 542522 西藏自治区阿里地区札达县
     * 542523 西藏自治区阿里地区噶尔县
     * 542524 西藏自治区阿里地区日土县
     * 542525 西藏自治区阿里地区革吉县
     * 542526 西藏自治区阿里地区改则县
     * 542527 西藏自治区阿里地区措勤县
     * 542528 西藏自治区阿里地区隆格尔县
     * 542600 西藏自治区林芝地区
     * 542621 西藏自治区林芝地区林芝县
     * 542622 西藏自治区林芝地区工布江达县
     * 542623 西藏自治区林芝地区米林县
     * 542624 西藏自治区林芝地区墨脱县
     * 542625 西藏自治区林芝地区波密县
     * 542626 西藏自治区林芝地区察隅县
     * 542627 西藏自治区林芝地区朗县
     * 610000 陕西省
     * 610100 陕西省西安市
     * 610101 陕西省西安市市辖区
     * 610102 陕西省西安市新城区
     * 610103 陕西省西安市碑林区
     * 610104 陕西省西安市莲湖区
     * 610111 陕西省西安市灞桥区
     * 610112 陕西省西安市未央区
     * 610113 陕西省西安市雁塔区
     * 610114 陕西省西安市阎良区
     * 610115 陕西省西安市临潼区
     * 610121 陕西省西安市长安县
     * 610122 陕西省西安市蓝田县
     * 610124 陕西省西安市周至县
     * 610125 陕西省西安市户县
     * 610126 陕西省西安市高陵县
     * 610200 陕西省铜川市
     * 610201 陕西省铜川市市辖区
     * 610202 陕西省铜川市城区
     * 610203 陕西省铜川市郊区
     * 610221 陕西省铜川市耀县
     * 610222 陕西省铜川市宜君县
     * 610300 陕西省宝鸡市
     * 610301 陕西省宝鸡市市辖区
     * 610302 陕西省宝鸡市渭滨区
     * 610303 陕西省宝鸡市金台区
     * 610321 陕西省宝鸡市宝鸡县
     * 610322 陕西省宝鸡市凤翔县
     * 610323 陕西省宝鸡市岐山县
     * 610324 陕西省宝鸡市扶风县
     * 610326 陕西省宝鸡市眉县
     * 610327 陕西省宝鸡市陇县
     * 610328 陕西省宝鸡市千阳县
     * 610329 陕西省宝鸡市麟游县
     * 610330 陕西省宝鸡市凤县
     * 610331 陕西省宝鸡市太白县
     * 610400 陕西省咸阳市
     * 610401 陕西省咸阳市市辖区
     * 610402 陕西省咸阳市秦都区
     * 610403 陕西省咸阳市杨陵区
     * 610404 陕西省咸阳市渭城区
     * 610422 陕西省咸阳市三原县
     * 610423 陕西省咸阳市泾阳县
     * 610424 陕西省咸阳市乾县
     * 610425 陕西省咸阳市礼泉县
     * 610426 陕西省咸阳市永寿县
     * 610427 陕西省咸阳市彬县
     * 610428 陕西省咸阳市长武县
     * 610429 陕西省咸阳市旬邑县
     * 610430 陕西省咸阳市淳化县
     * 610431 陕西省咸阳市武功县
     * 610481 陕西省咸阳市兴平市
     * 610500 陕西省渭南市
     * 610501 陕西省渭南市市辖区
     * 610502 陕西省渭南市临渭区
     * 610521 陕西省渭南市华县
     * 610522 陕西省渭南市潼关县
     * 610523 陕西省渭南市大荔县
     * 610524 陕西省渭南市合阳县
     * 610525 陕西省渭南市澄城县
     * 610526 陕西省渭南市蒲城县
     * 610527 陕西省渭南市白水县
     * 610528 陕西省渭南市富平县
     * 610581 陕西省渭南市韩城市
     * 610582 陕西省渭南市华阴市
     * 610600 陕西省延安市
     * 610601 陕西省延安市市辖区
     * 610602 陕西省延安市宝塔区
     * 610621 陕西省延安市延长县
     * 610622 陕西省延安市延川县
     * 610623 陕西省延安市子长县
     * 610624 陕西省延安市安塞县
     * 610625 陕西省延安市志丹县
     * 610626 陕西省延安市吴旗县
     * 610627 陕西省延安市甘泉县
     * 610628 陕西省延安市富县
     * 610629 陕西省延安市洛川县
     * 610630 陕西省延安市宜川县
     * 610631 陕西省延安市黄龙县
     * 610632 陕西省延安市黄陵县
     * 610700 陕西省汉中市
     * 610701 陕西省汉中市市辖区
     * 610702 陕西省汉中市汉台区
     * 610721 陕西省汉中市南郑县
     * 610722 陕西省汉中市城固县
     * 610723 陕西省汉中市洋县
     * 610724 陕西省汉中市西乡县
     * 610725 陕西省汉中市勉县
     * 610726 陕西省汉中市宁强县
     * 610727 陕西省汉中市略阳县
     * 610728 陕西省汉中市镇巴县
     * 610729 陕西省汉中市留坝县
     * 610730 陕西省汉中市佛坪县
     * 612400 陕西省安康地区
     * 612401 陕西省安康地区安康市
     * 612422 陕西省安康地区汉阴县
     * 612423 陕西省安康地区石泉县
     * 612424 陕西省安康地区宁陕县
     * 612425 陕西省安康地区紫阳县
     * 612426 陕西省安康地区岚皋县
     * 612427 陕西省安康地区平利县
     * 612428 陕西省安康地区镇坪县
     * 612429 陕西省安康地区旬阳县
     * 612430 陕西省安康地区白河县
     * 612500 陕西省商洛地区
     * 612501 陕西省商洛地区商州市
     * 612522 陕西省商洛地区洛南县
     * 612523 陕西省商洛地区丹凤县
     * 612524 陕西省商洛地区商南县
     * 612525 陕西省商洛地区山阳县
     * 612526 陕西省商洛地区镇安县
     * 612527 陕西省商洛地区柞水县
     * 612700 陕西省榆林地区
     * 612701 陕西省榆林地区榆林市
     * 612722 陕西省榆林地区神木县
     * 612723 陕西省榆林地区府谷县
     * 612724 陕西省榆林地区横山县
     * 612725 陕西省榆林地区靖边县
     * 612726 陕西省榆林地区定边县
     * 612727 陕西省榆林地区绥德县
     * 612728 陕西省榆林地区米脂县
     * 612729 陕西省榆林地区佳县
     * 612730 陕西省榆林地区吴堡县
     * 612731 陕西省榆林地区清涧县
     * 612732 陕西省榆林地区子洲县
     * 620000 甘肃省
     * 620100 甘肃省兰州市
     * 620101 甘肃省兰州市市辖区
     * 620102 甘肃省兰州市城关区
     * 620103 甘肃省兰州市七里河区
     * 620104 甘肃省兰州市西固区
     * 620105 甘肃省兰州市安宁区
     * 620111 甘肃省兰州市红古区
     * 620121 甘肃省兰州市永登县
     * 620122 甘肃省兰州市皋兰县
     * 620123 甘肃省兰州市榆中县
     * 620200 甘肃省嘉峪关市
     * 620201 甘肃省嘉峪关市市辖区
     * 620300 甘肃省嘉峪关市金昌市
     * 620301 甘肃省嘉峪关市市辖区
     * 620302 甘肃省嘉峪关市金川区
     * 620321 甘肃省嘉峪关市永昌县
     * 620400 甘肃省白银市
     * 620401 甘肃省白银市市辖区
     * 620402 甘肃省白银市白银区
     * 620403 甘肃省白银市平川区
     * 620421 甘肃省白银市靖远县
     * 620422 甘肃省白银市会宁县
     * 620423 甘肃省白银市景泰县
     * 620500 甘肃省天水市
     * 620501 甘肃省天水市市辖区
     * 620502 甘肃省天水市秦城区
     * 620503 甘肃省天水市北道区
     * 620521 甘肃省天水市清水县
     * 620522 甘肃省天水市秦安县
     * 620523 甘肃省天水市甘谷县
     * 620524 甘肃省天水市武山县
     * 620525 甘肃省天水市张家川回族自治县
     * 622100 甘肃省酒泉地区
     * 622101 甘肃省酒泉地区玉门市
     * 622102 甘肃省酒泉地区酒泉市
     * 622103 甘肃省酒泉地区敦煌市
     * 622123 甘肃省酒泉地区金塔县
     * 622124 甘肃省酒泉地区肃北蒙古族自治县
     * 622125 甘肃省酒泉地区阿克塞哈萨克族自治县
     * 622126 甘肃省酒泉地区安西县
     * 622200 甘肃省张掖地区
     * 622201 甘肃省张掖地区张掖市
     * 622222 甘肃省张掖地区肃南裕固族自治县
     * 622223 甘肃省张掖地区民乐县
     * 622224 甘肃省张掖地区临泽县
     * 622225 甘肃省张掖地区高台县
     * 622226 甘肃省张掖地区山丹县
     * 622300 甘肃省武威地区
     * 622301 甘肃省武威地区武威市
     * 622322 甘肃省武威地区民勤县
     * 622323 甘肃省武威地区古浪县
     * 622326 甘肃省武威地区天祝藏族自治县
     * 622400 甘肃省定西地区
     * 622421 甘肃省定西地区定西县
     * 622424 甘肃省定西地区通渭县
     * 622425 甘肃省定西地区陇西县
     * 622426 甘肃省定西地区渭源县
     * 622427 甘肃省定西地区临洮县
     * 622428 甘肃省定西地区漳县
     * 622429 甘肃省定西地区岷县
     * 622600 甘肃省陇南地区
     * 622621 甘肃省陇南地区武都县
     * 622623 甘肃省陇南地区宕昌县
     * 622624 甘肃省陇南地区成县
     * 622625 甘肃省陇南地区康县
     * 622626 甘肃省陇南地区文县
     * 622627 甘肃省陇南地区西和县
     * 622628 甘肃省陇南地区礼县
     * 622629 甘肃省陇南地区两当县
     * 622630 甘肃省陇南地区徽县
     * 622700 甘肃省平凉地区
     * 622701 甘肃省平凉地区平凉市
     * 622722 甘肃省平凉地区泾川县
     * 622723 甘肃省平凉地区灵台县
     * 622724 甘肃省平凉地区崇信县
     * 622725 甘肃省平凉地区华亭县
     * 622726 甘肃省平凉地区庄浪县
     * 622727 甘肃省平凉地区静宁县
     * 622800 甘肃省庆阳地区
     * 622801 甘肃省庆阳地区西峰市
     * 622821 甘肃省庆阳地区庆阳县
     * 622822 甘肃省庆阳地区环县
     * 622823 甘肃省庆阳地区华池县
     * 622824 甘肃省庆阳地区合水县
     * 622825 甘肃省庆阳地区正宁县
     * 622826 甘肃省庆阳地区宁县
     * 622827 甘肃省庆阳地区镇原县
     * 622900 甘肃省临夏回族自治州
     * 622901 甘肃省临夏回族自治州临夏市
     * 622921 甘肃省临夏回族自治州临夏县
     * 622922 甘肃省临夏回族自治州康乐县
     * 622923 甘肃省临夏回族自治州永靖县
     * 622924 甘肃省临夏回族自治州广河县
     * 622925 甘肃省临夏回族自治州和政县
     * 622926 甘肃省临夏回族自治州东乡族自治县
     * 622927 甘肃省临夏回族自治州积石山保安族东乡族撒拉族自治县
     * 623000 甘肃省甘南藏族自治州
     * 623001 甘肃省甘南藏族自治州合作市
     * 623021 甘肃省甘南藏族自治州临潭县
     * 623022 甘肃省甘南藏族自治州卓尼县
     * 623023 甘肃省甘南藏族自治州舟曲县
     * 623024 甘肃省甘南藏族自治州迭部县
     * 623025 甘肃省甘南藏族自治州玛曲县
     * 623026 甘肃省甘南藏族自治州碌曲县
     * 623027 甘肃省甘南藏族自治州夏河县
     * 630000 青海省
     * 630100 青海省西宁市
     * 630101 青海省西宁市市辖区
     * 630102 青海省西宁市城东区
     * 630103 青海省西宁市城中区
     * 630104 青海省西宁市城西区
     * 630105 青海省西宁市城北区
     * 630121 青海省西宁市大通回族土族自治县
     * 632100 青海省海东地区
     * 632121 青海省海东地区平安县
     * 632122 青海省海东地区民和回族土族自治县
     * 632123 青海省海东地区乐都县
     * 632124 青海省海东地区湟中县
     * 632125 青海省海东地区湟源县
     * 632126 青海省海东地区互助土族自治县
     * 632127 青海省海东地区化隆回族自治县
     * 632128 青海省海东地区循化撒拉族自治县
     * 632200 青海省海北藏族自治州
     * 632221 青海省海北藏族自治州门源回族自治县
     * 632222 青海省海北藏族自治州祁连县
     * 632223 青海省海北藏族自治州海晏县
     * 632224 青海省海北藏族自治州刚察县
     * 632300 青海省黄南藏族自治州
     * 632321 青海省黄南藏族自治州同仁县
     * 632322 青海省黄南藏族自治州尖扎县
     * 632323 青海省黄南藏族自治州泽库县
     * 632324 青海省黄南藏族自治州河南蒙古族自治县
     * 632500 青海省海南藏族自治州
     * 632521 青海省海南藏族自治州共和县
     * 632522 青海省海南藏族自治州同德县
     * 632523 青海省海南藏族自治州贵德县
     * 632524 青海省海南藏族自治州兴海县
     * 632525 青海省海南藏族自治州贵南县
     * 632600 青海省果洛藏族自治州
     * 632621 青海省果洛藏族自治州玛沁县
     * 632622 青海省果洛藏族自治州班玛县
     * 632623 青海省果洛藏族自治州甘德县
     * 632624 青海省果洛藏族自治州达日县
     * 632625 青海省果洛藏族自治州久治县
     * 632626 青海省果洛藏族自治州玛多县
     * 632700 青海省玉树藏族自治州
     * 632721 青海省玉树藏族自治州玉树县
     * 632722 青海省玉树藏族自治州杂多县
     * 632723 青海省玉树藏族自治州称多县
     * 632724 青海省玉树藏族自治州治多县
     * 632725 青海省玉树藏族自治州囊谦县
     * 632726 青海省玉树藏族自治州曲麻莱县
     * 632800 青海省海西蒙古族藏族自治州
     * 632801 青海省海西蒙古族藏族自治州格尔木市
     * 632802 青海省海西蒙古族藏族自治州德令哈市
     * 632821 青海省海西蒙古族藏族自治州乌兰县
     * 632822 青海省海西蒙古族藏族自治州都兰县
     * 632823 青海省海西蒙古族藏族自治州天峻县
     * 640000 宁夏回族自治区
     * 640100 宁夏回族自治区银川市
     * 640101 宁夏回族自治区银川市市辖区
     * 640102 宁夏回族自治区银川市城区
     * 640103 宁夏回族自治区银川市新城区
     * 640111 宁夏回族自治区银川市郊区
     * 640121 宁夏回族自治区银川市永宁县
     * 640122 宁夏回族自治区银川市贺兰县
     * 640200 宁夏回族自治区石嘴山市
     * 640201 宁夏回族自治区石嘴山市市辖区
     * 640202 宁夏回族自治区石嘴山市大武口区
     * 640203 宁夏回族自治区石嘴山市石嘴山区
     * 640204 宁夏回族自治区石嘴山市石炭井区
     * 640221 宁夏回族自治区石嘴山市平罗县
     * 640222 宁夏回族自治区石嘴山市陶乐县
     * 640223 宁夏回族自治区石嘴山市惠农县
     * 640300 宁夏回族自治区吴忠市
     * 640301 宁夏回族自治区吴忠市市辖区
     * 640302 宁夏回族自治区吴忠市利通区
     * 640321 宁夏回族自治区吴忠市中卫县
     * 640322 宁夏回族自治区吴忠市中宁县
     * 640323 宁夏回族自治区吴忠市盐池县
     * 640324 宁夏回族自治区吴忠市同心县
     * 640381 宁夏回族自治区吴忠市青铜峡市
     * 640382 宁夏回族自治区吴忠市灵武市
     * 642200 宁夏回族自治区固原地区
     * 642221 宁夏回族自治区固原地区固原县
     * 642222 宁夏回族自治区固原地区海原县
     * 642223 宁夏回族自治区固原地区西吉县
     * 642224 宁夏回族自治区固原地区隆德县
     * 642225 宁夏回族自治区固原地区泾源县
     * 642226 宁夏回族自治区固原地区彭阳县
     * 650000 新疆维吾尔自治区
     * 650100 新疆维吾尔族自治区乌鲁木齐市
     * 650101 新疆维吾尔族自治区乌鲁木齐市市辖区
     * 650102 新疆维吾尔族自治区乌鲁木齐市天山区
     * 650103 新疆维吾尔族自治区乌鲁木齐市沙依巴克区
     * 650104 新疆维吾尔族自治区乌鲁木齐市新市区
     * 650105 新疆维吾尔族自治区乌鲁木齐市水磨沟区
     * 650106 新疆维吾尔族自治区乌鲁木齐市头屯河区
     * 650107 新疆维吾尔族自治区乌鲁木齐市南山矿区
     * 650108 新疆维吾尔族自治区乌鲁木齐市东山区
     * 650121 新疆维吾尔族自治区乌鲁木齐市乌鲁木齐县
     * 650200 新疆维吾尔族自治区克拉玛依市
     * 650201 新疆维吾尔族自治区克拉玛依市市辖区
     * 650202 新疆维吾尔族自治区克拉玛依市独山子区
     * 650203 新疆维吾尔族自治区克拉玛依市克拉玛依区
     * 650204 新疆维吾尔族自治区克拉玛依市白碱滩区
     * 650205 新疆维吾尔族自治区克拉玛依市乌尔禾区
     * 652100 新疆维吾尔族自治区吐鲁番地区
     * 652101 新疆维吾尔族自治区吐鲁番地区吐鲁番市
     * 652122 新疆维吾尔族自治区吐鲁番地区鄯善县
     * 652123 新疆维吾尔族自治区吐鲁番地区托克逊县
     * 652200 新疆维吾尔族自治区哈密地区
     * 652201 新疆维吾尔族自治区哈密地区哈密市
     * 652222 新疆维吾尔族自治区哈密地区巴里坤哈萨克自治县
     * 652223 新疆维吾尔族自治区哈密地区伊吾县
     * 652300 新疆维吾尔族自治区昌吉回族自治州
     * 652301 新疆维吾尔族自治区昌吉回族自治州昌吉市
     * 652302 新疆维吾尔族自治区昌吉回族自治州阜康市
     * 652303 新疆维吾尔族自治区昌吉回族自治州米泉市
     * 652323 新疆维吾尔族自治区昌吉回族自治州呼图壁县
     * 652324 新疆维吾尔族自治区昌吉回族自治州玛纳斯县
     * 652325 新疆维吾尔族自治区昌吉回族自治州奇台县
     * 652327 新疆维吾尔族自治区昌吉回族自治州吉木萨尔县
     * 652328 新疆维吾尔族自治区昌吉回族自治州木垒哈萨克自治县
     * 652700 新疆维吾尔族自治区博尔塔拉蒙古自治州
     * 652701 新疆维吾尔族自治区博尔塔拉蒙古自治州博乐市
     * 652722 新疆维吾尔族自治区博尔塔拉蒙古自治州精河县
     * 652723 新疆维吾尔族自治区博尔塔拉蒙古自治州温泉县
     * 652800 新疆维吾尔族自治区巴音郭楞蒙古自治州
     * 652801 新疆维吾尔族自治区巴音郭楞蒙古自治州库尔勒市
     * 652822 新疆维吾尔族自治区巴音郭楞蒙古自治州轮台县
     * 652823 新疆维吾尔族自治区巴音郭楞蒙古自治州尉犁县
     * 652824 新疆维吾尔族自治区巴音郭楞蒙古自治州若羌县
     * 652825 新疆维吾尔族自治区巴音郭楞蒙古自治州且末县
     * 652826 新疆维吾尔族自治区巴音郭楞蒙古自治州焉耆回族自治县
     * 652827 新疆维吾尔族自治区巴音郭楞蒙古自治州和静县
     * 652828 新疆维吾尔族自治区巴音郭楞蒙古自治州和硕县
     * 652829 新疆维吾尔族自治区巴音郭楞蒙古自治州博湖县
     * 652900 新疆维吾尔族自治区阿克苏地区
     * 652901 新疆维吾尔族自治区阿克苏地区阿克苏市
     * 652922 新疆维吾尔族自治区阿克苏地区温宿县
     * 652923 新疆维吾尔族自治区阿克苏地区库车县
     * 652924 新疆维吾尔族自治区阿克苏地区沙雅县
     * 652925 新疆维吾尔族自治区阿克苏地区新和县
     * 652926 新疆维吾尔族自治区阿克苏地区拜城县
     * 652927 新疆维吾尔族自治区阿克苏地区乌什县
     * 652928 新疆维吾尔族自治区阿克苏地区阿瓦提县
     * 652929 新疆维吾尔族自治区阿克苏地区柯坪县
     * 653000 新疆维吾尔族自治区克孜勒苏柯尔克孜自治州
     * 653001 新疆维吾尔族自治区克孜勒苏柯尔克孜自治州阿图什市
     * 653022 新疆维吾尔族自治区克孜勒苏柯尔克孜自治州阿克陶县
     * 653023 新疆维吾尔族自治区克孜勒苏柯尔克孜自治州阿合奇县
     * 653024 新疆维吾尔族自治区克孜勒苏柯尔克孜自治州乌恰县
     * 653100 新疆维吾尔族自治区喀什地区
     * 653101 新疆维吾尔族自治区喀什地区喀什市
     * 653121 新疆维吾尔族自治区喀什地区疏附县
     * 653122 新疆维吾尔族自治区喀什地区疏勒县
     * 653123 新疆维吾尔族自治区喀什地区英吉沙县
     * 653124 新疆维吾尔族自治区喀什地区泽普县
     * 653125 新疆维吾尔族自治区喀什地区莎车县
     * 653126 新疆维吾尔族自治区喀什地区叶城县
     * 653127 新疆维吾尔族自治区喀什地区麦盖提县
     * 653128 新疆维吾尔族自治区喀什地区岳普湖县
     * 653129 新疆维吾尔族自治区喀什地区伽师县
     * 653130 新疆维吾尔族自治区喀什地区巴楚县
     * 653131 新疆维吾尔族自治区喀什地区塔什库尔干塔吉克自治县
     * 653200 新疆维吾尔族自治区和田地区
     * 653201 新疆维吾尔族自治区和田地区和田市
     * 653221 新疆维吾尔族自治区和田地区和田县
     * 653222 新疆维吾尔族自治区和田地区墨玉县
     * 653223 新疆维吾尔族自治区和田地区皮山县
     * 653224 新疆维吾尔族自治区和田地区洛浦县
     * 653225 新疆维吾尔族自治区和田地区策勒县
     * 653226 新疆维吾尔族自治区和田地区于田县
     * 653227 新疆维吾尔族自治区和田地区民丰县
     * 654000 新疆维吾尔族自治区伊犁哈萨克自治州
     * 654001 新疆维吾尔族自治区伊犁哈萨克自治州奎屯市
     * 654100 新疆维吾尔族自治区伊犁哈萨克自治州伊犁地区
     * 654101 新疆维吾尔族自治区伊犁哈萨克自治州伊宁市
     * 654121 新疆维吾尔族自治区伊犁哈萨克自治州伊宁县
     * 654122 新疆自治区伊犁哈萨克自治州察布查尔锡伯自治县
     * 654123 新疆维吾尔族自治区伊犁哈萨克自治州霍城县
     * 654124 新疆维吾尔族自治区伊犁哈萨克自治州巩留县
     * 654125 新疆维吾尔族自治区伊犁哈萨克自治州新源县
     * 654126 新疆维吾尔族自治区伊犁哈萨克自治州昭苏县
     * 654127 新疆维吾尔族自治区伊犁哈萨克自治州特克斯县
     * 654128 新疆维吾尔族自治区伊犁哈萨克自治州尼勒克县
     * 654200 新疆维吾尔族自治区塔城地区
     * 654201 新疆维吾尔族自治区塔城地区塔城市
     * 654202 新疆维吾尔族自治区塔城地区乌苏市
     * 654221 新疆维吾尔族自治区塔城地区额敏县
     * 654223 新疆维吾尔族自治区塔城地区沙湾县
     * 654224 新疆维吾尔族自治区塔城地区托里县
     * 654225 新疆维吾尔族自治区塔城地区裕民县
     * 654226 新疆维吾尔族自治区塔城地区和布克赛尔蒙古自治县
     * 654300 新疆维吾尔族自治区阿勒泰地区
     * 654301 新疆维吾尔族自治区阿勒泰地区阿勒泰市
     * 654321 新疆维吾尔族自治区阿勒泰地区布尔津县
     * 654322 新疆维吾尔族自治区阿勒泰地区富蕴县
     * 654323 新疆维吾尔族自治区阿勒泰地区福海县
     * 654324 新疆维吾尔族自治区阿勒泰地区哈巴河县
     * 654325 新疆维吾尔族自治区阿勒泰地区青河县
     * 654326 新疆维吾尔族自治区阿勒泰地区吉木乃县
     * 659000 新疆维吾尔族自治区直辖县级行政单位
     * 659001 新疆维吾尔族自治区石河子市
     */
}

