package com.huangkeqin.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * 短链接分页查询参数
 */
@Data
//为什么这里不需要继承Page<ShortLinkDO>而是直接继承Page
public class ShortLinkPageReqDTO extends Page{//继承Page<ShortLinkDO>，里面还有current size 两个属性
    /**
     * 分组标识
     */
    private String gid;
}
