package com.aa03.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aa03.shortlink.admin.common.biz.user.UserContext;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.dao.entity.GroupDo;
import com.aa03.shortlink.admin.dao.mapper.GroupMapper;
import com.aa03.shortlink.admin.dto.req.ShortLinkGroupSortReqDto;
import com.aa03.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDto;
import com.aa03.shortlink.admin.dto.resp.ShortLinkGroupRespDto;
import com.aa03.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDto;
import com.aa03.shortlink.admin.service.GroupService;
import com.aa03.shortlink.admin.toolkit.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDo> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @Override
    public void saveGroup(String groupName) {
        this.saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        String gid = RandomGenerator.generateRandomString();
        while (true) {
            if (!hasGid(username, gid)) break;
            gid = RandomGenerator.generateRandomString();
        }
        GroupDo groupDo = GroupDo.builder()
                .gid(gid)
                .name(groupName)
                .username(username)
                .sortOrder(0)
                .build();
        baseMapper.insert(groupDo);
    }

    @Override
    public List<ShortLinkGroupRespDto> listGroup() {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getDelFlag, 0)
                .orderByDesc(GroupDo::getSortOrder, GroupDo::getUpdateTime);
        List<GroupDo> groupDoList = baseMapper.selectList(queryWrapper);
        Result<List<ShortLinkCountQueryRespDto>> listResult = shortLinkRemoteService
                .listGroupShortLinkCount(groupDoList.stream().map(GroupDo::getGid).collect(Collectors.toList()));
        List<ShortLinkGroupRespDto> shortLinkGroupRespDtoList = BeanUtil.copyToList(groupDoList, ShortLinkGroupRespDto.class);

        shortLinkGroupRespDtoList.forEach(each -> {
            Optional<ShortLinkCountQueryRespDto> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(item -> each.setShortLinkCount(item.getShortLinkCount()));
        });

        return shortLinkGroupRespDtoList;

    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDto requestParam) {
        LambdaUpdateWrapper<GroupDo> queryWrapper = Wrappers.lambdaUpdate(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getGid, requestParam.getGid())
                .eq(GroupDo::getDelFlag, 0);
        GroupDo groupDo = new GroupDo();
        groupDo.setName(requestParam.getName());
        baseMapper.update(groupDo, queryWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDo> queryWrapper = Wrappers.lambdaUpdate(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getDelFlag, 0);
        GroupDo groupDo = new GroupDo();
        groupDo.setDelFlag(1);
        baseMapper.update(groupDo, queryWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDto> requestParam) {
        requestParam.forEach(item -> {
            GroupDo groupDo = GroupDo.builder()
                    .sortOrder(item.getSortOrder()).build();
            LambdaUpdateWrapper<GroupDo> queryWrapper = Wrappers.lambdaUpdate(GroupDo.class)
                    .eq(GroupDo::getUsername, UserContext.getUsername())
                    .eq(GroupDo::getGid, item.getGid())
                    .eq(GroupDo::getDelFlag, 0);
            baseMapper.update(groupDo, queryWrapper);
        });
    }

    private boolean hasGid(String username, String gid) {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDo groupDo = baseMapper.selectOne(queryWrapper);
        return groupDo != null;
    }
}
