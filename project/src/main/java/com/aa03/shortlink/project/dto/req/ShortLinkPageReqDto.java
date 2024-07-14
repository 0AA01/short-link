package com.aa03.shortlink.project.dto.req;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDto extends Page<ShortLinkDo> {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 排序标识
     */
    private String orderTag;
}
