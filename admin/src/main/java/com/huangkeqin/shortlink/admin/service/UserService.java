package com.huangkeqin.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huangkeqin.shortlink.admin.dao.entity.UserDO;
import com.huangkeqin.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.huangkeqin.shortlink.admin.dto.resp.UserRespDTO;


/**
 * 用户接口层
 */

public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param userName 用户名
     * @return
     */
    UserRespDTO getUserByUsername(String userName);

    /**
     * 查询用户名是否存在
     * @param username
     * @return
     */
    Boolean hasUsername(String username);

    /**
     * 注册用户
     * @param requestParam
     */
    void register(UserRegisterReqDTO requestParam);
}
