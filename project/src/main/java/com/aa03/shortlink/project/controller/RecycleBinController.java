package com.aa03.shortlink.project.controller;

import com.aa03.shortlink.project.common.convention.result.Result;
import com.aa03.shortlink.project.common.convention.result.Results;
import com.aa03.shortlink.project.dto.req.RecycleBinSaveReqDto;
import com.aa03.shortlink.project.service.RecycleBinService;
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

    private final RecycleBinService recycleBinService;

    /**
     * 回收站新增功能
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDto requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

}
