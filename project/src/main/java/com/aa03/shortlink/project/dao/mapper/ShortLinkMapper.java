package com.aa03.shortlink.project.dao.mapper;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDto;
import org.apache.ibatis.annotations.Param;

/**
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDo> {

    /**
     * 短链接访问统计自增
     */
    void incrementStats(@Param("gid") String gid,
                        @Param("fullShortUrl") String fullShortUrl,
                        @Param("totalPv") Integer totalPv,
                        @Param("totalUv") Integer totalUv,
                        @Param("totalUip") Integer totalUip);

    /**
     * 分页统计短链接
     */
    IPage<ShortLinkDo> pageLink(ShortLinkPageReqDto requestParam);

    /**
     * 分页统计回收站短链接
     */
    IPage<ShortLinkDo> pageRecycleBinLink(ShortLinkRecycleBinPageReqDto requestParam);
}
