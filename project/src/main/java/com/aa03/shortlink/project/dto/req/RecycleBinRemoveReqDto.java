package com.aa03.shortlink.project.dto.req;

import lombok.Data;

/**
 * 短链接回收站删除请求参数
 */
@Data
public class RecycleBinRemoveReqDto {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
