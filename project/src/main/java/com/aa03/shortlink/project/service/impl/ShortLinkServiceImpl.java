package com.aa03.shortlink.project.service.impl;

import cn.hutool.Hutool;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.aa03.shortlink.project.common.convention.exception.ClientException;
import com.aa03.shortlink.project.common.convention.exception.ServiceException;
import com.aa03.shortlink.project.common.enums.ValidDateTypeEnum;
import com.aa03.shortlink.project.dao.entity.*;
import com.aa03.shortlink.project.dao.mapper.*;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkUpdateReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCountQueryRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkPageRespDto;
import com.aa03.shortlink.project.service.ShortLinkService;
import com.aa03.shortlink.project.toolkit.HashUtil;
import com.aa03.shortlink.project.toolkit.LinkUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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

import static com.aa03.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.aa03.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;
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

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDto createShortLink(ShortLinkCreateReqDto requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                .domain(requestParam.getDomain())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createType(requestParam.getCreatedType())
                .validDate(requestParam.getValidDate())
                .validDateType(requestParam.getValidDateType())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix).enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFaviconByUrl(requestParam.getOriginUrl()))
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

    @Override
    public IPage<ShortLinkPageRespDto> pageShortLink(ShortLinkPageReqDto requestParam) {
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class).eq(ShortLinkDo::getGid, requestParam.getGid()).eq(ShortLinkDo::getEnableStatus, 0).eq(ShortLinkDo::getDelFlag, 0).orderByDesc(ShortLinkDo::getCreateTime);
        IPage<ShortLinkDo> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDto result = BeanUtil.toBean(each, ShortLinkPageRespDto.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public List<ShortLinkCountQueryRespDto> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDo> queryWrapper = Wrappers.query(new ShortLinkDo()).select("gid as gid, count(*) as shortLinkCount").in("gid", requestParam).eq("enable_status", 0).groupBy("gid");
        List<Map<String, Object>> shortLinkDoList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDoList, ShortLinkCountQueryRespDto.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateShortLink(ShortLinkUpdateReqDto requestParam) {
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class).eq(ShortLinkDo::getGid, requestParam.getGid()).eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl()).eq(ShortLinkDo::getDelFlag, 0).eq(ShortLinkDo::getEnableStatus, 0);
        ShortLinkDo hasShortLinkDo = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDo == null) {
            throw new ClientException(SHORT_LINK_NOT_EXIST);
        }
        // TODO 缺少创建时间
        ShortLinkDo shortLinkDo = ShortLinkDo.builder().domain(hasShortLinkDo.getDomain()).shortUri(hasShortLinkDo.getShortUri()).clickNum(hasShortLinkDo.getClickNum()).favicon(hasShortLinkDo.getFavicon()).createType(hasShortLinkDo.getCreateType()).gid(hasShortLinkDo.getGid()).originUrl(requestParam.getOriginUrl()).describe(requestParam.getDescribe()).validDateType(requestParam.getValidDateType()).validDate(requestParam.getValidDate()).build();
        if (Objects.equals(requestParam.getGid(), hasShortLinkDo.getGid())) {
            LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class).eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl()).eq(ShortLinkDo::getGid, requestParam.getGid()).eq(ShortLinkDo::getDelFlag, 0).eq(ShortLinkDo::getEnableStatus, 0).set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT), ShortLinkDo::getValidDate, null);
            baseMapper.update(shortLinkDo, updateWrapper);
        } else {
            LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class).eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl()).eq(ShortLinkDo::getGid, hasShortLinkDo.getGid()).eq(ShortLinkDo::getDelFlag, 0).eq(ShortLinkDo::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            shortLinkDo.setGid(requestParam.getGid());
            baseMapper.insert(shortLinkDo);
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));

        if (StrUtil.isNotBlank(originalLink)) {
            shortLinkStats(fullShortUrl, null, request, response);
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
                shortLinkStats(fullShortUrl, null, request, response);
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
            shortLinkStats(shortLinkDo.getFullShortUrl(), shortLinkDo.getGid(), request, response);
            ((HttpServletResponse) response).sendRedirect(shortLinkDo.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean(true);
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
            Runnable addResponseCookieTask = () -> {
                String uv = cn.hutool.core.lang.UUID.fastUUID().toString();
                Cookie uvCookie = new Cookie("uv", uv);
                uvCookie.setMaxAge(60 * 60 * 24 * 30);
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse) response).addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add(SHORT_LINK_UV_EXIST + fullShortUrl, uv);
            };

            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            Long uvAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_UV_EXIST + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            } else {
                addResponseCookieTask.run();
            }

            String remoteAddr = LinkUtil.getClientIp((HttpServletRequest) request);
            Long uipAdded = stringRedisTemplate.opsForSet().add(SHORT_LINK_UIP_EXIST + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0;

            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDo.class)
                        .eq(ShortLinkGotoDo::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDo linkGotoDo = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = linkGotoDo.getGid();
            }
            Date now = new Date();

            int hour = DateUtil.hour(now, true);
            Week week = DateUtil.dayOfWeekEnum(now);
            int weekValue = week.getIso8601Value();

            LinkAccessStatsDo linkAccessStatsDo = LinkAccessStatsDo.builder()
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .pv(1)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(now)
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDo);
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", remoteAddr);
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);

            String infoCode = localeResultObj.getString("infocode");

            LinkLocaleStatsDo linkLocaleStatsDo;

            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode,"10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.isBlank(province);
                linkLocaleStatsDo = LinkLocaleStatsDo.builder()
                        .fullShortUrl(fullShortUrl)
                        .province(unknownFlag ? "未知" : province)
                        .city(unknownFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .country("中国")
                        .gid(gid)
                        .date(now)
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDo);
                LinkOsStatsDo linkOsStatsDo = LinkOsStatsDo.builder()
                        .os(LinkUtil.getOperatingSystem((HttpServletRequest) request))
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(now)
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStatsDo);
            }
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
        }
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
            originUrl += UUID.randomUUID();
            shortUri = HashUtil.hashToBase62(originUrl);
            String fullShortUrl = requestParam.getDomain() + "/" + shortUri;
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
