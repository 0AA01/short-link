package com.aa03.shortlink.admin.controller;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接后管控制层
 */
@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {

    };

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDto> createShortLink(@RequestBody ShortLinkCreateReqDto requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 短链接分页查询
     *
     * @param requestParam 短链接分页查询请求参数
     * @return 短链接分页返回结果
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDto>> pageShortLink(ShortLinkPageReqDto requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }
}
