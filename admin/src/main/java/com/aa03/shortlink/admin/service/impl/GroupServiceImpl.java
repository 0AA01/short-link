package com.aa03.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aa03.shortlink.admin.common.biz.user.UserContext;
import com.aa03.shortlink.admin.common.convention.exception.ClientException;
import com.aa03.shortlink.admin.common.convention.exception.ServiceException;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.database.BaseDo;
import com.aa03.shortlink.admin.dao.entity.GroupDo;
import com.aa03.shortlink.admin.dao.entity.GroupUniqueDo;
import com.aa03.shortlink.admin.dao.mapper.GroupMapper;
import com.aa03.shortlink.admin.dao.mapper.GroupUniqueMapper;
import com.aa03.shortlink.admin.dto.req.ShortLinkGroupSortReqDto;
import com.aa03.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDto;
import com.aa03.shortlink.admin.dto.resp.ShortLinkGroupRespDto;
import com.aa03.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.aa03.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDto;
import com.aa03.shortlink.admin.service.GroupService;
import com.aa03.shortlink.admin.toolkit.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.aa03.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDo> implements GroupService {

    private final RBloomFilter<String> gidRegisterCachePenetrationBloomFilter;
    private final GroupUniqueMapper groupUniqueMapper;
    private final RedissonClient redissonClient;
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Value("${short-link.group.max-num}")
    private Integer groupMaxNum;

    @Override
    public void saveGroup(String groupName) {
        this.saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                    .eq(GroupDo::getUsername, username)
                    .eq(BaseDo::getDelFlag, 0);
            List<GroupDo> groupDoList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupDoList) && groupDoList.size() == groupMaxNum) {
                throw new ClientException(String.format("创建分组最多:%d个", groupMaxNum));
            }
            int retryCount = 0;
            int maxRetries = 10;
            String gid = null;
            while (retryCount < maxRetries) {
                gid = saveGroupUniqueReturnGid();
                if (StrUtil.isNotEmpty(gid)) {
                    GroupDo groupDO = GroupDo.builder()
                            .gid(gid)
                            .sortOrder(0)
                            .username(username)
                            .name(groupName)
                            .build();
                    baseMapper.insert(groupDO);
                    gidRegisterCachePenetrationBloomFilter.add(gid);
                    break;
                }
                retryCount++;
            }
            if (StrUtil.isEmpty(gid)) {
                throw new ServiceException("生成分组标识频繁");
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<ShortLinkGroupRespDto> listGroup() {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getUsername, UserContext.getUsername())
                .eq(GroupDo::getDelFlag, 0)
                .orderByDesc(GroupDo::getSortOrder, GroupDo::getUpdateTime);
        List<GroupDo> groupDoList = baseMapper.selectList(queryWrapper);
        Result<List<ShortLinkGroupCountQueryRespDto>> listResult = shortLinkActualRemoteService
                .listGroupShortLinkCount(groupDoList.stream().map(GroupDo::getGid).collect(Collectors.toList()));
        List<ShortLinkGroupRespDto> shortLinkGroupRespDtoList = BeanUtil.copyToList(groupDoList, ShortLinkGroupRespDto.class);

        shortLinkGroupRespDtoList.forEach(each -> {
            Optional<ShortLinkGroupCountQueryRespDto> first = listResult.getData().stream()
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

    private String saveGroupUniqueReturnGid() {
        String gid = RandomGenerator.generateRandomString();
        if (!gidRegisterCachePenetrationBloomFilter.contains(gid)) {
            GroupUniqueDo groupUniqueDO = GroupUniqueDo.builder()
                    .gid(gid)
                    .build();
            try {
                groupUniqueMapper.insert(groupUniqueDO);
            } catch (DuplicateKeyException e) {
                return null;
            }
        }
        return gid;
    }
}
