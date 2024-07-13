package com.aa03.shortlink.project.dao.mapper;

import com.aa03.shortlink.project.dao.entity.LinkAccessStatsDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 基础访问监控持久层
 */
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDo> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("INSERT INTO t_link_access_stats (full_short_url,gid,date,pv,uv,uip,hour,weekday,create_time,update_time,del_flag)" +
            "VALUES(#{linkAccessStatsDo.fullShortUrl}, #{linkAccessStatsDo.gid}, #{linkAccessStatsDo.date}, #{linkAccessStatsDo.pv}, #{linkAccessStatsDo.uv}, #{linkAccessStatsDo.uip}, #{linkAccessStatsDo.hour}, #{linkAccessStatsDo.weekday}, NOW(), NOW(), 0) ON DUPLICATE KEY " +
            "UPDATE  pv = pv + #{linkAccessStatsDo.pv},  uv = uv + #{linkAccessStatsDo.uv},  uip = uip + #{linkAccessStatsDo.uip};")
    void shortLinkStats(@Param("linkAccessStatsDo") LinkAccessStatsDo linkAccessStatsDoDo);
}
