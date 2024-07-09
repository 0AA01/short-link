package com.aa03.shortlink.admin.service;

import com.aa03.shortlink.admin.dao.entity.UserDo;
import com.aa03.shortlink.admin.dto.req.UserLoginReqDto;
import com.aa03.shortlink.admin.dto.req.UserRegisterReqDto;
import com.aa03.shortlink.admin.dto.req.UserUpdateReqDto;
import com.aa03.shortlink.admin.dto.resp.UserLoginRespDto;
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

    /**
     * 更新用户
     *
     * @param requestParam 更新用户请求参数
     */
    void updateUser(UserUpdateReqDto requestParam);

    /**
     * 用户登录
     *
     * @param requestParam 用户登录请求参数
     * @return 用户登录返回参数 Token
     */
    UserLoginRespDto userLogin(UserLoginReqDto requestParam);

    /**
     * 检查用户是否登录
     *
     * @param username 用户名
     * @param token    用户登录身份验证信息
     * @return 登录返回True 未登录返回False
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     *
     * @param username 用户名
     * @param token    用户登录身份验证信息
     */
    void logout(String username, String token);
}
