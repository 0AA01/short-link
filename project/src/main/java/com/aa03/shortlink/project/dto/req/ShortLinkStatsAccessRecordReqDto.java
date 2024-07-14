package com.aa03.shortlink.project.dto.req;

import com.aa03.shortlink.project.dao.entity.LinkAccessLogsDo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class ShortLinkStatsAccessRecordReqDto extends Page<LinkAccessLogsDo> {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

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
