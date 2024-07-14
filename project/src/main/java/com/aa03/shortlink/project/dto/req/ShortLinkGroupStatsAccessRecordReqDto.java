package com.aa03.shortlink.project.dto.req;

import com.aa03.shortlink.project.dao.entity.LinkAccessLogsDo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 分组短链接监控访问记录请求参数
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：link）获取项目资料
 */
@Data
public class ShortLinkGroupStatsAccessRecordReqDto extends Page<LinkAccessLogsDo> {

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
