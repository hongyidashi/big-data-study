package com.hl.weibo.dao;

import com.hl.weibo.constants.HBaseConstant;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 * 1. 发布微博
 * 2. 删除微博（不做）
 * 3. 关注用户
 * 4. 取关用户
 * 5. 获取用户微博详情
 * 6. 获取用户初始化页面
 * 作者: panhongtong
 * 创建时间: 2020-08-03 11:08
 **/
public class HBaseDao {

    private final static Logger logger = LoggerFactory.getLogger(HBaseDao.class);

    /**
     * 发布微博
     *
     * @param uid     用户id
     * @param content 内容
     */
    public static void publishWeibo(String uid, String content) throws IOException {
        // 获取连接
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 操作微博内容表，发布一条微博
        // 1. 获取内容表对象
        Table contTable = connection.getTable(TableName.valueOf(HBaseConstant.CONTENT_TABLE));

        // 2. 构建RowKey uid + 时间戳
        String rowKey = uid + System.currentTimeMillis();

        // 3. 创建put对象
        Put contPut = new Put(Bytes.toBytes(rowKey));

        // 4. 为put对象赋值
        contPut.addColumn(Bytes.toBytes(HBaseConstant.CONTENT_TABLE_CF), Bytes.toBytes("content"), Bytes.toBytes(content));

        // 5. 执行插入数据
        contTable.put(contPut);

        // 操作收件箱表，提醒fans
        // 1. 获取关系表对象
        Table relaTable = connection.getTable(TableName.valueOf(HBaseConstant.RELATION_TABLE));

        // 2. 获取这个用户的fans列
        Get get = new Get(Bytes.toBytes(uid));
        get.addFamily(Bytes.toBytes(HBaseConstant.RELATION_TABLE_CF2));
        Result relaTableResult = relaTable.get(get);

        // 3. 创建一个集合，用于存放微博内容表的put对象
        List<Put> inboxPuts = new ArrayList<>();

        // 4. 遍历fans
        for (Cell cell : relaTableResult.rawCells()) {

            // 5. 构建微博收件箱表的put对象
            Put inboxPut = new Put(CellUtil.cloneQualifier(cell));

            // 6. 赋值
            inboxPut.addColumn(Bytes.toBytes(HBaseConstant.INBOX_TABLE_CF), Bytes.toBytes(uid), Bytes.toBytes(rowKey));

            // 7. 加入集合
            inboxPuts.add(inboxPut);
        }

        // 8. 判断是否有fans，如果有，才插入收件箱表
        if (inboxPuts.size() > 0) {
            // 获取收件箱表对象
            Table inboxTable = connection.getTable(TableName.valueOf(HBaseConstant.INBOX_TABLE));
            // 插入表
            inboxTable.put(inboxPuts);
            // 关闭资源
            inboxTable.close();
        }

        // 关闭资源
        contTable.close();
        relaTable.close();
        connection.close();
    }

    /**
     * 关注用户
     *
     * @param uid     操作者ID
     * @param attends 要关注的人
     */
    public static void addAttends(String uid, String... attends) throws IOException {
        // 校验参数
        if (attends.length > 1) {
            logger.error("未选择关注用户");
            return;
        }

        // 获取连接对象
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 操作用户关系表
        // 1. 获取用户关系对象
        Table relaTable = connection.getTable(TableName.valueOf(HBaseConstant.RELATION_TABLE));

        // 2. 创建一个集合，用于存放用户关系表的put对象
        List<Put> relaPuts = new ArrayList<>();

        // 3. 创建操作者的put对象
        Put userRelaPut = new Put(Bytes.toBytes(uid));

        // 4. 循环创建被关注者的put对象
        for (String attend : attends) {
            // 5. 给操作者的put对象赋值
            userRelaPut.addColumn(Bytes.toBytes(HBaseConstant.RELATION_TABLE_CF1), Bytes.toBytes(attend), Bytes.toBytes(attend));

            // 6. 创建被关注者的put对象
            Put attendRelaPut = new Put(Bytes.toBytes(attend));

            // 7. 为被关注者对象赋值
            attendRelaPut.addColumn(Bytes.toBytes(HBaseConstant.RELATION_TABLE_CF2), Bytes.toBytes(uid), Bytes.toBytes(uid));

            // 8. 将被关注者put对象放入集合
            relaPuts.add(attendRelaPut);
        }

        // 9. 将操作者的put对象放入集合
        relaPuts.add(userRelaPut);

        // 10. 执行用户关系表的插入
        relaTable.put(relaPuts);

        // 操作收信箱表
        // 1. 获取微博内容表和收件箱表对象
        Table contTable = connection.getTable(TableName.valueOf(HBaseConstant.CONTENT_TABLE));
        Table inboxTable = connection.getTable(TableName.valueOf(HBaseConstant.INBOX_TABLE));

        // 2. 创建收件箱表put对象
        Put inboxPut = new Put(Bytes.toBytes(uid));

        // 3. 变量attends，获取每个被关注者近期的微博
        for (String attend : attends) {
            // 4. 获取近期微博集合，sacn
            Scan scan = new Scan(Bytes.toBytes(attend + "_"), Bytes.toBytes(attend + "|"));
            scan.addColumn(Bytes.toBytes(HBaseConstant.CONTENT_TABLE_CF), Bytes.toBytes("content"));
            ResultScanner contTableResult = contTable.getScanner(scan);

            // 定义一个时间戳
            long ls = System.currentTimeMillis();

            // 5. 对集合进行遍历
            for (Result result : contTableResult) {
                inboxPut.addColumn(Bytes.toBytes(HBaseConstant.INBOX_TABLE_CF), Bytes.toBytes(attend),ls, result.getRow());
            }
        }

        // 7. 判断当前put对象是否为空
        if (!inboxPut.isEmpty()) {
            inboxTable.put(inboxPut);
        }

        // 关闭资源
        relaTable.close();
        contTable.close();
        inboxTable.close();
        connection.close();
    }

