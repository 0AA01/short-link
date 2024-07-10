package com.aa03.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组排序请求参数
 */
@Data
public class ShortLinkGroupSortReqDto {

    /**
     * 分组名
     */
    private String gid;

    /**
     * 排序字段
     */
    private Integer sortOrder;
}
