package com.aa03.shortlink.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 跳转域名白名单配置文件
 */
@Data
@Component
@ConfigurationProperties(prefix = "short-link.flow-limit")
public class UserFlowRiskControlConfiguration {

    /**
     * 是否开启短链接后管流量风控
     */
    private Boolean enable;

    /**
     * 流量风控时间窗口，单位：秒
     */
    private String timeWindow;

    /**
     * 流量风控时间窗口内可访问次数
     */
    private Long maxAccessCount;
}
