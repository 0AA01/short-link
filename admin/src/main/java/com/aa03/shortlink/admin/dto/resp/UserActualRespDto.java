package com.aa03.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 用户无脱敏参数响应
 */
@Data
public class UserActualRespDto {
    /**
     * 用户名
     */
    private String username;

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
