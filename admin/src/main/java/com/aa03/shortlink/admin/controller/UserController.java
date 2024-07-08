package com.aa03.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.dto.req.UpdateUserReqDto;
import com.aa03.shortlink.admin.dto.req.UserRegisterReqDto;
import com.aa03.shortlink.admin.dto.resp.UserActualRespDto;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.aa03.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册用户
     *
     * @param requestParam 注册用户请求参数
     */
    @PostMapping("/user")
    public Result<Void> registerUser(@RequestBody UserRegisterReqDto requestParam) {
        userService.registerUser(requestParam);
        return Results.success();
    }

    @PutMapping("/api/short-link/v1/user")
    public Result<Void> updateUser(@RequestBody UpdateUserReqDto updateUserReqDto) {
        userService.updateUser(updateUserReqDto);
        return Results.success();
    }



}
