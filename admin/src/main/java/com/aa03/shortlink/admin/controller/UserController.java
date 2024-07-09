package com.aa03.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aa03.shortlink.admin.common.convention.result.Result;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.aa03.shortlink.admin.dto.req.UserLoginReqDto;
import com.aa03.shortlink.admin.dto.req.UserRegisterReqDto;
import com.aa03.shortlink.admin.dto.req.UserUpdateReqDto;
import com.aa03.shortlink.admin.dto.resp.UserActualRespDto;
import com.aa03.shortlink.admin.dto.resp.UserLoginRespDto;
import com.aa03.shortlink.admin.dto.resp.UserRespDto;
import com.aa03.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("/api/short-link/admin/v1/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/user/{username}")
    public Result<UserRespDto> getUserByUsername(@PathVariable("username") String username) {
        UserRespDto result = userService.getUserByUsername(username);
        return Results.success(result);
    }

    /**
     * 根据用户名查询无脱敏用户信息
     */
    @GetMapping("/actual/user/{username}")
    public Result<UserActualRespDto> getUserActualByUsername(@PathVariable("username") String username) {
        UserActualRespDto result = BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDto.class);
        return Results.success(result);
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
     */
    @PostMapping("/user")
    public Result<Void> registerUser(@RequestBody UserRegisterReqDto requestParam) {
        userService.registerUser(requestParam);
        return Results.success();
    }

    /**
     * 修改用户
     */
    @PutMapping("/user")
    public Result<Void> updateUser(@RequestBody UserUpdateReqDto requestParam) {
        userService.updateUser(requestParam);
        return Results.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/user/login")
    public Result<UserLoginRespDto> userLogin(@RequestBody UserLoginReqDto requestParam) {
        UserLoginRespDto result = userService.userLogin(requestParam);
        return Results.success(result);
    }

    /**
     * 检查用户是否登录
     */
    @GetMapping("/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userService.checkLogin(username, token));
    }

    /**
     * 用户退出登录
     */
    @DeleteMapping("/user/logout")
    public Result<Void> logout(@RequestParam("username") String username, @RequestParam("token") String token) {
        userService.logout(username, token);
        return Results.success();
    }
}
