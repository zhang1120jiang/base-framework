/*
 * www.unisinsight.com Inc.
 * Copyright (c) 2018 All Rights Reserved
 */
package com.unisinsight.sprite.common.page;

import com.github.pagehelper.Page;
import lombok.Data;

/**
 * 分页数据
 *
 * @author t17153 [tan.gang@h3c.com]
 * @date 2018/9/7 14:55
 * @since 1.0
 */
@Data
public class PageResult<T> {

    private Pagination paging;

    private T data;

    public static <T> PageResult<T> of(T list, Page page) {
        PageResult result = new PageResult();
        Pagination paging = new Pagination();
        paging.setPageNum(page.getPageNum());
        paging.setPageSize(page.getPageSize());
        paging.setTotal((int)page.getTotal());
        result.setPaging(paging);
        result.setData(list);
        return result;
    }
}
