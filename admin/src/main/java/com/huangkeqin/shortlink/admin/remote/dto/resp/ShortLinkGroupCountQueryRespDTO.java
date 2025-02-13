package com.huangkeqin.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询数量返回对象
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 分组下的短链接数量
     */
    private Integer shortLinkCount;

}
