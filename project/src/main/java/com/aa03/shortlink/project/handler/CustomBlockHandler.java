package com.aa03.shortlink.project.handler;

import com.aa03.shortlink.project.common.convention.result.Result;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCreateRespDto;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 自定义流控规则
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDto> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDto requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDto>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}