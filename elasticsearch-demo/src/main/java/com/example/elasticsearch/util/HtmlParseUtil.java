package com.example.elasticsearch.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;

/**
 * 描述: HTML解析工具
 * 作者: panhongtong
 * 创建时间: 2020-09-28 17:43
 **/
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        String url = "https://search.jd.com/Search?keyword=Java";
        Document document = Jsoup.parse(new URL(url), 30000);

        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");

        for (Element el : elements) {

        }

    }
}
