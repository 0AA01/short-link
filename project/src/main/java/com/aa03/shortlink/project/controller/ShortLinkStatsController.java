package com.aa03.shortlink.project.controller;

import com.aa03.shortlink.project.common.convention.result.Result;
import com.aa03.shortlink.project.common.convention.result.Results;
import com.aa03.shortlink.project.dto.req.ShortLinkGroupStatsReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkStatsReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsRespDto;
import com.aa03.shortlink.project.service.ShortLinkStatsService;
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

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDto> shortLinkStats(ShortLinkStatsReqDto requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsRespDto> groupShortLinkStats(ShortLinkGroupStatsReqDto requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStats(requestParam));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDto>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDto requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }


}
