package com.huangkeqin.shortlink.admin.controller;

import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.huangkeqin.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接分组控制层
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 新增短链接分组
     * @param requestParam
     */
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> saveGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 查询短链接分组
     * @return
     */
    @GetMapping("/api/short-link/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
       return Results.success(groupService.listGroup());
    }

    /**
     * 修改短链接分组名称
     */
    @PutMapping("/api/short-link/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();
    }
    /**
     * 删除短链接分组
     */
    @DeleteMapping("/api/short-link/v1/group")
    public Result<Void> deleteGroup(@RequestParam("gid") String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     * 短链接分组排序
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/v1/group/sort")
    public Result<Void> sort(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam) {
        groupService.sortGroup(requestParam);
        return Results.success();
    }


}
