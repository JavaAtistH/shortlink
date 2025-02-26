package com.huangkeqin.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huangkeqin.shortlink.project.dao.entity.ShortLinkDO;
import com.huangkeqin.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.huangkeqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.huangkeqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.huangkeqin.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.huangkeqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     * @param shortUri 短链接后缀
     * @param request 请求
     * @param response 响应
     */
    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response);

    /**
     * 短链接统计
     * @param statsRecord
     */
    void shortLinkStats(ShortLinkStatsRecordDTO statsRecord);
}
