/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.exception;

import com.unisinsight.sprite.common.constant.CommonContants;
import com.unisinsight.sprite.common.enums.BaseResultCode;

/**
 * description 统一异常处理
 *
 * @author liuran [KF.liuran@h3c.com]
 * @date 2018/9/6 17:12
 * @since 1.0
 */
public class CommonException extends RuntimeException {

    public CommonException(String message) {
        super(message);
    }

    private String errorCode;

    public static CommonException of(BaseResultCode resultCode) {
        CommonException exception = new CommonException(resultCode.getMessage());
        exception.errorCode = CommonContants.SYSTEM_ID + CommonContants.COMMON_SERVICE_ID
                + resultCode.getCode();

        return exception;
    }

    public static CommonException of(BaseResultCode resultCode, String message) {
        CommonException exception = new CommonException(message);
        exception.errorCode = CommonContants.SYSTEM_ID + CommonContants.COMMON_SERVICE_ID
                + resultCode.getCode();

        return exception;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
