package com.huangkeqin.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.huangkeqin.shortlink.admin.common.convention.result.Result;
import com.huangkeqin.shortlink.admin.common.convention.result.Results;
import com.huangkeqin.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.huangkeqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserActualRespDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserRespDTO;
import com.huangkeqin.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username){
      return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 获取真实的用户信息
     * @param username
     * @return
     */

    @GetMapping("/api/short-link/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username){
        //使用 BeanUtil.toBean 将查询到的 UserRespDTO 对象转换为 UserActualRespDTO 对象。
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username),UserActualRespDTO.class));
    }

    /**
     * 查询用户名是否存在
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username")String username){
        return Results.success(userService.hasUsername(username));
    }

    /**
     * 注册用户
     * @param requestParam
     * @return
     */
    @PostMapping("/api/short-link/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam) {
            userService.register(requestParam);
            return Results.success();
    }
}
