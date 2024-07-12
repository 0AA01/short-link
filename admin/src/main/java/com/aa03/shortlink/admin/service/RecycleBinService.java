package com.aa03.shortlink.admin.service;

import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 短链接回收站接口层
 */
public interface RecycleBinService {
    Result<IPage<ShortLinkPageRespDto>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDto requestParam);
}
