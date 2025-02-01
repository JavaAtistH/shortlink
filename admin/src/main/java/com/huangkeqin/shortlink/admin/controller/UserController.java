package com.huangkeqin.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import com.huangkeqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.huangkeqin.shortlink.admin.dto.resp.UserActualRespDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserRespDTO;
import com.huangkeqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 *
 * */
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * 根据用户名查询用户信息
     * */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){
      return Results.success(userService.getUserByUsername(username));
    }

    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username){
        //使用 BeanUtil.toBean 将查询到的 UserRespDTO 对象转换为 UserActualRespDTO 对象。
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username),UserActualRespDTO.class));
    }
}
