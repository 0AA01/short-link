package com.aa03.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 短链接回收站分页查询请求参数
 */
@Data
public class ShortLinkRecycleBinPageReqDto extends Page {
    private List<String> gidList;
}
