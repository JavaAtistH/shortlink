package com.huangkeqin.shortlink.admin.common.enums;

import com.huangkeqin.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_TOKEN_FAIL("A000200", "用户token验证失败"),
    USER_NULL("B00200", "用户不存在"),
    USER_NAME_EXIST("B00201", "用户名已存在"),
    USER_EXIST("B00201", "用户已存在"),
    USER_SAVE_ERROR("B00201", "用户新增失败");
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
