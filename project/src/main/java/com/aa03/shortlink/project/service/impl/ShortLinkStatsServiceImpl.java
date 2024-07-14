package com.aa03.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.aa03.shortlink.project.dao.entity.*;
import com.aa03.shortlink.project.dto.req.ShortLinkStatsReqDto;
import com.aa03.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import com.aa03.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import com.aa03.shortlink.project.dao.mapper.LinkBrowserStatsMapper;
import com.aa03.shortlink.project.dao.mapper.LinkDeviceStatsMapper;
import com.aa03.shortlink.project.dao.mapper.LinkLocaleStatsMapper;
import com.aa03.shortlink.project.dao.mapper.LinkNetworkStatsMapper;
import com.aa03.shortlink.project.dao.mapper.LinkOsStatsMapper;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsAccessDailyRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsBrowserRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsDeviceRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsLocaleCNRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsNetworkRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsOsRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsTopIpRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkStatsUvRespDto;
import com.aa03.shortlink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    @Override
    public ShortLinkStatsRespDto oneShortLinkStats(ShortLinkStatsReqDto requestParam) {
        List<LinkAccessStatsDo> listStatsByShortLink = linkAccessStatsMapper.listStatsByShortLink(requestParam);
        if (CollUtil.isEmpty(listStatsByShortLink)) {
            return null;
        }
        // 基础访问数据
        LinkAccessStatsDo pvUvUidStatsByShortLink = linkAccessLogsMapper.findPvUvUidStatsByShortLink(requestParam);
        // 基础访问详情
        List<ShortLinkStatsAccessDailyRespDto> daily = new ArrayList<>();
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(requestParam.getStartDate()), DateUtil.parse(requestParam.getEndDate()), DateField.DAY_OF_MONTH).stream()
                .map(DateUtil::formatDate)
                .toList();
        rangeDates.forEach(each -> listStatsByShortLink.stream()
                .filter(item -> Objects.equals(each, DateUtil.formatDate(item.getDate())))
                .findFirst()
                .ifPresentOrElse(item -> {
                    ShortLinkStatsAccessDailyRespDto accessDailyRespDTO = ShortLinkStatsAccessDailyRespDto.builder()
                            .date(each)
                            .pv(item.getPv())
                            .uv(item.getUv())
                            .uip(item.getUip())
                            .build();
                    daily.add(accessDailyRespDTO);
                }, () -> {
                    ShortLinkStatsAccessDailyRespDto accessDailyRespDTO = ShortLinkStatsAccessDailyRespDto.builder()
                            .date(each)
                            .pv(0)
                            .uv(0)
                            .uip(0)
                            .build();
                    daily.add(accessDailyRespDTO);
                }));
        // 地区访问详情（仅国内）
        List<ShortLinkStatsLocaleCNRespDto> localeCnStats = new ArrayList<>();
        List<LinkLocaleStatsDo> listedLocaleByShortLink = linkLocaleStatsMapper.listLocaleByShortLink(requestParam);
        int localeCnSum = listedLocaleByShortLink.stream()
                .mapToInt(LinkLocaleStatsDo::getCnt)
                .sum();
        listedLocaleByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / localeCnSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsLocaleCNRespDto localeCNRespDTO = ShortLinkStatsLocaleCNRespDto.builder()
                    .cnt(each.getCnt())
                    .locale(each.getProvince())
                    .ratio(actualRatio)
                    .build();
            localeCnStats.add(localeCNRespDTO);
        });
        // 小时访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<LinkAccessStatsDo> listHourStatsByShortLink = linkAccessStatsMapper.listHourStatsByShortLink(requestParam);
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    .map(LinkAccessStatsDo::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 高频访问IP详情
        List<ShortLinkStatsTopIpRespDto> topIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink = linkAccessLogsMapper.listTopIpByShortLink(requestParam);
        listTopIpByShortLink.forEach(each -> {
            ShortLinkStatsTopIpRespDto statsTopIpRespDTO = ShortLinkStatsTopIpRespDto.builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            topIpStats.add(statsTopIpRespDTO);
        });
        // 一周访问详情
        List<Integer> weekdayStats = new ArrayList<>();
        List<LinkAccessStatsDo> listWeekdayStatsByShortLink = linkAccessStatsMapper.listWeekdayStatsByShortLink(requestParam);
        for (int i = 1; i < 8; i++) {
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    .map(LinkAccessStatsDo::getPv)
                    .orElse(0);
            weekdayStats.add(weekdayCnt);
        }
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDto> browserStats = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStatsByShortLink = linkBrowserStatsMapper.listBrowserStatsByShortLink(requestParam);
        int browserSum = listBrowserStatsByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStatsByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsBrowserRespDto browserRespDTO = ShortLinkStatsBrowserRespDto.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .browser(each.get("browser").toString())
                    .ratio(actualRatio)
                    .build();
            browserStats.add(browserRespDTO);
        });
        // 操作系统访问详情
        List<ShortLinkStatsOsRespDto> osStats = new ArrayList<>();
        List<HashMap<String, Object>> listOsStatsByShortLink = linkOsStatsMapper.listOsStatsByShortLink(requestParam);
        int osSum = listOsStatsByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listOsStatsByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsOsRespDto osRespDTO = ShortLinkStatsOsRespDto.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .os(each.get("os").toString())
                    .ratio(actualRatio)
                    .build();
            osStats.add(osRespDTO);
        });
        // 访客访问类型详情
        List<ShortLinkStatsUvRespDto> uvTypeStats = new ArrayList<>();
        HashMap<String, Object> findUvTypeByShortLink = linkAccessLogsMapper.findUvTypeCntByShortLink(requestParam);
        int oldUserCnt = Integer.parseInt(
                Optional.ofNullable(findUvTypeByShortLink)
                        .map(each -> each.get("oldUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int newUserCnt = Integer.parseInt(
                Optional.ofNullable(findUvTypeByShortLink)
                        .map(each -> each.get("newUserCnt"))
                        .map(Object::toString)
                        .orElse("0")
        );
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        ShortLinkStatsUvRespDto newUvRespDTO = ShortLinkStatsUvRespDto.builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        uvTypeStats.add(newUvRespDTO);
        ShortLinkStatsUvRespDto oldUvRespDTO = ShortLinkStatsUvRespDto.builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStats.add(oldUvRespDTO);
        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDto> deviceStats = new ArrayList<>();
        List<LinkDeviceStatsDo> listDeviceStatsByShortLink = linkDeviceStatsMapper.listDeviceStatsByShortLink(requestParam);
        int deviceSum = listDeviceStatsByShortLink.stream()
                .mapToInt(LinkDeviceStatsDo::getCnt)
                .sum();
        listDeviceStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsDeviceRespDto deviceRespDTO = ShortLinkStatsDeviceRespDto.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceStats.add(deviceRespDTO);
        });
        // 访问网络类型详情
        List<ShortLinkStatsNetworkRespDto> networkStats = new ArrayList<>();
        List<LinkNetworkStatsDo> listNetworkStatsByShortLink = linkNetworkStatsMapper.listNetworkStatsByShortLink(requestParam);
        int networkSum = listNetworkStatsByShortLink.stream()
                .mapToInt(LinkNetworkStatsDo::getCnt)
                .sum();
        listNetworkStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsNetworkRespDto networkRespDTO = ShortLinkStatsNetworkRespDto.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(networkRespDTO);
        });
        return ShortLinkStatsRespDto.builder()
                .pv(pvUvUidStatsByShortLink.getPv())
                .uv(pvUvUidStatsByShortLink.getUv())
                .uip(pvUvUidStatsByShortLink.getUip())
                .daily(daily)
                .localeCnStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekdayStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .uvTypeStats(uvTypeStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .build();
    }
