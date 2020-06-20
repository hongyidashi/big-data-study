package com.hl.hfdsclient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

/**
 * 描述: 测试Hadoopclient
 * 作者: panhongtong
 * 创建时间: 2020-06-19 16:56
 **/
public class HDFSClient {

    @Test
    public void put() throws IOException, InterruptedException {
        // 就是core-site.xml里面的键值对
        Configuration configuration = new Configuration();

        // 参数：服务器路径，配置，用户名
        // hdfs：是schema（模式/协议
        FileSystem fileSystem = FileSystem.get(URI.create("hdfs://hadoop103:9000"), configuration, "root");
        fileSystem.copyFromLocalFile(new Path("/Users/panhongtong/mydir/test.txt"), new Path("/"));
        fileSystem.close();
    }
}
