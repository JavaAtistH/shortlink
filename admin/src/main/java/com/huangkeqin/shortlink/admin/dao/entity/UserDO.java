package com.huangkeqin.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huangkeqin.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author huangkeqin
 * @date 2018/4/19
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * UserDO类代表用户实体，用于在数据库中存储和操作用户相关数据
 */
@TableName("t_user")
public class UserDO {
    /**
     * 用户ID，唯一标识一个用户
     */
    private Long id;
    /**
     * 用户名，用户登录时使用
     */
    private String username;
    /**
     * 密码，与用户名一起用于身份验证
     */
    private String password;
    /**
     * 真实姓名，用户的真实姓名
     */
    private String realName;
    /**
     * 电话号码，用户的联系方式
     */
    private String phone;
    /**
     * 邮箱地址，用于用户注册、找回密码等
     */
    private String mail;
    /**
     * 删除时间戳，如果用户被软删除，记录删除时间
     */
    private Long deletionTime;
    /**
     * 创建时间，记录用户何时被创建
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 更新时间，记录用户信息最后一次更新的时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 删除标志，用于软删除用户，0表示未删除，1表示已删除
     */
    @TableField(fill = FieldFill.INSERT)
    private int delFlag;


}
