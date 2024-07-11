package com.aa03.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.aa03.shortlink.project.common.convention.exception.ClientException;
import com.aa03.shortlink.project.common.convention.exception.ServiceException;
import com.aa03.shortlink.project.common.enums.ValidDateTypeEnum;
import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dao.entity.ShortLinkGotoDo;
import com.aa03.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.aa03.shortlink.project.dao.mapper.ShortLinkMapper;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkUpdateReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCountQueryRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkPageRespDto;
import com.aa03.shortlink.project.service.ShortLinkService;
import com.aa03.shortlink.project.toolkit.HashUtil;
import com.aa03.shortlink.project.toolkit.LinkUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDto createShortLink(ShortLinkCreateReqDto requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain()).append("/").append(shortLinkSuffix).toString();
        ShortLinkDo shortLinkDo = ShortLinkDo.builder().domain(requestParam.getDomain()).originUrl(requestParam.getOriginUrl()).gid(requestParam.getGid()).createType(requestParam.getCreatedType()).validDate(requestParam.getValidDate()).validDateType(requestParam.getValidDateType()).describe(requestParam.getDescribe()).shortUri(shortLinkSuffix).enableStatus(0).fullShortUrl(fullShortUrl).build();
        ShortLinkGotoDo linkGotoDo = ShortLinkGotoDo.builder().fullShortUrl(fullShortUrl).gid(requestParam.getGid()).build();
        try {
            baseMapper.insert(shortLinkDo);
            shortLinkGotoMapper.insert(linkGotoDo);
        } catch (DuplicateKeyException ex) {
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ServiceException(SHORT_LINK_GENERATE_REPEAT);
        }
        stringRedisTemplate.opsForValue().set(
                fullShortUrl,
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidDate(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDto.builder().fullShortUrl("http://" + shortLinkDo.getFullShortUrl()).originUrl(shortLinkDo.getOriginUrl()).gid(requestParam.getGid()).build();
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
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }

        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            return;
        }

        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            return;
        }


        String lockFullShortUrl = String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl);
        RLock lock = redissonClient.getLock(lockFullShortUrl);

        try {
            lock.lock();
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
                return;
            }

            LambdaQueryWrapper<ShortLinkGotoDo> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDo.class)
                    .eq(ShortLinkGotoDo::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDo linkGotoDo = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (linkGotoDo == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;
            }
            LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class).eq(ShortLinkDo::getGid, linkGotoDo.getGid()).eq(ShortLinkDo::getFullShortUrl, fullShortUrl).eq(ShortLinkDo::getDelFlag, 0).eq(ShortLinkDo::getEnableStatus, 0);
            ShortLinkDo shortLinkDo = baseMapper.selectOne(queryWrapper);
            if (shortLinkDo != null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl), shortLinkDo.getOriginUrl());
                ((HttpServletResponse) response).sendRedirect(shortLinkDo.getOriginUrl());
            }
        } finally {
            lock.unlock();
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
