package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接后管控制层
 */
@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDto> createShortLink(@RequestBody ShortLinkCreateReqDto requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 短链接分页查询
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDto>> pageShortLink(ShortLinkPageReqDto requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 短链接信息修改
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDto requestParam) {
        return shortLinkRemoteService.updateShortLink(requestParam);
    }

}