//
//    @Override
//    public ShortLinkStatsRespDto groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
//        List<LinkAccessStatsDo> listStatsByGroup = linkAccessStatsMapper.listStatsByGroup(requestParam);
//        if (CollUtil.isEmpty(listStatsByGroup)) {
//            return null;
//        }
//        // 基础访问数据
//        LinkAccessStatsDo pvUvUidStatsByGroup = linkAccessLogsMapper.findPvUvUidStatsByGroup(requestParam);
//        // 基础访问详情
//        List<ShortLinkStatsAccessDailyRespDto> daily = new ArrayList<>();
//        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(requestParam.getStartDate()), DateUtil.parse(requestParam.getEndDate()), DateField.DAY_OF_MONTH).stream()
//                .map(DateUtil::formatDate)
//                .toList();
//        rangeDates.forEach(each -> listStatsByGroup.stream()
//                .filter(item -> Objects.equals(each, DateUtil.formatDate(item.getDate())))
//                .findFirst()
//                .ifPresentOrElse(item -> {
//                    ShortLinkStatsAccessDailyRespDto accessDailyRespDTO = ShortLinkStatsAccessDailyRespDto.builder()
//                            .date(each)
//                            .pv(item.getPv())
//                            .uv(item.getUv())
//                            .uip(item.getUip())
//                            .build();
//                    daily.add(accessDailyRespDTO);
//                }, () -> {
//                    ShortLinkStatsAccessDailyRespDto accessDailyRespDTO = ShortLinkStatsAccessDailyRespDto.builder()
//                            .date(each)
//                            .pv(0)
//                            .uv(0)
//                            .uip(0)
//                            .build();
//                    daily.add(accessDailyRespDTO);
//                }));
//        // 地区访问详情（仅国内）
//        List<ShortLinkStatsLocaleCNRespDto> localeCnStats = new ArrayList<>();
//        List<LinkLocaleStatsDo> listedLocaleByGroup = linkLocaleStatsMapper.listLocaleByGroup(requestParam);
//        int localeCnSum = listedLocaleByGroup.stream()
//                .mapToInt(LinkLocaleStatsDo::getCnt)
//                .sum();
//        listedLocaleByGroup.forEach(each -> {
//            double ratio = (double) each.getCnt() / localeCnSum;
//            double actualRatio = Math.round(ratio * 100.0) / 100.0;
//            ShortLinkStatsLocaleCNRespDto localeCNRespDTO = ShortLinkStatsLocaleCNRespDto.builder()
//                    .cnt(each.getCnt())
//                    .locale(each.getProvince())
//                    .ratio(actualRatio)
//                    .build();
//            localeCnStats.add(localeCNRespDTO);
//        });
//        // 小时访问详情
//        List<Integer> hourStats = new ArrayList<>();
//        List<LinkAccessStatsDo> listHourStatsByGroup = linkAccessStatsMapper.listHourStatsByGroup(requestParam);
//        for (int i = 0; i < 24; i++) {
//            AtomicInteger hour = new AtomicInteger(i);
//            int hourCnt = listHourStatsByGroup.stream()
//                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
//                    .findFirst()
//                    .map(LinkAccessStatsDo::getPv)
//                    .orElse(0);
//            hourStats.add(hourCnt);
//        }
//        // 高频访问IP详情
//        List<ShortLinkStatsTopIpRespDto> topIpStats = new ArrayList<>();
//        List<HashMap<String, Object>> listTopIpByGroup = linkAccessLogsMapper.listTopIpByGroup(requestParam);
//        listTopIpByGroup.forEach(each -> {
//            ShortLinkStatsTopIpRespDto statsTopIpRespDTO = ShortLinkStatsTopIpRespDto.builder()
//                    .ip(each.get("ip").toString())
//                    .cnt(Integer.parseInt(each.get("count").toString()))
//                    .build();
//            topIpStats.add(statsTopIpRespDTO);
//        });
//        // 一周访问详情
//        List<Integer> weekdayStats = new ArrayList<>();
//        List<LinkAccessStatsDo> listWeekdayStatsByGroup = linkAccessStatsMapper.listWeekdayStatsByGroup(requestParam);
//        for (int i = 1; i < 8; i++) {
//            AtomicInteger weekday = new AtomicInteger(i);
//            int weekdayCnt = listWeekdayStatsByGroup.stream()
//                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
//                    .findFirst()
//                    .map(LinkAccessStatsDo::getPv)
//                    .orElse(0);
//            weekdayStats.add(weekdayCnt);
//        }
//        // 浏览器访问详情
//        List<ShortLinkStatsBrowserRespDto> browserStats = new ArrayList<>();
//        List<HashMap<String, Object>> listBrowserStatsByGroup = linkBrowserStatsMapper.listBrowserStatsByGroup(requestParam);
//        int browserSum = listBrowserStatsByGroup.stream()
//                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
//                .sum();
//        listBrowserStatsByGroup.forEach(each -> {
//            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
//            double actualRatio = Math.round(ratio * 100.0) / 100.0;
//            ShortLinkStatsBrowserRespDto browserRespDTO = ShortLinkStatsBrowserRespDto.builder()
//                    .cnt(Integer.parseInt(each.get("count").toString()))
//                    .browser(each.get("browser").toString())
//                    .ratio(actualRatio)
//                    .build();
//            browserStats.add(browserRespDTO);
//        });
//        // 操作系统访问详情
//        List<ShortLinkStatsOsRespDto> osStats = new ArrayList<>();
//        List<HashMap<String, Object>> listOsStatsByGroup = linkOsStatsMapper.listOsStatsByGroup(requestParam);
//        int osSum = listOsStatsByGroup.stream()
//                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
//                .sum();
//        listOsStatsByGroup.forEach(each -> {
//            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
//            double actualRatio = Math.round(ratio * 100.0) / 100.0;
//            ShortLinkStatsOsRespDto osRespDTO = ShortLinkStatsOsRespDto.builder()
//                    .cnt(Integer.parseInt(each.get("count").toString()))
//                    .os(each.get("os").toString())
//                    .ratio(actualRatio)
//                    .build();
//            osStats.add(osRespDTO);
//        });
//        // 访问设备类型详情
//        List<ShortLinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
//        List<LinkDeviceStatsDO> listDeviceStatsByGroup = linkDeviceStatsMapper.listDeviceStatsByGroup(requestParam);
//        int deviceSum = listDeviceStatsByGroup.stream()
//                .mapToInt(LinkDeviceStatsDO::getCnt)
//                .sum();
//        listDeviceStatsByGroup.forEach(each -> {
//            double ratio = (double) each.getCnt() / deviceSum;
//            double actualRatio = Math.round(ratio * 100.0) / 100.0;
//            ShortLinkStatsDeviceRespDTO deviceRespDTO = ShortLinkStatsDeviceRespDTO.builder()
//                    .cnt(each.getCnt())
//                    .device(each.getDevice())
//                    .ratio(actualRatio)
//                    .build();
//            deviceStats.add(deviceRespDTO);
//        });
//        // 访问网络类型详情
//        List<ShortLinkStatsNetworkRespDto> networkStats = new ArrayList<>();
//        List<LinkAccessLogsDo> listNetworkStatsByGroup = linkNetworkStatsMapper.listNetworkStatsByGroup(requestParam);
//        int networkSum = listNetworkStatsByGroup.stream()
//                .mapToInt(LinkAccessLogsDo::getCnt)
//                .sum();
//        listNetworkStatsByGroup.forEach(each -> {
//            double ratio = (double) each.getCnt() / networkSum;
//            double actualRatio = Math.round(ratio * 100.0) / 100.0;
//            ShortLinkStatsNetworkRespDto networkRespDTO = ShortLinkStatsNetworkRespDto.builder()
//                    .cnt(each.getCnt())
//                    .network(each.getNetwork())
//                    .ratio(actualRatio)
//                    .build();
//            networkStats.add(networkRespDTO);
//        });
//        return ShortLinkStatsRespDto.builder()
//                .pv(pvUvUidStatsByGroup.getPv())
//                .uv(pvUvUidStatsByGroup.getUv())
//                .uip(pvUvUidStatsByGroup.getUip())
//                .daily(daily)
//                .localeCnStats(localeCnStats)
//                .hourStats(hourStats)
//                .topIpStats(topIpStats)
//                .weekdayStats(weekdayStats)
//                .browserStats(browserStats)
//                .osStats(osStats)
//                .deviceStats(deviceStats)
//                .networkStats(networkStats)
//                .build();
//    }
//
//    @Override
//    public IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
//        LambdaQueryWrapper<LinkAccessLogsDo> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDo.class)
//                .eq(LinkAccessLogsDo::getGid, requestParam.getGid())
//                .eq(LinkAccessLogsDo::getFullShortUrl, requestParam.getFullShortUrl())
//                .between(LinkAccessLogsDo::getCreateTime, requestParam.getStartDate(), requestParam.getEndDate())
//                .eq(LinkAccessLogsDo::getDelFlag, 0)
//                .orderByDesc(LinkAccessLogsDo::getCreateTime);
//        IPage<LinkAccessLogsDo> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);
//        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
//        List<String> userAccessLogsList = actualResult.getRecords().stream()
//                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
//                .toList();
//        List<Map<String, Object>> uvTypeList = linkAccessLogsMapper.selectUvTypeByUsers(
//                requestParam.getGid(),
//                requestParam.getFullShortUrl(),
//                requestParam.getStartDate(),
//                requestParam.getEndDate(),
//                userAccessLogsList
//        );
//        actualResult.getRecords().forEach(each -> {
//            String uvType = uvTypeList.stream()
//                    .filter(item -> Objects.equals(each.getUser(), item.get("user")))
//                    .findFirst()
//                    .map(item -> item.get("uvType"))
//                    .map(Object::toString)
//                    .orElse("旧访客");
//            each.setUvType(uvType);
//        });
//        return actualResult;
//    }
//
//    @Override
//    public IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
//        LambdaQueryWrapper<LinkAccessLogsDo> queryWrapper = Wrappers.lambdaQuery(LinkAccessLogsDo.class)
//                .eq(LinkAccessLogsDo::getGid, requestParam.getGid())
//                .between(LinkAccessLogsDo::getCreateTime, requestParam.getStartDate(), requestParam.getEndDate())
//                .eq(LinkAccessLogsDo::getDelFlag, 0)
//                .orderByDesc(LinkAccessLogsDo::getCreateTime);
//        IPage<LinkAccessLogsDo> linkAccessLogsDOIPage = linkAccessLogsMapper.selectPage(requestParam, queryWrapper);
//        IPage<ShortLinkStatsAccessRecordRespDTO> actualResult = linkAccessLogsDOIPage.convert(each -> BeanUtil.toBean(each, ShortLinkStatsAccessRecordRespDTO.class));
//        List<String> userAccessLogsList = actualResult.getRecords().stream()
//                .map(ShortLinkStatsAccessRecordRespDTO::getUser)
//                .toList();
//        List<Map<String, Object>> uvTypeList = linkAccessLogsMapper.selectGroupUvTypeByUsers(
//                requestParam.getGid(),
//                requestParam.getStartDate(),
//                requestParam.getEndDate(),
//                userAccessLogsList
//        );
//        actualResult.getRecords().forEach(each -> {
//            String uvType = uvTypeList.stream()
//                    .filter(item -> Objects.equals(each.getUser(), item.get("user")))
//                    .findFirst()
//                    .map(item -> item.get("uvType"))
//                    .map(Object::toString)
//                    .orElse("旧访客");
//            each.setUvType(uvType);
//        });
//        return actualResult;
//    }
}
