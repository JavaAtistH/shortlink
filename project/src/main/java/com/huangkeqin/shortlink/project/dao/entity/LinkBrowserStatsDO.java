package com.huangkeqin.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.huangkeqin.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 浏览器统计访问实体
 */
@Data
@TableName("t_link_browser_stats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkBrowserStatsDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private Date date;

    /**
     * gid
     */

    private String gid;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 浏览器
     */
    private String browser;
}
