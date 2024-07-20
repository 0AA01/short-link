package com.aa03.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.aa03.shortlink.project.common.convention.exception.ClientException;
import com.aa03.shortlink.project.common.convention.exception.ServiceException;
import com.aa03.shortlink.project.common.enums.ValidDateTypeEnum;
import com.aa03.shortlink.project.config.GotoDomainWhiteListConfiguration;
import com.aa03.shortlink.project.dao.entity.*;
import com.aa03.shortlink.project.dao.mapper.*;
import com.aa03.shortlink.project.dto.biz.ShortLinkStatsRecordDto;
import com.aa03.shortlink.project.dto.req.ShortLinkBatchCreateReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkUpdateReqDto;
import com.aa03.shortlink.project.dto.resp.*;
import com.aa03.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.aa03.shortlink.project.mq.producer.ShortLinkStatsSaveProducer;
import com.aa03.shortlink.project.service.LinkStatsTodayService;
import com.aa03.shortlink.project.service.ShortLinkService;
import com.aa03.shortlink.project.toolkit.HashUtil;
import com.aa03.shortlink.project.toolkit.LinkUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.aa03.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.aa03.shortlink.project.common.enums.ShortLinkErrorCodeEnum.*;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDo> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final ShortLinkStatsSaveProducer shortLinkStatsSaveProducer;
    private final LinkStatsTodayService linkStatsTodayService;
    private final GotoDomainWhiteListConfiguration gotoDomainWhiteListConfiguration;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDto createShortLink(ShortLinkCreateReqDto requestParam) {
        verificationWhiteList(requestParam.getOriginUrl());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix).enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFaviconByUrl(requestParam.getOriginUrl()))
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .build();
        ShortLinkGotoDo linkGotoDo = ShortLinkGotoDo.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            baseMapper.insert(shortLinkDo);
            shortLinkGotoMapper.insert(linkGotoDo);
        } catch (DuplicateKeyException ex) {
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ServiceException(SHORT_LINK_GENERATE_REPEAT);
        }
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDto.builder()
                .fullShortUrl("http://" + shortLinkDo.getFullShortUrl())
                .originUrl(shortLinkDo.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    private void verificationWhiteList(String originUrl) {
        Boolean enable = gotoDomainWhiteListConfiguration.getEnable();
        if (enable == null || !enable) {
            return;
        }
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误");
        }
        List<String> details = gotoDomainWhiteListConfiguration.getDetails();
        if (!details.contains(domain)) {
            throw new ClientException("为避免恶意网站链接，请生成以下网站链接：" + gotoDomainWhiteListConfiguration.getNames());
        }
    }

    @Override
    public ShortLinkBatchCreateRespDto batchCreateShortLink(ShortLinkBatchCreateReqDto requestParam) {
        List<String> originList = requestParam.getOriginUrls();
        List<String> describeList = requestParam.getDescribes();
        List<ShortLinkBaseInfoRespDto> result = new ArrayList<>();
        for (int i = 0; i < originList.size(); i++) {
            ShortLinkCreateReqDto shortLinkCreateReqDto = BeanUtil.toBean(requestParam, ShortLinkCreateReqDto.class);
            String originUrl = originList.get(i);
            String describe = describeList.get(i);
            shortLinkCreateReqDto.setOriginUrl(originUrl);
            shortLinkCreateReqDto.setDescribe(describe);
            try {
                ShortLinkCreateRespDto shortLink = createShortLink(shortLinkCreateReqDto);
                ShortLinkBaseInfoRespDto linkBaseInfoRespDto = ShortLinkBaseInfoRespDto.builder()
                        .fullShortUrl(shortLink.getFullShortUrl())
                        .originUrl(shortLink.getOriginUrl())
                        .describe(describe)
                        .build();
                result.add(linkBaseInfoRespDto);
            } catch (Throwable ex) {
                log.error("批量创建短链接失败，原始参数：{}", originUrl);
            }
        }
        return ShortLinkBatchCreateRespDto.builder()
                .baseLinkInfos(result)
                .total(result.size())
                .build();
    }

    @Override
    public IPage<ShortLinkPageRespDto> pageShortLink(ShortLinkPageReqDto requestParam) {
        IPage<ShortLinkDo> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDto result = BeanUtil.toBean(each, ShortLinkPageRespDto.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkCountQueryRespDto> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDo> queryWrapper = Wrappers.query(new ShortLinkDo())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDoList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDoList, ShortLinkCountQueryRespDto.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDto requestParam) {
        verificationWhiteList(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                .eq(ShortLinkDo::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDo::getDelFlag, 0)
                .eq(ShortLinkDo::getEnableStatus, 0);
        ShortLinkDo hasShortLinkDo = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDo == null) {
            throw new ClientException(SHORT_LINK_NOT_EXIST);
        }
        if (Objects.equals(requestParam.getGid(), hasShortLinkDo.getGid())) {
            LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                    .eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDo::getGid, requestParam.getGid())
                    .eq(ShortLinkDo::getDelFlag, 0)
                    .eq(ShortLinkDo::getEnableStatus, 0)
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT), ShortLinkDo::getValidDate, null);
            ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                    .domain(hasShortLinkDo.getDomain())
                    .shortUri(hasShortLinkDo.getShortUri())
                    .favicon(hasShortLinkDo.getFavicon())
                    .createdType(hasShortLinkDo.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLinkDo, updateWrapper);
        } else {
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, requestParam.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            rLock.lock();
            try {
                LambdaUpdateWrapper<ShortLinkDo> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                        .eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkDo::getGid, requestParam.getGid())
                        .eq(ShortLinkDo::getDelFlag, 0)
                        .eq(ShortLinkDo::getDelTime, 0L)
                        .eq(ShortLinkDo::getDelFlag, 0)
                        .eq(ShortLinkDo::getEnableStatus, 0);
                ShortLinkDo delShortLinkDo = ShortLinkDo.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDo.setDelFlag(1);
                baseMapper.update(delShortLinkDo, linkUpdateWrapper);
                ShortLinkDo shortLinkDO = ShortLinkDo.builder()
                        .domain(createShortLinkDefaultDomain)
                        .originUrl(requestParam.getOriginUrl())
                        .gid(requestParam.getGid())
                        .createdType(hasShortLinkDo.getCreatedType())
                        .validDateType(requestParam.getValidDateType())
                        .validDate(requestParam.getValidDate())
                        .describe(requestParam.getDescribe())
                        .shortUri(hasShortLinkDo.getShortUri())
                        .enableStatus(hasShortLinkDo.getEnableStatus())
                        .totalPv(hasShortLinkDo.getTotalPv())
                        .totalUv(hasShortLinkDo.getTotalUv())
                        .totalUip(hasShortLinkDo.getTotalUip())
                        .fullShortUrl(hasShortLinkDo.getFullShortUrl())
                        .favicon(getFaviconByUrl(requestParam.getOriginUrl()))
                        .delTime(0L)
                        .build();
                baseMapper.insert(shortLinkDO);

                LambdaQueryWrapper<LinkStatsTodayDo> statsTodayQueryWrapper = Wrappers.lambdaQuery(LinkStatsTodayDo.class)
                        .eq(LinkStatsTodayDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkStatsTodayDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkStatsTodayDo::getDelFlag, 0);
                List<LinkStatsTodayDo> linkStatsTodayDoList = linkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                if (CollUtil.isNotEmpty(linkStatsTodayDoList)) {
                    linkStatsTodayMapper.deleteBatchIds(linkStatsTodayDoList.stream()
                            .map(LinkStatsTodayDo::getId)
                            .toList()
                    );
                    linkStatsTodayDoList.forEach(each -> each.setGid(requestParam.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDoList);
                }
                LambdaQueryWrapper<ShortLinkGotoDo> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDo.class)
                        .eq(ShortLinkGotoDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(ShortLinkGotoDo::getGid, hasShortLinkDo.getGid());
                ShortLinkGotoDo shortLinkGotoDo = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                shortLinkGotoMapper.deleteById(shortLinkGotoDo.getId());
                shortLinkGotoDo.setGid(requestParam.getGid());
                shortLinkGotoMapper.insert(shortLinkGotoDo);
                LambdaUpdateWrapper<LinkAccessStatsDo> linkAccessStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessStatsDo.class)
                        .eq(LinkAccessStatsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessStatsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkAccessStatsDo::getDelFlag, 0);
                LinkAccessStatsDo linkAccessStatsDo = LinkAccessStatsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessStatsMapper.update(linkAccessStatsDo, linkAccessStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkLocaleStatsDo> linkLocaleStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkLocaleStatsDo.class)
                        .eq(LinkLocaleStatsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkLocaleStatsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkLocaleStatsDo::getDelFlag, 0);
                LinkLocaleStatsDo linkLocaleStatsDo = LinkLocaleStatsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkLocaleStatsMapper.update(linkLocaleStatsDo, linkLocaleStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkOsStatsDo> linkOsStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkOsStatsDo.class)
                        .eq(LinkOsStatsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkOsStatsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkOsStatsDo::getDelFlag, 0);
                LinkOsStatsDo linkOsStatsDo = LinkOsStatsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkOsStatsMapper.update(linkOsStatsDo, linkOsStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkBrowserStatsDo> linkBrowserStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkBrowserStatsDo.class)
                        .eq(LinkBrowserStatsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkBrowserStatsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkBrowserStatsDo::getDelFlag, 0);
                LinkBrowserStatsDo linkBrowserStatsDo = LinkBrowserStatsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkBrowserStatsMapper.update(linkBrowserStatsDo, linkBrowserStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkDeviceStatsDo> linkDeviceStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkDeviceStatsDo.class)
                        .eq(LinkDeviceStatsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkDeviceStatsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkDeviceStatsDo::getDelFlag, 0);
                LinkDeviceStatsDo linkDeviceStatsDo = LinkDeviceStatsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkDeviceStatsMapper.update(linkDeviceStatsDo, linkDeviceStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkNetworkStatsDo> linkNetworkStatsUpdateWrapper = Wrappers.lambdaUpdate(LinkNetworkStatsDo.class)
                        .eq(LinkNetworkStatsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkNetworkStatsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkNetworkStatsDo::getDelFlag, 0);
                LinkNetworkStatsDo linkNetworkStatsDo = LinkNetworkStatsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkNetworkStatsMapper.update(linkNetworkStatsDo, linkNetworkStatsUpdateWrapper);
                LambdaUpdateWrapper<LinkAccessLogsDo> linkAccessLogsUpdateWrapper = Wrappers.lambdaUpdate(LinkAccessLogsDo.class)
                        .eq(LinkAccessLogsDo::getFullShortUrl, requestParam.getFullShortUrl())
                        .eq(LinkAccessLogsDo::getGid, hasShortLinkDo.getGid())
                        .eq(LinkAccessLogsDo::getDelFlag, 0);
                LinkAccessLogsDo linkAccessLogsDo = LinkAccessLogsDo.builder()
                        .gid(requestParam.getGid())
                        .build();
                linkAccessLogsMapper.update(linkAccessLogsDo, linkAccessLogsUpdateWrapper);
            } finally {
                rLock.unlock();
            }
        }
        if (!Objects.equals(requestParam.getValidDateType(), hasShortLinkDo.getValidDateType())
                || !Objects.equals(requestParam.getValidDate(), hasShortLinkDo.getValidDate())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
            if (hasShortLinkDo.getValidDate() != null && hasShortLinkDo.getValidDate().before(new Date())) {
                if (Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getType())
                        || requestParam.getValidDate().after(new Date())) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();

        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");

        String fullShortUrl = serverName + serverPort + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));


        if (StrUtil.isNotBlank(originalLink)) {
            ShortLinkStatsRecordDto statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }

        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }

        String lockFullShortUrl = String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl);
        RLock lock = redissonClient.getLock(lockFullShortUrl);

        try {
            lock.lock();
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ShortLinkStatsRecordDto statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                shortLinkStats(fullShortUrl, null, statsRecord);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkGotoDo> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDo.class)
                    .eq(ShortLinkGotoDo::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDo linkGotoDo = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (linkGotoDo == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;
            }
            LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                    .eq(ShortLinkDo::getGid, linkGotoDo.getGid()).eq(ShortLinkDo::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDo::getDelFlag, 0)
                    .eq(ShortLinkDo::getEnableStatus, 0);
            ShortLinkDo shortLinkDo = baseMapper.selectOne(queryWrapper);
            if (shortLinkDo == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;
            }
            if (shortLinkDo.getValidDate() != null && shortLinkDo.getValidDate().before(new Date())) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDo.getOriginUrl(),
                    LinkUtil.getLinkCacheValidDate(shortLinkDo.getValidDate()), TimeUnit.MILLISECONDS
            );
            ShortLinkStatsRecordDto statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            shortLinkStats(fullShortUrl, null, statsRecord);
            ((HttpServletResponse) response).sendRedirect(shortLinkDo.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    private ShortLinkStatsRecordDto buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicReference<String> uv = new AtomicReference<>();
        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString());
            Cookie uvCookie = new Cookie("uv", uv.get());
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(uvCookie);
            uvFirstFlag.set(Boolean.TRUE);
            stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        uv.set(each);
                        Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                    }, addResponseCookieTask);
        } else {
            addResponseCookieTask.run();
        }
        String remoteAddr = LinkUtil.getClientIp(((HttpServletRequest) request));
        String os = LinkUtil.getOperatingSystem(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork(((HttpServletRequest) request));
        Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        return ShortLinkStatsRecordDto.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(uvFirstFlag.get())
                .uipFirstFlag(uipFirstFlag)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    @Override
    public void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDto statsRecord) {       Map<String, String> producerMap = new HashMap<>();
        producerMap.put("fullShortUrl", fullShortUrl);
        producerMap.put("gid", gid);
        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        shortLinkStatsSaveProducer.send(producerMap);
    }

    private String getFaviconByUrl(String url) {
        try {
            // 连接到网站并解析HTML
            Document doc = Jsoup.connect(url).get();

            // 尝试从<link>标签中获取favicon
            Element faviconElement = doc.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]").first();

            if (faviconElement != null) {
                String faviconUrl = faviconElement.attr("href");

                // 检查favicon URL是否是相对路径
                if (!faviconUrl.startsWith("http")) {
                    URL targetUrl = new URL(url);
                    String baseUrl = targetUrl.getProtocol() + "://" + targetUrl.getHost();
                    faviconUrl = baseUrl + faviconUrl;
                }
                return faviconUrl;
            }
            // 如果没有找到favicon，返回null或默认值
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private String generateSuffix(ShortLinkCreateReqDto requestParam) {
        int customGenerateCount = 0;
        String originUrl = requestParam.getOriginUrl();
        String shortUri = null;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException(SHORT_LINK_GENERATE_FREQUENTLY);
            }
            originUrl += UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originUrl);
            String fullShortUrl = createShortLinkDefaultDomain + "/" + shortUri;
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
