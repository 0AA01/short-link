package com.aa03.shortlink.project.service;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCreateRespDto;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDo> {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDto createShortLink(ShortLinkCreateReqDto requestParam);
}
