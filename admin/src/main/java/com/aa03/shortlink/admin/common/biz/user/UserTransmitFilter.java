package com.aa03.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;

import static com.aa03.shortlink.admin.common.constant.RedisCacheConstant.USER_TOKEN_LOGIN_KEY;
import static com.aa03.shortlink.admin.common.constant.UserConstant.USER_NAME_KEY;
import static com.aa03.shortlink.admin.common.constant.UserConstant.USER_TOKEN;

/**
 * 用户信息传输过滤器
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String username = httpServletRequest.getHeader(USER_NAME_KEY);
        String token = httpServletRequest.getHeader(USER_TOKEN);
        String key = USER_TOKEN_LOGIN_KEY + username;
        Object userInfoJsonStr = stringRedisTemplate.opsForHash().get(key, token);
        if (userInfoJsonStr != null) {
            UserInfoDto userInfoDto = JSON.parseObject(userInfoJsonStr.toString(), UserInfoDto.class);
            UserContext.setUser(userInfoDto);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }
}