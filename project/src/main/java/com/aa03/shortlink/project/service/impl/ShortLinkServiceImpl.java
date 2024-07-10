package com.aa03.shortlink.project.service.impl;

import cn.hutool.core.text.StrBuilder;
import com.aa03.shortlink.project.common.convention.exception.ServiceException;
import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dao.mapper.ShortLinkMapper;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.project.service.ShortLinkService;
import com.aa03.shortlink.project.toolkit.HashUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.aa03.shortlink.project.common.enums.ShortLinkErrorCodeEnum.SHORT_LINK_GENERATE_FREQUENTLY;
import static com.aa03.shortlink.project.common.enums.ShortLinkErrorCodeEnum.SHORT_LINK_GENERATE_REPEAT;

/**
 * 短链接接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDo> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
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
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        try {
            baseMapper.insert(shortLinkDo);
        } catch (DuplicateKeyException ex) {
            log.warn("短链接：{} 重复入库", fullShortUrl);
            throw new ServiceException(SHORT_LINK_GENERATE_REPEAT);
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDto.builder()
                .fullShortUrl(shortLinkDo.getFullShortUrl())
                .originUrl(shortLinkDo.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
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
