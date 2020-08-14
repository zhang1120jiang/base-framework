package com.unisinsight.sprite.common.enums;

/**
 * 基本结果代码
 */
public enum BaseResultCode {

    BASE_SUCCESS("0000", "操作成功"),
    REGISTER_UNLOGIN("1000", "系统未登录"),
    REGISTER_UNKNOWUSER("1001","用户不存在"),
    REGISTER_LOGIN_ERROR("1002","登录信息有误"),
    REGISTER_UPDATEPWD_ERROR("1003","旧密码错误"),
    SYS_INTERNAL_ERROR("0001", "系统内部异常"),

    IP_FORBIDDEN_ERROR("0002", "被禁止的IP"),
    IP_LIMIT_ERROR("0003", "当前IP请求超过限制"),
    IP_WRONG_ERROR("0004", "错误的IP地址"),
    IP_EMPTY_ERROR("0005", "IP地址不能为空"),

    UNKNOWN_SOURCE_ERROR("0006", "未知的请求源"),
    REQUEST_LIMIT_ERROR("0007", "请求超过次数限制"),

    API_OFF_ERROR("0008", "接口停用"),
    API_MAINTENACE_ERROR("0009", "接口维护"),

    VERSION_ERROR("0010", "版本号错误"),
    DOMAIN_ERROR("0011", "域名错误"),
    NETWORK_ERROR("0012", "网络错误"),
    UNKNOWN_ERROR("0013", "未知错误"),
    INTERNAL_ERROR("0014", "内部错误"),
    REQUEST_TIMEOUT_ERROR("0015", "请求超时"),
    MEMORY_OVERFLOW_ERROR("0016", "内存溢出"),
    DATA_SOURCE_ERROR("0017", "数据源异常"),

    INVALID_USER_ERROR("0018", "用户不存在"),
    PERMISSION_DENY_ERROR("0019", "用户没有权限"),
    REPETITIVE_SUBMISSION_ERROR("0020", "当前操作频繁"),
    DATA_EMPTY_ERROR("0021", "无数据"),
    RESOURCE_EXIST_ERROR("0022", "资源已存在"),
    RESULT_EMPTY_ERROR("0023", "查询无结果"),

    ID_EMPTY_ERROR("0024", "ID不能为空"),
    DATA_REFRESH_ERROR("0025", "数据更新中"),
    INVALID_PARAM_ERROR("0026", "参数校验错误"),
    INVALID_PHONE_ERROR("0027", "手机号码错误"),
    INVALID_IDENTITY_ERROR("0028", "身份证号码错误"),
    INVALID_EMAIL_ERROR("0029", "邮箱错误"),
    COORDINATE_ERROR("0030", "经纬度错误"),
    REQUEST_PATH_ERROR("0031", "请求路径错误"),
    DATA_FORMAT_ERROR("0032", "数据格式错误"),
    TIME_FORMAT_ERROR("0033", "时间参数错误"),

    LICENSE_NUMBER_ERROR("0034", "车牌号错误"),
    CAMERA_NUMBER_ERROR("0035", "摄像头编号错误"),
    CALLBACK_URL_ERROR("0036", "回调地址URL错误"),

    TIME_FORMAT_LIMIT_ERROR("0037", "时间格式必须是ISO-8601"),
    PAGE_INDEX_ERROR("0038", "页码超出限制"),
    PAGE_SIZE_ERROR("0039", "分页大小超出限制"),

    API_NOT_EXIST_ERROR("0040", "接口不存在"),
    DECODE_ERROR("0041", "解密失败"),
    LACK_NECCESSARY_PARAM_ERROR("0042", "缺少必要参数"),

    DOWNLOAD_ERROR("0043", "下载失败"),
    UNKNOWN_FILE_ERROR("0044", "未识别文件"),
    FILE_SIZE_ERROR("0045", "文件大小超过限制"),
    FILE_UPLOAD_ERROR("0046", "文件上传失败"),
    FILE_NOT_EXIST_ERROR("0047", "文件地址不存在"),
    AUTHEN_FALSE_ERROR("0048", "账号密码错误"),

    PARAM_SWITCH_ERROR("0049", "参数转换异常"),
    JSON_IO_ERROR("0050", "JSON转换异常"),

    SQL_EXCEPTION("0051", "SQL执行异常"),
    DATA_NOT_EXIST_ERROR("0052", "数据不存在"),
    DATA_NOT_UNIQUE_ERROR("0053", "返回结果不唯一"),
    PRIMARY_KEY_NOT_EMPTY_ERROR("0054", "主键不能为空"),
    HTTP_CONNECTION_ERROR("0055", "远程请求失败"),
    GRID_KIDS_NOT_EMPTY_ERROR("0056", "当前网格下楼栋信息不为空，无法删除"),
    BUILDING_KIDS_NOT_EMPTY_ERROR("0057", "当前楼栋下房屋信息不为空，无法删除"),
    BUILDING_DEAL_ERROR("0058", "执行错误"),
    FILE_RESOLVE_ERROR("0059", "文件读入异常"),
    INPUT_STREAM_ERROR("0060", "输入流异常"),
    DELETE_SUCESS("0061", "删除成功"),
    UPDATE_SUCESS("0062", "更新成功"),
    SOURCE_NOT_EXIST("0063", "资源不存在"),
    RESULT_NOT_UNIQUE("0064", "查询结果不唯一"),
    LIST_CONVERT_ERROR("0065","source不是list不能进行转换"),
    THIRDPARD_CALL_ERROR("0066", "调用三方接口失败"),
    FORCE_GROUP_KIDS_NOT_EMPTY("1001", "当前的力量分组下的实有力量不为空，无法删除"),
    IMPORT_ERROR("1002","导入失败"),
    VALUE_RANGE_ERROR("2001","参数值域异常"),
    PERSONNEL_TRACE_REPORT_ERROR("1013","工作人员轨迹上报失败"),
    REGISTER_GRID_NULL_ERROR("1010","用户无网格权限数据"),
    CONVERT_BEAN_ERROR("1011","实体类类型转换错误"),
    TAG_IS_EXISTS("1014","标签已存在"),
    FLOOR_KIDS_NOT_EMPTY_ERROR("0067", "当前楼层下房屋信息不为空，无法删除"),
    BUILD_AREADY_EXIST("0068", "楼栋code已存在"),
    USER_ERROR("3032", "用户信息无效"),
    PHONE_NUMBER_ERROR("1003", "电话号码有误"),
    ID_NUMBER_ERROR("1004", "身份证号码有误");


    private String code;
    private String message;

    BaseResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static BaseResultCode getMessageByCode(String code) {
        for (BaseResultCode resut : BaseResultCode.values()) {
            if (resut.code == code) {
                return resut;
            }
        }
        return null;
    }

}
