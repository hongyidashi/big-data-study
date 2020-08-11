# Hadoop
# hadoop核心
1. [HDFS](#HDFS): Hadoop Distributed File System 分布式文件系统  
+ [设计原则](#设计原则)  
+ [HDFS组成架构](#HDFS组成架构)
+ [HDFS读写数据](#HDFS读写数据)
+ [NN和2NN工作机制](#NN和2NN工作机制)
+ [DataNode工作机制](#DataNode工作机制)
+ [HDFS的优缺点](#HDFS的优缺点)

2. [MapReduce](#MapReduce)：分布式运算框架
+ [MapReduce进程](#MapReduce进程)
+ [MapReduce核心思想](#MapReduce核心思想)
+ [MapReduce处理数据流程](#MapReduce处理数据流程)
    - [总览](#总览)
    - [具体流程](#具体流程)
    - [Map-Read](#Map-Read)
    - [MapTask-Map](#MapTask-Map)
    - [MapTask-Shuffle-Collect](#MapTask-Shuffle-Collect)
    - [MapTask-Shuffle-Spill](#MapTask-Shuffle-Spill)
    - [MapTask-Shuffle-Combine](#MapTask-Shuffle-Combine)
    - [ReduceTask-Shuffle-Copy](#ReduceTask-Shuffle-Copy)
    - [ReduceTask-Shuffle-Merge](#ReduceTask-Shuffle-Merge)
    - [ReduceTask-Reduce](#ReduceTask-Reduce)
+ [MapReduce优化方法](#MapReduce优化方法)

3. [Yarn](#Yarn): Yet Another Resource Negotiator 资源管理调度系统
+ [为啥要有Yarn-拓展](#为啥要有Yarn-拓展)
+ [Yarn架构](#Yarn架构)
+ [Yarn工作机制](#Yarn工作机制)
+ [YarnJob提交流程](#YarnJob提交流程)
+ [Yarn的一些补充](#Yarn的一些补充)
+ [资源调度器](#资源调度器)
+ [任务的推测执行](#任务的推测执行)

## <span id="HDFS">HDFS</span>
HDFS （Hadoop Distributed File System）是 Hadoop 下的**分布式文件系统**，具有高容错、高吞吐量等特性，可以部署在低成本的硬件上。
![HDFS架构](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1595347800725&di=2dfad87fd2b4e7e3f5d72df915008993&imgtype=0&src=http%3A%2F%2Fimg.mukewang.com%2F5a1e2d4a00015ec112800720.png)
使用场景：适合一次写入多次读出的场景，不支持文件的修改！

### <span id="HDFS设计原则">HDFS设计原则</span>
1. 文件以块(block)方式存储
2. 每个块大小远比多数文件系统的大(预设128M，HDFS1.X是64M)
3. 通过副本机制提高可靠度和读取吞吐量
4. 每个区块至少分到三台DataNode上（一般，对namenode进行raid1配置，对datanode进行raid5配置）
5. 单一 master (NameNode)来协调存储元数据(metadata)
6. 客户端对文件没有缓存机制 (No data caching)

### <span id="HDFS组成架构">HDFS组成架构</span>

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


### <span id="HDFS读写数据">HDFS读写数据</span>
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
10. Block 传输完成后通知 NameNode，生成元数据。

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
>为了最大限度地减少全局带宽消耗和读取延迟，HDFS 会选择离客户端最近的节点中的副本来响应读取请求。  
 如果客户端与 DataNode 节点在同一机架上，且存在所需的副本，则该副本会首读用来响应取请求。  
 如果 HDFS 群集跨越多个数据中心，则驻留在本地数据中心的副本优先于任何远程副本。  

### <span id="NN和2NN工作机制">NN和2NN工作机制</span>
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

### <span id="DataNode工作机制">DataNode工作机制</span>
![DataNode工作机制](https://img2018.cnblogs.com/blog/1721350/201907/1721350-20190724121237434-1189571085.png)
1. 一个数据块在DataNode上以文件形式存储在磁盘上，包括两个文件，一个是数据本身，一个是元数据包括数据块的长度，块数据的校验和，以及时间戳；
2. DataNode启动后向NameNode注册，通过后，周期性（1小时）的向NameNode上报所有的块信息；
3. 心跳是每3秒一次，心跳返回结果带有NameNode给该DataNode的命令如复制块数据到另一台机器，或删除某个数据块。如果超过10分钟没有收到某个DataNode的心跳，则认为该节点不可用；
4. 集群运行中可以安全加入和退出一些机器。

### <span id="HDFS的优缺点">HDFS的优缺点</span>

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


## <span id="MapReduce">MapReduce</span>
MapReduce 作业通过将输入的数据集拆分为独立的块，这些块由 map 以并行的方式处理，框架对 map 的输出进行排序，然后输入到 reduce 中。  
MapReduce 框架专门用于 <key，value> 键值对处理，它将作业的输入视为一组 <key，value> 对，并生成一组 <key，value> 对作为输出。输出和输出的 key 和 value 都必须实现Writable 接口。
![MapReduce架构图](https://img-blog.csdnimg.cn/20181228212253308.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzQzMTkzNzk3,size_16,color_FFFFFF,t_70)

### <span id="MapReduce进程">MapReduce进程</span>
1. MrAppMaster:负责整个程序的过程调度及状态协调
2. MapTask:负责map阶段的整个处理流程
3. ReduceTask:负责reduce阶段的整个数据处理流程

### <span id="MapReduce核心思想">MapReduce核心思想</span>
MapReduce的思想就是“**分而治之**”。  
1. Mapper负责“分”，即把复杂的任务分解为若干个“简单的任务”来处理。“简单的任务”包含三层含义：  
一是数据或计算的规模相对原任务要大大缩小；  
二是就近计算原则，即任务会分配到存放着所需数据的节点上进行计算；  
三是这些小任务可以并行计算，彼此间几乎没有依赖关系。  

2. Reducer负责对map阶段的结果进行汇总。至于需要多少个Reducer，用户可以根据具体问题，通过在mapred-site.xml配置文件里设置参数mapred.reduce.tasks的值，缺省值为1。

### <span id="MapReduce处理数据流程">MapReduce处理数据流程</span>

#### <span id="总览">总览</span>
![总体流程](https://img-blog.csdn.net/20170517152328185?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvYWlqaXVkdQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center)
1. 首先Map节点对所划分的数据进行并行处理，从不同的输入数据产生相应的中间结果输出；
2. 在进行Reduce之前，必须要等到所有的Map任务执行完成，且在进入Reduce之前会有一个过程将Map的输出结果进行汇总（shuffle），以便Reduce节点可以完全基于本节点上的数据计算最终的结果；
3. 各个Reduce节点各自进行计算，各自负责处理不同的中间结果数据集合；
4. 汇总所有Reduce的输出结果即可获得最终结果。

#### <span id="具体流程">具体流程</span>
1. MapReduce提供了一个 InputFormat 对象用来读取数据，并进行切分，切分后的数据被切分成为多个 InputSplit(输入切片)，
每一个 InputSplit 将对应一个 MapTask。注意 InputSplit 存储的并不是数据本身，而是一个分片长度和一个记录数据位置的数组；
2. 在进入Map之前，需要通过 InputFormat 方法调用 getRecordReader() 方法生成 RecordReader，RecordReader 再通过 createKey()、
createValue() 方法创建可供map处理的<key,value>对； RecordReader 即为从数据分块中读取数据记录并转化为键值对的类；
3. 在Map输出中间结果之前，需要经过一个 Combiner(合并) 对象将该map输出的相同主键key下的所有键值对合并成为一个键值对；
4. Map会将输出的中间结果发送给 Reduce 节点，在这之前，还是需要通过 Partitioner(分区) 对象进行数据分区，将数据发送到合适的 Reduce 节点上，
避免不同Reduce节点上的数据有相关性，保证每一个 Reduce 节点可以独立完成本地计算。并且在传入 Reduce 节点之前还会自动将所有键值对按照主键值进行排序；
5. Reduce 计算完成之后，会经过 OutputFormat 对象指定输出数据的具体格式，最终将数据输出并写回到 HDFS 上。

**数据切片**：数据切片只是在逻辑上对输入进行分片，并不会在磁盘上将其切分成片进行  。

---

#### <span id="Map-Read">Map-Read</span>
MapTask 通过用户编写的 RecordReader，从输入 InputSplit 中解析出一个个 key/value。

**MapTask并行度决定机制**  
MapTask的并行度决定map阶段的任务处理并发度，进而影响到整个job的处理速度；一个job的map阶段并行度由客户端在**提交job时决定**。  

**Hadoop序列化**  
Java的序列化是一个重量级序列化框架，对象被序列化后会附带许多额外信息，不方便大数据在网络传输。  
Hadoop序列化特点：
- **紧凑**：高效使用存储空间
- **快速**：读写的额外开销很小
- **可扩展**：随着通信协议升级而升级
- **互操作**：支持多语言的交互

**MapReduce框架提供了几种 InputFormat 的切片机制**
1. FileInputFormat
+ 简单的按照文件的内容长度进行切片
+ 切片大小默认等于block的大小
+ 切片时不考虑数据整体，而是逐个针对每个文件单独切片

>不管文件多小，都会是一个单独的切片，都会交给一个 MapTask，这样如果有大量小文件，就会产生大量的 MapTask，处理效率极其低下

2. CombineFileInputFormat
+ 可以将多个小文件从逻辑上规划到一个切片中

**切片机制**：  
虚拟存储：将输入目录下所有文件大小，依次和设置的 setMaxInputSplitSize 值比较，如果不大于设置的最大值，逻辑上划分一个块。
如果输入文件大于设置的最大值且大于两倍，那么以最大值切割一块；当剩余数据大小超过设置的最大值且不大于最大值2倍，
此时将文件均分成2个虚拟存储块（防止出现太小切片）。  
切片过程：判断虚拟存储的文件大小是否大于 setMaxInputSplitSize 值，大于等于则单独形成一个切片；
如果不大于则跟下一个虚拟存储文件进行合并，共同形成一个切片。

**自定义InputFormat**
步骤：  
- 自定义一个类继承 FileInputFormat;
- 改写 RecordReader，实现一次读取一个完整文件封装为KV；
- 在输出时使用 SequenceFileOutputFormat输出合并文件。

#### <span id="MapTask-Map">MapTask-Map</span>
该阶段主要是将解析出的key/value交给用户编写map()函数处理，并产生一系列新的key/value。

---

#### <span id="Shuffle-Collect">Shuffle-Collect</span>
在用户编写 map() 函数中，当数据处理完成后，一般会调用 OutputCollector.collect() 输出结果。
在该函数内部，它会将生成的 key/value 分区（调用Partitioner），并写入一个环形内存缓冲区中Map的输出结果是由 collector 处理的，
每个 MapTask 不断地将键值对输出到环形缓冲区中。
  
**环形缓冲区**  
1. 内存中构造的一个环形数据结构，使用环形数据结构是为了更有效地使用内存空间，在内存中放置尽可能多的数据；
2. 数据结构其实就是个字节数组，里面不光放置了数据，还放置了一些索引数据；
3. 数据区域和索引数据区域在 Kvbuffer 中是相邻不重叠的两个区域，用一个分界点来划分两者；每次 Spill(溢出)之后都会更新一次分界点；
4. 索引记录了value的起始位置、key的起始位置、partition值、value的长度；
5. 环形缓存区的大小默认为100M,当保存的数据达到80%时,就将缓存区的数据溢出到磁盘上保存。

**Partition(分区)**  
对于map输出的每一个键值对，系统都会给定一个 partition，partition 值默认是通过计算key的hash值后对 Reduce task 的数量取模获得。
如果一个键值对的 partition 值为1，意味着这个键值对会交给第一个 Reducer 处理。

**Partition能解决的问题**  
- **输出到不同文件**：能将数据按照条件输出到不同文件，一个 Partition 即一个文件；
- **解决数据倾斜**：根据业务需求对数据进行分区，尽量平衡每个 Reducer 处理的数据量；
>数据倾斜  
>对于某些数据集，由于很多不同的key的hash值都一样，导致这些键值对都被分给同一个Reducer处理，而其他的Reducer处理的键值对很少，
>从而拖延整个任务的进度。
- **排序**：可用于分区排序。

---

#### <span id="MapTask-Shuffle-Spill">MapTask-Shuffle-Spill</span>
当环形缓冲区满后，MapReduce 会将数据写到本地磁盘上，生成一个临时文件。
需要注意的是，将数据写入本地磁盘之前，**先要对数据进行一次本地排序，并在必要时对数据进行合并、压缩等操作**。

**溢写**  
- 环形缓存区的数据达到其容量的80%时就会溢出到磁盘上进行保存，在此过程中，程序会对数据进行**分区**(默认HashPartition)和**排序**(默认根据key进行快排)
- 缓存区不断溢出的数据形成多个小文件。

**溢写过程**：
1. 对缓存区内的数据排序（快排），先按 Partition 排，再按 key 排，保证分区有序；
2. 按照分区编号由小到大依次将每个分区中的数据写入任务工作目录下的临时文件 output/spillN.out（N表示当前溢写次数）中。
如果用户设置了 Combiner(合并)，则写入文件之前，对每个分区中的数据进行一次聚集操作;
3. 将分区数据的**元信息**写到内存索引数据结构 SpillRecord 中，元信息包括在临时文件中的偏移量、压缩前数据大小和压缩后数据大小。
如果当前内存索引大小超过**1MB**，则将内存索引写到文件output/spillN.out.index中。

**WritableComparable排序**  
MapTask 和 ReducerTask 均会对数据进行排序，该操作属于Hadoop默认行为，不管逻辑上是否需要。  
MapReduce中的排序：
- 对于MapTask，环形缓冲区达到一定阈值时，会对缓冲区中的数据进行一次快排；数据处理完毕后，会对磁盘上所有文件进行归并排序；
- 对于ReducerTask，当所有数据拷贝完毕后，会统一对内存和磁盘上的数据进行一次归并排序。

自定义排序：bean对象做为key传输，需要实现 WritableComparable 接口重写 compareTo() 方法，就可以实现排序。

**分组**  
分组和上面提到的 partition（分区）不同，分组发生在 reduce 端，reduce 的输入数据，会根据 key 是否相等而分为一组，
如果 key 相等的，则这些 key 所对应的 value 值会作为一个迭代器对象传给 reduce 函数。
>如果key是用户自定义的bean对象，那么就算两个对象的内容都相同，这两个bean对象也不相等，也会被分为两组，
>该bean需要继承 WritableComparator 并重写compare() 方法才能被分为同一组。

**Combiner(map端的reduce)**  
- combiner 的输入是 Map 的输出，combiner 的输出作为 Reduce 的输入，很多情况下可以直接将 reduce 函数作为 combiner 函数来使用；
- combiner 的意义就是对每一个 MapTask 的输出进行局部汇总，以减小网络传输量；
- 要保证不管调用几次combiner函数都不会影响最终的结果，Combiner 属于优化方案。

---

#### <span id="MapTask-Shuffle-Combine">MapTask-Shuffle-Combine</span>
当所有数据处理完成后，MapTask对所有临时文件进行一次合并，以确保最终只会生成一个数据文件。

---

#### <span id="ReduceTask-Shuffle-Copy">ReduceTask-Shuffle-Copy</span>
ReduceTask 从各个 MapTask 上远程拷贝一片数据，如果某一片数据大小超过一定阈值，则写到磁盘上，否则直接放到内存中。  
job的第一个 map 结束后，所有的 reduce 就开始尝试从完成的 map 中下载该 reduce 对应的 partition 部分数据，因此 map 和 reduce 是交叉进行的。
>为什么要交叉进行  
>由于job的每一个map都会根据reduce(n)数将数据分成map 输出结果分成n个partition，
>所以map的中间结果中是有可能包含每一个reduce需要处理的部分数据的。~~别问，问就是效率~~这么做的目的是为了优化执行时间。

copy 线程(Fetcher)通过HTTP方式请求 MapTask 所在的 TaskTracker 获取 MapTask 的输出文件，默认最大并行度（同时到多少个Mapper下载数据）为 5 。  
>如果下载过程中出现数据丢失、断网等问题咋办  
>这样 reduce 的下载就有可能失败，所以reduce的下载线程并不会无休止的等待下去，当一定时间后下载仍然失败，那么下载线程就会放弃这次下载，
>并在随后尝试从另外的地方下载（因为这段时间map可能重跑）。下载时间默认为**180000秒**，一般情况下都会调大这个参数，~~别问，问就是经验~~这是企业级最佳实战。

---

#### <span id="ReduceTask-Shuffle-Merge">ReduceTask-Shuffle-Merge</span>
在远程拷贝数据的同时，ReduceTask启动了两个后台线程对内存和磁盘上的文件进行合并，以防止内存使用过多或磁盘上文件过多。  

**Reduce端的 shuffle 也有一个环形缓冲区，同样会进行 partition、combine、排序等过程。**

Copy 过来的数据会先放入内存缓冲区中，然后当使用内存达到一定量的时候才 Spill 磁盘。  
与 Map 端类似，这也是溢写的过程，这个过程中如果设置有 Combiner，也是会启用的，然后在磁盘中生成了众多的溢写文件。
**这种 merge 方式一直在运行，直到没有 Map 端的数据时才结束，然后启动磁盘到磁盘的 merge 方式生成最终的那个文件。**  

merge三种形式
+ 内存到内存Merge（memToMemMerger）：这种合并将内存中的 map 输出合并，然后再写入内存；这种合并默认关闭；
+ 内存中Merge（inMemoryMerger）：当缓冲中数据达到配置的阈值时，这些数据在内存中被合并、写入机器磁盘；
+ 磁盘上的Merge（onDiskMerger）
  - Copy过程中磁盘Merge：在 copy 过来的数据不断写入磁盘的过程中，一个后台线程会把这些文件合并为更大的、有序的文件；
  这里的合并只是为了减少最终合并的工作量，也就是在 map 输出还在拷贝时，就开始进行一部分合并工作；合并的过程一样会进行全局排序；
  - 最终磁盘中Merge：当所有map输出都拷贝完毕之后，所有数据被最后合并成一个整体有序的文件，作为reduce任务的输入；
  这个合并过程是一轮一轮进行的，最后一轮的合并结果直接推送给 reduce 作为输入，节省了磁盘操作的一个来回。
>Copy过程中磁盘Merge  
>如果map的输出结果进行了压缩，则在合并过程中，需要在内存中解压后才能给进行合并。

>最终磁盘中Merge  
>最后（所有 Map 输出都拷贝到 reduce 之后）进行合并的 Map 输出可能来自合并后写入磁盘的文件，也可能来自内存缓冲，
>在最后写入内存的 Map 输出可能没有达到阈值触发合并，所以还留在内存中。  
>每一轮合并不一定合并平均数量的文件数，指导原则是使整个合并过程中写入磁盘的数据量最小，为了达到这个目的，
>则需要最终的一轮合并中合并尽可能多的数据，因为最后一轮的数据直接作为reduce的输入，无需写入磁盘再读出。

---

#### <span id="ReduceTask-Reduce">ReduceTask-Reduce</span>
通过 reduce()函数（用户自定义业务逻辑）将计算结果写到HDFS上。  
Reduce 在这个阶段，框架为已分组的输入数据中的每个 <key, (list of values)>对调用一次 reduce(WritableComparable,Iterator, OutputCollector, Reporter)方法。
Reduce 任务的输出通常是通过调用 OutputCollector.collect(WritableComparable,Writable)写入文件系统的。**Reducer 的输出是没有排序的**。

**OutputFormat 数据输出**  
OutputFormat 是所有 MapReduce 输出的基类，常见的实现类有：
- TextOutputFormat：默认输出格式，它把每条记录写为文本；
- SequenceFileOutputFormat：将 SequenceFileOutputFormat 的输出作为后续 MapReduce 的输入，这种格式紧凑，容易被压缩。
- 自定义OutputFormat：
  1. 自定义一个类继承 FileOutputFormat；
  2. 重写 getRecordWriter() 方法，并在其中返回一个自定义 RecordWriter 实现类。

### <span id="MapReduce优化方法">MapReduce优化方法</span>
MapReduce 优化方法主要从六个方面考虑：数据输入、Map阶段、Reduce阶段、IO传输、数据倾斜问题和常用的调优参数。

1. 数据输入
+ 执行MR任务前将小文件合并，减少Map任务个数；
+ 有大量小文件时，采用 CombineTextInputFormat 来作为输入

2. Map阶段
+ 调整触发 Spill（溢写）的内存上限，减少 Spill次数；
+ 增大 Merge（合并）文件数目，减少 Merge 次数；
+ **在不影响业务逻辑的前提下**，先进行 Combine 处理，减少I/O；

3. Reduce阶段
+ 合理设置Map和Reduce数，不能太多也不能太少；
+ 设置Map和Reduce共存，使Map运行到一定阶段的时候Reduce也开始运行；
+ 规避使用Reduce；

4. 其他略，感觉没啥用

## <span id="Yarn">Yarn</span>
Yarn 是一个资源调度平台，负责为运算程序提供服务器运算资源，相当于一个分布式的操作系统平台，
而 MapReduce 等运算程序则相当于运行于操作系统之上的应用程序。

YARN是Hadoop2.0中的资源管理系统，它的基本思想是将 JobTracker 的两个主要功能(资源管理和作业调度/监控)分离，
主要方法是创建一个全局的 ResourceManager(RM) 和若干个针对应用程序的 ApplicationMaster(AM)。
其中 RM 负责整个系统的资源管理和分配，而 AM 负责单个应用程序的管理。应用程序是指传统的MapReduce作业或作业的DAG(有向无环图)。

### <span id="为啥要有Yarn-拓展">为啥要有Yarn-拓展</span>

**hadoop1.x时代**  
在hadoop1.x时代，负责执行 MapReduce 的两个角色分别叫做 JobTracker 和 TaskTracker，
JobTracker 负责监控 MapReduce 集群中每一个节点的运行状况和 job 启动以及运行失败后的重启等操作；
TaskTracker 在 MapReduce 集群中的每一个节点上都会存在，它负责监控自己所在节点的上作业的运行状况以及资源的使用情况，
并且通过心跳的方式反馈给 JobTracker，然后 JobTracker 会根据这些信息分配 job 运行在哪些机器上。

**存在问题**  
1. JobTracker 是 MapReduce 的集中处理点，存在单点故障；
2. JobTracker 完成了太多的任务，造成了过多的资源消耗，当 MapReduce job非常多的时候，会造成很大的内存开销，潜在来说，
也增加了 JobTracker fail 的风险；
3. 在 TaskTracker 端，以 map/reduce task的数目作为资源的表示过于简单，没有考虑到cpu/内存的占用情况，
如果两个大内存消耗的task被调度到了一块，很容易出现OOM；
4. 在 TaskTracker 端，把资源强制划分为map task slot和reduce task slot，如果当系统中只有 MapTask 或者只有 ReduceTask 的时候，
会造成资源的浪费，也就是前面提到过的集群资源利用的问题。

**所以有了Yarn**  
YARN 的核心观点是将对于集群的资源管理和作业调度分割开来。

### <span id="Yarn架构">Yarn架构</span>
![YARN架构](https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1595761805591&di=59b8856e8e070d1ccc4f57d0fa81c259&imgtype=0&src=http%3A%2F%2Fjavaquan.com%2Ffile%2Fimage%2F20190130%2F2019013014022516772549.PNG)
YARN 主要由 ResourceManager、NodeManager、ApplicationMaster 和 Container 等组件构成。  

1. ResourceManager(RM)  
RM 是一个全局的资源管理器，管理整个集群的计算资源，并将这些资源分配给应用程序。
- 处理来自客户端的请求；
- 启动和监控 ApplicationMaster，并在它运行失败时重新启动它；
- 监控 NodeManager；
- 资源管理与调度，接收来自 ApplicationMaster 的资源申请请求，并为之分配资源。

2. ApplicationMaster(AM)  
应用程序级别的，管理运行在 YARN 上的应用程。
- 用户提交的每个应用程序均包含一个 AM，它可以运行在 RM 以外的机器上；
- 负责数据切分；
- 为应用程序申请资源并分配给内部的任务；
- 任务监控与容错。

3. NodeManager(NM)  
YARN 中每个节点上的代理，它管理 Hadoop 集群中单个计算节点。
- 管理单节点上的资源；
- 处理 RM 和 AM 的命令。

4. Container  
Container 是 YARN 中资源的抽象，它封装了某个节点上的多维度资源，如内存、CPU、磁盘、网络等。  
Container 由 AM 向 RM 申请的，由 RM 中的资源调度器异步分配给 AM。Container 的运行是由 AM 向资源所在的 NM 发起。

### <span id="Yarn工作机制">Yarn工作机制</span>
![工作机制流程图](https://img-blog.csdnimg.cn/20190925194234719.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2E3NTUxOTk0NDM=,size_16,color_FFFFFF,t_70)
1. MR 程序提交到客户端所在的节点；
2. YarnRunner 向 ResourceManager 申请一个 Application；
3. RM 将该应用程序的资源路径返回给 YarnRunner；
4. 该程序将运行所需资源提交到 HDFS 上；
5. 程序资源提交完毕后，申请运行 MRAppMaster；（注：MRAppMaster 是 ApplicationMaster 实现类~~，大概是这么个意思~~）
6. RM 将用户的请求初始化成一个 Task；
7. 其中一个 NodeManager 领取到 Task 任务；
8. 该 NodeManager 创建容器 Container，并产生 MRAppMaster(其实 [MapReduce](#MapReduce进程) 里也有介绍过，应该是同一个东西)；
9. Container 从 HDFS 上拷贝资源到本地；
10. MRAppMaster 向 RM 申请运行 MapTask 资源；
11. RM 将运行 MapTask 任务分配给另外两个 NodeManager，另两个 NodeManager 分别领取任务并创建容器；
12. MR 向两个接收到任务的 NodeManager 发送程序启动脚本，这两个 NodeManager 分别启动 MapTask，MapTask 对数据分区排序；
13. MrAppMaster 等待所有 MapTask 运行完毕后，向 RM 申请容器，运行 ReduceTask；
14. ReduceTask 向 MapTask 获取相应分区的数据；
15. 程序运行完毕后，MR 会向 RM 申请注销自己。

**个人的一些理解**  
1. Task 是运行在某个 NodeManager 管理下的 Container 里，而非直接运行在 NodeManager 本身；
2. 关于第13点，并不需要等待**所有**的 MapTask 运行完毕再运行 ReduceTask，二者应该是可以并行的。

### <span id="YarnJob提交流程">YarnJob提交流程</span>
![Yarn Job提交流程](https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=856711650,2447342781&fm=15&gp=0.jpg)
1. 作业提交  
client 调用 job.waitForCompletion 方法，向整个集群提交 MapReduce 作业 (第 1 步) ；  
新的作业 ID(应用 ID) 由资源管理器(RM)分配 (第 2 步)；  
作业的 client 核实作业的输出, 计算输入的 split, 将作业的资源 (包括 Jar 包，配置文件, split 信息) 拷贝给 HDFS(第 3 步)；   
最后, 通过调用资源管理器的 submitApplication() 来提交作业 (第 4 步)。  

2. 作业初始化  
当资源管理器(RM)收到 submitApplIcation() 的请求时, 就将该请求发给调度器 (Scheduler), 调度器分配 container, 
然后资源管理器在该 container 内启动应用管理器进程, 由节点管理器(NM)监控 (第 5 步)；  
MapReduce 作业的应用管理器是一个主类为 MRAppMaster 的 Java 应用，其通过创造一些 bookkeeping 对象来监控作业的进度, 
得到任务的进度和完成报告 (第 6 步)；  
然后其通过分布式文件系统得到由客户端计算好的输入 split(第 7 步)；  
最后为每个输入 split 创建一个 map 任务, 根据 mapreduce.job.reduces 创建 reduce 任务对象。

3. 任务分配  
如果作业很小, 应用管理器会选择在其自己的 JVM 中运行任务；如果不是小作业, 
那么应用管理器向资源管理器请求 container 来运行所有的 map 和 reduce 任务 (第 8 步)。  
这些请求是通过心跳来传输的, 包括每个 map 任务的数据位置，比如存放输入 split 的主机名和机架 (rack)，调度器利用这些信息来调度任务，
尽量将任务分配给存储数据的节点, 或者分配给和存放输入 split 的节点相同机架的节点。

4. 任务运行  
当一个任务由资源管理器的调度器分配给一个 container 后，应用管理器(AM)通过联系节点管理器来启动 container(第 9 步)；  
任务由一个主类为 YarnChild 的 Java 应用执行， 在运行任务之前首先本地化任务需要的资源，比如作业配置，JAR 文件, 
以及分布式缓存的所有文件 (第 10 步)；  
运行 map 或 reduce 任务 (第 11 步)。  
YarnChild 运行在一个专用的 JVM 中, 但是 YARN 不支持 JVM 重用。

5. 进度和状态更新  
YARN 中的任务将其进度和状态 (包括 counter) 返回给应用管理器(AM), 
客户端每秒 (通 mapreduce.client.progressmonitor.pollinterval 设置) 向应用管理器请求进度更新, 展示给用户。

6. 作业完成
除了向应用管理器(AM)请求作业进度外, 客户端每 5 分钟都会通过调用 waitForCompletion() 来检查作业是否完成，
时间间隔可以通过 mapreduce.client.completion.pollinterval 来设置。作业完成之后, 应用管理器和 container 会清理工作状态，
OutputCommiter 的作业清理方法也会被调用。作业的信息会被作业历史服务器存储以备之后用户核查。

### <span id="Yarn的一些补充">Yarn的一些补充</span>
1. 用户提交的程序的运行逻辑对 yarn 是透明的，yarn 并不需要知道；
2. yarn 只提供运算资源的调度（用户程序向 yarn 申请资源，yarn 就负责分配资源）；
3. yarn 中的老大叫 ResourceManager（知道所有小弟的资源情况，以做出资源分配），
yarn 中具体提供运算资源的角色叫 NodeManager（小弟）；
4. yarn 与运行的用户程序完全解耦，就意味着 yarn 上可以运行各种类型的分布式运算程序（mapreduce 只是其中的一种），
比如mapreduce、storm程序，spark程序(不要纠结这些个是啥~~，因为目前我也不知道~~)...只要他们各自的框架中有符合yarn规范的资源请求机制即可；
6. Yarn 是一个通用的资源调度平台，企业中存在的各种运算集群都可以整合在一个物理集群上，提高资源利用率，方便数据共享。

### <span id="资源调度器">资源调度器</span>
目前，Hadoop作业调度器主要有三种：FIFO、Capacity Scheduler和Fair Scheduler。  
Hadoop2.7.2默认的资源调度器是Capacity Scheduler。  

1. FIFO Scheduler  
FIFO Scheduler 把应用按提交的顺序排成**一个队列**，这是一个**先进先出队列**，在进行资源分配的时候，先给队列中最头上的应用进行分配资源，
待最头上的应用需求满足后再给下一个分配，以此类推。  

**缺点**  
耗时长的任务会导致后提交的一直处于等待状态，资源利用率不高；当集群多人共享，显然不合理，不适合共享集群，
共享集群更适合采用 **Capacity Scheduler** 或 **Fair Scheduler**。

---

2. Capacity Scheduler  
- 支持**多个队列**，每个队列可配置一定的资源量，每个队列采用 FIFO 调度策略；
- 为了防止同一个用户作业独占队列中的资源，调度器会对同一个用户提交的作业所占的资源量进行限定；

**调度流程**
1. 计算每个队列中正在进行的任务数与其应分得的计算资源之间的比值，选择一个该值最小的队列——最闲的队列；
2. 按照作业的**优先级**和**提交的时间顺序**，同时考虑用户资源量限制和内存限制对队列内任务排序；
3. 多个队列按照任务先后顺序并行执行任务。

---

3. Fair Scheduler
公平调度是一种对于全局资源，对于所有应用作业来说，都均匀分配的资源分配方法。默认情况，公平调度器 Fair Scheduler 基于内存来安排公平调度策略。
也可以配置为同时基于内存和CPU来进行调度（Dominant Resource Fairness）。在一个队列内，可以使用 FIFO、FAIR、DRF 调度策略对应用进行调度。
FairScheduler 允许保障性的分配最小资源到队列。

**调度流程**  
- **支持多队列多用户**，每个队列中的资源量可以配置，同一个队列中的作业**公平共享**队列中的所有资源；
- 每个队列中的 Job 按照优先级分配资源，优先级越高分配资源越多，但**每个 Job 都会分配到资源**以确保公平；
- 在一个队列中，**Job 的资源缺额越大，越先获得资源执行**。作业是按照缺额的高低来先后执行的。

>缺额  
>在资源有限的情况下，每个 Job 理想情况下获得的资源与实际获得的资源存在一种差距，这个差距就叫做缺额。

### <span id="任务的推测执行">任务的推测执行</span>
**出现的问题**  
作业完成时间取决于最慢的任务完成时间：一个作业由若干个Map任务和Reduce任务构成。因硬件老化、软件Bug等，某些任务可能运行非常慢。

**推测执行机制**  
发现拖后腿的任务，比如某个任务运行速度远慢于任务平均速度。为拖后腿任务启动一个备份任务，同时运行。谁先运行完，则采用谁的结果。

**执行推测任务的前提条件**  
- 每个 Task 只能有一个备份任务；
- 当前 Job 已完成的 Task 必须不小于0.05（5%）；
- 开启推测执行参数设置。mapred-site.xml文件中默认是打开的。

**不能启用推测执行机制情况**  
- 任务间存在严重的负载倾斜；
- 特殊任务，比如任务向数据库中写数据。
