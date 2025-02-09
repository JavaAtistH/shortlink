package com.huangkeqin.shortlink.admin.controller;

import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import com.huangkeqin.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.huangkeqin.shortlink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}
