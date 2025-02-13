package com.huanghkeqin.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanghkeqin.shortlink.project.dao.entity.ShortLinkDO;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;


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

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 获取短链接分组内数量
     * @param requestParam
     * @return 分组内数量
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);
}
