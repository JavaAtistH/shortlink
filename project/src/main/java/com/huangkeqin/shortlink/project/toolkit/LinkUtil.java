package com.huangkeqin.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;
import static com.huangkeqin.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;
/**
 * 短链接工具类
 */
public class LinkUtil {
    /**
     * 获取短链接有效期
     * @param validDate
     * @return
     */
    public static long getLinkCacheValidDate(Date validDate){
        return Optional.ofNullable(validDate).
                map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);

    }
}
