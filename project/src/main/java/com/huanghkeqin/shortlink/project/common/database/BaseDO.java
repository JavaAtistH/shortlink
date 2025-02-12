package com.huanghkeqin.shortlink.project.common.database;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;


/**
 * 数据库对象基础数据，用于记录创建时间和更新时间，以及删除标志
 */
@Data
public class BaseDO {
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


