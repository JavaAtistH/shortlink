package com.huangkeqin.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import com.huangkeqin.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.huangkeqin.shortlink.admin.remote.ShortLinkRemoteService;
import com.huangkeqin.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.huangkeqin.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.huangkeqin.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.huangkeqin.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控数据
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;



    /**
     * 访问单个短链接指定时间内监控数据
     *
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam){
        return shortLinkActualRemoteService.oneShortLinkStats(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getEnableStatus(),
                requestParam.getStartDate(),
                requestParam.getEndDate());
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getEnableStatus(),
                requestParam.getCurrent(),
                requestParam.getSize());
    }

}
