package com.aa03.shortlink.project.dao.mapper;

import com.aa03.shortlink.project.dao.entity.LinkDeviceStatsDo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 访问设备统计持久层
 */
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDo> {

    /**
     * 记录基础访问监控数据
     */
    @Insert("INSERT INTO t_link_Device_stats (full_short_url, gid, date, cnt, device, create_time, update_time, del_flag) " +
            "VALUES( #{linkDeviceStats.fullShortUrl}, #{linkDeviceStats.gid}, #{linkDeviceStats.date}, #{linkDeviceStats.cnt}, #{linkDeviceStats.device}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkDeviceStats.cnt};")
    void shortLinkDeviceState(@Param("linkDeviceStats") LinkDeviceStatsDo linkDeviceStatsDO);
}
