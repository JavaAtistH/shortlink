package com.huangkeqin.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录接口返回参数响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRespDTO {
    /**
     * 用户token
     */
    private String token;

}
