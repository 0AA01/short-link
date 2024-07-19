package com.aa03.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.aa03.shortlink.admin.common.convention.exception.ClientException;
import com.aa03.shortlink.admin.common.convention.result.Results;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

import static com.aa03.shortlink.admin.common.constant.RedisCacheConstant.USER_TOKEN_LOGIN_KEY;
import static com.aa03.shortlink.admin.common.constant.UserConstant.USER_NAME_KEY;
import static com.aa03.shortlink.admin.common.constant.UserConstant.USER_TOKEN;
import static com.aa03.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_FAIL;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String username = httpServletRequest.getHeader("username");
        if (StrUtil.isNotBlank(username)) {
            String userId = httpServletRequest.getHeader("userId");
            String realName = httpServletRequest.getHeader("realName");
            UserInfoDto userInfoDTO = new UserInfoDto(userId, username, realName);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}