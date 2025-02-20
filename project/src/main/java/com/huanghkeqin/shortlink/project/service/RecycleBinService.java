package com.huanghkeqin.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huanghkeqin.shortlink.project.dao.entity.ShortLinkDO;
import com.huanghkeqin.shortlink.project.dto.req.RecycleBinSaveReqDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {
    /**
     * 保存到回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);
}
