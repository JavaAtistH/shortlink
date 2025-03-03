package com.huangkeqin.shortlink.admin.remote;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.remote.dto.req.*;
import com.huangkeqin.shortlink.admin.remote.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接远程调用中台服务
 */
@FeignClient("short-link-project")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接的方法
     * 该方法通过HTTP POST请求向指定服务端点发送创建短链接的请求，并解析响应结果
     * 使用默认方法(default method)允许接口提供一个默认实现
     *
     * @param requestParam 短链接创建请求参数对象，包含创建短链接所需的信息
     * @return ShortLinkCreateRespDTO 短链接创建响应对象，包含创建后的短链接信息
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 修改短链接
     * @param requestParam 短链接修改请求参数对象，包含修改短链接所需的信息
     */
    @PostMapping("/api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam);


    /**
     * 根据请求参数分页查询短链接信息
     * 此方法使用默认方法特性，允许在接口中定义默认实现
     * 主要用途是当直接使用 HTTP 请求从外部服务获取分页数据时，减少实现类的冗余代码
     *
     * 如groupId (gid), 当前页码 (current) 和每页大小 (size)
     * @return 返回一个包含分页响应对象的Result对象，泛型为ShortLinkPageRespDTO
     * 包含有关短链接页面查询结果的详细信息
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestParam("gid") String gid,
                                                     @RequestParam("orderTag") String orderTag,
                                                     @RequestParam("current") Long current,
                                                     @RequestParam("size") Long size);


    /**
     * 查询分组短链接总量
     *
     * @param requestParam
     * @return
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> linkGroupShortLinkCount(@RequestParam("requestParam")List<String> requestParam);

    /**
     * 根据URL获取标题
     * @param url 目标网址URL
     * @return 网站标题
     */
    @GetMapping("/api/short-link/v1/title")
     Result<String> getTitleByUrl(@RequestParam("url") String url);


    /**
     * 回收短链接功能
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam);

    /**
     * 从回收站恢复短链接功能
     * @param requestParam 短链接恢复请求参数对象，包含恢复短链接所需的信息
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam);

    /**
     * 从回收站移除短链接功能
     * @param requestParam
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
     void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam);

    /**
     * 分页查询已被回收的短链接
     * @param
     * @return
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@RequestParam("gidList") List<String> gidList,
                                                                 @RequestParam("current") Long current,
                                                                 @RequestParam("size") Long size);

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     * @param
     * @return
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
     Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestParam("fullShortUrl") String fullShortUrl,
                                                                                 @RequestParam("gid") String gid,
                                                                                 @RequestParam("startDate") String startDate,
                                                                                 @RequestParam("endDate") String endDate,
                                                                                 @RequestParam("enableStatus") Integer enableStatus,
                                                                                 @RequestParam("current") Long current,
                                                                                 @RequestParam("size") Long size);
    /**
     * 访问分组短链接指定时间内监控访问记录数据
     *
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     */
    @GetMapping("/api/short-link/v1/stats")
     Result<ShortLinkStatsRespDTO> oneShortLinkStats(@RequestParam("fullShortUrl") String fullShortUrl,
                                                     @RequestParam("gid") String gid,
                                                     @RequestParam("enableStatus") Integer enableStatus,
                                                     @RequestParam("startDate") String startDate,
                                                     @RequestParam("endDate") String endDate);




}
