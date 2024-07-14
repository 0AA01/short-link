package com.aa03.shortlink.project.service;

import com.aa03.shortlink.project.dto.req.ShortLinkStatsReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsRespDto;

public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsRespDto oneShortLinkStats(ShortLinkStatsReqDto requestParam);
//
//    /**
//     * 获取分组短链接监控数据
//     *
//     * @param requestParam 获取分组短链接监控数据入参
//     * @return 分组短链接监控数据
//     */
//    ShortLinkStatsRespDto groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam);
//
//    /**
//     * 访问单个短链接指定时间内访问记录监控数据
//     *
//     * @param requestParam 获取短链接监控访问记录数据入参
//     * @return 访问记录监控数据
//     */
//    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);
//
//    /**
//     * 访问分组短链接指定时间内访问记录监控数据
//     *
//     * @param requestParam 获取分组短链接监控访问记录数据入参
//     * @return 分组访问记录监控数据
//     */
//    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}
