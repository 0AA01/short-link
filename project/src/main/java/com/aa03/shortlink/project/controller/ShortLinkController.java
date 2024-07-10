package com.aa03.shortlink.project.controller;

import com.aa03.shortlink.project.common.convention.result.Result;
import com.aa03.shortlink.project.common.convention.result.Results;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接分组
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDto> createShortLink(@RequestBody ShortLinkCreateReqDto requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }
}
