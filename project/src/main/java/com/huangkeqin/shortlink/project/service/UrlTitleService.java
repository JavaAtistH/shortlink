package com.huangkeqin.shortlink.project.service;

/**
 * URL标题接口层
 */
public interface UrlTitleService {

    /**
     * 根据URL获取标题
     * @param url 目标网址URL
     * @return 网站标题
     */
    String getTitleByUrl(String url);
}
