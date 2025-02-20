package com.huanghkeqin.shortlink.project.dto.req;

import lombok.Data;

@Data
public class RecycleBinSaveReqDTO {
    /**
     * 分组id
     */
    private String gid;
    /**
     * 完整短链接
     */
    private String fullShortUrl;

}
