package com.aa03.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.aa03.shortlink.admin.common.biz.user.UserContext;
import com.aa03.shortlink.admin.common.convention.exception.ServiceException;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.dao.entity.GroupDo;
import com.aa03.shortlink.admin.dao.mapper.GroupMapper;
import com.aa03.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.aa03.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDto;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkPageRespDto;
import com.aa03.shortlink.admin.service.RecycleBinService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum.USER_GROUP_NULL;

/**
 * 短链接回收站接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 回收站分页查询
     *
     * @param requestParam 短链接分页查询请求参数
     * @return 短链接分页返回结果
     */
    @Override
    public Result<Page<ShortLinkPageRespDto>> pageRecycleShortLink(ShortLinkRecycleBinPageReqDto requestParam) {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getDelFlag, 0);
        List<GroupDo> groupDoList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDoList)) {
            throw new ServiceException(USER_GROUP_NULL);
        }
        requestParam.setGidList(groupDoList.stream().map(GroupDo::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getGidList(), requestParam.getCurrent(), requestParam.getSize());
    }

}
