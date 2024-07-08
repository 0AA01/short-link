package com.aa03.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.aa03.shortlink.admin.dto.resp.UserActualRespDto;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.aa03.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("/api/short-link/v1/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/user/{username}")
    public Result<UserRespDto> getUserByUsername(@PathVariable("username") String username) {
        UserRespDto userByUsername = userService.getUserByUsername(username);
        return Results.success(userByUsername);
    }

    /**
     * 根据用户名查询无脱敏用户信息
     */
    @GetMapping("/actual/user/{username}")
    public Result<UserActualRespDto> getUserActualByUsername(@PathVariable("username") String username) {
        UserActualRespDto userByUsername = BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDto.class);
        return Results.success(userByUsername);
    }
}
