package com.aa03.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.aa03.shortlink.admin.common.biz.user.UserContext;
import com.aa03.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.aa03.shortlink.admin.common.convention.exception.ClientException;
import com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.aa03.shortlink.admin.dao.entity.UserDo;
import com.aa03.shortlink.admin.dao.mapper.UserMapper;
import com.aa03.shortlink.admin.dto.req.UserLoginReqDto;
import com.aa03.shortlink.admin.dto.req.UserRegisterReqDto;
import com.aa03.shortlink.admin.dto.req.UserUpdateReqDto;
import com.aa03.shortlink.admin.dto.resp.UserLoginRespDto;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.aa03.shortlink.admin.service.GroupService;
import com.aa03.shortlink.admin.service.UserService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.aa03.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.aa03.shortlink.admin.common.constant.RedisCacheConstant.USER_TOKEN_LOGIN_KEY;
import static com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDo> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void registerUser(UserRegisterReqDto requestParam) {
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            UserDo userDo = BeanUtil.toBean(requestParam, UserDo.class);
            int inserted = baseMapper.insert(userDo);
            if (inserted < 0) {
                throw new ClientException(BaseErrorCode.CLIENT_ERROR);
            }
            groupService.saveGroup(requestParam.getUsername(), "默认分组");
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateUser(UserUpdateReqDto userUpdateReqDto) {
        if (!Objects.equals(userUpdateReqDto.getUsername(), UserContext.getUsername())) {
            throw new ClientException("当前登录用户修改请求异常");
        }
        LambdaUpdateWrapper<UserDo> queryWrapper = Wrappers.lambdaUpdate(UserDo.class)
                .eq(UserDo::getUsername, userUpdateReqDto.getUsername());
        UserDo userDo = BeanUtil.toBean(userUpdateReqDto, UserDo.class);
        baseMapper.update(userDo, queryWrapper);
    }

    @Override
    public UserLoginRespDto userLogin(UserLoginReqDto requestParam) {
        LambdaUpdateWrapper<UserDo> queryWrapper = Wrappers.lambdaUpdate(UserDo.class)
                .eq(UserDo::getUsername, requestParam.getUsername())
                .eq(UserDo::getPassword, requestParam.getPassword())
                .eq(UserDo::getDelFlag, 0);
        UserDo userDo = baseMapper.selectOne(queryWrapper);
        if (userDo == null) {
            throw new ClientException(USER_NOT_EXIST);
        }
        String key = USER_TOKEN_LOGIN_KEY + requestParam.getUsername();
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(key);
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.expire(key, 30, TimeUnit.DAYS);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDto(token);
        }
        String uuid = UUID.randomUUID().toString();
        Map<String, String> userInfoMap = new HashMap<>();
        userInfoMap.put(uuid, JSON.toJSONString(userDo));
        stringRedisTemplate.opsForHash().putAll(key, userInfoMap);
        //TODO 需要cache改成30分钟、30天前端会有错误
        stringRedisTemplate.expire(key, 30, TimeUnit.DAYS);
        return new UserLoginRespDto(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        String key = USER_TOKEN_LOGIN_KEY + username;
        return stringRedisTemplate.opsForHash().get(key, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            String key = USER_TOKEN_LOGIN_KEY + username;
            stringRedisTemplate.opsForHash().delete(key, token);
            return;
        }
        throw new ClientException(USER_NOT_LOGIN);
    }
}
