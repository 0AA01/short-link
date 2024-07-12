package com.aa03.shortlink.project.controller;

import com.aa03.shortlink.project.common.convention.result.Result;
import com.aa03.shortlink.project.common.convention.result.Results;
import com.aa03.shortlink.project.dto.req.RecycleBinRecoverReqDto;
import com.aa03.shortlink.project.dto.req.RecycleBinSaveReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkPageRespDto;
import com.aa03.shortlink.project.service.RecycleBinService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 回收站新增功能
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDto requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 回收站分页查询
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDto>> pageShortLink(ShortLinkRecycleBinPageReqDto requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }

    /**
     * 回收站恢复功能
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverReqDto requestParam) {
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }
}
