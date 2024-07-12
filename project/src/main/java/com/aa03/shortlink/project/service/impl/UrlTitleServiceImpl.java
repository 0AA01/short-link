package com.aa03.shortlink.project.service.impl;

import com.aa03.shortlink.project.common.convention.exception.ClientException;
import com.aa03.shortlink.project.common.convention.exception.ServiceException;
import com.aa03.shortlink.project.service.UrlTitleService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;

/**
 * URL标题接口实现层
 */
@Slf4j
@Service
public class UrlTitleServiceImpl implements UrlTitleService {
    @Override
    public String getTitleByUrl(String url) {
        try {
            // 连接到网站并解析HTML
            Document doc = Jsoup.connect(url).get();
            // 获取网页标题
            return doc.title();
        } catch (IOException e) {
//            log.warn("获取网站标题失败");
            return null;
        }
    }

    @Override
    public String getFaviconByUrl(String url) {
        try {
            // 连接到网站并解析HTML
            Document doc = Jsoup.connect(url).get();

            // 尝试从<link>标签中获取favicon
            Element faviconElement = doc.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]").first();

            if (faviconElement != null) {
                String faviconUrl = faviconElement.attr("href");

                // 检查favicon URL是否是相对路径
                if (!faviconUrl.startsWith("http")) {
                    URL targetUrl = new URL(url);
                    String baseUrl = targetUrl.getProtocol() + "://" + targetUrl.getHost();
                    faviconUrl = baseUrl + faviconUrl;
                }
                return faviconUrl;
            }
            // 如果没有找到favicon，返回null或默认值
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
