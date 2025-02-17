package com.huanghkeqin.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huanghkeqin.shortlink.project.common.convention.result.Result;
import com.huanghkeqin.shortlink.project.common.convention.result.Results;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.huanghkeqin.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.huanghkeqin.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.huanghkeqin.shortlink.project.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 短链接跳转
     * @param shortUri
     * @param request
     * @param response
     */
    @GetMapping("/{short-uri}")
        public void redirect(@PathVariable("short-uri") String shortUri, HttpServletRequest request, HttpServletResponse response){
         shortLinkService.restoreUrl(shortUri,request,response);
    }
    /**
     * 创建短链接
     * @param requestParam 请求参数
     * @return 完整短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/v1/page")
     public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询短链接分组短链接数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }


}
