package com.aa03.shortlink.admin.common.enums;

import com.aa03.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {

    USER_TOKEN_FAIL("A00200", "用户Token验证失败"),
    USER_NULL("B00200", "用户记录不存在"),
    USER_NAME_EXIST("B00201", "用户名已存在"),
    USER_EXIST("B00202", "用户记录已存在"),
    USER_SAVE_ERROR("B00203", "用户记录新增失败"),
    USER_NOT_EXIST("B00204", "用户登录信息错误"),
    USER_AUTHENTICATION_EXPIRED("B00205", "用户登录信息过期"),
    USER_HAS_LOGIN("B00206", "用户已登录"),
    USER_NOT_LOGIN("B00207", "用户未登录");

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
