package com.aa03.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dao.mapper.ShortLinkMapper;
import com.aa03.shortlink.project.dto.req.RecycleBinRecoverReqDto;
import com.aa03.shortlink.project.dto.req.RecycleBinSaveReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkPageRespDto;
import com.aa03.shortlink.project.service.RecycleBinService;
import com.aa03.shortlink.project.toolkit.LinkUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.aa03.shortlink.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.aa03.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 短链接回收站接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDo> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveRecycleBin(RecycleBinSaveReqDto requestParam) {
        LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                .eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDo::getGid, requestParam.getGid())
                .eq(ShortLinkDo::getEnableStatus, 0)
                .eq(ShortLinkDo::getDelFlag, 0);
        ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                .enableStatus(1)
                .build();

        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
        baseMapper.update(shortLinkDo, updateWrapper);
    }

    @Override
    public IPage<ShortLinkPageRespDto> pageShortLink(ShortLinkRecycleBinPageReqDto requestParam) {
        LambdaQueryWrapper<ShortLinkDo> queryWrapper = Wrappers.lambdaQuery(ShortLinkDo.class)
                .in(ShortLinkDo::getGid, requestParam.getGidList())
                .eq(ShortLinkDo::getEnableStatus, 1)
                .eq(ShortLinkDo::getDelFlag, 0)
                .orderByDesc(ShortLinkDo::getUpdateTime);
        IPage<ShortLinkDo> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDto result = BeanUtil.toBean(each, ShortLinkPageRespDto.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDto requestParam) {
        LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                .eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDo::getGid, requestParam.getGid())
                .eq(ShortLinkDo::getEnableStatus, 1)
                .eq(ShortLinkDo::getDelFlag, 0);
        ShortLinkDo shortLinkDo = ShortLinkDo.builder()
                .enableStatus(0)
                .build();

        baseMapper.update(shortLinkDo, updateWrapper);
        if (shortLinkDo != null) {
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, shortLinkDo.getFullShortUrl()),
                    shortLinkDo.getOriginUrl(),
                    LinkUtil.getLinkCacheValidDate(shortLinkDo.getValidDate()), TimeUnit.MILLISECONDS
            );
            stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, shortLinkDo.getFullShortUrl()));
        }
    }

    @Override
    public void removeRecycleBin(RecycleBinRecoverReqDto requestParam) {
        LambdaUpdateWrapper<ShortLinkDo> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDo.class)
                .eq(ShortLinkDo::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDo::getGid, requestParam.getGid())
                .eq(ShortLinkDo::getEnableStatus, 1)
                .eq(ShortLinkDo::getDelFlag, 0);

        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
        baseMapper.delete(updateWrapper);
    }
}
