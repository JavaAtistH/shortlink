package com.huangkeqin.shortlink.admin.remote;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.remote.dto.req.*;
import com.huangkeqin.shortlink.admin.remote.dto.resp.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {
    /**
     * 创建短链接的方法
     * 该方法通过HTTP POST请求向指定服务端点发送创建短链接的请求，并解析响应结果
     * 使用默认方法(default method)允许接口提供一个默认实现
     *
     * @param requestParam 短链接创建请求参数对象，包含创建短链接所需的信息
     * @return ShortLinkCreateRespDTO 短链接创建响应对象，包含创建后的短链接信息
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 将请求参数转换为JSON字符串形式，以便通过HTTP POST请求发送
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        // 解析响应字符串为指定类型的对象，以获取短链接创建结果
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 根据请求参数分页查询短链接信息
     * 此方法使用默认方法特性，允许在接口中定义默认实现
     * 主要用途是当直接使用 HTTP 请求从外部服务获取分页数据时，减少实现类的冗余代码
     *
     * @param requestParam 包含分页查询的请求参数，如groupId (gid), 当前页码 (current) 和每页大小 (size)
     * @return 返回一个包含分页响应对象的Result对象，泛型为ShortLinkPageRespDTO
     * 包含有关短链接页面查询结果的详细信息
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        // 初始化一个HashMap来存储请求参数，以便于HTTP请求中使用
        Map<String, Object> resultMap = new HashMap<>();
        // 将groupId (gid), 当前页码 (current) 和每页大小 (size) 放入结果映射中
        resultMap.put("gid", requestParam.getGid());
        resultMap.put("current", requestParam.getCurrent());
        resultMap.put("size", requestParam.getSize());
        // 使用HttpUtil工具类发送GET请求到指定URL，并携带请求参数，获取分页查询的结果
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", resultMap);
        // 使用FastJSON解析HTTP响应字符串，转换为指定类型的对象
        // 这里使用匿名内部类的方式指定类型引用，以解析复杂的泛型结构
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 查询分组短链接总量
     *
     * @param requestParam
     * @return
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> linkGroupShortLinkCount(List<String> requestParam) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("requestParam", requestParam);
        String resultCountQueryStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", resultMap);
        return JSON.parseObject(resultCountQueryStr, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     * @param requestParam 短链接修改请求参数对象，包含修改短链接所需的信息
     */
    default void updateShortLink(ShortLinkUpdateReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
    };


    /**
     * 根据URL获取标题
     * @param url 目标网址URL
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(@RequestParam("url") String url){
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    /**
     * 回收短链接功能
     * @param requestParam
     * @return
     */
    default void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
    }

    /**
     * 分页查询已被回收的短链接
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        // 初始化一个HashMap来存储请求参数，以便于HTTP请求中使用
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("gidList", requestParam.getGidList());
        // 将groupId (gid), 当前页码 (current) 和每页大小 (size) 放入结果映射中
        resultMap.put("current", requestParam.getCurrent());
        resultMap.put("size", requestParam.getSize());
        // 使用HttpUtil工具类发送GET请求到指定URL，并携带请求参数，获取分页查询的结果
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", resultMap);
        // 使用FastJSON解析HTTP响应字符串，转换为指定类型的对象
        // 这里使用匿名内部类的方式指定类型引用，以解析复杂的泛型结构
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }


    /**
     * 从回收站恢复短链接功能
     * @param requestParam 短链接恢复请求参数对象，包含恢复短链接所需的信息
     */
   default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam){
       HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
   }

    /**
     * 从回收站移除短链接功能
     * @param requestParam
     */
   default void removeRecycleBin(RecycleBinRemoveReqDTO requestParam){
       HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
   }

    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam){
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     * @param requestParam
     * @return
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {});
    }
}
