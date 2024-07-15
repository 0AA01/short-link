package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkStatsReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接数据分析控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDto> shortLinkStats(ShortLinkStatsReqDto requestParam) {
        return shortLinkRemoteService.oneShortLinkStats(requestParam);
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDto> groupShortLinkStats(ShortLinkGroupStatsReqDto requestParam) {
        return shortLinkRemoteService.groupShortLinkStats(requestParam);
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDto>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDto requestParam) {
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordRespDto>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDto requestParam) {
        return shortLinkRemoteService.groupShortLinkStatsAccessRecord(requestParam);
    }
}
