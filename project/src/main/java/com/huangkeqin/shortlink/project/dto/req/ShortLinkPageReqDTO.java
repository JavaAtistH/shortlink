package com.huangkeqin.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huangkeqin.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;

/**
 * 短链接分页查询参数
 */
@Data
public class ShortLinkPageReqDTO extends Page<ShortLinkDO>{//继承Page<ShortLinkDO>，里面还有current size 两个属性
    /**
     * 分组标识
     */
    private String gid;
}
