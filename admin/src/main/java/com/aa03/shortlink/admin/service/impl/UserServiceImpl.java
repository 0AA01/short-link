package com.aa03.shortlink.admin.service.impl;

import com.aa03.shortlink.admin.common.convention.exception.ClientException;
import com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.aa03.shortlink.admin.dao.entity.UserDo;
import com.aa03.shortlink.admin.dao.mapper.UserMapper;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.aa03.shortlink.admin.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements UserService {

    @Override
    public UserRespDto getUserByUsername(String username) {
        LambdaQueryWrapper<UserDo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDo::getUsername, username);
        UserDo userDo = baseMapper.selectOne(queryWrapper);

        if (userDo == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }

        UserRespDto userRespDto = new UserRespDto();
        BeanUtils.copyProperties(userDo, userRespDto);
        return userRespDto;
    }
}
