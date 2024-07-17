package com.aa03.shortlink.admin.common.constant;

/**
 * 短链接后管 Redis 缓存常量类
 */
public class RedisCacheConstant {

    /**
     * 用户注册分布式锁
     */
    public static final String LOCK_USER_REGISTER_KEY = "shor-link:lock_user:register:";

    /**
     * 用户TOKEN前缀KEY
     */
    public static final String USER_TOKEN_LOGIN_KEY = "shor-link:lock_user:token:login:";

    /**
     * 分组创建分布式锁
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock_group-create:%s";
}
