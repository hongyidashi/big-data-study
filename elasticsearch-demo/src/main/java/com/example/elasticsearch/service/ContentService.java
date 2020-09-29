package com.example.elasticsearch.service;

import cn.hutool.json.JSONUtil;
import com.example.elasticsearch.model.Content;
import com.example.elasticsearch.util.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 描述:
 * 作者: panhongtong
 * 创建时间: 2020-09-29 10:16
 **/
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient client;

    public Boolean parseContent(String keyword) throws IOException {
        List<Content> contents = HtmlParseUtil.parseJD(keyword);

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("3m");
        for (int i = 0; i < contents.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods").source(JSONUtil.toJsonStr(contents.get(i)), XContentType.JSON));
        }

        BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);

        return !bulk.hasFailures();
    }

    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 分页
        sourceBuilder.from(pageNo).size(pageSize);

        // 精确匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQuery);
        sourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<h2>");
        highlightBuilder.postTags("</h2>");
        sourceBuilder.highlighter(highlightBuilder);

        // 执行查询
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 解析结果
        List<Map<String, Object>> resultSet = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            // 解析高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            // 将原来的字段替换为高亮字段
            if (title != null) {
                Text[] fragments = title.fragments();
                StringBuilder newTitle = new StringBuilder();
                for (Text text : fragments) {
                    newTitle.append(text);
                }
                sourceAsMap.put("title", newTitle.toString());
            }
            resultSet.add(sourceAsMap);
        }

        return resultSet;
    }
}
