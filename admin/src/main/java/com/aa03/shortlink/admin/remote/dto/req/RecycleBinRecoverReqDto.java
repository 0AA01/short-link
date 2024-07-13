package com.aa03.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 短链接回收站恢复请求参数
 */
@Data
public class RecycleBinRecoverReqDto {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}