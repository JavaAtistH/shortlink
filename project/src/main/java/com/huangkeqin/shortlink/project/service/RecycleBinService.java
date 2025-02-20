package com.huangkeqin.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huangkeqin.shortlink.project.dao.entity.ShortLinkDO;
import com.huangkeqin.shortlink.project.dto.req.*;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {
    /**
     * 保存到回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询被回收的短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复被回收的短链接
     * @param requestParam 请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);

    /**
     * 删除回收站中的短链接
     * @param requestParam
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
