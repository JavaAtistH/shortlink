package com.huangkeqin.shortlink.project.dto.req;

import lombok.Data;

/**
 * 回收站中删除短链接
 */
@Data
public class RecycleBinRemoveReqDTO {
    /**
     * 分组id
     */
    private String gid;
    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
