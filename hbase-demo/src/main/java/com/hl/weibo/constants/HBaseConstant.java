package com.hl.weibo.constants;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * 描述: HBase常量类
 * 作者: panhongtong
 * 创建时间: 2020-07-31 16:47
 **/
public class HBaseConstant {

    /**
     * HBASE配置
     */
    public static final Configuration CONFIGURATION = HBaseConfiguration.create();

    /**
     * 命名空间名
     */
    public static final String NAMESPACE = "weibo";

    /**
     * 微博内容表
     */
    public static final String CONTENT_TABLE = "weibo:content";
    /**
     * 内容信息
     */
    public static final String CONTENT_TABLE_CF = "info";
    public static final int CONTENT_TABLE_VERSION = 1;

    /**
     * 微博内容表
     */
    public static final String RELATION_TABLE = "weibo:relation";
    /**
     * 关注
     */
    public static final String RELATION_TABLE_CF1 = "weibo:attends";
    /**
     * 粉丝
     */
    public static final String RELATION_TABLE_CF2 = "weibo:fans";
    public static final int RELATION_TABLE_VERSION = 1;

    /**
     * 收件表
     */
    public static final String INBOX_TABLE = "weibo:inbox";
    /**
     * 收件信息
     */
    public static final String INBOX_TABLE_CF = "info";
    public static final int INBOX_TABLE_VERSION = 2;

}
