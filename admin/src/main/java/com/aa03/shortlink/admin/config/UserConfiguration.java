package com.aa03.shortlink.admin.config;

import com.aa03.shortlink.admin.common.biz.user.UserFlowRiskControlFilter;
import com.aa03.shortlink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.aa03.shortlink.admin.common.constant.UserConstant.USER_TRANSMIT_FILTER_ORDER;

/**
 * 用户配置自动装配
 */
@Configuration
public class UserConfiguration {

    /**
     * 用户信息传递过滤器
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter() {
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        registration.setOrder(USER_TRANSMIT_FILTER_ORDER);
        return registration;
    }


    /**
     * 用户操作流量风控过滤器
     */
    @Bean
    @ConditionalOnProperty(name = "short-link.flow-limit.enable", havingValue = "true")
    public FilterRegistrationBean<UserFlowRiskControlFilter> globalUserFlowRiskControlFilter(StringRedisTemplate stringRedisTemplate, UserFlowRiskControlConfiguration userFlowRiskControlConfiguration) {
        FilterRegistrationBean<UserFlowRiskControlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserFlowRiskControlFilter(stringRedisTemplate, userFlowRiskControlConfiguration));
        registration.addUrlPatterns("/*");
        registration.setOrder(10);
        return registration;
    }
}