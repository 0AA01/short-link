package com.aa03.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 短链接分组修改请求参数
 */
@Data
public class ShortLinkGroupUpdateReqDto {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名
     */
    private String name;
}
