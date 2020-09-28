# Hadoop练习面试题


## <span id="入门">入门</span>
1. 常用端口号  
HDFS：50070(2.x)/9870(3.x)  
历史服务器：19888  
MapReduce：8088  
客户端：9000/8020  

## <span id="HDFS">HDFS</span>
1. 读写流程  
TODO

2. 小文件问题  
**危害**：
   - 占用 NameNode 元数据内存，因为一个文件不论多小都会占用 150字节 的内存；
   - 增加切片，增加 MapTask 个数，增加计算内存。

**优化手段**：
   - 采用har归档，将小文件归档；
   - 采用 CombineTextInputformat，改变切片；
   - 开启 JVM 重用。