package com.huangkeqin.shortlink.admin.dto.req;


import lombok.Data;

/**
 * 短链接分组创建参数
 */
@Data
public class ShortLinkGroupSortReqDTO {
    /**
     * 分组id
     */
    private String gid;

    /**
     * 排序
     */
    private Integer sortOrder;
}
