package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDto> createShortLink(@RequestBody ShortLinkCreateReqDto requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 短链接分页查询
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageRespDto>> pageShortLink(ShortLinkPageReqDto requestParam) {
        return shortLinkActualRemoteService.pageShortLink(
                requestParam.getGid(),
                requestParam.getOrderTag(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }

    /**
     * 短链接信息修改
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDto requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

}