    /**
     * 取关
     * @param uid     操作者ID
     * @param attends 要关注的人
     */
    public static void deleteAttends(String uid, String... attends) throws IOException {
        // 校验参数
        if (attends.length > 1) {
            logger.error("未选择取关用户");
            return;
        }

        // 获取连接对象
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 操作用户关系表
        // 1. 获取用户关系对象
        Table relaTable = connection.getTable(TableName.valueOf(HBaseConstant.RELATION_TABLE));

        // 2. 创建一个集合，用于存放用户关系表的del对象
        List<Delete> relaDels = new ArrayList<>();

        // 3. 创建操作者的del对象
        Delete userRelaDel = new Delete(Bytes.toBytes(uid));

        // 4. 循环创建被关注者的del对象
        for (String attend : attends) {
            // 5. 给操作者的del对象赋值
            userRelaDel.addColumns(Bytes.toBytes(HBaseConstant.RELATION_TABLE_CF1), Bytes.toBytes(attend));

            // 6. 创建被取关者的del对象
            Delete attendRelaDelete = new Delete(Bytes.toBytes(attend));

            // 7. 为被取关者对象赋值
            attendRelaDelete.addColumns(Bytes.toBytes(HBaseConstant.RELATION_TABLE_CF2), Bytes.toBytes(uid));

            // 8. 将被取关者del对象放入集合
            relaDels.add(attendRelaDelete);
        }

        // 9. 将操作者的del对象放入集合
        relaDels.add(userRelaDel);

        // 10. 执行用户关系表的插入
        relaTable.delete(userRelaDel);

        // 操作收信箱表
        // 1. 获取微博内容表和收件箱表对象
        Table contTable = connection.getTable(TableName.valueOf(HBaseConstant.CONTENT_TABLE));
        Table inboxTable = connection.getTable(TableName.valueOf(HBaseConstant.INBOX_TABLE));

        // 2. 创建收件箱表del对象
        Delete inboxDel = new Delete(Bytes.toBytes(uid));

        // 3. 变量attends，删除相关微博
        for (String attend : attends) {
            inboxDel.addColumn(Bytes.toBytes(HBaseConstant.INBOX_TABLE_CF), Bytes.toBytes(attend));
        }

        // 4. 判断当前del对象是否为空
        if (!inboxDel.isEmpty()) {
            inboxTable.delete(inboxDel);
        }

        // 关闭资源
        relaTable.close();
        contTable.close();
        inboxTable.close();
        connection.close();
    }

    /**
     * 获取初始化界面
     * @param uid 用户ID
     */
    public static void getInitPage(String uid) throws IOException {
        // 获取连接对象
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 1. 获取收件箱、微博内容表对象
        Table inboxTable = connection.getTable(TableName.valueOf(HBaseConstant.INBOX_TABLE));
        Table contTable = connection.getTable(TableName.valueOf(HBaseConstant.CONTENT_TABLE));

        // 2. 创建收件箱Get对象并获取数据，注意版本
        Get uidGet = new Get(Bytes.toBytes(uid));
        // 设置版本
        uidGet.setMaxVersions();
        // 设置列簇和列
        uidGet.addFamily(Bytes.toBytes(HBaseConstant.INBOX_TABLE_CF));
        Result inboxTableResult = inboxTable.get(uidGet);

        // 3. 遍历获取的数据
        for (Cell cell : inboxTableResult.rawCells()) {
            // 4. 构建微博内容表get对象
            Get contGet = new Get(CellUtil.cloneValue(cell));

            // 5. 获取该get对象的具体内容
            Result result = contTable.get(contGet);
            for (Cell rawCell : result.rawCells()) {
                // 6. 解析并打印
                System.out.println(Bytes.toString(CellUtil.cloneQualifier(cell)));
                System.out.println(Bytes.toString(CellUtil.cloneValue(rawCell)));
                System.out.println("----------------------------");
            }

            System.out.println("********************************");
        }

        // 关闭资源
        inboxTable.close();
        contTable.close();
        connection.close();
    }

    /**
     * 获取某个用户的所有微博
     * @param uid 用户ID
     */
    public static void getWeibo(String uid) throws IOException {
        // 获取连接对象
        Connection connection = ConnectionFactory.createConnection(HBaseConstant.CONFIGURATION);

        // 1. 获取微博内容表对象
        Table contTable = connection.getTable(TableName.valueOf(HBaseConstant.CONTENT_TABLE));

        // 2. 构建scan对象
        Scan scan = new Scan();

        // 3. 设置过滤器
        RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(uid + "_"));
        scan.setFilter(rowFilter);

        // 4. 获取数据
        ResultScanner results = contTable.getScanner(scan);

        // 5. 解析并打印数据
        for (Result result : results) {
            for (Cell cell : result.rawCells()) {
                System.out.println(Bytes.toString(CellUtil.cloneValue(cell)));
            }
        }

        // 6. 关闭资源
        contTable.close();
        connection.close();
    }
}
