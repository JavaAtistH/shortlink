package com.huangkeqin.shortlink.admin.dto.resp;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huangkeqin.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.Data;

/**
 * 用户返回参数响应
 * @author huangkeqin
 * @date
 */
@Data
public class UserActualRespDTO {
    /**
     * 用户ID，唯一标识一个用户
     */
    private Integer id;
    /**
     * 用户名，用户登录时使用
     */
    private String username;
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
    /**
     * 删除时间戳，如果用户被软删除，记录删除时间
     */


}
