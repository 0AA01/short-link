package com.aa03.shortlink.project.dto.req;

import lombok.Data;

@Data
public class ShortLinkGroupStatsReqDto {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
