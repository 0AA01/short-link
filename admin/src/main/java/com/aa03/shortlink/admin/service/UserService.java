package com.aa03.shortlink.admin.service;

import com.aa03.shortlink.admin.dao.entity.UserDo;
import com.aa03.shortlink.admin.dto.req.UserRegisterReqDto;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDo> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDto getUserByUsername(String username);

    /**
     * 查询是否存在该用户名
     *
     * @param username 用户名
     * @return 存在返回True 不存在返回False
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     *
     * @param requestParam 注册用户请求参数
     */
    void registerUser(UserRegisterReqDto requestParam);
}
