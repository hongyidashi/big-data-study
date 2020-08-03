package com.hl.weibo.utils;

import com.hl.weibo.constants.HBaseConstant;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 描述: hbase工具类，用于创建命名空间、表。
 * 作者: panhongtong
 * 创建时间: 2020-07-31 16:45
 **/
public class HBaseUtil {

    private final static Logger logger = LoggerFactory.getLogger(HBaseUtil.class);

    /**
     * 创建命名空间
     *
     * @param nameSpace 命名空间名
     */
    public static boolean createNameSpace(String nameSpace) throws IOException {
        // 创建连接
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 获取admin对象
        Admin admin = connection.getAdmin();

        // 创建命名空间描述器
        NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(nameSpace).build();

        // 创建命名空间
        try {
            admin.createNamespace(namespaceDescriptor);
        } catch (NamespaceExistException e) {
            logger.error(nameSpace + "已存在，创建失败");

            return false;
        } finally {
            admin.close();
            connection.close();
        }
        logger.info("命名空间创建成功");
        return true;
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param nameSpace 命名空间
     * @param cfs       列簇信息
     */
    public static boolean createTable(String tableName, String nameSpace, int version, String... cfs) throws IOException {
        // 确定表命名空间
        if (nameSpace != null) {
            tableName = nameSpace + ":" + tableName;
        }

        // 校验
        if (cfs == null) {
            logger.error("无列簇信息，创建失败");
            return false;
        }
        if (tableIsExists(tableName)) {
            logger.error("表已存在");
            return false;
        }
        if (version <= 0) {
            logger.error("版本不能小于等于0");
            return false;
        }

        // 创建连接
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 获取admin对象
        Admin admin = connection.getAdmin();

        // 判断命名空间是否存在，如果不存在则创建
        try {
            admin.getNamespaceDescriptor(nameSpace);
        } catch (NamespaceNotFoundException e) {
            logger.info("命名空间不存在，创建命名空间");
            createNameSpace(nameSpace);
        }

        // 创建描述器
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));

        // 添加列簇信息
        for (String cf : cfs) {
            // 创建列簇描述器
            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
            // 指定列族的版本个数，默认个数是 1
            hColumnDescriptor.setMaxVersions(version);
            // 添加列簇
            hTableDescriptor.addFamily(hColumnDescriptor);
        }

        try {
            // 创建表
            admin.createTable(hTableDescriptor);
        } finally {
            admin.close();
            connection.close();
        }

        boolean exists = tableIsExists(tableName);

        if (exists) {
            logger.info(tableName + "表创建成功");
            return true;
        } else {
            logger.error(tableName + "表创建失败");
            return false;
        }
    }

    /**
     * 判断表是否存在
     *
     * @param tableName 表名
     */
    private static boolean tableIsExists(String tableName) throws IOException {
        // 创建连接
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 获取admin对象
        Admin admin = connection.getAdmin();

        boolean exists = false;
        try {
            exists = admin.tableExists(TableName.valueOf(tableName));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            admin.close();
            connection.close();
        }

        logger.info("表" + tableName + "是否存在：" + exists);
        return exists;
    }
}
