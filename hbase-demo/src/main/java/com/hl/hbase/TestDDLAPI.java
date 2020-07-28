package com.hl.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * 描述: 测试HBase DDL API
 * 作者: panhongtong
 * 创建时间: 2020-07-28 11:36
 **/
public class TestDDLAPI {

    private Admin admin;
    private Connection connection;

    @Test
    public void testAPI() throws Exception {
        //createTable("testCreateAPI","testCreateNS","info1","info2");
        //deleteTable("testCreateAPI");
        //createNameSpace("testCreateNS");
        //deleteNameSpace("testCreateNS", true);
    }

    //------------------------------------ DDL 操作 ------------------------------------
    /**
     * 删除命名空间
     * @param nameSpace    删除命名空间名
     * @param isDeletTable 如果命名空间有表是否把表也删除
     * @throws IOException
     */
    public void deleteNameSpace(String nameSpace, boolean isDeletTable) throws IOException {
        try {
            admin.getNamespaceDescriptor(nameSpace);
        } catch (NamespaceNotFoundException e) {
            System.out.println(nameSpace + "命名空间不存在");
            return;
        }

        // 要先判断是否有表且有表的情况下是否删除
        TableName[] tableNames = admin.listTableNamesByNamespace(nameSpace);
        if (tableNames.length > 0 && !isDeletTable) {
            System.out.println(nameSpace + "命名空间下有表，不能删除");
            return;
        }

        // 删除表
        for (TableName tableName : tableNames) {
            deleteTable(tableName.getNameAsString());
        }

        admin.deleteNamespace(nameSpace);

        try {
            admin.getNamespaceDescriptor(nameSpace);
        } catch (NamespaceNotFoundException e) {
            System.out.println("命名空间删除成功");
            return;
        }
        System.out.println("命名空间删除失败");
    }

    /**
     * 创建命名空间
     *
     * @param nameSpace 命名空间名
     */
    public void createNameSpace(String nameSpace) throws IOException {
        // 创建命名空间描述器
        NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(nameSpace).build();

        // 创建命名空间
        try {
            admin.createNamespace(namespaceDescriptor);
        } catch (NamespaceExistException e) {
            System.out.println(nameSpace + "已存在，创建失败");
            return;
        }
        System.out.println("命名空间创建成功");

    }

    /**
     * 删除表
     *
     * @param tableName 表名
     */
    public void deleteTable(String tableName) throws IOException {
        // 判断表是否存在
        if (!tableIsExists(tableName)) {
            System.out.println("该表不存");
            return;
        }

        // 停用表
        admin.disableTables(tableName);

        // 删除表
        admin.deleteTable(TableName.valueOf(tableName));

        if (tableIsExists(tableName)) {
            System.out.println(tableName + "删除失败");
        } else {
            System.out.println(tableName + "删除成功");
        }
    }

    /**
     * 创建表
     *
     * @param tableName 表名
     * @param nameSpace 命名空间
     * @param cfs       列簇信息
     */
    public void createTable(String tableName, String nameSpace, String... cfs) throws IOException {
        // 确定表命名空间
        if (nameSpace != null) {
            tableName = nameSpace + ":" + tableName;
        }

        // 校验
        if (cfs == null) {
            System.out.println("无列簇信息，创建失败");
            return;
        }
        if (tableIsExists(tableName)) {
            System.out.println("表已存在");
            return;
        }

        // 判断命名空间是否存在，如果不存在则创建
        try {
            admin.getNamespaceDescriptor(nameSpace);
        } catch (NamespaceNotFoundException e) {
            createNameSpace(nameSpace);
        }

        // 创建描述器
        HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));

        // 添加列簇信息
        for (String cf : cfs) {
            // 创建列簇描述器
            HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(cf);
            // 指定列族的版本个数，默认个数是 1
            // hColumnDescriptor.setMaxVersions(3);
            // 添加列簇
            hTableDescriptor.addFamily(hColumnDescriptor);
        }

        // 创建表
        admin.createTable(hTableDescriptor);

        boolean exists = tableIsExists(tableName);

        if (exists) {
            System.out.println("表创建成功");
        } else {
            System.out.println("表创建失败");
        }

    }

    /**
     * 判断表是否存在
     *
     * @param tableName 表名
     */
    public boolean tableIsExists(String tableName) throws IOException {
        boolean exists = admin.tableExists(TableName.valueOf(tableName));
        System.out.println("表" + tableName + "是否存在：" + exists);
        return exists;
    }

    //------------------------------------ 前置 操作 ------------------------------------

    @Before
    public void before() throws IOException {
        // 配置文件
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hadoop103,hadoop104,hadoop105");

        // 建立连接
        connection = ConnectionFactory.createConnection(configuration);

        // 创建客户端
        admin = connection.getAdmin();
    }

    @After
    public void after() throws IOException {
        // 关闭
        connection.close();
        admin.close();
    }
}
