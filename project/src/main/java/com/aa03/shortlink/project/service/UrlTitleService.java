package com.aa03.shortlink.project.service;

/**
 * URL标题接口层
 */
public interface UrlTitleService {

    /**
     * 根据URL获取网站标题
     *
     * @param url 目标网站地址
     * @return 网站标题
     */
    String getTitleByUrl(String url);

    /**
     * 根据URL获取网站的图标
     *
     * @param url 目标网站地址
     * @return 网站的图标地址
     */
    String getFaviconByUrl(String url);
}
