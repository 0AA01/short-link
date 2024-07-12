package com.aa03.shortlink.project.service;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dto.req.RecycleBinRecoverReqDto;
import com.aa03.shortlink.project.dto.req.RecycleBinSaveReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkPageRespDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 短链接回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDo> {

    /**
     * 回收站新增功能
     *
     * @param requestParam 回收站新增请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDto requestParam);

    /**
     * 回收站分页查询
     *
     * @param requestParam 回收站查询请求参数
     * @return 回收站分页返回结果
     */
    IPage<ShortLinkPageRespDto> pageShortLink(ShortLinkRecycleBinPageReqDto requestParam);

    /**
     * 回收站恢复
     *
     * @param requestParam 回收站恢复请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDto requestParam);
}
