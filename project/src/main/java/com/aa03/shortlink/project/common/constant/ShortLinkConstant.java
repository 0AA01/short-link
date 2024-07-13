package com.aa03.shortlink.project.common.constant;

/**
 * 短链接常量类
 */
public class ShortLinkConstant {

    /**
     * 永久短链接默认过期时间
     * 2626560000L毫秒 == 1月
     */
    public static final long DEFAULT_CACHE_VALID_TIME = 2626560000L;

    /**
     * 高德获取地区接口
     */
    public static final String AMAP_REMOTE_URL = "https://restapi.amap.com/v3/ip";
}
