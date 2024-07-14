package com.aa03.shortlink.project.dao.mapper;

import com.aa03.shortlink.project.dao.entity.LinkAccessStatsDo;
import com.aa03.shortlink.project.dto.req.ShortLinkStatsReqDto;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aa03.shortlink.project.dto.req.ShortLinkGroupStatsReqDto;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
    void shortLinkStats(@Param("linkAccessStatsDo") LinkAccessStatsDo linkAccessStatsDo);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, date;")
    List<LinkAccessStatsDo> listStatsByShortLink(@Param("param") ShortLinkStatsReqDto requestParam);

    /**
     * 根据分组获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, date;")
    List<LinkAccessStatsDo> listStatsByGroup(@Param("param") ShortLinkGroupStatsReqDto requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, hour;")
    List<LinkAccessStatsDo> listHourStatsByShortLink(@Param("param") ShortLinkStatsReqDto requestParam);

    /**
     * 根据分组获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, hour;")
    List<LinkAccessStatsDo> listHourStatsByGroup(@Param("param") ShortLinkGroupStatsReqDto requestParam);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, gid, weekday;")
    List<LinkAccessStatsDo> listWeekdayStatsByShortLink(@Param("param") ShortLinkStatsReqDto requestParam);

    /**
     * 根据分组获取指定日期内小时基础监控数据
     */
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, weekday;")
    List<LinkAccessStatsDo> listWeekdayStatsByGroup(@Param("param") ShortLinkGroupStatsReqDto requestParam);
}
