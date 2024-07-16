package com.aa03.shortlink.project.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量创建短链接单条记录参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortLinkBaseInfoRespDto {

    /**
     * 描述信息
     */
    private String describe;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 短链接
     */
    private String fullShortUrl;
}
