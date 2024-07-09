package com.aa03.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录接口返回参数响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRespDto {

    /**
     * 用户Token
     */
    private String token;
}
