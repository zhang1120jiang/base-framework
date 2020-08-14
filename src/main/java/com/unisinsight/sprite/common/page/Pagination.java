/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.page;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 分页数据返回参数
 *
 * @author t17153 [tan.gang@h3c.com]
 * @date 2018/9/7 14:56
 * @since 1.0
 */
@Data
public class Pagination {

    /**
     * 总数量
     */
    private int total;

    /**
     * 页码，从1开始
     */
    @JsonProperty("page_num")
    @JSONField(name = "page_num")
    private int pageNum;

    /**
     * 页面大小
     */
    @JsonProperty("page_size")
    @JSONField(name = "page_size")
    private int pageSize;

}
