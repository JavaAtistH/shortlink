package com.huangkeqin.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 回收站恢复请求参数
 */
@Data
public class RecycleBinRecoverReqDTO {
    /**
     * 分组id
     */
    private String gid;
    /**
     * 完整短链接
     */
    private String fullShortUrl;

}
