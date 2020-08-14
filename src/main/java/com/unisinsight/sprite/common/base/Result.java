/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.base;

import lombok.Data;

/**
 * description 响应数据的封装
 *
 * @author qiuweiwu [qiu.weiwu@unisinsight.com]
 * @date 2018/11/30 10:18
 * @since 1.0
 */
@Data
public class Result<T> {

    private String code;

    private String message;

    private T data;
}
