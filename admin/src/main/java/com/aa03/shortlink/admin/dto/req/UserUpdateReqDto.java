package com.aa03.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户更新请求参数
 */
@Data
public class UserUpdateReqDto {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
