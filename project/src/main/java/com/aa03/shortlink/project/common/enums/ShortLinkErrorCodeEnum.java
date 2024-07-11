package com.aa03.shortlink.project.common.enums;

import com.aa03.shortlink.project.common.convention.errorcode.IErrorCode;

public enum ShortLinkErrorCodeEnum implements IErrorCode {


    SHORT_LINK_GENERATE_ERROR("A100100", "短链接生成错误"),
    SHORT_LINK_GENERATE_FREQUENTLY("A100101", "短链接生成频繁,请稍后再试"),
    SHORT_LINK_GENERATE_REPEAT("A100102", "短链接生成重复"),
    SHORT_LINK_NOT_EXIST("A100200", "短链接不存在");

    private final String code;

    private final String message;

    ShortLinkErrorCodeEnum(String code, String message) {
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
