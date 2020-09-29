package com.example.elasticsearch.util;

import com.example.elasticsearch.model.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述: HTML解析工具
 * 作者: panhongtong
 * 创建时间: 2020-09-28 17:43
 **/
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        parseJD("樊文花").forEach(System.out::println);

    }

    public static List<Content> parseJD(String keywords) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keywords;
        Document document = Jsoup.parse(new URL(url), 30000);

        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");

        List<Content> result = new ArrayList<>();

        for (Element el : elements) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            result.add(new Content(title, img, price));

        }

        return result;
    }
}
