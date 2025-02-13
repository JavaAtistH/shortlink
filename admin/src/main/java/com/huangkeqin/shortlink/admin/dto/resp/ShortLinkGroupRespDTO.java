package com.huangkeqin.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 短链接分组响应实体对象
 */
@Data
public class ShortLinkGroupRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
