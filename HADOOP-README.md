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
    - [Shuffle-Collect](#Shuffle-Collect)
    - [Shuffle-Spill](#Shuffle-Spill)
    - [Shuffle-Collect](#Shuffle-Combine)
3. YARN: Yet Another Resource Negotiator 资源管理调度系统



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
>为了最大限度地减少全局带宽消耗和读取延迟，HDFS 会选择最接客户端的节点中的副本来响应读取请求。  
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

**数据切片**：数据切片只是在逻辑上对输入进行分片，并不会在磁盘上将其切分成片进行  

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

2. CombineTextInputFormat
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


#### <span id="Shuffle-Spill">Shuffle-Spill</span>
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

#### <span id="Shuffle-Combine">Shuffle-Combine</span>


