package com.unisinsight.sprite.common.base;

import com.unisinsight.collect.universally.web.common.Constants;
import com.unisinsight.collect.universally.web.common.enums.ResultCode;
import com.unisinsight.ic.commons.constant.CommonContants;
import com.unisinsight.ic.commons.enums.BaseResultCode;

/**
 * 返回结果生成
 */
public class ResultGenerator {

    public static  Result genSuccessResult() {
        Result result = new Result();
        result.setCode(CommonContants.SYSTEM_ID + Constants.SERVICE_ID
                + BaseResultCode.BASE_SUCCESS.getCode());
        result.setMessage(BaseResultCode.BASE_SUCCESS.getMessage());
        return result;
    }

    public static <T> Result<T> genSuccessResult(T data) {
        Result<T> result = new Result<>();
        result.setCode(CommonContants.SYSTEM_ID + Constants.SERVICE_ID
                + BaseResultCode.BASE_SUCCESS.getCode());
        result.setMessage(BaseResultCode.BASE_SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static Result genResult(BaseResultCode resultCode) {
        Result result = new Result();
        result.setCode(CommonContants.SYSTEM_ID + Constants.SERVICE_ID
                + resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    public static <T> Result<T> genResult(BaseResultCode resultCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(CommonContants.SYSTEM_ID + Constants.SERVICE_ID
                + resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        result.setData(data);
        return result;
    }

    public static Result genResult(ResultCode resultCode) {
        Result result = new Result();
        result.setCode(CommonContants.SYSTEM_ID + Constants.SERVICE_ID
                + resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    public static <T> Result<T> genResult(ResultCode code, T data) {
        Result<T> result = new Result<>();
        result.setCode(CommonContants.SYSTEM_ID + Constants.SERVICE_ID
                + code.getCode());
        result.setMessage(code.getMessage());
        result.setData(data);
        return result;
    }

    private ResultGenerator(){

    }
}
