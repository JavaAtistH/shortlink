package com.huangkeqin.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import com.huangkeqin.shortlink.admin.remote.ShortLinkRemoteService;
import com.huangkeqin.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.huangkeqin.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.huangkeqin.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.huangkeqin.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.huangkeqin.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接后管控制层
 */
@RestController
public class ShortLinkController {
    //TODO 后续重构为SpringCloud Feign调用
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 创建短链接
     * @param requestParam 请求参数
     * @return 完整短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }
    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
