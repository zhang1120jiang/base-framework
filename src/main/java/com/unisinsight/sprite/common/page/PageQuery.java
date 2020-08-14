/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.page;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求数据
 *
 * @author t17153 [tan.gang@h3c.com]
 * @date 2018/9/7 14:55
 * @since 1.0
 */
@Data
public class PageQuery implements Serializable {
    private static final long serialVersionUID = 6320982368059524189L;

    /**
     * 当前页
     */
    @JsonProperty("page_num")
    private int pageNum = 1;
    /**
     * 每页的数量
     */
    @JsonProperty("page_size")
    private int pageSize = 20;

    /**
     * 排序字段
     */
    @JsonProperty("order_field")
    @ApiModelProperty(value = "排序字段", hidden = true)
    private String orderField;

    /**
     * 排序规则
     */
    @JsonProperty("order_rule")
    @ApiModelProperty(value = "排序规则", hidden = true)
    private String orderRule ;

}
