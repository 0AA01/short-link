package com.aa03.shortlink.project.dto.req;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 短链接回收站分页查询请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDto extends Page<ShortLinkDo> {
    private List<String> gidList;
}
