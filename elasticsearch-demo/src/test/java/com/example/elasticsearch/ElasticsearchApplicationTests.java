package com.example.elasticsearch;

import cn.hutool.json.JSONUtil;
import com.example.elasticsearch.model.Dog;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class ElasticsearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void testCreateIndex() throws IOException {
        // 创建请求
        CreateIndexRequest request = new CreateIndexRequest("mitsuha02");
        // 执行请求
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    void testAddDocument() throws IOException {
        Dog dog = new Dog("大福", 3);
        IndexRequest request = new IndexRequest("mitsuha02");

        request.id("1");
        request.source(JSONUtil.toJsonStr(dog), XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

    @Test
    void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("mitsuha02", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    void testBulkRequest() throws IOException {
        BulkRequest request = new BulkRequest();
        List<Dog> dogList = new ArrayList<>();

        dogList.add(new Dog("断腿少女2", 2));
        dogList.add(new Dog("断腿少女3", 3));
        dogList.add(new Dog("断腿少女4", 4));
        dogList.add(new Dog("断腿少女5", 5));
        dogList.add(new Dog("mitsuha", 22));

        for (Dog dog : dogList) {
            request.add(new IndexRequest("mitsuha02").source(JSONUtil.toJsonStr(dog), XContentType.JSON));
        }

        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

}
