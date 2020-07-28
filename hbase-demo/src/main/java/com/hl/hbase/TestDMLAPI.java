package com.hl.hbase;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * 描述: 测试HBase DML API
 * 作者: panhongtong
 * 创建时间: 2020-07-28 15:24
 **/
public class TestDMLAPI {

    private Connection connection;
    private Table table;

    @Test
    public void testAPI() throws IOException {
        //putData("stu", "1005", "info1", "name", "GDX");
        //putData("stu", "1005", "info1", "name", "shit");
        //getData("stu", "1005", null, null);
        //getData("stu", "1005", "info1", null, 0);
        scanData("stu");
    }

    /**
     * 通过scan的方式获取数据
     * @param tableName
     * @throws IOException
     */
    public void scanData(String tableName) throws IOException {
        // 获取表
        table = connection.getTable(TableName.valueOf(tableName));

        // 创建scan对象
        Scan scan = new Scan();

        // 设置列和列簇
        //scan.addFamily();
        //scan.addColumn();

        // 版本设置
        //scan.setMaxVersions();

        // 获取结果并解析
        ResultScanner results = table.getScanner(scan);
        for (Result result : results) {
            for (Cell cell : result.rawCells()) {
                System.out.println(
                        "CF:" + Bytes.toString(CellUtil.cloneFamily(cell)) +
                                ",CN:" + Bytes.toString(CellUtil.cloneQualifier(cell)) +
                                ",Value:" + Bytes.toString(CellUtil.cloneValue(cell))
                );
            }
        }
    }

    /**
     * 通过get的方式获取数据
     * @param tableName
     * @param rowKey
     * @param cf
     * @param cn
     * @param version
     */
    public void getData(String tableName, String rowKey, String cf, String cn, Integer version) throws IOException {
        // 获取表
        table = connection.getTable(TableName.valueOf(tableName));

        // 获取get对象
        Get get = new Get(Bytes.toBytes(rowKey));

        // 设置列簇和列
        if (StringUtils.isNotEmpty(cf) && StringUtils.isNotEmpty(cn)) {
            get.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn));
        } else if (StringUtils.isNotEmpty(cf)) {
            get.addFamily(Bytes.toBytes(cf));
        }

        // 指定版本  这里要设置了存放版本数才行，不然只有一个版本
        if (version != null && version == 0) {
            // 获取当前可获取的所有版本
            get.setMaxVersions();
        } else if (version != null) {
            get.setMaxVersions(version);
        }

        // 执行获得结果
        Result result = table.get(get);

        // 解析并打印结果
        for (Cell cell : result.rawCells()) {
            System.out.println(
                    "CF:" + Bytes.toString(CellUtil.cloneFamily(cell)) +
                            ",CN:" + Bytes.toString(CellUtil.cloneQualifier(cell)) +
                            ",Value:" + Bytes.toString(CellUtil.cloneValue(cell))
            );
        }

    }

    /**
     * put数据
     * @param tableName 表名
     * @param rowKey rowKey
     * @param cf 列簇
     * @param cn 字段名
     * @param value 字段值
     */
    public void putData(String tableName, String rowKey, String cf, String cn, String value) throws IOException {
        // 获取表
        table = connection.getTable(TableName.valueOf(tableName));

        // 因为HBase缓存用的是字节数组，没有String等类型，所以都要转换
        // 获取put对象
        Put put = new Put(Bytes.toBytes(rowKey));
        // 给put对象赋值
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn), Bytes.toBytes(value));

        // put 这里可以放一个put集合，用于批量处理
        table.put(put);

    }

    @Before
    public void before() throws IOException {
        // 配置文件
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", "hadoop103,hadoop104,hadoop105");

        // 建立连接
        connection = ConnectionFactory.createConnection(configuration);
    }

    @After
    public void after() throws IOException {
        // 关闭连接
        connection.close();
        table.close();
    }
}
