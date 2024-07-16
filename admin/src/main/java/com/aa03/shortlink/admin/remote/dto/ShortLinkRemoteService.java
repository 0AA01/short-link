package com.aa03.shortlink.admin.remote.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.dto.resp.ShortLinkGroupRespDto;
import com.aa03.shortlink.admin.remote.dto.req.*;
import com.aa03.shortlink.admin.remote.dto.resp.*;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {


    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    default Result<ShortLinkCreateRespDto> createShortLink(ShortLinkCreateReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量短链接创建信息
     */
    default Result<ShortLinkBatchCreateRespDto> batchCreateShortLink(ShortLinkBatchCreateReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create/batch", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 短链接分页查询
     *
     * @param requestParam 短链接分页查询请求参数
     * @return 短链接分页返回结果
     */
    default Result<IPage<ShortLinkPageRespDto>> pageShortLink(ShortLinkPageReqDto requestParam) {
        Map<String, Object> requestMap = new HashedMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("orderTag", requestParam.getOrderTag());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 查询短链接分组内数量
     *
     * @param requestParam 查询短链接分组内数量请求参数
     * @return 查询短链接分组内数量响应
     */
    default Result<List<ShortLinkCountQueryRespDto>> listGroupShortLinkCount(List<String> requestParam) {
        Map<String, Object> requestMap = new HashedMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }


    /**
     * 短链接信息修改
     *
     * @param requestParam 修改短链接请求参数
     */
    default Result<Void> updateShortLink(ShortLinkUpdateReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
        return Results.success();
    }

    /**
     * 根据URL获取网站标题
     *
     * @param url 目标网站地址
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(String url) {
        Map<String, Object> requestMap = new HashedMap<>();
        requestMap.put("url", url);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 根据URL获取网站的图标
     *
     * @param url 目标网站地址
     * @return 网站的图标地址
     */
    default Result<String> getFaviconByUrl(String url) {
        Map<String, Object> requestMap = new HashedMap<>();
        requestMap.put("url", url);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/favicon", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }


    /**
     * 回收站新增功能
     *
     * @param requestParam 回收站新增请求参数
     */
    default Result<Void> saveRecycleBin(RecycleBinSaveReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 回收站分页查询
     *
     * @param requestParam 短链接分页查询请求参数
     * @return 短链接分页返回结果
     */
    default Result<IPage<ShortLinkPageRespDto>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDto requestParam) {
        Map<String, Object> requestMap = new HashedMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }


    /**
     * 回收站恢复
     *
     * @param requestParam 回收站恢复请求参数
     */
    default Result<Void> recoverRecycleBin(RecycleBinRecoverReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }


    /**
     * 回收站删除
     *
     * @param requestParam 回收站恢复请求参数
     */
    default Result<Void> removeRecycleBin(RecycleBinRecoverReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    default Result<ShortLinkStatsRespDto> oneShortLinkStats(ShortLinkStatsReqDto requestParam) {
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 获取分组短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    default Result<ShortLinkStatsRespDto> groupShortLinkStats(ShortLinkGroupStatsReqDto requestParam) {
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     *
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDto>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDto requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record", stringObjectMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     *
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDto>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDto requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group", stringObjectMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }
}
