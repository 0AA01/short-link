package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.RecycleBinSaveReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 回收站新增功能
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDto requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 回收站分页查询
     */
    @GetMapping("/api/short-link/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDto>> pageShortLink(ShortLinkPageReqDto requestParam) {
        return shortLinkRemoteService.pageRecycleShortLink(requestParam);
    }

}
