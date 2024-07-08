package com.aa03.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aa03.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.aa03.shortlink.admin.common.convention.exception.ClientException;
import com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.aa03.shortlink.admin.dao.entity.UserDo;
import com.aa03.shortlink.admin.dao.mapper.UserMapper;
import com.aa03.shortlink.admin.dto.req.UserRegisterReqDto;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.aa03.shortlink.admin.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum.USER_EXIST;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Override
    public UserRespDto getUserByUsername(String username) {
        LambdaQueryWrapper<UserDo> queryWrapper = Wrappers.lambdaQuery(UserDo.class)
                .eq(UserDo::getUsername, username);
        queryWrapper.eq(UserDo::getUsername, username);
        UserDo userDo = baseMapper.selectOne(queryWrapper);

        if (userDo == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }

        UserRespDto userRespDto = new UserRespDto();
        BeanUtils.copyProperties(userDo, userRespDto);
        return userRespDto;
    }

    @Override
    public Boolean hasUsername(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void registerUser(UserRegisterReqDto requestParam) {
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_EXIST);
        }
        UserDo userDo = BeanUtil.toBean(requestParam, UserDo.class);
        int inserted = baseMapper.insert(userDo);
        if (inserted < 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
    }
}
