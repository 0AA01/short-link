package com.aa03.shortlink.project.service;

import com.aa03.shortlink.project.dao.entity.ShortLinkDo;
import com.aa03.shortlink.project.dto.biz.ShortLinkStatsRecordDto;
import com.aa03.shortlink.project.dto.req.ShortLinkBatchCreateReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.project.dto.req.ShortLinkUpdateReqDto;
import com.aa03.shortlink.project.dto.resp.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDo> {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDto createShortLink(ShortLinkCreateReqDto requestParam);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量创建短链接返回参数
     */
    ShortLinkBatchCreateRespDto batchCreateShortLink(ShortLinkBatchCreateReqDto requestParam);

    /**
     * 短链接分页查询
     *
     * @param requestParam 短链接分页查询请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDto> pageShortLink(ShortLinkPageReqDto requestParam);

    /**
     * 查询短链接分组内数量
     *
     * @param requestParam 查询短链接分组内数量请求参数
     * @return 查询短链接分组内数量响应
     */
    List<ShortLinkGroupCountQueryRespDto> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 短链接信息修改
     *
     * @param requestParam 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDto requestParam);

    /**
     * 短链接跳转原始链接
     *
     * @param shortUri 短链接后缀
     * @param request  HTTP请求
     * @param response HTTP响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param shortLinkStatsRecord 短链接统计实体参数
     */
    void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDto shortLinkStatsRecord);
}
