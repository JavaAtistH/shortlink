package com.huangkeqin.shortlink.admin.remote.dto.req;

import lombok.Data;

import java.util.Date;

/**
 * 短链接创建请求对象
 */
@Data
public class ShortLinkCreateReqDTO {
    private String domain; // 域名
    private String originUrl; // 原始链接
    private String gid; // 分组标识
    private Integer createdType; // 创建类型 1：控制台 0：接口
    private Integer  validDateType; // 有效期类型 0：永久有效 1：用户自定义
    private Date validDate; // 有效期
    private String describe; // 描述
}
