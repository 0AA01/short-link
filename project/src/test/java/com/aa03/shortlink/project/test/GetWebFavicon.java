package com.aa03.shortlink.project.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;

public class GetWebFavicon {

    public static String getFaviconUrl(String websiteUrl) {
        try {
            // 连接到网站并解析HTML
            Document doc = Jsoup.connect(websiteUrl).get();

            // 尝试从<link>标签中获取favicon
            Element faviconElement = doc.select("link[rel~=(?i)^(shortcut|icon|shortcut icon)$]").first();

            if (faviconElement != null) {
                String faviconUrl = faviconElement.attr("href");

                // 检查favicon URL是否是相对路径
                if (!faviconUrl.startsWith("http")) {
                    URL url = new URL(websiteUrl);
                    String baseUrl = url.getProtocol() + "://" + url.getHost();
                    faviconUrl = baseUrl + faviconUrl;
                }

                return faviconUrl;
            }

            // 如果没有找到favicon，返回null或默认值
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String url = "http://www.baidu.com";
        String faviconUrl = getFaviconUrl(url);
        System.out.println(faviconUrl);
    }
}
