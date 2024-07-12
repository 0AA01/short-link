package com.aa03.shortlink.project.service;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dto.req.RecycleBinSaveReqDto;
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
}
