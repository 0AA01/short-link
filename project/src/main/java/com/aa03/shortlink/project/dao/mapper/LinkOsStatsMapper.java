package com.aa03.shortlink.project.dao.mapper;

import com.aa03.shortlink.project.dao.entity.LinkOsStatsDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 操作系统统计持久层
 */
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDo> {


    /**
     * 记录访问短链接操作系统数据
     */
    @Insert("INSERT INTO " +
            "t_link_os_stats (full_short_url, gid, date, cnt, os, create_time, update_time, del_flag) " +
            "VALUES( #{linkOsStats.fullShortUrl},  #{linkOsStats.gid}, #{linkOsStats.date}, #{linkOsStats.cnt}, #{linkOsStats.os}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkOsStats.cnt};")
    void shortLinkOsState(@Param("linkOsStats") LinkOsStatsDo linkOsStatsDo);

}
