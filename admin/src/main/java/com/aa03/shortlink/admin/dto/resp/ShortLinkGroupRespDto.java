package com.aa03.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 短链接分组返回实体
 */
@Data
public class ShortLinkGroupRespDto {

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建用户
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
