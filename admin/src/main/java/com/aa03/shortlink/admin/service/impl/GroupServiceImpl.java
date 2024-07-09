package com.aa03.shortlink.admin.service.impl;

import com.aa03.shortlink.admin.dao.entity.GroupDo;
import com.aa03.shortlink.admin.dao.mapper.GroupMapper;
import com.aa03.shortlink.admin.service.GroupService;
import com.aa03.shortlink.admin.toolkit.RandomGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDo> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid = RandomGenerator.generateRandomString();
        while (true) {
            if (!hasGid(gid)) break;
            gid = RandomGenerator.generateRandomString();
        }
        GroupDo groupDo = GroupDo.builder()
                .gid(gid)
                .name(groupName)
                .sortOrder(0)
                .build();
        baseMapper.insert(groupDo);
    }

    private boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDo> queryWrapper = Wrappers.lambdaQuery(GroupDo.class)
                .eq(GroupDo::getGid, gid)
                .eq(GroupDo::getUsername, null);
        GroupDo groupDo = baseMapper.selectOne(queryWrapper);
        return groupDo != null;
    }
}
