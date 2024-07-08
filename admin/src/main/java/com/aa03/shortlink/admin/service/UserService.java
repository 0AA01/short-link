package com.aa03.shortlink.admin.service;

import com.aa03.shortlink.admin.dao.entity.UserDo;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDo> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDto getUserByUsername(String username);
}
