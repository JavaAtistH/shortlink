package com.huangkeqin.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huangkeqin.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_link")
public class ShortLinkDO extends BaseDO {
    private Long id; // ID
    private String domain; // 域名
    private String shortUri; // 短链接
    private String fullShortUrl; // 完整短链接
    private String originUrl; // 原始链接
    private Integer clickNum; // 点击量
    private String gid; // 分组标识
    /**
     * 网站图标
     */
    private String favicon;
    private Integer enableStatus; // 启用标识 0：未启用 1：已启用
    private Integer createdType; // 创建类型 0：控制台 1：接口
    private Integer  validDateType; // 有效期类型 0：永久有效 1：用户自定义
    private Date validDate; // 有效期
    //describe 是 MySQL 关键字，导致 SQL 解析出错
    @TableField("`describe`")
    private String describe; // 描述
}
