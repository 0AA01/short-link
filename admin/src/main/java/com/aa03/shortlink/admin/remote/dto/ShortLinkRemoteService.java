package com.aa03.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkCreateReqDto;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkPageReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.commons.collections4.map.HashedMap;

import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    default Result<ShortLinkCreateRespDto> createShortLink(ShortLinkCreateReqDto requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkPageRespDto>> pageShortLink(ShortLinkPageReqDto requestParam) {
        Map<String, Object> requestMap = new HashedMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }
}
