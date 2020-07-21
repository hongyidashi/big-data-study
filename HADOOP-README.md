# Hadoop
# hadoop核心
1. HDFS: Hadoop Distributed File System 分布式文件系统
2. YARN: Yet Another Resource Negotiator 资源管理调度系统
3. Mapreduce：分布式运算框架



## HDFS
HDFS （Hadoop Distributed File System）是 Hadoop 下的**分布式文件系统**，具有高容错、高吞吐量等特性，可以部署在低成本的硬件上。
![HDFS架构](https://www.liuhe36.cn/wp-content/uploads/2013/05/2011090120573543.gif)
使用场景：适合一次写入多次读出的场景，不支持文件的修改!

### HDFS设计原则
1. 文件以块(block)方式存储
2. 每个块带下远比多数文件系统来的大(预设128M，HDFS1.X是64M)
3. 通过副本机制提高可靠度和读取吞吐量
4. 每个区块至少分到三台DataNode上（一般，对namenode进行raid1配置，对datanode进行raid5配置）
5. 单一 master (NameNode)来协调存储元数据(metadata)
6. 客户端对文件没有缓存机制 (No data caching)

### HDFS组成架构

1. NameNode(NN)  

**作用：**  
- 管理HDFS的名称空间
- 管理数据块(Block)映射信息
- 配置副本策略
- 处理客户端读写请求
    
**文件包括：**  
- fsimage:元数据镜像文件。存储某一时段NameNode内存元数据信息。
- edits:操作日志文件。
- fstime:保存最近一次checkpoint的时间。  

**工作特点：**  
NameNode始终在内存中保存metedata，用于处理“读请求”，到有“写请求”到来时，NameNode首先会写editlog到磁盘，
即向edits文件中写日志，成功返回后，才会修改内存，并且向客户端返回。  
Hadoop会维护一个fsimage文件，也就是NameNode中metedata的镜像，但是fsimage不会随时与NameNode内存中的metedata保持一致，
而是每隔一段时间通过合并edits文件来更新内容。Secondary NameNode就是用来合并fsimage和edits文件来更新NameNode的metedata的。  
到达两次checkpoint最大时间间隔或edits文件的最大值时就会checkpoint。

2. DataNode(DN)  
**作用：**  
- 存储实际的数据块
- 执行数据块的读/写操作

3. Secondary NameNode(2NN)  
**作用：**  
- 辅助NN，分担其工作量
- 定期合并fsimage和fsedits，并推送给NN
- 在紧急情况下，可辅助恢复NN

4. Client
**作用：**  
- 文件切分。文件上传HDFS时，Client按照Block大小切分文件，然后进行存储
- 与NameNode交互，获取文件位置信息
- 与DataNode交互，读取或写入数据
- Client提供一些命令管理和访问HDFS


### HDFS读写数据
**写数据**
![HDFS写数据流程](https://img2018.cnblogs.com/blog/1595409/201904/1595409-20190414145523723-826688867.png)
1. 校验：客户端通过 DistributedFileSystem 模块向 NameNode 请求上传文件，NameNode 会检查目标文件是否已经存在，父目录是否存在;
2. 响应：NameNode 返回是否可以上传的信号；
3. 请求 NameNode：客户端对上传的数据根据块进行切片，并请求第一块 Block 上传到哪几个 DataNode 服务器上；
4. 响应 DataNode节点 信息：NameNode 根据副本数等信息返回可上传的 DataNode 节点，例如这里的 dn1,dn2,dn3；
5. 建立通道：客户端通过 FSDataOutputStream 模块请求dn1上传数据，dn1收到请求会继续调用dn2，然后dn2调用dn3，将这个通信管道建立完成；
6. DataNode 响应 Client：dn1、dn2、dn3逐级应答客户端；
7. 上传数据到 DataNode：客户端开始往 dn1上传第一个 Block（先从磁盘读取数据放到一个本地内存缓存），以 Packet 为单位，
dn1收到一个 Packet 就会传给 dn2，dn2传给 dn3；dn1每传一个 Packet 会放入一个应答队列等待应答；
8. 通知 NameNode 上传完成：当一个 Block 传输完成之后，客户端再次请求 NameNode 上传第二个 Block的服务器；
9. 关闭输入输出流；
10. 两个 Block 都传输完成后通知 NameNode，生成元数据。

**在 HDFS写数据的过程中，NameNode会选择距离最近的 DataNode接收数据。**

**Block 的副本放置策略**
>当复制因子为 3 时，HDFS 的放置策略是：  
若客户端位于 datanode 上，则将一个副本放在本地计算机上，否则放在随机 datanode 上
在另一个（远程）机架上的节点上放置另一个副本，最后一个在同一个远程机架中的另一个节点上。  
机架故障的可能性远小于节点故障的可能性。
此策略可以减少机架间写入流量，从而提高写入性能，而不会影响数据可靠性和可用性（读取性能）。
这样减少了读取数据时使用的聚合网络带宽，因为块只放在两个唯一的机架，而不是三个。

**读数据**
![HDFS读数据流程](https://img2018.cnblogs.com/blog/1595409/201904/1595409-20190414151226689-1541120599.png)
1. 客户端通过 Distributed FileSystem 向 NameNode 请求下载文件，NameNode 通过查询元数据，找到文件块所在的 DataNode 地址进行返回；
2. 挑选一台 DataNode（就近原则，然后随机）服务器，请求读取数据。当第一次读取完成之后，才进行第二次块的读取；
3. DataNode 开始传输数据给客户端（从磁盘里面读取数据输入流，以 Packet 为单位来做校验）；
4. 客户端以 Packet 为单位接收，先在本地缓存，然后写入目标文件。

**下载时副本的选择**
>为了最大限度地减少全局带宽消耗和读取延迟，HDFS 会选择最接客户端的节点中的副本来响应读取请求。  
 如果客户端与 DataNode 节点在同一机架上，且存在所需的副本，则该副本会首读用来响应取请求。  
 如果 HDFS 群集跨越多个数据中心，则驻留在本地数据中心的副本优先于任何远程副本。  

### NN和2NN工作机制
![NN和2NN工作机制](https://img-blog.csdnimg.cn/20190709221712267.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hlMTU5MzY5Mzg2MzY=,size_16,color_FFFFFF,t_70)
**第一阶段：NameNode启动**
1. 第一次启动 NameNode 格式化后，创建 Fsimage 和 Edits 文件。如果不是第一次启动，直接加载编辑日志和镜像文件到内存；
2. 客户端对元数据进行增删改的请求；
3. NameNode 记录操作日志，更新滚动日志；
4. NameNode在内存中对数据进行增删改；

**第二阶段：Secondary NameNode工作**
1. Secondary NameNode 询问 NameNode 是否需要 CheckPoint。直接带回 NameNode 是否检查结果；
2. Secondary NameNode 请求执行 CheckPoint；
3. NameNode 滚动正在写的 Edits 日志。
4. 将滚动前的编辑日志和镜像文件拷贝到 Secondary NameNode；
5. Secondary NameNode 加载编辑日志和镜像文件到内存，并合并；
6. 生成新的镜像文件 fsimage.chkpoint；
7. 拷贝 fsimage.chkpoint 到 NameNode；
8. NameNode 将 fsimage.chkpoint 重新命名成 fsimage。

### DataNode工作机制
![DataNode工作机制](https://img2018.cnblogs.com/blog/1721350/201907/1721350-20190724121237434-1189571085.png)
1. 一个数据块在DataNode上以文件形式存储在磁盘上，包括两个文件，一个是数据本身，一个是元数据包括数据块的长度，块数据的校验和，以及时间戳；
2. DataNode启动后向NameNode注册，通过后，周期性（1小时）的向NameNode上报所有的块信息；
3. 心跳是每3秒一次，心跳返回结果带有NameNode给该DataNode的命令如复制块数据到另一台机器，或删除某个数据块。如果超过10分钟没有收到某个DataNode的心跳，则认为该节点不可用；
4. 集群运行中可以安全加入和退出一些机器。

### HDFS的优缺点

**优点**
1. 高容错性
- 数据自动保存多个副本。它通过增加副本的形式，提高容错性；
- 某一个副本丢失以后，它可以自动恢复，这是由 HDFS 内部机制实现的，我们不必关心。

2. 适合批处理
- 它是通过移动计算而不是移动数据；
- 它会把数据位置暴露给计算框架。

3. 适合大数据处理
- 数据规模：能够处理数据规模达到 GB、TB、甚至PB级别的数据；
- 文件规模：能够处理百万规模以上的文件数量，数量相当之大；
- 节点规模：能够处理10K节点的规模。

4. 流式数据访问
- 一次写入，多次读取，不能修改，只能追加；
- 它能保证数据的一致性。

5. 可构建在廉价机器上
- 它通过多副本机制，提高可靠性；
- 它提供了容错和恢复机制。比如某一个副本丢失，可以通过其它副本来恢复。

**缺点**
1. 不适合低延时数据访问；
- 比如毫秒级的来存储数据，这是不行的，它做不到；
- 它适合高吞吐率的场景，就是在某一时间内写入大量的数据。但是它在低延时的情况  下是不行的，比如毫秒级以内读取数据，这样它是很难做到的。

2. 无法高效的对大量小文件进行存储
- 存储大量小文件的话，它会占用  NameNode大量的内存来存储文件、目录和块信息。这样是不可取的，因为NameNode的内存总是有限的；
- 小文件存储的寻道时间会超过读取时间，它违反了HDFS的设计目标。

3. 并发写入、文件随机修改
- 一个文件只能有一个写，不允许多个线程同时写；
- 仅支持数据 append（追加），不支持文件的随机修改。
