package com.huanghkeqin.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huanghkeqin.shortlink.project.dao.entity.ShortLinkDO;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;


/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 创建短链接
     * @param requestParam 创建短链接传入参数
     * @return
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);
}
