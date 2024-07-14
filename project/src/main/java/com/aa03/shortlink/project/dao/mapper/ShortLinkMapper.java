package com.aa03.shortlink.project.dao.mapper;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dto.req.ShortLinkPageReqDto;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接持久化层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDo> {

    /**
     * 短链接访问统计自增
     */
    @Update(
            "update t_link set " +
                    "total_pv = total_pv + #{totalPv}, " +
                    "total_uv = total_uv + #{totalUv}, " +
                    "total_uip = total_uip + #{totalUip} " +
                    "where " +
                    "gid = #{gid} and " +
                    "full_short_url = #{fullShortUrl};"
    )
    void incrementState(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip
    );

    /**
     * 短链接分页查询
     */
    IPage<ShortLinkDo> pageLink(ShortLinkPageReqDto requestParam);
}
