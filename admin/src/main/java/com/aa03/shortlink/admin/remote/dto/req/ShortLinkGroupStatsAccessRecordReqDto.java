package com.aa03.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;


/**
 * 短链接分组监控数据请求参数
 */
@Data
public class ShortLinkGroupStatsAccessRecordReqDto extends Page {

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
