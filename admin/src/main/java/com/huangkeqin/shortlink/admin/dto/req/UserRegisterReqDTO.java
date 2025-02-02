package com.huangkeqin.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {
    /**
     * 用户名，用户登录时使用
     */
    private String username;
    /**
     * 密码，与用户名一起用于身份验证
     */
    private String password;
    /**
     * 真实姓名，用户的真实姓名
     */
    private String realName;
    /**
     * 电话号码，用户的联系方式
     */
    private String phone;
    /**
     * 邮箱地址，用于用户注册、找回密码等
     */
    private String mail;
}
