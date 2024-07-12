package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.RecycleBinSaveReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}
